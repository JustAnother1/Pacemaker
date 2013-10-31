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
package de.nomagic.printerController.core;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Heater_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Executor
{
    private final ActionHandler handler;
    private String lastErrorReason = null;
    private int currentExtruder = 0; // Max 3 Extruders (0..2)
    double[] targetTemperatures = new double[5]; // Print Bed + Chamber + 3 Extruders


    // allowed difference to target temperature in degree Celsius.
    private final double ACCEPTED_TEMPERATURE_DEVIATION = 0.1;

    // time between to polls to client in miliseconds
    private final int POLL_INTERVALL = 100;


    public Executor(ActionHandler handler)
    {
        this.handler = handler;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void close()
    {
    }

    public boolean doShutDown()
    {
        if(false == handler.doAction(Action_enum.doShutDown))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean doImmediateShutDown()
    {
        if(false == handler.doAction(Action_enum.doImmediateShutDown))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

// Stepper Control

    public boolean addPauseFor(final Double seconds)
    {
        if(false == handler.doAction(Action_enum.pauseMovement, seconds))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean addMoveTo(final RelativeMove move)
    {
        if(false == handler.doAction(Action_enum.relativeMove, move))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean startHoming(Axis_enum[] axis)
    {
        if(false == handler.doAction(Action_enum.homeAxis, axis))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean waitForEndOfHoming()
    {
        boolean isHoming = true;
        do
        {
            try
            {
                Thread.sleep(POLL_INTERVALL);
            }
            catch(InterruptedException e)
            {
            }
            ActionResponse response = handler.getValue(Action_enum.getIsHoming);
            if(null == response)
            {
                return false;
            }
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                return false;
            }
            else
            {
                isHoming = response.getBoolean();
            }
        } while(true == isHoming);
        return true;
    }

    public boolean disableAllStepperMotors()
    {
        if(false == handler.doAction(Action_enum.disableMotor))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean enableAllStepperMotors()
    {
        if(false == handler.doAction(Action_enum.enableMotor))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean setStepsPerMilimeter(final Axis_enum axle, final Double stepsPerMillimeter)
    {
        if(false == handler.doAction(Action_enum.setStepsPerMilimeter, axle, stepsPerMillimeter))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }


// FAN

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 255 = max
     */
    public boolean setFanSpeedfor(final int fan, final int speed)
    {
        if(false == handler.doAction(Action_enum.setFanSpeed, fan, speed))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }


// Temperature

    /** sets the desired Temperature for the currently active Extruder
     * and does not wait for the Extruder to reach the temperature.
     *
     * @param temperature The Temperature in degree Celsius.
     */
    public boolean setCurrentExtruderTemperatureNoWait(final Double temperature)
    {
        switch(currentExtruder)
        {
        case 0: return setTemperatureNoWait(Heater_enum.Extruder_0, temperature);
        case 1: return setTemperatureNoWait(Heater_enum.Extruder_1, temperature);
        case 2: return setTemperatureNoWait(Heater_enum.Extruder_2, temperature);
        default: lastErrorReason = "Invalid Extruder Number !"; return false;
        }
    }

    public boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature)
    {
        if(true == setCurrentExtruderTemperatureNoWait(temperature))
        {
            return waitForEverythingInLimits();
        }
        else
        {
            return false;
        }
    }

    /** waits until all heaters created the required Temperatures. */
    public boolean waitForEverythingInLimits()
    {
        for(Heater_enum heater : Heater_enum.values())
        {
            if(false == waitForHeaterInLimits(heater))
            {
                return false;
            }
        }
        return true;
    }

    public boolean setPrintBedTemperatureNoWait(final Double temperature)
    {
        return setTemperatureNoWait(Heater_enum.Print_Bed, temperature);
    }

    public boolean setChamberTemperatureNoWait(final Double temperature)
    {
        return setTemperatureNoWait(Heater_enum.Chamber, temperature);
    }

    public boolean setPrintBedTemperatureAndDoWait(final Double temperature)
    {
        if(true == setTemperatureNoWait(Heater_enum.Print_Bed, temperature))
        {
            return waitForHeaterInLimits(Heater_enum.Print_Bed);
        }
        else
        {
            return false;
        }
    }

    private boolean waitForHeaterInLimits(final Heater_enum heater)
    {
        double curTemperature = 0.0;
        double targetTemp = 0.0;
        switch(heater)
        {
        case Chamber:    targetTemp = targetTemperatures[0]; break;
        case Print_Bed:  targetTemp = targetTemperatures[1]; break;
        case Extruder_0: targetTemp = targetTemperatures[2]; break;
        case Extruder_1: targetTemp = targetTemperatures[3]; break;
        case Extruder_2: targetTemp = targetTemperatures[4]; break;
        default: lastErrorReason = "Invalid Extruder Number !"; return false;
        }

        if(   (targetTemp > 0.0 - ACCEPTED_TEMPERATURE_DEVIATION)
           && (targetTemp < 0.0 + ACCEPTED_TEMPERATURE_DEVIATION))
        {
            // if the heater is not heating
            return true;
        }

        do
        {
            try
            {
                Thread.sleep(POLL_INTERVALL);
            }
            catch(InterruptedException e)
            {
            }
            ActionResponse response = handler.getValue(Action_enum.getHeaterTemperature, heater);
            if(null == response)
            {
                return false;
            }
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                return false;
            }
            else
            {
                curTemperature = response.getTemperature();
            }
        } while(   (curTemperature < targetTemp - ACCEPTED_TEMPERATURE_DEVIATION) // too cold
                || (curTemperature > targetTemp + ACCEPTED_TEMPERATURE_DEVIATION)); // too hot
        return true;
    }

    private boolean setTemperatureNoWait( final Heater_enum heater, final Double temperature)
    {
        switch(heater)
        {
        case Chamber:    targetTemperatures[0] = temperature; break;
        case Print_Bed:  targetTemperatures[1] = temperature; break;
        case Extruder_0: targetTemperatures[2] = temperature; break;
        case Extruder_1: targetTemperatures[3] = temperature; break;
        case Extruder_2: targetTemperatures[4] = temperature; break;
        default: lastErrorReason = "Invalid Extruder Number !"; return false;
        }

        if(false == handler.doAction(Action_enum.setHeaterTemperature, temperature, heater))
        {
            lastErrorReason = handler.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

}
