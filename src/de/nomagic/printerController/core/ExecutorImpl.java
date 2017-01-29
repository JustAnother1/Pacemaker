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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Fan_enum;
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Output_enum;
import de.nomagic.printerController.Setting;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.core.devices.Fan;
import de.nomagic.printerController.core.devices.Heater;
import de.nomagic.printerController.core.devices.Movement;
import de.nomagic.printerController.core.devices.Printer;
import de.nomagic.printerController.core.devices.Switch;
import de.nomagic.printerController.core.devices.TemperatureSensor;
import de.nomagic.printerController.pacemaker.ClientConnectionFactory;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ExecutorImpl implements Executor
{

    // allowed difference to target temperature in degree Celsius.
    private static final double ACCEPTED_TEMPERATURE_DEVIATION = 0.4;

    // time between to polls to client in miliseconds
    private static final int POLL_INTERVALL_MS = 100;

    // time between the first time the temperature is in the accepted temperature band
    // until the next command will be started.
    // The time is the POLL_INTERVALL multiplied by HEATER_SETTLING_TIME_IN_POLLS.
    private static final int HEATER_SETTLING_TIME_IN_POLLS = 10;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Cfg cfg;

    // Devices:
    private Movement move;
    private HashMap<Integer, Printer> print = new HashMap<Integer, Printer>();
    private HashMap<Integer, Fan> fans = new HashMap<Integer, Fan>();
    private HashMap<Heater_enum, TemperatureSensor> TempSensors = new HashMap<Heater_enum, TemperatureSensor>();
    private HashMap<Switch_enum, Switch> Switches = new HashMap<Switch_enum, Switch>();

    private volatile boolean isOperational = false;
    private volatile String lastErrorReason = null;

    private HashMap<Heater_enum, Heater> heaters = new HashMap<Heater_enum, Heater>();

    private volatile int currentExtruder = 0; // Max 3 Extruders (0..2)
    private double[] targetTemperatures = new double[Heater_enum.size];
    private Vector<TemperatureObserver> observers = new Vector<TemperatureObserver>();
    private final TimeOutTask timeout = new TimeOutTask();


    public ExecutorImpl(Cfg cfg)
    {
        timeout.start();
        this.cfg = cfg;

        move = new Movement(timeout, cfg);

        if(true ==connectToPrinter())
        {
            isOperational = checkIfOperational();
        }
        else
        {
            log.error("Initialization of Client failed !");
            isOperational = false;
        }
    }

    /**
     *
     * @return true if everything is ready to start.
     */
    public boolean isOperational()
    {
        return isOperational;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void close(Reference ref)
    {
        letMovementStop(ref);
        timeout.interrupt();
    }

    public boolean istheHeaterConfigured(Heater_enum func)
    {
        final Heater h = heaters.get(func);
        if(null == h)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        final int numClients = cfg.getNumberOfClients();
        for(int i = 0; i < numClients; i++)
        {
            sb.append("Connection " + i + " : " + print.get(i).toString() + "\n");
        }
        sb.append("Configured Fans:\n");
        for (Fan_enum fe : Fan_enum.values())
        {
            final Fan f = fans.get(fe.getValue());
            if(null != f)
            {
                sb.append(fe.toString() + " : " +  f.toString() + "\n");
            }
        }
        sb.append("Configured Heaters:\n");
        for (Heater_enum he : Heater_enum.values())
        {
            final Heater h = heaters.get(he);
            if(null != h)
            {
                sb.append(he.toString() + " : " +  h.toString() + "\n");
            }
        }
        sb.append("Configured Temperature Sensors:\n");
        for (Heater_enum he : Heater_enum.values())
        {
            final TemperatureSensor h = TempSensors.get(he);
            if(null != h)
            {
                sb.append(he.toString() + " : " +  h.toString() + "\n");
            }
        }

        sb.append("Configured Switches:\n");
        for (Switch_enum swe : Switch_enum.values())
        {
           final Switch sw = Switches.get(swe);
            if(null != sw)
            {
                sb.append(swe.toString() + " : " +  sw.toString() + "\n");
            }
        }

        return "ActionHandler:\n" +
               "operational : " + isOperational + "\n" +
               "Last Error : " + lastErrorReason + " \n" +
               move.toString() + " \n" +
                   sb.toString();
    }

    private boolean checkIfOperational()
    {
        if(null == print)
        {
            log.error("Print is missing !");
            return false;
        }
        return true;
    }

    private boolean connectToPrinter()
    {
        final int numClients = cfg.getNumberOfClients();
        log.info("Connecting to {} Client(s).", numClients);
        for(int i = 0; i < numClients; i++)
        {
            final String clientDefinition = cfg.getConnectionDefinitionOfClient(i);
            if(null == clientDefinition)
            {
                log.error("Client Definition for Client {} is null !", i);
                return false;
            }
            else
            {
                log.info("Client Definition: " + clientDefinition);
            }
            final Protocol pro = new Protocol(ClientConnectionFactory.establishConnectionTo(clientDefinition), timeout);
            if(false == pro.isOperational())
            {
                log.error("Client connection failed ! " + clientDefinition);
                return false;
            }
            log.info("Protocol is operational");
            // First send the configuration.
            // The configuration might have an effect on the other values.
            Reference ref = new Reference("Executor Initialisation");
            if(false == applyConfiguration(pro, i, ref))
            {
                pro.closeConnection();
                return false;
            }
            final DeviceInformation di = pro.getDeviceInformation(ref);
            // get available Devices
            // check for all devices if they are configured
            // if yet then create the instances for them
            if(null == di)
            {
                log.error("Failed to read the Device Information from this client !");
                pro.closeConnection();
                return false;
            }
            log.info("Connected to : " + di);
            print.put(i, new Printer(pro));
            mapFans(di, pro, i);
            mapHeaters(di, pro, i);
            mapTemperatureSensors(di,pro,i);
            mapOutputs(di, pro, i);
            mapSwitches(di, pro, i);
            if(false == move.addConnection(di, cfg, pro, i, Switches))
            {
                log.error("Failed to configure the Steppers !");
                // Clients are allowed to have no steppers! -> this is OK
            }
        }
        return true;
    }

    private boolean applyConfiguration(Protocol pro, int connectionNumber, Reference ref)
    {
        final Vector<Setting> settings = cfg.getAllFirmwareSettingsFor(connectionNumber);
        if(null == settings)
        {
            // nothing to configure for this client -> successful
            return true;
        }
        else
        {
            for(int i = 0; i < settings.size(); i++)
            {
                final Setting curSetting = settings.get(i);
                final String setting = curSetting.getName();
                final String value = curSetting.getValue();
                log.debug("Writing to Client : -{}- = -{}- !", setting, value);
                if(false == pro.writeFirmwareConfigurationValue(setting, value, ref))
                {
                    log.error("Failed to apply Firmware specific configuration!");
                    return false;
                }
            }
            return true;
        }
    }


    private void mapFans(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberPwmSwitchedOutputs(); i++)
        {
            final Fan_enum func = cfg.getFunctionOfFan(connectionNumber, i);
            if(null != func)
            {
                // this Fan is used
                final Fan f = new Fan(pro, i);
                fans.put(func.getValue(), f);
            }
        }
    }

    private void mapSwitches(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberSwitches(); i++)
        {
            final Switch_enum func = cfg.getFunctionOfSwitch(connectionNumber, i);
            if(null != func)
            {
                // this Switch is used
                final Switch sw = new Switch(pro, i);
                Switches.put(func, sw);
            }
        }
    }

    private void mapOutputs(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberOutputSignals(); i++)
        {
            final Output_enum func = cfg.getFunctionOfOutput(connectionNumber, i);
            if(null != func)
            {
                Fan_enum fanFunc = null;
                switch(func)
                {
                case Fan_Hot_End_0:
                    fanFunc = Fan_enum.Extruder_0;
                    break;

                case Fan_Hot_End_1:
                    fanFunc = Fan_enum.Extruder_1;
                    break;

                case Fan_Hot_End_2:
                    fanFunc = Fan_enum.Extruder_2;
                    break;

                default:
                    break;
                }
                if(null != fanFunc)
                {
                    // this Output is a Fan
                    final Fan f = new Fan(pro, i, false);
                    fans.put(fanFunc.getValue(), f);
                }
            }
        }
    }

    private void mapTemperatureSensors(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberTemperatureSensors(); i++)
        {
            final Heater_enum func = cfg.getFunctionOfTemperatureSensor(connectionNumber, i);
            if(null != func)
            {
                // this Temperature Sensor is used
                final TemperatureSensor s = new TemperatureSensor(pro, i);
                TempSensors.put(func, s);

                final Heater h = heaters.get(func);
                if(null == h)
                {
                    // No heater :-(
                }
                else
                {
                    h.setTemperatureSensor(s);
                    heaters.put(func, h);
                }
            }
        }
    }

    private void mapHeaters(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberHeaters(); i++)
        {
            final Heater_enum func = cfg.getFunctionOfHeater(connectionNumber, i);
            if(null != func)
            {
                // this heater is used
                Heater h = heaters.get(func);
                if(null == h)
                {
                    h = new Heater();
                }
                h.setHeaterNumber(i, pro);
                heaters.put(func, h);
            }
        }
    }

    public boolean doShutDown(Reference ref)
    {
        boolean success = true;
        final Set<Integer> ks = print.keySet();
        final Iterator<Integer> it = ks.iterator();
        while(it.hasNext())
        {
            final Printer curP = print.get(it.next());
            if(false == curP.doShutDown(ref))
            {
                success = false;
                lastErrorReason = curP.getLastErrorReason();
            }
        }
        return success;
    }

    public boolean doImmediateShutDown(Reference ref)
    {
        boolean success = true;
        final Set<Integer> ks = print.keySet();
        final Iterator<Integer> it = ks.iterator();
        while(it.hasNext())
        {
            final Printer curP =print.get(it.next());
            if(false == curP.doImmediateShutDown(ref))
            {
                success = false;
                lastErrorReason = curP.getLastErrorReason();
            }
        }
        return success;
    }

