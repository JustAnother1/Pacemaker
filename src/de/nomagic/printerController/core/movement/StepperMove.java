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
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.core.devices.Stepper;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class StepperMove
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private boolean isHomingMove = false;
    private boolean hasCommand = false;
    private double mmX = 0.0;
    private double mmY = 0.0;
    private double mmZ = 0.0;

    private boolean on;
    private Integer[] switches;
    private HashMap<Integer, Stepper> activeAxis = new HashMap<Integer, Stepper>();

    public StepperMove()
    {
    }

    @Override
    public String toString()
    {
        return "StepperMove [isHomingMove=" + isHomingMove +
                            ", hasCommand=" + hasCommand +
                            ", mmX=" + mmX +
                            ", mmY=" + mmY +
                            ", mmZ=" + mmZ +
                            "]";
    }

    public void addAxisMotors(Stepper motor)
    {
        activeAxis.put(motor.getStepperNumber(), motor);
    }

    public Integer[] getAllActiveSteppers()
    {
        final Set<Integer> s = activeAxis.keySet();
        final Iterator<Integer> it = s.iterator();
        final Vector<Integer> res = new Vector<Integer>();
        while(true == it.hasNext())
        {
            final Integer curAxis = it.next();
            final Stepper motor = activeAxis.get(curAxis);
            if(0 != motor.getSteps())
            {
                res.add(curAxis);
            }
        }
        return res.toArray(new Integer[0]);
    }

    public boolean[] getAxisDirectionIsIncreasing(Integer[] activeSteppers)
    {
        final boolean[] res = new boolean[activeSteppers.length];
        for(int i = 0; i < activeSteppers.length; i++)
        {
            final Integer curAxis = activeSteppers[i];
            final Stepper motor = activeAxis.get(curAxis);
            final int steps = motor.getSteps();
            if(0 > steps)
            {
                if(false == motor.isDirectionInverted())
                {
                    res[i] = false;
                }
                else
                {
                    res[i] = true;
                }
            }
            else
            {
                if(false == motor.isDirectionInverted())
                {
                    res[i] = true;
                }
                else
                {
                    res[i] = false;
                }
            }
        }
        return res;
    }

    public Integer[] getSteps(Integer[] activeSteppers)
    {
        final Integer[] res = new Integer[activeSteppers.length];
        for(int i = 0; i < activeSteppers.length; i++)
        {
            final Stepper motor = activeAxis.get(activeSteppers[i]);
            res[i] = motor.getSteps();
        }
        return res;
    }

    public void setIsHoming(boolean home)
    {
        isHomingMove = home;
    }

    public boolean isHomingMove()
    {
        return isHomingMove;
    }

    public boolean hasCommand()
    {
        return hasCommand;
    }

    public int getMaxSteps()
    {
        int maxSteps = 0;
        final Set<Integer> s = activeAxis.keySet();
        final Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            final int idx = it.next();
            final Stepper motor = activeAxis.get(idx);
            final int steps = motor.getSteps();
            if(steps > maxSteps)
            {
                maxSteps = steps;
            }
        }
        return maxSteps;
    }

    public int getStepperWithMostSteps()
    {
        long maxSteps = 0;
        int MotorIdx = -1;
        final Set<Integer> s = activeAxis.keySet();
        final Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            final int idx = it.next();
            final Stepper motor = activeAxis.get(idx);
            final long steps = Math.abs(motor.getSteps());
            if(steps > maxSteps)
            {
                maxSteps = steps;
                MotorIdx = idx;
            }
        }
        return MotorIdx;
    }

    /** Splits this move into several moves.
     *
     * @param maxStepsPerMove
     * @return
     */
    public StepperMove[] splitInto(int maxStepsPerMove)
    {
        final Stepper MaxMotor = activeAxis.get(getStepperWithMostSteps());
        final int maxSteps = MaxMotor.getSteps();
        int numParts = maxSteps / maxStepsPerMove;
        if(maxSteps > numParts * maxStepsPerMove)
        {
            // integer divide rounded down
            numParts = numParts + 1;
        }
        // create the classes
        final StepperMove[] res = new StepperMove[numParts];
        for(int i = 0; i < numParts; i++)
        {
            final StepperMove sm = new StepperMove();
            sm.isHomingMove = this.isHomingMove;
            res[i] = sm;
        }
        // add the move parts
        final Set<Integer> s = activeAxis.keySet();
        final Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            final int idx = it.next();
            final Stepper motor = activeAxis.get(idx);
            final int Steps = motor.getSteps();
            final int stepsPerSplit = Steps / numParts;
            for(int i = 0; i < (numParts - 1); i++)
            {
                final Stepper splitMotor = new Stepper(motor);
                splitMotor.setSteps(stepsPerSplit);
                res[i].addAxisMotors(splitMotor);
            }
            final Stepper splitMotor = new Stepper(motor);
            final int restOfSteps = Steps - ((numParts -1) * stepsPerSplit);
            splitMotor.setSteps(restOfSteps);
            res[numParts -1].addAxisMotors(splitMotor);
        }
        return res;
    }

    public void setMaxEndSpeedMmPerSecondFor(int axis, double speed)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null != motor)
        {
            motor.setMaxEndSpeedMmPerSecond(speed);
            activeAxis.put(axis, motor);
        }
    }

    public double getMaxEndSpeedMmPerSecondFor(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0;
        }
        else
        {
            return motor.getMaxEndSpeedMmPerSecond();
        }
    }

    public double getMaxSpeedMmPerSecondFor(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0;
        }
        else
        {
            return motor.getMaxSpeedMmPerSecond();
        }
    }

    public int getStepsOnStepper(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0;
        }
        else
        {
            return motor.getSteps();
        }
    }

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        hasCommand = true;
        this.on = on;
        this.switches = switches;
    }

    public boolean getOn()
    {
        return on;
    }

    public Integer[] getSwitches()
    {
        return switches;
    }

    public double getMaxEndSpeedStepsPerSecondFor(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0.0;
        }
        else
        {
            return motor.getMaxEndSpeedStepsPerSecond();
        }
    }

    public double getMaxSpeedStepsPerSecondFor(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0.0;
        }
        else
        {
            return motor.getMaxTravelSpeedStepsPerSecond();
        }
    }

    public double getMaxAccelerationStepsPerSecond2(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0.0;
        }
        else
        {
            return motor.getMaxAccelerationStepsPerSecond();
        }
    }

    public double getMaxPossibleSpeedStepsPerSecond(int axis)
    {
        final Stepper motor = activeAxis.get(axis);
        if(null == motor)
        {
            log.trace("Asked for inactive Axis ! {}", axis);
            return 0.0;
        }
        else
        {
            return motor.getMaxPossibleSpeedStepsPerSecond();
        }
    }

    public double getMmX()
    {
        return mmX;
    }

    public void setMmX(double mmX)
    {
        this.mmX = mmX;
    }

    public double getMmY()
    {
        return mmY;
    }

    public void setMmY(double mmY)
    {
        this.mmY = mmY;
    }

    public double getMmZ()
    {
        return mmZ;
    }

    public void setMmZ(double mmZ)
    {
        this.mmZ = mmZ;
    }

}
