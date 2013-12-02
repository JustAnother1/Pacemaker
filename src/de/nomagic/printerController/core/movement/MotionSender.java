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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class MotionSender
{
    public static final double TOLLERANCE_SPEED_IN_MILLIMETER = 0.001;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private HashMap<Integer, Double> startSpeeds = new HashMap<Integer, Double>();

    private Protocol pro;
    private MovementQueue mq = new MovementQueue();

    public MotionSender()
    {
    }

    public void setProtocol(Protocol pro)
    {
        this.pro = pro;
        pro.ClearQueue();
    }

    /** adds a move to the Planner Queue.
     *
     * @param sm The move that shall be added to the Queue.
     * @return Index of in the Queue or -1 in case of a problem.
     */
    public void add(StepperMove sm)
    {
        int steps = sm.getMaxSteps();
        if(Protocol.MAX_STEPS_PER_MOVE > steps)
        {
            mq.add(sm);
            checkQueue();
            return;
        }
        else
        {
            // we need to split that move into smaller moves
            StepperMove[] moves = sm.splitInto(Protocol.MAX_STEPS_PER_MOVE);
            for(int i = 0; i < moves.length; i++)
            {
                mq.add(moves[i]);
            }
            return;
        }
    }

    public void flushQueueToClient()
    {
        do
        {
            checkQueue();
        }while(false == mq.isEmpty());
    }

    /** check if we can /should /can not send movement commands to the client.
     *
     */
    private void checkQueue()
    {
        if(true == mq.isEmpty())
        {
            return;
        }
        boolean clientHasFreeSlots = pro.hasFreeQueueSlots();
        do
        {
            // Client has free slots,...
            sendMoveCommand();
            clientHasFreeSlots = pro.hasFreeQueueSlots();
        }while((true == clientHasFreeSlots) && (0 < mq.size()));
    }

    private double getBrakingDistance(double v1, double v2, double a)
    {
        // v1, v2 = steps per second
        // a = steps /second*second
        // S = (v1 + v2)/2 * abs(v1 - v2)/a
        return ((v1 + v2)/2) * (Math.abs(v1 - v2)/a);
    }

    private double getSpeedfor(Double startSpeed, int accellerationSteps, double maxAccelleration, boolean accelerate)
    {
        // V = sqr(2 * s* a
        double change = Math.sqrt(2 * accellerationSteps * maxAccelleration);
        if(true == accelerate)
        {
            return startSpeed + change;
        }
        else
        {
            return startSpeed - change;
        }
    }

    private void sendMoveCommand()
    {
        StepperMove sm = mq.getMove(0);
        if(null == sm)
        {
            return;
        }
        if(true == sm.hasCommand())
        {
            if(false == pro.endStopOnOff(sm.getOn(), sm.getSwitches()))
            {
                log.error("Could not enable/diable the end stop switches !");
            }
        }
        Integer[] activeSteppers = sm.getAllActiveSteppers();
        if(1 > activeSteppers.length)
        {
            // this is an empty move as end of move marking,
            mq.finishedOneMove();
            return;
        }
        // there is movement in this Move
        boolean[] axisDirectionIsIncreasing = sm.getAxisDirectionIsIncreasing();
        Integer[] steps = sm.getSteps();
        int primaryAxis = sm.getStepperWithMostSteps();

        // Speed calculation
        int StepsOnAxis = Math.abs(sm.getStepsOnStepper(primaryAxis));
        int accellerationSteps = 0;
        int DecellerationSteps = 0;

        Double startSpeed = startSpeeds.get(primaryAxis);
        if(null == startSpeed)
        {
            startSpeed = 0.0;
        }
        double MaxEndSpeed = sm.getMaxEndSpeedStepsPerSecondFor(primaryAxis);
        double MaxTravelSpeed = sm.getMaxSpeedStepsPerSecondFor(primaryAxis);
        double MaxAccelleration = sm.getMaxAccelerationStepsPerSecond2(primaryAxis);
        double MaxPossibleSpeed = sm.getMaxPossibleSpeedStepsPerSecond(primaryAxis);

        if(startSpeed > MaxEndSpeed)
        {
            // we _need_ to decelerate to this speed
            DecellerationSteps =  DecellerationSteps +
                    (int)getBrakingDistance(startSpeed, MaxEndSpeed, MaxAccelleration);
            StepsOnAxis = StepsOnAxis - DecellerationSteps;
        }
        else if(startSpeed < MaxEndSpeed)
        {
            // we _can_ try to accelerate to this speed
            int neededSteps = (int)getBrakingDistance(startSpeed, MaxEndSpeed, MaxAccelleration);
            if(StepsOnAxis > neededSteps)
            {
                // we have the steps so lets do it
                accellerationSteps = accellerationSteps + neededSteps;
                StepsOnAxis = StepsOnAxis - neededSteps;
            }
            else
            {
                // we accelerate as much as we can
                accellerationSteps = accellerationSteps + StepsOnAxis;
                StepsOnAxis = 0;
            }
        }
        if(0 < StepsOnAxis)
        {
            // if start is faster than max end then we already accounted for
            // the additional deceleration and therefore can go on with this speed.
            // if start is slower than max end speed then we already accounted for
            // the additional acceleration and therefore can also go on with the max end speed.
            double adoptedSpeed = Math.max(startSpeed, MaxEndSpeed);
            // we can now _try_ to accelerate to the max travel speed
            int neededSteps = (int)getBrakingDistance(adoptedSpeed, MaxTravelSpeed, MaxAccelleration);
            if(neededSteps > 2*neededSteps)
            {
                // we have the steps so lets do it
                accellerationSteps = accellerationSteps + neededSteps;
                DecellerationSteps =  DecellerationSteps + neededSteps;
            }
        }

        double speed = getSpeedfor(startSpeed, accellerationSteps, MaxAccelleration, true);
        double endSpeed = getSpeedfor(speed, DecellerationSteps,MaxAccelleration, false);

        int speedFactor = (int)((speed /MaxPossibleSpeed) * 256);
        int endSpeedFactor = (int)((endSpeed /MaxPossibleSpeed) * 256);
        // Update start Speeds
        double speedPerStep = endSpeed/ Math.abs(sm.getStepsOnStepper(primaryAxis));
        for(int i = 0; i < activeSteppers.length; i++)
        {
            startSpeeds.put(activeSteppers[i], steps[i] * speedPerStep);
        }
        boolean res = pro.addBasicLinearMove(
                activeSteppers,
                sm.isHomingMove(),
                speedFactor,
                endSpeedFactor,
                accellerationSteps,
                DecellerationSteps,
                axisDirectionIsIncreasing,
                primaryAxis,
                steps);

        if(false == res)
        {
            log.error("Oh oh !");
            //TODO
        }
        else
        {
            mq.finishedOneMove();
        }
    }

    public boolean hasAllMovementFinished()
    {
        if(0 == pro.getNumberOfCommandsInClientQueue())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
