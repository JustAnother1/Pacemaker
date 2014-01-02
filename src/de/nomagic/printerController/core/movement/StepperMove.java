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
import java.util.Vector;

import de.nomagic.printerController.Axis_enum;
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
    private double[] mm;
    private int maxSteps = 0;
    private int StepperWithMostSteps = -1;
    private int IdxOfStepperWithMostSteps = -1;

    private boolean on;
    private Integer[] switches;

    private Vector<Integer> activeSteppers = new Vector<Integer>();
    private HashMap<Integer, Boolean> AxisDirectionIsIncreasing = new HashMap<Integer, Boolean>();
    private HashMap<Integer, Integer> StepsOnAxis = new HashMap<Integer, Integer>();
    private HashMap<Integer, Double> maxEndSpeedMmPerSecond = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> maxSpeedMmPerSecond = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> StepsPerMm = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> maxAccelerationStepsPerSecond = new HashMap<Integer, Double>();
    private HashMap<Integer, Integer> maxPossibleSpeedStepsPerSecond = new  HashMap<Integer, Integer>();


    public StepperMove()
    {
        mm = new double[Axis_enum.size];
        for(int i = 0; i < Axis_enum.size; i++)
        {
            mm[i] = 0.0;
        }
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        for(Axis_enum axis: Axis_enum.values())
        {
            sb.append(", mm" + axis.toString() + "=" +  mm[axis.ordinal()]);
        }

        return "StepperMove [isHomingMove=" + isHomingMove +
                            ", hasCommand=" + hasCommand +
                            sb.toString() +
                            "]";
    }

    public void addAxisMotors(Stepper motor)
    {
        final Integer stepperNum = motor.getStepperNumber();
        activeSteppers.add(stepperNum);
        final int steps = motor.getSteps();
        StepsOnAxis.put(stepperNum, steps);
        if(maxSteps < Math.abs(steps))
        {
            maxSteps = Math.abs(steps);
            StepperWithMostSteps = stepperNum;
            IdxOfStepperWithMostSteps = AxisDirectionIsIncreasing.size();
            // This stepper is not added yet so the size == the next used Index
        }
        Boolean isIncreasing = false;
        if(0 > steps)
        {
            if(false == motor.isDirectionInverted())
            {
                isIncreasing = false;
            }
            else
            {
                isIncreasing = true;
            }
        }
        else
        {
            if(false == motor.isDirectionInverted())
            {
                isIncreasing = true;
            }
            else
            {
                isIncreasing = false;
            }
        }
        AxisDirectionIsIncreasing.put(stepperNum, isIncreasing);
        maxSpeedMmPerSecond.put(stepperNum, motor.getMaxSpeedMmPerSecond());
        StepsPerMm.put(stepperNum, motor.getStepsPerMm());
        maxAccelerationStepsPerSecond.put(stepperNum, motor.getMaxAccelerationStepsPerSecond());
        final int maxPossibleForMotor = motor.getMaxPossibleSpeedStepsPerSecond();
        final double maxSpeedForMove = motor.getMaxTravelSpeedStepsPerSecond();
        if(maxSpeedForMove > maxPossibleForMotor)
        {
            maxPossibleSpeedStepsPerSecond.put(stepperNum, maxPossibleForMotor);
        }
        else
        {
            maxPossibleSpeedStepsPerSecond.put(stepperNum, (int)maxSpeedForMove);
        }
    }

    public Integer[] getAllActiveSteppers()
    {
        return activeSteppers.toArray(new Integer[0]);
    }

    public boolean[] getAxisDirectionIsIncreasing()
    {
        final boolean[] res = new boolean[activeSteppers.size()];
        for(int i = 0; i < res.length; i++)
        {
            res[i] = AxisDirectionIsIncreasing.get(activeSteppers.get(i));
        }
        return res;
    }

    public Integer[] getSteps()
    {
        final Integer[] res = new Integer[activeSteppers.size()];
        for(int i = 0; i < res.length; i++)
        {
            res[i] = StepsOnAxis.get(activeSteppers.get(i));
        }
        return res;
    }

    public void setMaxEndSpeedMmPerSecondFor(int axis, double speed)
    {
        maxEndSpeedMmPerSecond.put(axis, speed);
    }

    public double getMaxEndSpeedMmPerSecondFor(int axis)
    {
        return maxEndSpeedMmPerSecond.get(axis);
    }

    public double getMaxEndSpeedStepsPerSecondFor(int axis)
    {
        return getMaxEndSpeedMmPerSecondFor(axis) * StepsPerMm.get(axis);
    }

    public double getMaxSpeedMmPerSecondFor(int axis)
    {
        return maxSpeedMmPerSecond.get(axis);
    }

    public double getMaxSpeedStepsPerSecondFor(int axis)
    {
        return getMaxSpeedMmPerSecondFor(axis) * StepsPerMm.get(axis);
    }

    public double getMaxAccelerationStepsPerSecond2(int axis)
    {
        return maxAccelerationStepsPerSecond.get(axis);
    }

    public int getMaxPossibleSpeedStepsPerSecond(int axis)
    {
        return maxPossibleSpeedStepsPerSecond.get(axis);
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

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        hasCommand = true;
        this.on = on;
        this.switches = switches;
    }

    public int getStepsOnStepper(int axis)
    {
        return StepsOnAxis.get(axis);
    }

    public boolean getOn()
    {
        return on;
    }

    public Integer[] getSwitches()
    {
        return switches;
    }

    public int getMaxSteps()
    {
        return maxSteps;
    }

    public int getStepperWithMostSteps()
    {
        return StepperWithMostSteps;
    }

    public int getIndexOfStepperWithMostSteps()
    {
        return IdxOfStepperWithMostSteps;
    }

    /** Splits this move into several moves.
     *
     * @param maxStepsPerMove
     * @return
     */
    public StepperMove[] splitInto(int maxStepsPerMove)
    {
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
            sm.StepperWithMostSteps = StepperWithMostSteps;
            sm.IdxOfStepperWithMostSteps = IdxOfStepperWithMostSteps;
            sm.activeSteppers.addAll(activeSteppers);
            sm.AxisDirectionIsIncreasing.putAll(AxisDirectionIsIncreasing);
            sm.maxEndSpeedMmPerSecond.putAll(maxEndSpeedMmPerSecond);
            sm.maxSpeedMmPerSecond.putAll(maxSpeedMmPerSecond);
            sm.StepsPerMm.putAll(StepsPerMm);
            sm.maxAccelerationStepsPerSecond.putAll(maxAccelerationStepsPerSecond);
            sm.maxPossibleSpeedStepsPerSecond.putAll(maxPossibleSpeedStepsPerSecond);
            res[i] = sm;
        }
        // copy the command but only to the first move
        if(true == hasCommand)
        {
            res[0].hasCommand = true;
            res[0].on = on;
            res[0].switches = switches;
        }
        // add the move parts
        for(int i = 0; i < activeSteppers.size(); i++)
        {
            final Integer StepperNum = activeSteppers.get(i);
            final int allSteps = StepsOnAxis.get(StepperNum);
            final int StepsPerPart = allSteps/ numParts;
            for(int j = 0; j < numParts; j++)
            {
                res[j].StepsOnAxis.put(StepperNum, StepsPerPart);
            }
        }
        final int newMaxSteps = res[0].StepsOnAxis.get(res[0].StepperWithMostSteps);
        for(int i = 0; i < res.length; i++)
        {
            for(Axis_enum axis: Axis_enum.values())
            {
                res[i].mm[axis.ordinal()] = this.mm[axis.ordinal()]/res.length;// res.length == numParts
            }
            res[i].maxSteps = newMaxSteps;
        }
        return res;
    }

    public double getMm(Axis_enum axis)
    {
        return mm[axis.ordinal()];
    }

    public void setMm(double mm, Axis_enum axis)
    {
        this.mm[axis.ordinal()] = mm;
    }

}