// Stepper Control

    public boolean addPauseFor(final Double seconds, Reference ref)
    {
        if(false == move.addPause(seconds))
        {
            lastErrorReason = move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean addMoveTo(final RelativeMove relMove, Reference ref)
    {
        log.trace("adding the move {}", relMove);
        if(false == move.addRelativeMove(relMove, ref))
        {
            lastErrorReason = move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean letMovementStop(Reference ref)
    {
        if(false == move.letMovementStop(ref))
        {
            lastErrorReason = move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean startHoming(Axis_enum[] axis, Reference ref)
    {
        if(false == move.homeAxis(axis, ref))
        {
            log.error("Homing Failed!");
            lastErrorReason = "Homing Failed : " + move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean disableAllStepperMotors(Reference ref)
    {
        if(false == move.disableAllMotors(ref))
        {
            lastErrorReason = move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean enableAllStepperMotors(Reference ref)
    {
        if(false == move.enableAllMotors(ref))
        {
            lastErrorReason = move.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean setStepsPerMilimeter(final Axis_enum axle, final Double stepsPerMillimeter)
    {
        if(false == move.setStepsPerMillimeter(axle, stepsPerMillimeter))
        {
            lastErrorReason = move.getLastErrorReason();
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
    public boolean setFanSpeedfor(final int fan, final int speed, Reference ref)
    {
        final Fan theFan = fans.get(fan);
        if(null == theFan)
        {
            log.warn("Tried to set Fan Speed for invalid({}) Fan!", fan);
            return false; // We do not need to stop the printing - we just ignore that fan.
        }
        else
        {
            if(false == theFan.setSpeed(speed, ref))
            {
                lastErrorReason = theFan.getLastErrorReason();
                return false;
            }
            else
            {
                return true;
            }
        }
    }


// Temperature

    /** sets the desired Temperature for the currently active Extruder
     * and does not wait for the Extruder to reach the temperature.
     *
     * @param temperature The Temperature in degree Celsius.
     */
    public boolean setCurrentExtruderTemperatureNoWait(final Double temperature, Reference ref)
    {
        switch(currentExtruder)
        {
        case 0: return setTemperatureNoWait(Heater_enum.Extruder_0, temperature, ref);
        case 1: return setTemperatureNoWait(Heater_enum.Extruder_1, temperature, ref);
        case 2: return setTemperatureNoWait(Heater_enum.Extruder_2, temperature, ref);
        default:
            lastErrorReason = "Invalid Extruder Number !";
            return false;
        }
    }

    public boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature,
                                                          final GCodeResultStream resultStream,
                                                          Reference ref)
    {
        if(false == letMovementStop(ref))
        {
            return false;
        }
        if(true == setCurrentExtruderTemperatureNoWait(temperature, ref))
        {
            return waitForEverythingInLimits(resultStream, ref);
        }
        else
        {
            return false;
        }
    }

    /** waits until all heaters created the required Temperatures. */
    public boolean waitForEverythingInLimits(final GCodeResultStream resultStream, Reference ref)
    {
        for(Heater_enum heater : Heater_enum.values())
        {
            if(false == waitForHeaterInLimits(heater, resultStream, ref))
            {
                return false;
            }
        }
        return true;
    }

    public boolean setPrintBedTemperatureNoWait(final Double temperature, Reference ref)
    {
        return setTemperatureNoWait(Heater_enum.Print_Bed, temperature, ref);
    }

    public boolean setChamberTemperatureNoWait(final Double temperature, Reference ref)
    {
        return setTemperatureNoWait(Heater_enum.Chamber, temperature,ref);
    }

    public boolean setPrintBedTemperatureAndDoWait(final Double temperature, final GCodeResultStream resultStream, Reference ref)
    {
        if(false == letMovementStop(ref))
        {
            return false;
        }
        if(true == setTemperatureNoWait(Heater_enum.Print_Bed, temperature, ref))
        {
            return waitForHeaterInLimits(Heater_enum.Print_Bed, resultStream, ref);
        }
        else
        {
            return false;
        }
    }

    private double handleGetTemperature(final Heater_enum heater, Reference ref)
    {
        final TemperatureSensor sensor = TempSensors.get(heater);
        if(null == sensor)
        {
            log.trace("Tried to get Heater temperature from invalid Temperature Sensor!");
            return 0.0;
        }
        else
        {
            final double curTemp = sensor.getTemperature(ref);
            Fan theFan = null;
            switch(heater)
            {
            case Extruder_0:
                theFan = fans.get(Fan_enum.Extruder_0.getValue());
                break;

            case Extruder_1:
                theFan = fans.get(Fan_enum.Extruder_1.getValue());
                break;

            case Extruder_2:
                theFan = fans.get(Fan_enum.Extruder_2.getValue());
                break;

            default:
                // do nothing;
                break;
            }
            if(null != theFan)
            {
                if(curTemp > HOT_END_FAN_ON_TEMPERATURE)
                {
                    theFan.setSpeed(Fan.MAX_SPEED, ref);
                }
                else
                {
                    theFan.setSpeed(0, ref);
                }
            }
            return curTemp;
        }
    }

    private boolean waitForHeaterInLimits(final Heater_enum heater, final GCodeResultStream resultStream, Reference ref)
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
            curTemperature = handleGetTemperature(heater, ref);
            resultStream.writeLine("T : " + curTemperature + " °C");
            for(int i = 0; i < observers.size(); i++)
            {
                final TemperatureObserver watcher = observers.get(i);
                watcher.update(heater, curTemperature);
            }
            if(lastTemperature != curTemperature)
            {
                log.debug("Temperature at {} is {} !", heater, curTemperature);
                lastTemperature = curTemperature;
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
        log.trace("Heater is in Limits");
        return true;
    }

    private boolean setTemperatureNoWait( final Heater_enum heater, final Double temperature, Reference ref)
    {
        targetTemperatures[heater.ordinal()] = temperature;
        final Heater theHeater = heaters.get(heater);
        if(null == theHeater)
        {
            lastErrorReason = "Tried to set Heater temperature for invalid Heater!";
            return false;
        }
        else
        {
            if(false == theHeater.setTemperature(temperature, ref))
            {
                lastErrorReason = theHeater.getLastErrorReason();
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public double requestTemperatureOfHeater(Heater_enum pos, Reference ref)
    {
        double curTemperature = 0.0;
        curTemperature = handleGetTemperature(pos, ref);
        for(int i = 0; i < observers.size(); i++)
        {
            final TemperatureObserver watcher = observers.get(i);
            watcher.update(pos, curTemperature);
        }
        return curTemperature;
    }

    public String getCurrentExtruderTemperature(Reference ref)
    {
        Heater_enum h;
        switch(currentExtruder)
        {
        case 0: h = Heater_enum.Extruder_0; break;
        case 1: h = Heater_enum.Extruder_1; break;
        case 2: h = Heater_enum.Extruder_2; break;
        default: h = Heater_enum.Extruder_0; break;
        }
        return  String.valueOf(requestTemperatureOfHeater(h, ref));
    }

    public String getHeatedBedTemperature(Reference ref)
    {
        return  String.valueOf(requestTemperatureOfHeater(Heater_enum.Print_Bed, ref));
    }


// Switches

    public int getStateOfSwitch(Switch_enum theSwitch, Reference ref)
    {
        final Switch sw = Switches.get(theSwitch);
        if(null == sw)
        {
            log.trace("Tried to get State from invalid Switch !");
            return SWITCH_STATE_NOT_AVAILABLE;
        }
        else
        {
            return sw.getState(ref);
        }
    }

    public boolean switchExtruderTo(int num, Reference ref)
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

    public Reply sendRawOrderFrame(int ClientNumber, int order, Integer[] parameterBytes, int length)
    {
        final Printer thePrinter = print.get(ClientNumber);
        if(null == thePrinter)
        {
            log.error("Can not send a Frame to a not existing client Number {} !", ClientNumber);
            return null;
        }
        else
        {
            return thePrinter.sendRawOrderFrame(order, parameterBytes, length);
        }
    }

    public void waitForClientQueueEmpty(Reference ref)
    {
        letMovementStop(ref);
        int numUsedSlots = getNumberOfUserSlotsInClientQueue(ref);
        if(0 < numUsedSlots)
        {
            do
            {
                try
                {
                    Thread.sleep(Protocol.QUEUE_TIMEOUT_MS);
                }
                catch(InterruptedException e)
                {
                }
                numUsedSlots = getNumberOfUserSlotsInClientQueue(ref);
                log.debug("used Slots: {}", numUsedSlots);
            }while(0 < numUsedSlots);
        }
        // else Queue already empty
    }

    private int getNumberOfUserSlotsInClientQueue(Reference ref)
    {
        final int res = move.getNumberOfUsedSlotsInClientQueue(ref);
        if(0 > res)
        {
            lastErrorReason = move.getLastErrorReason();
        }
        return res;
    }

    public boolean runPIDautotune(Heater_enum Extruder,
                                  Double Temperature,
                                  int numCycles,
                                  GCodeResultStream resultStream,
                                  Reference ref)
    {
        // TODO Auto-generated method stub
        // configure Heater to Bang Bang
        // switch Heater on 100%
        // measure temperature as fast as possible
        // start of cycle
        // wait for temperature to reach target temperature
        // switch heater completely off == 0%
        // wait for temperature to go below target temperature
        // switch Heater on 100%
        // end of cycle
        // after defined number of cycles calculate PID values
        // from measurements of delta T and time between high and low.
        // switch heater completely off == 0%
        // stop measuring the temperature
        return false;
    }

    public void registerTemperatureObserver(TemperatureObserver observer)
    {
        observers.add(observer);
    }

    public TimeoutHandler getTimeoutHandler()
    {
        return timeout;
    }

}
