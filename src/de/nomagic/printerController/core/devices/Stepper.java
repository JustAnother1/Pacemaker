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
package de.nomagic.printerController.core.devices;


/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Stepper
{
    public static final int MAX_STEPS_PER_PART = 65535;

    private int DistanceInSteps = 0;

    private int StepperNumber;
    private double MaxAccelleration;
    private int maxPossibleStepsPerSecond;
    private boolean DirectionInverted;
    private Double StepsPerMillimeter;
    private Double RoundingError = 0.0;
    private double maxEndSpeed = 0.0;
    private double maxSpeed = 0.0;


    public Stepper(int StepperNumber ,
                   double maxAccelleration,
                   int maxPossibleStepsPerSecond,
                   boolean DirectionInverted,
                   double StepsPerMillimeter)
    {
        this.StepperNumber = StepperNumber;
        this.MaxAccelleration = maxAccelleration;
        this.maxPossibleStepsPerSecond = maxPossibleStepsPerSecond;
        this.DirectionInverted = DirectionInverted;
        this.StepsPerMillimeter = StepsPerMillimeter;
    }

    public Stepper(Stepper src)
    {
        this.StepperNumber = src.getStepperNumber();
        this.MaxAccelleration = src.MaxAccelleration;
        this.DirectionInverted = src.DirectionInverted;
        this.StepsPerMillimeter = src.StepsPerMillimeter;
    }

    public void setStepsPerMillimeter(Double steps)
    {
        StepsPerMillimeter = steps;
    }

    public boolean isDirectionInverted()
    {
        return DirectionInverted;
    }

    @Override
    public String toString()
    {
        return "[num=" + StepperNumber +
               " maxAccel=" + MaxAccelleration +
               " dirInv=" + DirectionInverted +
               " steps/mm=" + StepsPerMillimeter +
               " max possible Steps/s=" + maxPossibleStepsPerSecond + "]\n";
    }

    public int getStepperNumber()
    {
        return StepperNumber;
    }

    public void clearMove()
    {
        DistanceInSteps = 0;
    }

    public void addMove(double distanceInMillimeters)
    {
        double Steps = (distanceInMillimeters + RoundingError) * StepsPerMillimeter;
        DistanceInSteps = (int)Steps;
        RoundingError = (Steps - DistanceInSteps)/StepsPerMillimeter;
    }

    public void setSteps(int steps)
    {
        DistanceInSteps = steps;
    }

    public int getSteps()
    {
        return DistanceInSteps;
    }

    public double getMaxEndSpeedMmPerSecond()
    {
        return maxEndSpeed;
    }

    public double getMaxEndSpeedStepsPerSecond()
    {
        return maxEndSpeed * StepsPerMillimeter;
    }

    public double getMaxSpeedMmPerSecond()
    {
        return maxSpeed;
    }

    public double getMaxTravelSpeedStepsPerSecond()
    {
        return maxSpeed * StepsPerMillimeter;
    }

    public void setMaxEndSpeedMmPerSecond(double maxEndSpeed)
    {
        this.maxEndSpeed = maxEndSpeed;
    }

    public void setMaxSpeedMmPerSecond(double maxSpeed)
    {
        this.maxSpeed = maxSpeed;
    }

    public void setMaxSpeedStepsPerSecond(double maxSpeed)
    {
        this.maxSpeed = maxSpeed / StepsPerMillimeter;
    }

    public int getMaxPossibleSpeedStepsPerSecond()
    {
        return maxPossibleStepsPerSecond;
    }

    public double getMaxAccelerationStepsPerSecond()
    {
        return MaxAccelleration;
    }

}
