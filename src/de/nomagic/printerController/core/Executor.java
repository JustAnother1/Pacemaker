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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Switch_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Executor
{
    public static final int SWITCH_STATE_OPEN = 0;
    public static final int SWITCH_STATE_CLOSED = 1;
    public static final int SWITCH_STATE_NOT_AVAILABLE = 2;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final ActionHandler handler;
    private String lastErrorReason = null;
    private int currentExtruder = 0; // Max 3 Extruders (0..2)
    private double[] targetTemperatures = new double[Heater_enum.size];

    // allowed difference to target temperature in degree Celsius.
    private final double ACCEPTED_TEMPERATURE_DEVIATION = 0.4;

    // time between to polls to client in miliseconds
    private final int POLL_INTERVALL_MS = 100;

    // time between the first time the temperature is in the accepted temperature band
    // until the next command will be started.
    // The time is the POLL_INTERVALL multiplied by HEATER_SETTLING_TIME_IN_POLLS.
    private final int HEATER_SETTLING_TIME_IN_POLLS = 10;


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
        letMovementStop();
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
        log.trace("adding the move {}", move);
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

    public boolean letMovementStop()
    {
        if(false == handler.doAction(Action_enum.endOfMove))
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
        // TODO once the end stops are hit move away from end stops and move to them again but this time slower
        boolean isHoming = true;
        do
        {
            try
            {
                Thread.sleep(POLL_INTERVALL_MS);
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
        default:
            lastErrorReason = "Invalid Extruder Number !";
            return false;
        }
    }

    public boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature)
    {
        if(false == letMovementStop())
        {
            return false;
        }
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
        if(false == letMovementStop())
        {
            return false;
        }
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
        double lastTemperature = 0.0;
        double curTemperature = 0.0;
        double targetTemp = 0.0;
        targetTemp = targetTemperatures[heater.ordinal()];

        if(   (targetTemp > 0.0 - ACCEPTED_TEMPERATURE_DEVIATION)
           && (targetTemp < 0.0 + ACCEPTED_TEMPERATURE_DEVIATION))
        {
            // if the heater is not heating
            return true;
        }

        int settleCounter = 0;
        do
        {
            try
            {
                Thread.sleep(POLL_INTERVALL_MS);
            }
            catch(InterruptedException e)
            {
            }
            ActionResponse response = handler.getValue(Action_enum.getTemperature, heater);
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
                if(lastTemperature != curTemperature)
                {
                    log.debug("Temperature at {} is {} !", heater, curTemperature);
                    lastTemperature = curTemperature;
                }
            }
            if(   (curTemperature < targetTemp - ACCEPTED_TEMPERATURE_DEVIATION) // too cold
               || (curTemperature > targetTemp + ACCEPTED_TEMPERATURE_DEVIATION)) // too hot
            {
                // We leaved the allowed band so start again
                settleCounter = 0;
            }
            else
            {
                settleCounter++;
            }
        } while(settleCounter < HEATER_SETTLING_TIME_IN_POLLS);
        return true;
    }

    private boolean setTemperatureNoWait( final Heater_enum heater, final Double temperature)
    {
        targetTemperatures[heater.ordinal()] = temperature;
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

    public String getCurrentExtruderTemperature()
    {
        double curTemperature = 0.0;
        Heater_enum h;
        switch(currentExtruder)
        {
        case 0: h = Heater_enum.Extruder_0; break;
        case 1: h = Heater_enum.Extruder_1; break;
        case 2: h = Heater_enum.Extruder_2; break;
        default: h = Heater_enum.Extruder_0; break;
        }
        ActionResponse response = handler.getValue(Action_enum.getTemperature, h);
        if(null == response)
        {
            log.error("Did not get a response to get Heater Temperature Action !");
        }
        else
        {
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                log.error(lastErrorReason);
            }
            else
            {
                curTemperature = response.getTemperature();
            }
        }
        return  String.valueOf(curTemperature);
    }

    public String getHeatedBedTemperature()
    {
        double curTemperature = 0.0;
        ActionResponse response = handler.getValue(Action_enum.getTemperature, Heater_enum.Print_Bed);
        if(null == response)
        {
            log.error("Did not get a response to get Heater Temperature Action !");
        }
        else
        {
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                log.error(lastErrorReason);
            }
            else
            {
                curTemperature = response.getTemperature();
            }
        }
        return  String.valueOf(curTemperature);
    }

    public int getStateOfSwitch(Switch_enum theSwitch)
    {
        int curState = SWITCH_STATE_NOT_AVAILABLE;
        ActionResponse response = handler.getValue(Action_enum.getStateOfSwitch, theSwitch);
        if(null == response)
        {
            log.error("Did not get a response to get State of Switch Action !");
        }
        else
        {
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                log.error(lastErrorReason);
            }
            else
            {
                curState = response.getInt();
            }
        }
        return curState;
    }

    public boolean switchExtruderTo(int num)
    {
        /*
         * The sequence followed is:
         * - Set the current extruder to its standby temperature specified by G10,
         * - Set the new extruder to its operating temperature specified by G10
         *   and wait for all temperatures to stabilise,
         * - Apply any X, Y, Z offset for the new extruder specified by G10,
         * - Use the new extruder.
         */
        // TODO parking position
        return false;
    }

    public void waitForClientQueueEmpty()
    {
        letMovementStop();
        int numUsedSlots = getNumberOfUserSlotsInClientQueue();
        if(0 < numUsedSlots)
        {
            do
            {
                try
                {
                    Thread.sleep(POLL_INTERVALL_MS);
                }
                catch(InterruptedException e)
                {
                }
                numUsedSlots = getNumberOfUserSlotsInClientQueue();
                log.trace("used Slots: {}", numUsedSlots);
            }while(0 < numUsedSlots);
        }
        // else Queue already empty
    }

    private int getNumberOfUserSlotsInClientQueue()
    {
        int usedSlots = 0;
        ActionResponse response = handler.getValue(Action_enum.getUsedSlotsClientQueue);
        if(null == response)
        {
            log.error("Did not get a response to get Number of used Slot in Client Queue Action !");
        }
        else
        {
            if(false == response.wasSuccessful())
            {
                lastErrorReason = handler.getLastErrorReason();
                log.error(lastErrorReason);
            }
            else
            {
                usedSlots = response.getInt();
            }
        }
        return usedSlots;
    }

}
