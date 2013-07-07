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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Planner
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final double PRECISION = 0.0001;

    private final Protocol proto;
    private DeviceInformation printerAbilities = null;

    private final double[] stepsPerMM = new double[Cfg.NUMBER_OF_AXIS];
    private final boolean[] homingAxis = new boolean[Cfg.NUMBER_OF_AXIS];
    private final boolean[] activeHeaters = new boolean[Cfg.NUMBER_OF_HEATERS];
    private final double[] heatersTargetTemperatures = new double[Cfg.NUMBER_OF_HEATERS];
    private int currentExtruder = 0;

    // allowed difference to target temperature in degree Celsius.
    private final double ACCEPTED_TEMPERATURE_DEVIATION = 0.1;


    public Planner(final Protocol proto)
    {
        if(null == proto)
        {
            throw new NullPointerException();
        }
        this.proto = proto;
        currentExtruder = Cfg.EXTRUDER_1;
        for(int i = 0; i < Cfg.NUMBER_OF_HEATERS; i++)
        {
            activeHeaters[i] = false;
            heatersTargetTemperatures[i] = 0;
        }
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            homingAxis[i] = false;
        }
        readPrinterAbilities();
    }

    private void readPrinterAbilities()
    {
        printerAbilities = proto.getDeviceInformation();
        if(null == printerAbilities)
        {
            log.error("Could not read the Printer abilities !");
            return;
        }
    }

    public DeviceInformation getPrinterAbilities()
    {
        return printerAbilities;
    }

    public boolean doImmediateShutDown()
    {
        for(int i = 0; i < Cfg.NUMBER_OF_HEATERS; i++)
        {
            activeHeaters[i] = false;
            heatersTargetTemperatures[i] = 0;
        }
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            homingAxis[i] = false;
        }
        if(false == proto.doEmergencyStopPrint())
        {
            log.error("Falied to Stop the Print !");
            disableAllStepperMotors();
            return false;
        }
        else
        {
            return disableAllStepperMotors();
        }
    }

// Stepper Control

    public boolean addPauseFor(final Double seconds)
    {
        return proto.addPauseToQueue(seconds);
    }

    public boolean addMoveTo(final double[] move)
    {
        if(true == printerAbilities.hasExtensionBasicMove())
        {
            log.error("Falied to send a linear move (Not implemented)!");
            return false;
        }
        else
        {
            log.error("Skipped Move as printer does not support it !");
            return false;
        }
    }

    private final Vector<Integer> listOfHomeAxes = new Vector<Integer>();

    /** homes the specified axis.
     *
     * @param axis For definition of the numbers see GCodeDecoder.
     */
    public boolean addHomeAxis(final int axis)
    {
        listOfHomeAxes.add(axis);
        homingAxis[axis] = true;
        return true;
    }

    public void startHoming()
    {
        proto.startHomeOnAxes(listOfHomeAxes);
        listOfHomeAxes.clear();
    }

    public void waitForEndOfHoming()
    {
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            if(true == homingAxis[i])
            {
                proto.waitForEndSwitchTriggered(i);
            }
        }
        log.info("Homing completed !");
        return;
    }

    public boolean disableAllStepperMotors()
    {
        return proto.disableAllStepperMotors();
    }

    public boolean enableAllStepperMotors()
    {
        return proto.enableAllStepperMotors();
    }

    public void setStepsPerMilimeter(final int axis, final Double stepsPerMillimeter)
    {
        stepsPerMM[axis] = stepsPerMillimeter;
    }


// FAN

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 255 = max
     */
    public boolean setFanSpeedfor(final int fan, final int speed)
    {
        return proto.setFanSpeedfor(fan, speed);
    }


// Temperature

    /** sets the desired Temperature for the currently active Extruder
     * and does not wait for the Extruder to reach the temperature.
     *
     * @param temperature The Temperature in degree Celsius.
     */
    public boolean setCurrentExtruderTemperatureNoWait(final Double temperature)
    {
        return setTemperatureNoWait(temperature, currentExtruder);
    }

    public boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature)
    {
        if(true == setCurrentExtruderTemperatureNoWait(temperature))
        {
            double targetTemp = heatersTargetTemperatures[currentExtruder];
            proto.waitForHeaterInLimits(currentExtruder,
                    targetTemp - ACCEPTED_TEMPERATURE_DEVIATION,
                    targetTemp + ACCEPTED_TEMPERATURE_DEVIATION);
            return true;
        }
        else
        {
            return false;
        }
    }

    /** waits until all heaters created the required Temperatures. */
    public boolean waitForEverythingInLimits()
    {
        for(int i = 0; i < Cfg.NUMBER_OF_HEATERS; i++)
        {
            if(true == activeHeaters[i])
            {
                double targetTemp = heatersTargetTemperatures[i];
                proto.waitForHeaterInLimits(i,
                        targetTemp - ACCEPTED_TEMPERATURE_DEVIATION,
                        targetTemp + ACCEPTED_TEMPERATURE_DEVIATION);
            }
        }
        return true;
    }

    public boolean setPrintBedTemperatureNoWait(final Double temperature)
    {
        return setTemperatureNoWait(temperature, Cfg.PRINT_BED);
    }

    public boolean setChamberTemperatureNoWait(final Double temperature)
    {
        return setTemperatureNoWait(temperature, Cfg.CHAMBER);
    }

    public boolean setPrintBedTemperatureAndDoWait(final Double temperature)
    {
        if(true == setTemperatureNoWait(temperature, Cfg.PRINT_BED))
        {
            double targetTemp = heatersTargetTemperatures[Cfg.PRINT_BED];
            proto.waitForHeaterInLimits(Cfg.PRINT_BED,
                    targetTemp - ACCEPTED_TEMPERATURE_DEVIATION,
                    targetTemp + ACCEPTED_TEMPERATURE_DEVIATION);
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean setTemperatureNoWait(final Double temperature, final int heaterNum)
    {
        if(false == proto.setTemperature(heaterNum, temperature))
        {
            log.error("Falied to set the Temperature !");
            return false;
        }
        else
        {
            heatersTargetTemperatures[heaterNum] = temperature;
            if(temperature > 0 + PRECISION) // 0 is off
            {
                activeHeaters[heaterNum] = true;
            }
            else
            {
                activeHeaters[heaterNum] = false;
            }
            return true;
        }
    }

}
