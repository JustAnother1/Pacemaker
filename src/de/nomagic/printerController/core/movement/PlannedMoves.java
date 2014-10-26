/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>
 *
 */
package de.nomagic.printerController.core.movement;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.core.ActionResponse;
import de.nomagic.printerController.core.Action_enum;
import de.nomagic.printerController.core.Event;
import de.nomagic.printerController.core.EventSource;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class PlannedMoves implements EventSource
{
    public static final int QUEUE_FLUSH_TIMEOUT_MS = 50;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private LinkedList<BasicLinearMove> entries = new LinkedList<BasicLinearMove>();
    private volatile boolean needsFlushing = false;
    private Protocol pro;
    private int MaxClientStepsPerSecond;
    private double currentSpeedMmS = 0.0;
    private final int timeoutID;
    private final TimeoutHandler to;

    public PlannedMoves(int MaxClientStepsPerSecond, TimeoutHandler to)
    {
        this.MaxClientStepsPerSecond = MaxClientStepsPerSecond;
        this.to= to;
        timeoutID = to.createTimeout(new Event(Action_enum.timeOut, null, this), QUEUE_FLUSH_TIMEOUT_MS);
        if(TimeoutHandler.ERROR_FAILED_TO_CREATE_TIMEOUT == timeoutID)
        {
            log.error("Failed to create Queue Flush Timeout !");
        }
        else
        {
            to.startTimeout(timeoutID);
        }
    }

    @Override
    public String toString()
    {
        return "pro=" + pro + ", Max Steps =" + MaxClientStepsPerSecond + "/second\n";
    }

    public void addProtocol(Protocol pro)
    {
        this.pro = pro;
    }

    @Override
    public void reportEventStatus(ActionResponse response)
    {
        to.startTimeout(timeoutID);
        final int size= entriesSize();
        if(1 == size)
        {
            if(false == needsFlushing)
            {
                needsFlushing = true;
            }
            else
            {
                // OK the queue has been with a single move for two times -> flush the Queue
                if(true == flushQueueToClient())
                {
                    needsFlushing = false;
                }
                else
                {
                    // TODO Can we do something better here ?
                    System.exit(99);
                }
            }
        }
        else
        {
            needsFlushing = false;
        }
    }

    public boolean flushQueueToClient()
    {
        final int size= entriesSize();
        if(0 < size)
        {
            if(true == sendAllPossibleMoves())
            {
                return sendLastMoves();
            }
            else
            {
                return false;
            }
        }
        // else no moves to send
        return true;
    }

    public boolean addMove(BasicLinearMove aMove)
    {
        addEndSpeedToLastMove(aMove);
        synchronized(entries)
        {
            entries.addLast(aMove);
        }
        return sendAllPossibleMoves();
    }

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        // Add the command to the last issued move
        BasicLinearMove aMove = null;
        try
        {
            synchronized(entries)
            {
                aMove = entries.removeLast();
            }
        }
        catch(NoSuchElementException e)
        {
            aMove = null;
        }
        if(null == aMove)
        {
            aMove = new BasicLinearMove(MaxClientStepsPerSecond);
            log.trace("created Move({}) to hold end stop command.", aMove.getId());
        }
        aMove.addEndStopOnOffCommand(on, switches);
        synchronized(entries)
        {
            entries.addLast(aMove);
        }
    }

    public boolean hasAllMovementFinished()
    {
        synchronized(entries)
        {
            return entries.isEmpty();
        }
    }

    private int entriesSize() // TODO find better name
    {
        synchronized(entries)
        {
            return entries.size();
        }
    }

    private void addEndSpeedToLastMove(BasicLinearMove aMove)
    {
        if(false == hasAllMovementFinished())
        {
            if(true == aMove.hasMovementData())
            {
                synchronized(entries)
                { // TODO make synchronized block shorter
                    // we can now calculate the max end Speed for the last added move
                    int idxOfFirstMove = entries.size() -1; // last element
                    BasicLinearMove firstMove = entries.get(idxOfFirstMove);
                    if(false == firstMove.hasMovementData())
                    {
                        idxOfFirstMove --;
                        boolean found = false;
                        while((false == found) && (-1 < idxOfFirstMove))
                        {
                            firstMove = entries.get(idxOfFirstMove);
                            if(true == firstMove.hasMovementData())
                            {
                                found = true;
                            }
                            else
                            {
                                idxOfFirstMove--;
                            }
                        }
                        if(false == found)
                        {
                            // only one move with movement data -> can not calculate
                            return;
                        }
                    }

                    final double[] firstVector =new double[Axis_enum.size];
                    for(Axis_enum axis: Axis_enum.values())
                    {
                        firstVector[axis.ordinal()] = firstMove.getMm(axis);
                    }
                    log.trace("getting the move [{},{},z]",
                              firstVector[Axis_enum.X.ordinal()],
                              firstVector[Axis_enum.Y.ordinal()]);

                    final double[] secondVector = new double[Axis_enum.size];
                    for(Axis_enum axis: Axis_enum.values())
                    {
                        secondVector[axis.ordinal()] = aMove.getMm(axis);
                    }
                    log.trace("getting the move [{},{},z]", secondVector[Axis_enum.X.ordinal()],
                                                            secondVector[Axis_enum.Y.ordinal()]);

                    // set end Speed
                    final double endSpeedFactor = getMaxEndSpeedFactorFor(firstVector, secondVector);
                    // send first move
                    log.trace("endSpeedFactor = {}", endSpeedFactor);
                    firstMove.setEndSpeedFactor(endSpeedFactor);
                    entries.set(idxOfFirstMove, firstMove);
                }
            }
        }
    }

    private boolean sendAllPossibleMoves()
    {
        final int size = entriesSize();
        boolean success = true;
        if(1 > size)
        {
            // no moves available to send
            return success;
        }
        int entrySize;
        do
        {
            success = sendOneMoveIfPossible();
            entrySize = entriesSize();
        }while((true == success) && (1 > entrySize));
        // other moves can not be send -> we are done here
        return true;
    }

    private boolean sendLastMoves()
    {
        int size = entriesSize();
        if(1 > size)
        {
            // no moves available to send
            return true;
        }
        synchronized(entries)
        {
            final BasicLinearMove aMove = entries.removeLast();
            aMove.setEndSpeedMms(0.0);
            entries.addLast(aMove);
        }
        if(false == sendAllPossibleMoves())
        {
            return false;
        }
        // now the Queue _must_ be empty!
        size = entriesSize();
        if(0 != size)
        {
            log.error("{} Moves in Queue after flushing !", size);
            for(int i = 0; i < size; i++)
            {
                if(false == sendMove())
                {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    private boolean sendMove()
    {
        if(null == pro)
        {
            // No chance to do anything without a Protocol
            return false;
        }
        try
        {
            final BasicLinearMove aMove;
            synchronized(entries)
            {
                aMove = entries.removeFirst();
            }
            if(false == pro.addBasicLinearMove(aMove))
            {
                return false;
            }
            if(true == aMove.hasACommand())
            {
                return aMove.sendCommandTo(pro);
            }
            return true;
        }
        catch(NoSuchElementException e)
        {
            // no move to send
            // -> another Job well done !
            return true;
        }
    }

    private boolean sendMoveWithEndSpeedSet(BasicLinearMove aMove)
    {
        // we know the speed we need to have at the end of this move
        double desiredEndSpeedMmS = aMove.getEndSpeedMms();
        int steps = aMove.getStepsOnActiveStepper(aMove.getPrimaryAxis());
        final int stepsToAchiveEndSpeed = Math.abs(
                aMove.getNumberOfStepsOnPrimaryAxisForSpeedChange(currentSpeedMmS, desiredEndSpeedMmS));
        if(steps > stepsToAchiveEndSpeed)
        {
            log.trace("we can achieve the end speed and have some steps left");
            // these steps can be used to accelerate to a higher speed
            log.trace("steps needed to achieve end Speed = {}", stepsToAchiveEndSpeed);
            steps = steps - stepsToAchiveEndSpeed;
            final double maxSpeedChangeMmS = aMove.getSpeedChangeMmSOnPrimaryAxisForSteps(steps/2);
            log.trace("maxSpeedChange = {} mm/s", maxSpeedChangeMmS);
            final double maxPossibleSpeedMmS = aMove.getMaxPossibleSpeedMmS();
            log.trace("maxPossibleSpeed = {} mm/s", maxPossibleSpeedMmS);
            final double travelSpeedMmS = Math.max(currentSpeedMmS, desiredEndSpeedMmS);
            log.trace("travelSpeed = {} mm/s", travelSpeedMmS);
            int accell = 0;
            int decell = 0;
            if(travelSpeedMmS > maxPossibleSpeedMmS)
            {
                log.error("Travel speed too high! end speed = {} mm/s currentSpeed = {} mm/s",
                           desiredEndSpeedMmS, currentSpeedMmS);
                aMove.setTravelSpeedMms(maxPossibleSpeedMmS);
            }
            else if(travelSpeedMmS + maxSpeedChangeMmS > maxPossibleSpeedMmS)
            {
                log.trace("we can not use all the steps to accelerate");
                final int accelerateSteps = Math.abs(
                        aMove.getNumberOfStepsOnPrimaryAxisForSpeedChange(travelSpeedMmS, maxPossibleSpeedMmS));
                aMove.setTravelSpeedMms(maxPossibleSpeedMmS);
                accell = accelerateSteps;
                decell = accelerateSteps;
            }
            else
            {
                log.trace("we just use all steps for speed changes");
                accell = steps/2;
                decell = steps - accell;
                aMove.setTravelSpeedMms(travelSpeedMmS + maxSpeedChangeMmS);
            }
            if(desiredEndSpeedMmS > currentSpeedMmS)
            {
                aMove.setAccelerationSteps(stepsToAchiveEndSpeed + accell);
                aMove.setDecelerationSteps(decell);
            }
            else
            {
                aMove.setAccelerationSteps(accell);
                aMove.setDecelerationSteps(stepsToAchiveEndSpeed + decell);
            }
        }
        else
        {
            log.trace("we need to use all the steps to achieve the end speed.");
            // we need to use all the steps to achieve the end speed.
            if(steps < stepsToAchiveEndSpeed)
            {
                log.warn("Not enough steps to achieve end speed (steps={}, end speed={} mm/sec)",
                          steps, desiredEndSpeedMmS);
                aMove.setEndSpeedMms(aMove.getSpeedChangeMmSOnPrimaryAxisForSteps(steps));
                desiredEndSpeedMmS = aMove.getEndSpeedMms();
            }
            if(desiredEndSpeedMmS > currentSpeedMmS)
            {
                aMove.setAccelerationSteps(steps);
            }
            else
            {
                aMove.setDecelerationSteps(steps);
            }
            if(true == aMove.endSpeedIsZero())
            {
                aMove.setTravelSpeedMms(currentSpeedMmS);
            }
            else
            {
                aMove.setTravelSpeedMms(desiredEndSpeedMmS);
            }
        }
        currentSpeedMmS = desiredEndSpeedMmS;
        synchronized(entries)
        {
            entries.set(0, aMove);
        }
        return sendMove();
    }

    private boolean sendOneMoveIfPossible()
    {
        if(true == hasAllMovementFinished())
        {
            // no movement data to send
            return false;
        }
        final BasicLinearMove aMove;
        synchronized(entries)
        {
            aMove = entries.getFirst();
        }
        if(false == aMove.hasMovementData())
        {
            // no movement data only command
            return sendMove();
        }
        if(false == aMove.hasEndSpeedSet())
        {
            double maxEndSpeedMmS = aMove.getMaxEndSpeedMmS();
            // we can send this move if we find a move that has a end Speed of 0,
            // or if we have more steps in the Queue than needed to decelerate
            // from the max end Speed of this move to 0.
            int idx = 1;
            boolean found = false;
            int stepsNeeded = aMove.getNumberOfStepsOnPrimaryAxisForSpeedChange(maxEndSpeedMmS, 0.0);
            log.trace("steps needed = {}", stepsNeeded);
            int stepsSeen = 0;
            boolean first = true;
            final int size = entriesSize();
            while(idx < size)
            {
                final BasicLinearMove otherMove;
                synchronized(entries)
                {
                    otherMove = entries.get(idx);
                }
                if(true == otherMove.hasMovementData())
                {
                    if(true == first)
                    {
                        first = false;
                        final double maxSpeedNextMoveMmS = otherMove.getMaxPossibleSpeedMmS();
                        if(maxSpeedNextMoveMmS < maxEndSpeedMmS)
                        {
                            // we need to reduce the end speed further
                            // to avoid being to fast for the following move
                            maxEndSpeedMmS = maxSpeedNextMoveMmS;
                            stepsNeeded = aMove.getNumberOfStepsOnPrimaryAxisForSpeedChange(maxEndSpeedMmS, 0.0);
                            log.trace("steps needed = {}", stepsNeeded);
                        }
                    }
                    stepsSeen = stepsSeen + otherMove.getStepsOnActiveStepper(aMove.getPrimaryAxis());
                    if(stepsNeeded < stepsSeen)
                    {
                        log.trace("we have enough steps in the queue -> we can send this move");
                        aMove.setEndSpeedMms(maxEndSpeedMmS);
                        found = true;
                    }
                    else if(true == otherMove.endSpeedIsZero())
                    {
                        log.trace("we found a move that ends with speed = 0.");
                        final double possibleEndSpeedMmS = aMove.getSpeedChangeMmSOnPrimaryAxisForSteps(stepsSeen);
                        if(possibleEndSpeedMmS > maxEndSpeedMmS)
                        {
                            aMove.setEndSpeedMms(maxEndSpeedMmS);
                        }
                        else
                        {
                            aMove.setEndSpeedMms(possibleEndSpeedMmS);
                        }
                        found = true;
                    }
                }
                // else move without movement
                idx++;
            }
            if(false == found)
            {
                // we can not send this move
                return false; // no error
            }
        }
        // the end speed will now be set
        sendMoveWithEndSpeedSet(aMove);
        return true;
    }

    private double getMaxEndSpeedFactorFor(double[] vec_one, double[] vec_two)
    {
        vec_one = normalize(vec_one);
        vec_two = normalize(vec_two);
        double max = 0.0;
        for(Axis_enum axis: Axis_enum.values())
        {
            max = Math.max(max, cornerBreakFactor(vec_one[axis.ordinal()], vec_two[axis.ordinal()]));
        }
        return max;
    }

    private double cornerBreakFactor(double in, double out)
    {
        if((in > 0) && (out > 0))
        {
            return 1 - Math.abs(in -out);
        }
        if((in < 0) && ( out < 0))
        {
            return 1 - Math.abs(in -out);
        }
        // else one is 0 or they point in opposing directions
        return 0.0;
    }

    private double[] normalize(double[] vec)
    {
        double sum = 0.0;
        for(Axis_enum axis: Axis_enum.values())
        {
            sum = sum + Math.abs(vec[axis.ordinal()]);
        }
        for(Axis_enum axis: Axis_enum.values())
        {
            vec[axis.ordinal()] = vec[axis.ordinal()] / sum;
        }
        return vec;
    }

}
