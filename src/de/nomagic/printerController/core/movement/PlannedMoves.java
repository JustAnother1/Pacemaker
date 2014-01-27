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
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class PlannedMoves
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private LinkedList<BasicLinearMove> entries = new LinkedList<BasicLinearMove>();

    private Protocol pro;
    private int MaxClientStepsPerSecond;
    private double currentSpeed = 0.0;

    public PlannedMoves(int MaxClientStepsPerSecond)
    {
        this.MaxClientStepsPerSecond = MaxClientStepsPerSecond;
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

    public boolean flushQueueToClient()
    {
        if(0 < entries.size())
        {
            sendAllPossibleMoves();
            return sendLastMoves();
        }
        // else no moves to send
        return true;
    }

    public void addMove(BasicLinearMove aMove)
    {
        addEndSpeedToLastMove(aMove);
        entries.addLast(aMove);
        sendAllPossibleMoves();
    }

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        // Add the command to the last issued move
        BasicLinearMove aMove = null;
        try
        {
            aMove = entries.removeLast();
        }
        catch(NoSuchElementException e)
        {
            aMove = null;
        }
        if(null == aMove)
        {
            aMove = new BasicLinearMove(MaxClientStepsPerSecond);
        }
        aMove.addEndStopOnOffCommand(on, switches);
        entries.addLast(aMove);
    }

    public boolean hasAllMovementFinished()
    {
        return entries.isEmpty();
    }



    private void addEndSpeedToLastMove(BasicLinearMove aMove)
    {
        if(false == entries.isEmpty())
        {
            if(true == aMove.hasMovementData())
            {
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

    private void sendAllPossibleMoves()
    {
        if(1 > entries.size())
        {
            // no moves available to send
            return;
        }
        boolean success = true;
        do
        {
            success = sendOneMoveIfPossible();
        }while(true == success);
        // other moves can not be send -> we are done here
    }

    private boolean sendLastMoves()
    {
        if(1 > entries.size())
        {
            // no moves available to send
            return true;
        }
        final BasicLinearMove aMove = entries.removeLast();
        aMove.setEndSpeed(0.0);
        entries.addLast(aMove);
        boolean success = true;
        do
        {
            success = sendOneMoveIfPossible();
        }while(true == success);
        // now the Queue _must_ be empty!
        if(0 != entries.size())
        {
            log.error("Moves in Queue after flushing !");
            for(int i = 0; i < entries.size(); i++)
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
            final BasicLinearMove aMove = entries.removeFirst();
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
            // no move to send :-(
            return false;
        }
    }

    private void sendMoveWithEndSpeedSet(BasicLinearMove aMove)
    {
        // we know the speed we need to have at the end of this move
        final double desiredEndSpeed = aMove.getEndSpeed();
        int steps = aMove.getStepsOnActiveStepper(aMove.getPrimaryAxis());
        final int stepsToAchiveEndSpeed = aMove.getNumberOfStepsForSpeedChange(currentSpeed, desiredEndSpeed);
        if(steps > stepsToAchiveEndSpeed)
        {
            // we can achieve the end speed and have some steps left
            // these steps can be used to accelerate to a higher speed
            steps = steps - stepsToAchiveEndSpeed;
            final double maxSpeedChange = aMove.getSpeedChangeForSteps(steps/2);
            final double maxPossibleSpeed = aMove.getMaxPossibleSpeed();
            final double travelSpeed = Math.max(currentSpeed, desiredEndSpeed);
            int accell = 0;
            int decell = 0;
            if(travelSpeed + maxSpeedChange > maxPossibleSpeed)
            {
                // we can not use all the steps to accelerate
                final int accelerateSteps = aMove.getNumberOfStepsForSpeedChange(travelSpeed, maxPossibleSpeed);
                aMove.setTravelSpeed(maxPossibleSpeed);
                accell = accelerateSteps;
                decell = accelerateSteps;
            }
            else
            {
                // we just use all steps for speed changes
                accell = steps/2;
                decell = steps - accell;
                aMove.setTravelSpeed(travelSpeed + maxSpeedChange);
            }
            if(desiredEndSpeed > currentSpeed)
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
            // we need to use all the steps to achieve the end speed.
            if(steps < stepsToAchiveEndSpeed)
            {
                log.warn("Not enougth steps to achieve end speed (steps={}, ens speed={})", steps, desiredEndSpeed);
            }
            if(desiredEndSpeed > currentSpeed)
            {
                aMove.setAccelerationSteps(steps);
            }
            else
            {
                aMove.setDecelerationSteps(steps);
            }
            aMove.setTravelSpeed(currentSpeed);
        }
        currentSpeed = desiredEndSpeed;
        entries.set(0, aMove);
        sendMove();
    }

    private boolean sendOneMoveIfPossible()
    {
        if(true == entries.isEmpty())
        {
            // no movement data to send
            return false;
        }
        final BasicLinearMove aMove = entries.getFirst();
        if(false == aMove.hasMovementData())
        {
            // no movement data only command
            sendMove();
            return true;
        }
        if(false == aMove.hasEndSpeedSet())
        {
            final double maxEndSpeed = aMove.getMaxEndSpeed();
            // we can send this move if we find a move that has a end Speed of 0,
            // or if we have more steps in the Queue than needed to decelerate from the max end Speed of this move to 0.
            int idx = 1;
            boolean found = false;
            final int stepsNeeded = aMove.getNumberOfStepsForSpeedChange(maxEndSpeed, 0.0);
            int stepsSeen = 0;
            while(idx < entries.size())
            {
                final BasicLinearMove otherMove = entries.get(idx);
                stepsSeen = stepsSeen + otherMove.getStepsOnActiveStepper(otherMove.getPrimaryAxis());
                if(stepsNeeded < stepsSeen)
                {
                    // we have enough steps in the queue -> we can send this move
                    aMove.setEndSpeed(maxEndSpeed);
                    found = true;
                }
                if(true == otherMove.endSpeedIsZero())
                {
                    // we found a move that ends with speed = 0.
                    final double possibleEndSpeed = aMove.getSpeedChangeForSteps(stepsSeen);
                    aMove.setEndSpeed(possibleEndSpeed);
                    found = true;
                }
                idx++;
            }
            if(false == found)
            {
                // we can not send this move
                return false;
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
