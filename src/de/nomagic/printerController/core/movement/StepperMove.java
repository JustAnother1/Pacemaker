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

import de.nomagic.printerController.core.devices.Stepper;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class StepperMove
{
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

    public void addAxisMotors(Stepper motor)
    {
        activeAxis.put(motor.getStepperNumber(), motor);
    }

    public Integer[] getAllActiveSteppers()
    {
        Set<Integer> s = activeAxis.keySet();
        return s.toArray(new Integer[0]);
    }

    public boolean[] getAxisDirectionIsIncreasing()
    {
        boolean[] res = new boolean[activeAxis.size()];
        Set<Integer> s = activeAxis.keySet();
        Iterator<Integer> it = s.iterator();
        int i = 0;
        while(it.hasNext())
        {
            Stepper motor = activeAxis.get(it.next());
            int steps = motor.getSteps();
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
            i++;
        }
        return res;
    }

    public Integer[] getSteps()
    {
        Integer[] res = new Integer[activeAxis.size()];
        Set<Integer> s = activeAxis.keySet();
        Iterator<Integer> it = s.iterator();
        int i = 0;
        while(it.hasNext())
        {
            Stepper motor = activeAxis.get(it.next());
            res[i] = motor.getSteps();
            i++;
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
        Set<Integer> s = activeAxis.keySet();
        Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            int idx = it.next();
            Stepper motor = activeAxis.get(idx);
            int steps = motor.getSteps();
            if(steps > maxSteps)
            {
                maxSteps = steps;
            }
        }
        return maxSteps;
    }

    public int getStepperWithMostSteps()
    {
        int maxSteps = 0;
        int MotorIdx = -1;
        Set<Integer> s = activeAxis.keySet();
        Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            int idx = it.next();
            Stepper motor = activeAxis.get(idx);
            int steps = motor.getSteps();
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
        Stepper MaxMotor = activeAxis.get(getStepperWithMostSteps());
        int maxSteps = MaxMotor.getSteps();
        int numParts = maxSteps / maxStepsPerMove;
        if(maxSteps > numParts * maxStepsPerMove)
        {
            // integer divide rounded down
            numParts = numParts + 1;
        }
        // create the classes
        StepperMove[] res = new StepperMove[numParts];
        for(int i = 0; i < numParts; i++)
        {
            StepperMove sm = new StepperMove();
            sm.isHomingMove = this.isHomingMove;
            res[i] = sm;
        }
        // add the move parts
        Set<Integer> s = activeAxis.keySet();
        Iterator<Integer> it = s.iterator();
        while(true == it.hasNext())
        {
            int idx = it.next();
            Stepper motor = activeAxis.get(idx);
            int Steps = motor.getSteps();
            int stepsPerSplit = Steps / numParts;
            for(int i = 0; i < (numParts - 1); i++)
            {
                Stepper splitMotor = new Stepper(motor);
                splitMotor.setSteps(stepsPerSplit);
                res[i].addAxisMotors(splitMotor);
            }
            Stepper splitMotor = new Stepper(motor);
            int restOfSteps = Steps - ((numParts -1) * stepsPerSplit);
            splitMotor.setSteps(restOfSteps);
            res[numParts -1].addAxisMotors(splitMotor);
        }
        return res;
    }

    public void setMaxEndSpeedMmPerSecondFor(int axis, double speed)
    {
        Stepper motor = activeAxis.get(axis);
        motor.setMaxEndSpeedMmPerSecond(speed);
        activeAxis.put(axis, motor);
    }

    public double getMaxEndSpeedMmPerSecondFor(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxEndSpeedMmPerSecond();
    }

    public double getMaxSpeedMmPerSecondFor(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxSpeedMmPerSecond();
    }

    public int getStepsOnStepper(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getSteps();
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
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxEndSpeedStepsPerSecond();
    }

    public double getMaxSpeedStepsPerSecondFor(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxTravelSpeedStepsPerSecond();
    }

    public double getMaxAccelerationStepsPerSecond2(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxAccelerationStepsPerSecond();
    }

    public double getMaxPossibleSpeedStepsPerSecond(int axis)
    {
        Stepper motor = activeAxis.get(axis);
        return motor.getMaxPossibleSpeedStepsPerSecond();
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