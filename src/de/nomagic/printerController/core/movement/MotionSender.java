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
    private MovementQueue mq = new MovementQueue("MotionSender");

    public MotionSender()
    {
    }

    @Override
    public String toString()
    {
        return "MotionSender pro=" + pro;
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
        log.trace("adding the move {}", sm);
        final int steps = sm.getMaxSteps();
        if(Protocol.MAX_STEPS_PER_MOVE > steps)
        {
            log.trace("directly");
            mq.add(sm);
        }
        else
        {
            log.trace("split");
            // we need to split that move into smaller moves
            final StepperMove[] moves = sm.splitInto(Protocol.MAX_STEPS_PER_MOVE);
            for(int i = 0; i < moves.length; i++)
            {
                mq.add(moves[i]);
            }
        }
        checkQueue();
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
        // t = time in seconds
        // v2 = v1 - a*t  -> t = (v2 - v1)/a
        // s = v * t
        // ->
        // S = (v1 + v2)/2 * abs(v1 - v2)/a
        return ((v1 + v2)/2) * (Math.abs(v1 - v2)/a);
    }

    private double getSpeedfor(Double startSpeed, int accellerationSteps, double maxAccelleration, boolean accelerate)
    {
        // V = sqr(2 * s* a
        final double change = Math.sqrt(2 * accellerationSteps * maxAccelleration);
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
        log.trace("sendMoveCommand: mq.size = {} ", mq.size());
        final StepperMove sm = mq.getMove(0);
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
            else
            {
                log.trace("Send end Stop command");
            }
        }
        final Integer[] activeSteppers = sm.getAllActiveSteppers();
        if(1 > activeSteppers.length)
        {
            // this is an empty move as end of move marking,
            log.trace("found empty Move");
            mq.finishedOneMove();
            return;
        }
        // there is movement in this Move
        final boolean[] axisDirectionIsIncreasing = sm.getAxisDirectionIsIncreasing();

        final Integer[] steps = sm.getSteps();
        for(int i = 0; i < activeSteppers.length; i++)
        {
            log.trace("direction is increasing = {}", axisDirectionIsIncreasing[i]);
            log.trace("Active Axis = {}", activeSteppers[i]);
            log.trace("Steps on Axis = {}", steps[i]);
            if(0 > steps[i])
            {
                steps[i] = Math.abs(steps[i]);
            }
            log.trace("direction is increasing = {}", axisDirectionIsIncreasing[i]);
        }
        final int primaryAxis = sm.getStepperWithMostSteps();
        final int IdxOfPrimaryAxis = sm.getIndexOfStepperWithMostSteps();
        log.trace("primary Axis = {}", primaryAxis);

        // Speed calculation
        int StepsOnAxis = Math.abs(sm.getStepsOnStepper(primaryAxis));
        log.trace("Steps on Axis = {}", StepsOnAxis);
        int accellerationSteps = 0;
        int DecellerationSteps = 0;

        Double startSpeed = startSpeeds.get(primaryAxis);
        if(null == startSpeed)
        {
            startSpeed = 0.0;
        }
        log.trace("start Speed = {}", startSpeed);
        final double MaxEndSpeed = sm.getMaxEndSpeedStepsPerSecondFor(primaryAxis);
        log.trace("max end Speed = {}", MaxEndSpeed);
        final double MaxTravelSpeed = sm.getMaxSpeedStepsPerSecondFor(primaryAxis);
        log.trace("max travel Speed = {}", MaxTravelSpeed);
        final double MaxAccelleration = sm.getMaxAccelerationStepsPerSecond2(primaryAxis);
        log.trace("max Acceleration = {}", MaxAccelleration);
        final double MaxPossibleSpeed = sm.getMaxPossibleSpeedStepsPerSecond(primaryAxis);
        log.trace("max possible Speed = {}", MaxPossibleSpeed);

        if(startSpeed > MaxEndSpeed)
        {
            // we _need_ to decelerate to this speed
            DecellerationSteps =  DecellerationSteps +
                    (int)getBrakingDistance(startSpeed, MaxEndSpeed, MaxAccelleration);
            StepsOnAxis = StepsOnAxis - DecellerationSteps;
            log.trace("decellerate - adoption: {}", DecellerationSteps);
        }
        else if(startSpeed < MaxEndSpeed)
        {
            // we _can_ try to accelerate to this speed
            final int neededSteps = (int)getBrakingDistance(startSpeed, MaxEndSpeed, MaxAccelleration);
            if(StepsOnAxis > neededSteps)
            {
                // we have the steps so lets do it
                accellerationSteps = accellerationSteps + neededSteps;
                StepsOnAxis = StepsOnAxis - neededSteps;
                log.trace("accellerate - adoption(1): {}", accellerationSteps);
            }
            else
            {
                // we accelerate as much as we can
                accellerationSteps = accellerationSteps + StepsOnAxis;
                StepsOnAxis = 0;
                log.trace("accellerate - adoption(2): {}", accellerationSteps);
            }
        }
        if(0 < StepsOnAxis)
        {
            // if start is faster than max end then we already accounted for
            // the additional deceleration and therefore can go on with this speed.
            // if start is slower than max end speed then we already accounted for
            // the additional acceleration and therefore can also go on with the max end speed.
            final double adoptedSpeed = Math.max(startSpeed, MaxEndSpeed);
            log.trace("adopted  speed: {}", adoptedSpeed);
            // we can now _try_ to accelerate to the max travel speed
            final int neededSteps = (int)getBrakingDistance(adoptedSpeed, MaxTravelSpeed, MaxAccelleration);
            log.trace("needed steps: {}", neededSteps);
            if(StepsOnAxis > 2*neededSteps)
            {
                // we have the steps so lets do it
                accellerationSteps = accellerationSteps + neededSteps;
                DecellerationSteps =  DecellerationSteps + neededSteps;
            }
            else
            {
                // we can not accelerate to the max speed so go as fast as possible
                accellerationSteps = StepsOnAxis/2;
                DecellerationSteps = StepsOnAxis - accellerationSteps;
            }
        }

        double speed = getSpeedfor(startSpeed, accellerationSteps, MaxAccelleration, true);
        if(speed > MaxPossibleSpeed)
        {
            speed = MaxPossibleSpeed;
        }
        final double endSpeed = getSpeedfor(speed, DecellerationSteps,MaxAccelleration, false);
        // In the last move before a stop it can happen that this move does not have enough steps
        // to come to a full stop.
        // we rely on the Under Run avoidance of the Client here. So the deceleration steps will be
        // to few to actually do the breaking. But the Under run avoidance should have kicked in already,
        // so that the Client does not move with the specified speed but slower.
        // Therefore everything should be alright even though we made a mistake here,..
        if(accellerationSteps > steps[IdxOfPrimaryAxis])
        {
            accellerationSteps = steps[IdxOfPrimaryAxis];
        }
        if(DecellerationSteps > steps[IdxOfPrimaryAxis])
        {
            DecellerationSteps = steps[IdxOfPrimaryAxis];
        }
        log.trace("accelleration Steps = {}", accellerationSteps);
        log.trace("decelleration Steps = {}", DecellerationSteps);
        log.trace("speed = {}", speed);
        log.trace("end speed = {}", endSpeed);

        final int speedFactor = (int)((speed /MaxPossibleSpeed) * 255);
        log.trace("speed factor = {}", speedFactor);
        final int endSpeedFactor = (int)((endSpeed /MaxPossibleSpeed) * 255);
        log.trace("end speed factor = {}", endSpeedFactor);
        // Update start Speeds
        final double speedPerStep = endSpeed/ Math.abs(sm.getStepsOnStepper(primaryAxis));
        for(int i = 0; i < activeSteppers.length; i++)
        {
            startSpeeds.put(activeSteppers[i], steps[i] * speedPerStep);
        }

        for(int i = 0; i < axisDirectionIsIncreasing.length; i++)
        {
            log.trace("axisDirectionIsIncreasing[" + i + "] = " + axisDirectionIsIncreasing[i]);
        }

        final boolean res = pro.addBasicLinearMove(
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
        // else ok
        mq.finishedOneMove();
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
