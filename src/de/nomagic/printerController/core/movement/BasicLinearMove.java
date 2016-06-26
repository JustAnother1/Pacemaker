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
import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.core.devices.Stepper;
import de.nomagic.printerController.pacemaker.Protocol;



/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class BasicLinearMove
{
    /** everything shorter than this will be assumed to be 0 */
    public static final double MIN_MOVEMENT_DISTANCE_MM_SECOND = 0.00001;

    public static final double MOVEMENT_SPEED_TOLERANCE_MM_SECOND = 0.0001;
    /** if the axis has steps the speed may not be 0. So this is the speed is will have at least */
    public static final double MIN_MOVEMENT_SPEED_MM_SECOND = 0.1;

    private static int nextId = 0; // singleton

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private double feedrateMmPerMinute = MIN_MOVEMENT_SPEED_MM_SECOND * 60;
    private boolean isHoming = false;
    private HashMap<Axis_enum, Double> distancesMm = new HashMap<Axis_enum, Double>();
    private HashMap<Axis_enum, Double> roundingErrorSteps = new HashMap<Axis_enum, Double>();
    private HashMap<Integer, Axis_enum> AxisMapping = new HashMap<Integer, Axis_enum>();
    private Vector<Integer> activeAxises = new Vector<Integer>();
    private HashMap<Integer, Integer> StepsOnAxis = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> MaxSpeedStepsPerSecondOnAxis = new HashMap<Integer, Integer>();
    private HashMap<Integer, Boolean> AxisDirectionIncreasing = new HashMap<Integer, Boolean>();
    private int primaryAxis = -1;
    private int StepsOnPrimaryAxis = -1;
    private double PrimaryAxisStepsPerMm = 1;
    private double PrimaryAxisMaxAceleration = 1;

    private boolean hasCommand = false;
    private boolean hasMovement = false;
    private boolean Command_on = true;
    private Integer[] Command_switches;

    private final int MaxPossibleClientSpeedInStepsPerSecond;
    private int maxStepperNumber = -1;
    private double endSpeedMms = 0.0;
    private boolean hasEndSpeed = false;
    private double endSpeedFactor = 1.0; // no limitation
    private double travelSpeedMms = 0.0;
    private int AccelerationSteps = 0;
    private int DecelerationSteps = 0;
    // this speed in mm/s can be reduced to 0 in an instant( with an allowed Jerk)
    private double maxJerkMms = 0;
    private boolean JerkIsSet = false;

    private int myId;

    private int numParts = 1;

    public BasicLinearMove(int MaxPossibleClientSpeedStepsPerSecond)
    {
        this.MaxPossibleClientSpeedInStepsPerSecond = MaxPossibleClientSpeedStepsPerSecond;
        myId = nextId;
        nextId++;
        for(Axis_enum axis: Axis_enum.values())
        {
            roundingErrorSteps.put(axis, 0.0);
        }
    }

    public boolean hasMovementData()
    {
        return hasMovement;
    }

    public void setFeedrateMmPerMinute(double feedrateMmPerMinute)
    {
        if(MIN_MOVEMENT_SPEED_MM_SECOND * 60 < feedrateMmPerMinute)
        {
            this.feedrateMmPerMinute = feedrateMmPerMinute;
        }
        // else ignore invalid Feedrate
    }

    public void setDistanceMm(Axis_enum axis, double distanceMm)
    {
        log.debug("ID{}: adding {} = {} mm", myId, axis, distanceMm);
        distancesMm.put(axis, distanceMm);
        if(MIN_MOVEMENT_DISTANCE_MM_SECOND < Math.abs(distanceMm))
        {
            hasMovement = true;
        }
    }

    public double getMm(Axis_enum axis)
    {
        try
        {
            return distancesMm.get(axis);
        }
        catch(NullPointerException e)
        {
            return 0.0;
        }
    }

    public void setIsHoming(boolean b)
    {
        isHoming = b;
    }

    public boolean isHomingMove()
    {
        return isHoming;
    }

    public void splitTo(int parts)
    {
        numParts = parts;
    }

    public int getNumberOfActiveSteppers()
    {
        return activeAxises.size();
    }

    public int getHighestStepperNumber()
    {
        return maxStepperNumber;
    }

    public int getStepsOnActiveStepper(int i, int curPart)
    {
        int allSteps = getStepsOnActiveStepper(i);
        int stepsPerPart = allSteps/numParts;
        if(curPart < (numParts -1))
        {
            return stepsPerPart;
        }
        else
        {
            // last part
            // fix rounding
            return allSteps - ((numParts -1) * stepsPerPart);
        }
    }

    public int getStepsOnActiveStepper(int i)
    {
        try
        {
            return StepsOnAxis.get(i);
        }
        catch(NullPointerException e)
        {
            return 0;
        }
    }

    public int getPrimaryAxis()
    {
        return primaryAxis;
    }

    private void addSteppersJerk(Stepper stepper)
    {
        final double jerkMms = stepper.getMaxJerkSpeedMmS();
        if(false == JerkIsSet)
        {
            JerkIsSet = true; // first jerk setting for this move
            maxJerkMms = jerkMms;
        }
        if(maxJerkMms > jerkMms)
        {
            // this axis can not do as much Jerk as the other Axis
            // so the Jerk for this move needs to be reduced
            maxJerkMms = jerkMms;
            // TODO handle Jerk on a per Axis basis
        }
    }

    private void addSteppersSteps(Stepper stepper, int steps)
    {
        if(false == stepper.isDirectionInverted())
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(stepper.getStepperNumber(), true);
            }
            else
            {
                AxisDirectionIncreasing.put(stepper.getStepperNumber(), false);
            }
        }
        else
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(stepper.getStepperNumber(), false);
            }
            else
            {
                AxisDirectionIncreasing.put(stepper.getStepperNumber(), true);
            }
        }
        StepsOnAxis.put(stepper.getStepperNumber(), Math.abs(steps));
    }

    private int getSteps(Stepper stepper, Axis_enum ax)
    {
        final double exactSteps = roundingErrorSteps.get(ax) + (distancesMm.get(ax) * stepper.getStepsPerMm());
        int steps = (int) Math.round(exactSteps);
        log.debug("ID{}: exact Steps = {}, got rounded to {}", myId, exactSteps, steps);
        final Double difference = exactSteps - steps;
        roundingErrorSteps.put(ax, difference);
        if(0 == steps)
        {
            return 0;
        }
        hasMovement = true;
        if(StepsOnPrimaryAxis < Math.abs(steps))
        {
            StepsOnPrimaryAxis = Math.abs(steps);
            final int number = stepper.getStepperNumber();
            log.trace("ID{}: primary Axis is {} !", myId, number);
            primaryAxis = number;
            PrimaryAxisStepsPerMm = stepper.getStepsPerMm();
            PrimaryAxisMaxAceleration = stepper.getMaxAccelerationStepsPerSecond();
        }
        return steps;
    }

    public void addMovingAxis(Stepper stepper, Axis_enum ax)
    {
        if(null == stepper)
        {
            return;
        }

        final int steps = getSteps(stepper, ax);
        if(1 > Math.abs(steps))
        {
            return;
        }

        final Integer number = stepper.getStepperNumber();
        log.debug("ID{}: adding Stepper {} for Axis {}", myId, number, ax);
        AxisMapping.put(number, ax);
        if(maxStepperNumber < number)
        {
            maxStepperNumber = number;
        }
        addSteppersJerk(stepper);
        activeAxises.add(number);
        addSteppersSteps(stepper, steps);
        final int maxSpeedStepsPerSecond = stepper.getMaxPossibleSpeedStepsPerSecond();
        MaxSpeedStepsPerSecondOnAxis.put(number, maxSpeedStepsPerSecond);
        log.trace("ID{}: Stepper {} for Axis {} has a max Steps/sec of {} !", myId, number, ax, maxSpeedStepsPerSecond);
    }

    public int getActiveSteppersMap()
    {
        int ActiveSteppersMap = 0;
        for(int i = 0; i < activeAxises.size(); i++)
        {
            ActiveSteppersMap = ActiveSteppersMap | 1 << activeAxises.get(i);
        }
        return ActiveSteppersMap;
    }

    public int getDirectionMap()
    {
        int DirectionMap = 0;
        for(int i = 0; i < activeAxises.size(); i++)
        {
            final int StepperNumber = activeAxises.get(i);
            if(true == AxisDirectionIncreasing.get(StepperNumber))
            {
                DirectionMap = DirectionMap | (1 << StepperNumber);
            }
        }
        return DirectionMap;
    }

    public int getTravelSpeedFraction(int curPart)
    {
        int curPartAccelSteps= getAccelerationSteps(curPart);
        if(0 == curPartAccelSteps)
        {
            // Acceleration is finished
            return getTravelSpeedFraction();
        }
        else if(AccelerationSteps == curPartAccelSteps)
        {
            // complete acceleration in this part so travel speed is the same
            return getTravelSpeedFraction();
        }
        else
        {
            // this part does only a part of the acceleration.
            return getTravelSpeedFraction() * (curPartAccelSteps/AccelerationSteps);
        }
    }

    public int getTravelSpeedFraction()
    {
        int fraction = (int)((travelSpeedMms * PrimaryAxisStepsPerMm * 255)
                                / MaxPossibleClientSpeedInStepsPerSecond);
        if(1 > fraction)
        {
            // speed of 0 is not allowed !
            log.info("ID{}: Increased Travel Speed Fraction to 1 !", myId);
            fraction = 1;
        }
        log.trace("ID{}: travel speed = {} mm/s -> fraction = {}", myId, travelSpeedMms, fraction);
        return fraction;
    }

    public void setTravelSpeedMms(double speedMms)
    {
        log.trace("ID{}: Nominal speed set to {} mm/s!", myId, speedMms);
        travelSpeedMms = speedMms;
    }

    public void setEndSpeedMms(double theSpeedMms)
    {
        endSpeedMms = theSpeedMms;
        log.trace("ID{}: end speed set to {} mm/s", myId, theSpeedMms);
        hasEndSpeed = true;
    }

    public double getEndSpeedMms()
    {
        return endSpeedMms;
    }

    public boolean endSpeedIsZero()
    {
        if(MOVEMENT_SPEED_TOLERANCE_MM_SECOND < endSpeedMms)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public int getEndSpeedFraction(int curPart)
    {
        int decelStepsInThisPart = getDecelerationSteps(curPart);
        if(0 == decelStepsInThisPart)
        {
            // no breaking so end speed is travel speed
            return getTravelSpeedFraction(curPart);
        }
        else if(DecelerationSteps == decelStepsInThisPart)
        {
            // all breaking in this part
            return getEndSpeedFraction();
        }
        else
        {
            // only part of the breaking in this part
            return getEndSpeedFraction() * (decelStepsInThisPart/DecelerationSteps);
        }
    }


    public int getEndSpeedFraction()
    {
        final int fraction =  (int)((endSpeedMms * PrimaryAxisStepsPerMm * 255)
                                      / MaxPossibleClientSpeedInStepsPerSecond);
        log.trace("ID{}: end speed = {} mm/s -> fraction = {}", myId, endSpeedMms, fraction);
        return fraction;
    }

    public void setAccelerationSteps(int steps)
    {
        AccelerationSteps = steps;
    }

    public void setDecelerationSteps(int steps)
    {
        DecelerationSteps = steps;
    }

    public int getAccelerationSteps(int curPart)
    {
        int primaryStepsPerPart = StepsOnPrimaryAxis / numParts;
        int stepsInThisPhase = AccelerationSteps - (curPart * primaryStepsPerPart);
        if(1 > stepsInThisPhase)
        {
            return 0;
        }
        else if(primaryStepsPerPart < stepsInThisPhase)
        {
            return primaryStepsPerPart;
        }
        else
        {
            return stepsInThisPhase;
        }
    }

    public int getAccelerationSteps()
    {
        return AccelerationSteps;
    }

    public int getDecelerationSteps(int curPart)
    {
        int primaryStepsPerPart = StepsOnPrimaryAxis / numParts;
        int stepsInThisPhase =  DecelerationSteps - ((numParts-(curPart + 1)) * primaryStepsPerPart);
        if(1 > stepsInThisPhase)
        {
            return 0;
        }
        else
        {
            return stepsInThisPhase;
        }
    }

    public int getDecelerationSteps()
    {
        return DecelerationSteps;
    }

    public boolean hasEndSpeedSet()
    {
        return hasEndSpeed;
    }

    private double getBrakingDistance(double v1, double v2, double a)
    {
        // v1, v2 = mm per second
        // a = mm /second*second
        // t = time in seconds
        // v2 = v1 - a*t -> t = (v2 - v1)/a
        // s = v * t
        // ->
        // S = abs(v1 - v2)/2 * abs(v1 - v2)/a
        final double deltaV = Math.abs(v1 - v2);
        return (deltaV/2) * (deltaV/a);
    }

    public int getNumberOfStepsOnPrimaryAxisForSpeedChange(double startSpeedMmS, double endSpeedMmS)
    {
        startSpeedMmS = Math.abs(startSpeedMmS);
        endSpeedMmS = Math.abs(endSpeedMmS);
        final double distanceMm = Math.abs(getBrakingDistance(startSpeedMmS, endSpeedMmS, getMaxAcceleration()));
        final int res = (int)(distanceMm * PrimaryAxisStepsPerMm);
        log.trace("ID{}: We need {} steps to accelerate from {} mm/s to {} mms/s (in {} mm)",
                   myId,        res,                 startSpeedMmS, endSpeedMmS, distanceMm);
        return res;
    }

    public double getMaxAcceleration()
    {
        return PrimaryAxisMaxAceleration;
    }

    public double getSpeedChangeMmSOnPrimaryAxisForSteps(int steps)
    {
        // V = sqr(2 * s* a)
        final double change = Math.sqrt(2 * (steps/PrimaryAxisStepsPerMm) * getMaxAcceleration());
        log.trace("ID{}: Speed change of {} mm/s possible with {} steps", myId, change, steps);
        return change;
    }

    private double getDistanceOnXYZinMm()
    {
        double distanceOnXYZMm = 0.0;
        final Iterator<Double> it = distancesMm.values().iterator();
        while(it.hasNext())
        {
            distanceOnXYZMm = distanceOnXYZMm + Math.abs(it.next());
        }
        log.trace("ID{}: distance on X Y Z = {} mm", myId, distanceOnXYZMm);
        return distanceOnXYZMm;
    }

    private double getFeedrateOnPrimaryAxisMmSec()
    {
        // TODO : Currently 10mm on Y and 10mm on X lead to a Speed on primary axis of 0.5 of the Feedrate.
        // But the Distance traveled in such a move is not 20mm but only about 14mm. So the Feedrate could be higher.
        // TODO The angle of the move has to be taken into account.
        double SpeedPerMmSec = 0;
        log.trace("ID{}: Feedrate = {} mm/minute", myId, feedrateMmPerMinute);
        final double distanceOnXYZMm = getDistanceOnXYZinMm();
        final double distanceOnPrimaryAxis = Math.abs(distancesMm.get(AxisMapping.get(primaryAxis)));
        if(distanceOnXYZMm > distanceOnPrimaryAxis)
        {
            final double factor = distanceOnPrimaryAxis/distanceOnXYZMm;
            log.trace("ID{}: Speed Factor = {}", myId, factor);
            SpeedPerMmSec = (feedrateMmPerMinute/60) * factor;
        }
        else
        {
            // move with only one Axis involved
            SpeedPerMmSec = feedrateMmPerMinute/60;
        }
        log.trace("ID{}: Feedrate on primary Axis = {} mm/sec", myId, SpeedPerMmSec);
        return SpeedPerMmSec;
    }

    public double getMaxPossibleSpeedStepsPerSecond()
    {
    	Integer help = MaxSpeedStepsPerSecondOnAxis.get(primaryAxis);
    	if(null == help)
    	{
    		log.error("No Max Steps per Second for Axis {} !", primaryAxis);
    		return MIN_MOVEMENT_SPEED_MM_SECOND;
    	}
        double maxSpeedStepPerSec = help.doubleValue();
        log.trace("ID{}: Max Speed = {}", myId, maxSpeedStepPerSec);
        // Test if this speed is ok for the other axis
        // (speed on primary Axis) / (steps on primary axis) = a
        // a * (steps on the axis) = (speed on that axis)
        // speed on each axis must be smaller than the max speed on that axis
        double a = maxSpeedStepPerSec / Math.abs(StepsOnAxis.get(primaryAxis));
        for(int i = 0; i < activeAxises.size(); i++)
        {
            final int axisNumber = activeAxises.get(i);
            final int steps = StepsOnAxis.get(axisNumber);
            final int speed = (int) (a * steps);
            if(speed > MaxSpeedStepsPerSecondOnAxis.get(axisNumber))
            {
                log.info("ID{}: Speed of {}steps/s to high for this Axis !", myId, speed);
                // calculate new possible max Speed
                a = MaxSpeedStepsPerSecondOnAxis.get(axisNumber) / steps;
                maxSpeedStepPerSec = a * Math.abs(StepsOnAxis.get(primaryAxis));
                log.info("ID{}: max Speed of {}steps/s (a={}) for this Axis !",myId, maxSpeedStepPerSec, a);
                if(MIN_MOVEMENT_SPEED_MM_SECOND > maxSpeedStepPerSec)
                {
                    maxSpeedStepPerSec = MIN_MOVEMENT_SPEED_MM_SECOND;
                }
                log.info("ID{}: Reduced Speed to {}steps/s !", myId, maxSpeedStepPerSec);
                // try again
                i = 0;
                continue;
            }
            // else ok
        }
        final double FeedrateStepPerSec = getFeedrateOnPrimaryAxisMmSec() * PrimaryAxisStepsPerMm;
        if(maxSpeedStepPerSec > FeedrateStepPerSec)
        {
            // speed is restricted by Feedrate
            maxSpeedStepPerSec = FeedrateStepPerSec;
        }
        final double maxClientSpeedStepsPerSecond = MaxPossibleClientSpeedInStepsPerSecond;
        log.trace("ID{}: Max client Speed = {} setps per second", myId, maxClientSpeedStepsPerSecond);
        if(maxSpeedStepPerSec > maxClientSpeedStepsPerSecond)
        {
            // speed is restricted by the number of steps the client can do per second
            maxSpeedStepPerSec = maxClientSpeedStepsPerSecond;
        }
        log.trace("ID{}: Max possible Speed = {}", myId, maxSpeedStepPerSec);
        return maxSpeedStepPerSec;
    }

    public double getMaxPossibleSpeedMmS()
    {
        return getMaxPossibleSpeedStepsPerSecond() / PrimaryAxisStepsPerMm;
    }

    public double getMaxEndSpeedMmS()
    {
    	Integer help = MaxSpeedStepsPerSecondOnAxis.get(primaryAxis);
    	if(null == help)
    	{
    		return 0.0;
    	}
        final double maxSpeedMmS =  help.doubleValue() / PrimaryAxisStepsPerMm;
        log.trace("ID{}: Max Speed = {} mm/sec", myId, maxSpeedMmS);
        double maxEndSpeedMmS = maxSpeedMmS * endSpeedFactor;
        if(maxEndSpeedMmS < maxJerkMms)
        {
            if(maxSpeedMmS > maxJerkMms)
            {
                maxEndSpeedMmS = maxJerkMms;
            }
            else
            {
                maxEndSpeedMmS = maxSpeedMmS;
            }
        }
        log.trace("ID{}: Max end speed = {} mm/sec", myId, maxEndSpeedMmS);
        // Test if this speed is ok for the other axis
        // speed on primary Axis / steps on primary axis = a
        // a * steps on the axis = speed on that axis
        // speed on each axis must be smaller than the max speed on that axis
        double a = (maxEndSpeedMmS * PrimaryAxisStepsPerMm) / StepsOnAxis.get(primaryAxis);
        for(int i = 0; i < activeAxises.size(); i++)
        {
            final int axisNumber = activeAxises.get(i);
            final int steps = StepsOnAxis.get(axisNumber);
            final int speedStepsPerSecond = (int) (a * steps);
            if(speedStepsPerSecond > MaxSpeedStepsPerSecondOnAxis.get(axisNumber))
            {
                log.info("ID{}: Speed to high for other Axis !", myId);
                // calculate new possible max Speed
                a = MaxSpeedStepsPerSecondOnAxis.get(axisNumber) / steps;
                maxEndSpeedMmS = (a * StepsOnAxis.get(primaryAxis))/PrimaryAxisStepsPerMm;
                // try again
                i = 0;
                continue;
            }
            // else ok
        }

        final double SpeedPerMmSec = getFeedrateOnPrimaryAxisMmSec();
        if(maxEndSpeedMmS > SpeedPerMmSec)
        {
            // speed is restricted by Feedrate
            maxEndSpeedMmS = SpeedPerMmSec;
        }
        final double maxClientSpeedMmS = MaxPossibleClientSpeedInStepsPerSecond / PrimaryAxisStepsPerMm;
        log.trace("ID{}: Max client Speed = {} mm/sec", myId, maxClientSpeedMmS);
        if(maxEndSpeedMmS > maxClientSpeedMmS)
        {
            // speed is restricted by the number of steps the client can do per second
            maxEndSpeedMmS = maxClientSpeedMmS;
        }
        log.trace("ID{}: Max end Speed = {} mm/sec", myId, maxEndSpeedMmS);
        return maxEndSpeedMmS;
    }

    public void setEndSpeedFactor(double endSpeedFactor)
    {
        if((1 < endSpeedFactor) || (0 > endSpeedFactor))
        {
            // not possible
            log.error("Invalid End Speed Factor of {} !", endSpeedFactor);
            return;
        }
        this.endSpeedFactor = endSpeedFactor;
    }

    public int getId()
    {
        return myId;
    }

    // Functions relating to the end Stop command that can be attached to a movement command:

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        hasCommand = true;
        Command_on = on;
        Command_switches = switches;
    }

    public boolean hasACommand()
    {
        return hasCommand;
    }

    public boolean sendCommandTo(Protocol pro)
    {
        if(true == hasCommand)
        {
            if(null != pro)
            {
                return pro.endStopOnOff(Command_on, Command_switches);
            }
        }
        // else nothing to send
        return false;
    }
}
