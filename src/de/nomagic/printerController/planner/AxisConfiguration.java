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
package de.nomagic.printerController.planner;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class AxisConfiguration
{
    private int MinSwitch = -1;
    private int MaxSwitch = -1;
    private boolean minInverted = true;
    private boolean maxInverted = true;
    private int stepperNumber = -1;
    private int secondStepperNumber = -1;
    private double stepsPerMillimeter = -1;

    public AxisConfiguration()
    {
    }

    public boolean hasSecondStepper()
    {
        if(-1 == secondStepperNumber)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void setStepperNumber(final int stepperNumber)
    {
        this.stepperNumber = stepperNumber;
    }

    public int getStepperNumber()
    {
        return stepperNumber;
    }

    public void setSecondStepperNumber(final int secondStepperNumber)
    {
        this.secondStepperNumber = secondStepperNumber;
    }

    public int getSecondStepper()
    {
        return secondStepperNumber;
    }

    public double getStepsPerMillimeter()
    {
        return stepsPerMillimeter;
    }

    public void setStepsPerMillimeter(final double steps)
    {
        stepsPerMillimeter = steps;
    }

    public int getMinSwitch()
    {
        return MinSwitch;
    }

    public int getMaxSwitch()
    {
        return MaxSwitch;
    }

    public void setMinSwitch(final int minSwitch)
    {
        MinSwitch = minSwitch;
    }

    public void setMaxSwitch(final int maxSwitch)
    {
        MaxSwitch = maxSwitch;
    }

    public boolean getMaxSwitchInverted()
    {
        return maxInverted;
    }

    public boolean getMinSwitchInverted()
    {
        return minInverted;
    }

    public void setMinInverted(final boolean minInverted)
    {
        this.minInverted = minInverted;
    }

    public void setMaxInverted(final boolean maxInverted)
    {
        this.maxInverted = maxInverted;
    }

}
