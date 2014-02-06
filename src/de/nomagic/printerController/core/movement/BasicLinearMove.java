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
    public static final double MIN_MOVEMENT_DISTANCE = 0.00001;

    public static final double MOVEMENT_SPEED_TOLERANCE_MM_SECOND = 0.0001;
    /** if the axis has steps the speed may not be 0. So this is the speed is will have at least */
    public static final double MIN_MOVEMENT_SPEED_MM_SECOND = 0.1;

    private static int nextId = 0; // singleton

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private double feedrateMmPerMinute = MIN_MOVEMENT_SPEED_MM_SECOND * 60;
    private boolean isHoming = false;
    private HashMap<Axis_enum, Double> distances = new HashMap<Axis_enum, Double>();
    private HashMap<Axis_enum, Double> roundingError = new HashMap<Axis_enum, Double>();
    private HashMap<Integer, Axis_enum> AxisMapping = new HashMap<Integer, Axis_enum>();
    private Vector<Integer> activeAxises = new Vector<Integer>();
    private HashMap<Integer, Integer> StepsOnAxis = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> MaxSpeedStepsPerSecondOnAxis = new HashMap<Integer, Integer>();
    private HashMap<Integer, Boolean> AxisDirectionIncreasing = new HashMap<Integer, Boolean>();
    private int NumBytesNeeededForSteps = 1;
    private int primaryAxis = -1;
    private int StepsOnPrimaryAxis = -1;
    private double PrimaryAxisStepsPerMm = 1;
    private double PrimaryAxisMaxAceleration = 1;

    private boolean hasCommand = false;
    private boolean hasMovement = false;
    private boolean Command_on = true;
    private Integer[] Command_switches;

    private final int MaxPossibleClientSpeed;
    private int maxStepperNumber = -1;
    private double endSpeed = 0.0;
    private boolean hasEndSpeed = false;
    private double endSpeedFactor = 1.0; // no limitation
    private double travelSpeed = 0.0;
    private int AccelerationSteps = 0;
    private int DecelerationSteps = 0;

    private int myId;

    public BasicLinearMove(int MaxPossibleClientSpeed)
    {
        this.MaxPossibleClientSpeed = MaxPossibleClientSpeed;
        myId = nextId;
        nextId++;
        for(Axis_enum axis: Axis_enum.values())
        {
            roundingError.put(axis, 0.0);
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
    }

    public void setDistanceMm(Axis_enum axis, double distance)
    {
        log.debug("ID{}: adding {} = {} mm", myId, axis, distance);
        distances.put(axis, distance);
        if(MIN_MOVEMENT_DISTANCE < distance)
        {
            hasMovement = true;
        }
    }

    public double getMm(Axis_enum axis)
    {
        try
        {
            return distances.get(axis);
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

    public int getNumberOfActiveSteppers()
    {
        return activeAxises.size();
    }

    public int getHighestStepperNumber()
    {
        return maxStepperNumber;
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

    public int getBytesPerStep()
    {
        return NumBytesNeeededForSteps;
    }

    public int getPrimaryAxis()
    {
        return primaryAxis;
    }

    public void addMovingAxis(Stepper stepper, Axis_enum ax)
    {
        if(null == stepper)
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
        final double exactSteps = roundingError.get(ax) + (distances.get(ax) * stepper.getStepsPerMm());
        final int steps = (int) Math.round(exactSteps);
        log.debug("ID{}: exact Steps = {}, got rounded to {}", myId, exactSteps, steps);
        final Double difference = exactSteps - steps;
        roundingError.put(ax, difference);
        if(0 == steps)
        {
            return;
        }
        hasMovement = true;
        if(255 < Math.abs(steps))
        {
            log.debug("ID{}: we will need 2 bytes for steps", myId);
            NumBytesNeeededForSteps = 2;
        }
        if(StepsOnPrimaryAxis < Math.abs(steps))
        {
            StepsOnPrimaryAxis = Math.abs(steps);
            primaryAxis = number;
            PrimaryAxisStepsPerMm = stepper.getStepsPerMm();
            PrimaryAxisMaxAceleration = stepper.getMaxAccelerationStepsPerSecond();
        }
        activeAxises.add(number);
        if(false == stepper.isDirectionInverted())
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(number, true);
            }
            else
            {
                AxisDirectionIncreasing.put(number, false);
            }
        }
        else
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(number, false);
            }
            else
            {
                AxisDirectionIncreasing.put(number, true);
            }
        }
        StepsOnAxis.put(number, Math.abs(steps));
        final int maxSpeedStepsPerSecond = stepper.getMaxPossibleSpeedStepsPerSecond();
        MaxSpeedStepsPerSecondOnAxis.put(number, maxSpeedStepsPerSecond);
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

    public int getTravelSpeedFraction()
    {
        return (int)((travelSpeed * PrimaryAxisStepsPerMm * 255)/ MaxPossibleClientSpeed);
    }

    public void setTravelSpeed(double speed)
    {
        log.trace("ID{}: Nominal speed set to {} mm/s!", myId, speed);
        travelSpeed = speed;
    }

    public void setEndSpeed(double theSpeed)
    {
        endSpeed = theSpeed;
        log.trace("ID{}: end speed set to {} mm/s", myId, theSpeed);
        hasEndSpeed = true;
    }

    public double getEndSpeed()
    {
        return endSpeed;
    }

    public boolean endSpeedIsZero()
    {
        if(MOVEMENT_SPEED_TOLERANCE_MM_SECOND < endSpeed)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public int getEndSpeedFraction()
    {
        return (int)((endSpeed * PrimaryAxisStepsPerMm * 255)/ MaxPossibleClientSpeed);
    }

    public void setAccelerationSteps(int steps)
    {
        AccelerationSteps = steps;
    }

    public void setDecelerationSteps(int steps)
    {
        DecelerationSteps = steps;
    }

    public int getAccelerationSteps()
    {
        return AccelerationSteps;
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
        // v1, v2 = steps per second
        // a = steps /second*second
        // t = time in seconds
        // v2 = v1 - a*t -> t = (v2 - v1)/a
        // s = v * t
        // ->
        // S = (v1 + v2)/2 * abs(v1 - v2)/a
        return ((v1 + v2)/2) * (Math.abs(v1 - v2)/a);
    }

    public int getNumberOfStepsForSpeedChange(double startSpeed, double endSpeed)
    {
        final double distanceMm = Math.abs(getBrakingDistance(startSpeed, endSpeed, getMaxAcceleration()));
        return (int)(distanceMm * PrimaryAxisStepsPerMm);
    }

    public double getMaxAcceleration()
    {
        return PrimaryAxisMaxAceleration;
    }

    public double getSpeedChangeForSteps(int steps)
    {
        // V = sqr(2 * s* a
        final double change = Math.sqrt(2 * steps * getMaxAcceleration());
        return change;
    }

    public double getMaxPossibleSpeed()
    {
        double distanceOnXYZMm = 0.0;
        final Iterator<Double> it = distances.values().iterator();
        while(it.hasNext())
        {
            distanceOnXYZMm = distanceOnXYZMm + Math.abs(it.next());
        }
        log.trace("ID{}: distance on X Y Z = {} mm", myId, distanceOnXYZMm);

        double SpeedPerMmSec = (feedrateMmPerMinute/60) / distanceOnXYZMm;
        log.trace("ID{}: Speed Factor = {} mm/second", myId, SpeedPerMmSec);
        SpeedPerMmSec = SpeedPerMmSec * distances.get(AxisMapping.get(primaryAxis));

        double maxSpeed = MaxSpeedStepsPerSecondOnAxis.get(primaryAxis);
        // Test if this speed is ok for the other axis
        // speed on primary Axis / steps on primary axis = a
        // a * steps on the axis = speed on that axis
        // speed on each axis must be smaller than the max speed on that axis
        double a = maxSpeed / StepsOnAxis.get(primaryAxis);
        for(int i = 0; i < activeAxises.size(); i++)
        {
            final int axisNumber = activeAxises.get(i);
            final int steps = StepsOnAxis.get(axisNumber);
            final int speed = (int) (a * steps);
            if(speed > MaxSpeedStepsPerSecondOnAxis.get(axisNumber))
            {
                log.error("ID{}: Speed to high for other Axis !", myId);
                // calculate new possible max Speed
                a = MaxSpeedStepsPerSecondOnAxis.get(axisNumber) / steps;
                maxSpeed = a * StepsOnAxis.get(primaryAxis);
                // try again
                i = 0;
                continue;
            }
            // else ok
        }

        if(maxSpeed > SpeedPerMmSec)
        {
            // speed is restricted by Feedrate
            maxSpeed = SpeedPerMmSec;
        }
        return maxSpeed;
    }

    public double getMaxEndSpeed()
    {
        if(MOVEMENT_SPEED_TOLERANCE_MM_SECOND > endSpeedFactor)
        {
            return 0.0;
        }
        double distanceOnXYZMm = 0.0;
        final Iterator<Double> it = distances.values().iterator();
        while(it.hasNext())
        {
            distanceOnXYZMm = distanceOnXYZMm + Math.abs(it.next());
        }
        log.trace("ID{}: distance on X Y Z = {} mm", myId, distanceOnXYZMm);

        double SpeedPerMmSec = (feedrateMmPerMinute/60) / distanceOnXYZMm;
        log.trace("ID{}: Speed Factor = {} mm/second", myId, SpeedPerMmSec);
        SpeedPerMmSec = SpeedPerMmSec * distances.get(AxisMapping.get(primaryAxis));

        final int maxSpeed = MaxSpeedStepsPerSecondOnAxis.get(primaryAxis);
        double maxEndSpeed = maxSpeed * endSpeedFactor;
        // Test if this speed is ok for the other axis
        // speed on primary Axis / steps on primary axis = a
        // a * steps on the axis = speed on that axis
        // speed on each axis must be smaller than the max speed on that axis
        double a = maxEndSpeed / StepsOnAxis.get(primaryAxis);
        for(int i = 0; i < activeAxises.size(); i++)
        {
            final int axisNumber = activeAxises.get(i);
            final int steps = StepsOnAxis.get(axisNumber);
            final int speed = (int) (a * steps);
            if(speed > MaxSpeedStepsPerSecondOnAxis.get(axisNumber))
            {
                log.error("ID{}: Speed to high for other Axis !", myId);
                // calculate new possible max Speed
                a = MaxSpeedStepsPerSecondOnAxis.get(axisNumber) / steps;
                maxEndSpeed = a * StepsOnAxis.get(primaryAxis);
                // try again
                i = 0;
                continue;
            }
            // else ok
        }

        if(maxEndSpeed > SpeedPerMmSec)
        {
            // speed is restricted by Feedrate
            maxEndSpeed = SpeedPerMmSec;
        }
        return maxEndSpeed;
    }

    public void setEndSpeedFactor(double endSpeedFactor)
    {
        if((1 < endSpeedFactor) || (0 > endSpeedFactor))
        {
            // not possible
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
