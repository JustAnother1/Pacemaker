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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Fan_enum;
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
 *
 */
public class ActionHandler extends Thread implements EventSource, TimeoutHandler
{
    public static final double HOT_END_FAN_ON_TEMPERATURE = 50.0;
    public static final int MAX_TIMEOUT = 10;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Cfg cfg;
    private boolean isOperational = false;
    private String lastErrorReason = null;
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    private BlockingQueue<ActionResponse> resultQueue = new LinkedBlockingQueue<ActionResponse>();
    // Devices:
    private Movement move;
    private HashMap<Integer, Printer> print = new HashMap<Integer, Printer>();
    private HashMap<Integer, Fan> fans = new HashMap<Integer, Fan>();
    private HashMap<Heater_enum, Heater> heaters = new HashMap<Heater_enum, Heater>();
    private HashMap<Heater_enum, TemperatureSensor> TempSensors = new HashMap<Heater_enum, TemperatureSensor>();
    private HashMap<Switch_enum, Switch> Switches = new HashMap<Switch_enum, Switch>();

    private HashMap<Integer, Event> TimeoutEvents = new HashMap<Integer, Event>();
    private HashMap<Integer, Integer> TimeoutTimes = new HashMap<Integer, Integer>();
    private int nextTimeoutID = -1;
    private int[] countdowns = new int[MAX_TIMEOUT];
    private long lastTime = 0;
    private boolean TimeoutsActive = false;
    private volatile boolean isRunning = false;
    // timedExecutor Actions will be logged as [pool-1-thread-1]
    // one task should be enough
    private ScheduledThreadPoolExecutor timedExecutor = new ScheduledThreadPoolExecutor(1);

    public ActionHandler(Cfg cfg)
    {
        super("ActionHandler");
        this.cfg = cfg;
        timedExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        timedExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        timedExecutor.setRemoveOnCancelPolicy(true);

        move = new Movement(this, cfg);
        for(int i = 0; i < MAX_TIMEOUT; i++)
        {
            countdowns[i] = -1;
        }

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

    private boolean checkIfOperational()
    {
        if(null == print)
        {
            log.error("Print is missing !");
            return false;
        }
        return true;
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

    /**
     *
     * @return true if everything is ready to start.
     */
    public boolean isOperational()
    {
        return isOperational;
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
            final Protocol pro = new Protocol(ClientConnectionFactory.establishConnectionTo(clientDefinition));
            if(false == pro.isOperational())
            {
                log.error("Client connection failed ! " + clientDefinition);
                return false;
            }
            log.info("Protocol is operational");
            pro.activateKeepAlive(timedExecutor);
            // First send the configuration.
            // The configuration might have an effect on the other values.
            if(false == applyConfiguration(pro, i))
            {
                return false;
            }
            final DeviceInformation di = pro.getDeviceInformation();
            // get available Devices
            // check for all devices if they are configured
            // if yet then create the instances for them
            if(null == di)
            {
                log.error("Failed to read the Device Information from this client !");
                return false;
            }
            log.info("Connected to : " + di);
            print.put(i, new Printer(pro));
            mapFans(di, pro, i);
            mapHeaters(di, pro, i);
            mapTemperatureSensors(di,pro,i);
            mapOutputs(di, pro, i);
            mapSwitches(di, pro, i);
            move.addConnection(di, cfg, pro, i, Switches);
        }
        return true;
    }

    public void readConfigurationFromClient(Protocol pro)
    {
        final Vector<String> settings = new Vector<String>();
        String curSetting = "";
        // get all the settings
        do
        {
            curSetting =  pro.traverseFirmwareConfiguration(curSetting);
            if(0 < curSetting.length())
            {
                settings.add(curSetting);
            }
        } while(0 < curSetting.length());
        // get parameters for settings
        log.info("Firmware Specific Settings:");
        for(int i = 0; i < settings.size(); i++)
        {
            final String allInfo = pro.getCompleteDescriptionForSetting(settings.get(i));
            final String Value = pro.readFirmwareConfigurationValue(settings.get(i));
            log.info("" + i + " : " + allInfo + " = " + Value + " !");
        }
        log.info("end of List");
    }

    private boolean applyConfiguration(Protocol pro, int connectionNumber)
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
                if(false == pro.writeFirmwareConfigurationValue(setting, value))
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

    private void handleShutDown(Event e)
    {
        boolean success = true;
        final Set<Integer> ks = print.keySet();
        final Iterator<Integer> it = ks.iterator();
        while(it.hasNext())
        {
            final Printer curP =print.get(it.next());
            if(false == curP.doShutDown())
            {
                success = false;
                lastErrorReason = curP.getLastErrorReason();
            }
        }
        if(false == success)
        {
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleImmediateShutDown(Event e)
    {
        boolean success = true;
        final Set<Integer> ks = print.keySet();
        final Iterator<Integer> it = ks.iterator();
        while(it.hasNext())
        {
            final Printer curP =print.get(it.next());
            if(false == curP.doImmediateShutDown())
            {
                success = false;
                lastErrorReason = curP.getLastErrorReason();
            }
        }
        if(false == success)
        {
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleSendRawOrderFrame(Event e)
    {
        final Printer thePrinter = print.get((Integer)e.getParameter());
        if(null == thePrinter)
        {
            reportFailed(e);
        }
        else
        {
            final Reply r = thePrinter.sendRawOrderFrame((Integer)e.getParameter2(),
                                                         (Integer[])e.getParameter3(),
                                                         (Integer)e.getParameter4());
            if(null == r)
            {
                reportFailed(e);
            }
            else
            {
                final EventSource src = e.getSrc();
                if(null != src)
                {
                    src.reportEventStatus(new ActionResponse((Object)r));
                }
                // else nobody cares
            }
        }
    }

    private void handlePauseMovement(Event e)
    {
        if(false == move.addPause((Double)e.getParameter()))
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleRelativeMove(Event e)
    {
        if(false == move.addRelativeMove((RelativeMove)e.getParameter()))
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleEndOfMove(Event e)
    {
        if(false == move.letMovementStop())
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleHomeAxis(Event e)
    {
        if(false == move.homeAxis((Axis_enum[])e.getParameter()))
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleEnableMotors(Event e)
    {
        if(false == move.enableAllMotors())
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleDisableMotors(Event e)
    {
        if(false == move.disableAllMotors())
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleSetSteppsPerMillimeter(Event e)
    {
        if(false == move.setStepsPerMillimeter((Axis_enum)e.getParameter(), (Double)e.getParameter2()))
        {
            lastErrorReason = move.getLastErrorReason();
            reportFailed(e);
        }
        else
        {
            reportSuccess(e);
        }
    }

    private void handleSetFanSpeed(Event e)
    {
        final int FanIdx = (Integer)e.getParameter();
        final Fan theFan = fans.get(FanIdx);
        if(null == theFan)
        {
            log.warn("Tried to set Fan Speed for invalid({}) Fan!", FanIdx);
            reportSuccess(e); // We do not need to stop the printing - we just ignore that fan.
        }
        else
        {
            if(false == theFan.setSpeed((Integer)e.getParameter2()))
            {
                lastErrorReason = theFan.getLastErrorReason();
                reportFailed(e);
            }
            else
            {
                reportSuccess(e);
            }
        }
    }

    private void handleSetHeaterTemperature(Event e)
    {
        final Heater theHeater = heaters.get((Heater_enum)e.getParameter2());
        if(null == theHeater)
        {
            lastErrorReason = "Tried to set Heater temperature for invalid Heater!";
            reportFailed(e);
        }
        else
        {
            if(false == theHeater.setTemperature((Double)e.getParameter()))
            {
                lastErrorReason = theHeater.getLastErrorReason();
                reportFailed(e);
            }
            else
            {
                reportSuccess(e);
            }
        }
    }

    private void handleGetTemperature(Event e)
    {
        final Heater_enum location = (Heater_enum)e.getParameter();
        final TemperatureSensor sensor = TempSensors.get(location);
        if(null == sensor)
        {
            log.trace("Tried to get Heater temperature from invalid Temperature Sensor!");
            reportDoubleResult(e, 0.0);
        }
        else
        {
            final Double curTemp = sensor.getTemperature();
            Fan theFan = null;
            switch(location)
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
                    theFan.setSpeed(Fan.MAX_SPEED);
                }
                else
                {
                    theFan.setSpeed(0);
                }
            }
            reportDoubleResult(e, curTemp);
        }
    }

    private void handleGeStateOfSwitch(Event e)
    {
        final Switch_enum theSwitch = (Switch_enum) e.getParameter();
        final Switch sw = Switches.get(theSwitch);
        if(null == sw)
        {
            log.trace("Tried to get State from invalid Switch !");
            reportIntResult(e, Executor.SWITCH_STATE_NOT_AVAILABLE);
        }
        else
        {
            reportIntResult(e, sw.getState());
        }
    }

    private void handleUsedSlotsClientQueue(Event e)
    {
        final int res = move.getNumberOfUsedSlotsInClientQueue();
        if(0 > res)
        {
            lastErrorReason = move.getLastErrorReason();
        }
        reportIntResult(e, res);
    }

    private void handleEvent(Event e)
    {
        switch(e.getType())
        {
        // For data Types of parameters see Executor !
        case doShutDown:
            handleShutDown(e);
            break;

        case doImmediateShutDown:
            handleImmediateShutDown(e);
            break;

        case pauseMovement:
            handlePauseMovement(e);
            break;

        case relativeMove:
            handleRelativeMove(e);
            break;

        case endOfMove:
            handleEndOfMove(e);
            break;

        case homeAxis:
            handleHomeAxis(e);
            break;

        case getIsHoming:
            final boolean res = move.isHoming();
            reportBooleanResult(e, res);
            break;

        case enableMotor:
            handleEnableMotors(e);
            break;

        case disableMotor:
            handleDisableMotors(e);
            break;

        case setStepsPerMilimeter:
            handleSetSteppsPerMillimeter(e);
            break;

        case setFanSpeed:
            handleSetFanSpeed(e);
            break;

        case setHeaterTemperature:
            handleSetHeaterTemperature(e);
            break;

        case getTemperature:
            handleGetTemperature(e);
            break;

        case getStateOfSwitch:
            handleGeStateOfSwitch(e);
            break;

        case getUsedSlotsClientQueue:
            handleUsedSlotsClientQueue(e);
            break;

        case sendRawOrderFrame:
            handleSendRawOrderFrame(e);
            break;

        case timeOut:
            final EventSource src = e.getSrc();
            if(null != src)
            {
                src.reportEventStatus(new ActionResponse(e.getParameter()));
            }
            break;

        default:
            lastErrorReason = "Invalid Event Type ! " + e.getType();
            log.error(lastErrorReason);
            reportFailed(e);
            break;
        }
    }

    @Override
    public void run()
    {
        Event e;
        boolean shouldClose = false;
        isRunning = true;
        while((false == shouldClose) || (0 < eventQueue.size()))
        {
            try
            {
                if(true == this.isInterrupted())
                {
                    shouldClose = true;
                }
                if(true == shouldClose)
                {
                    final int size = eventQueue.size();
                    log.trace("Shuting down: remeining Evnets: {} !", size);
                    if(0 == size)
                    {
                        break;
                    }
                }
                e = eventQueue.take();
                if(null != e)
                {
                    handleEvent(e);
                }
                checkTimeouts();
            }
            catch(InterruptedException e1)
            {
                log.info("Has been Interrupted !");
                // -> end the thread
                shouldClose = true;
            }
        }
        log.trace("close all connections");
        final Set<Integer> conSet = print.keySet();
        final Iterator<Integer> conIt = conSet.iterator();
        while(conIt.hasNext())
        {
            final Integer curConn = conIt.next();
            final Printer curPrinter = print.get(curConn);
            curPrinter.closeConnection();
        }
        isRunning = false;
    }

    @Override
    public int createTimeout(Event e, int ms)
    {
        nextTimeoutID ++;
        if(MAX_TIMEOUT > nextTimeoutID)
        {
            TimeoutEvents.put(nextTimeoutID, e);
            TimeoutTimes.put(nextTimeoutID, ms);
            return nextTimeoutID;
        }
        else
        {
            return ERROR_FAILED_TO_CREATE_TIMEOUT;
        }
    }

    @Override
    public void startTimeout(int timeoutId)
    {
        countdowns[timeoutId] = TimeoutTimes.get(timeoutId);
        TimeoutsActive = true;
    }

    @Override
    public void stopTimeout(int timeoutId)
    {
        countdowns[timeoutId] = -1;
        for(int i = 0; i < MAX_TIMEOUT; i++)
        {
            if(countdowns[i] > -1)
            {
                return;
            }
        }
        TimeoutsActive = false;
        lastTime = 0;
    }


    private void checkTimeouts()
    {
        if(true == TimeoutsActive)
        {
            if(0 == lastTime)
            {
                // first call after activation
                lastTime = System.currentTimeMillis();
            }
            else
            {
                if(lastTime < System.currentTimeMillis())
                {
                    // at least one ms has passed
                    boolean hasMoreActiveTimeouts = false;
                    for(int i = 0; i < MAX_TIMEOUT; i++)
                    {
                        if(countdowns[i] > -1)
                        {
                            countdowns[i] = countdowns[i] -1;
                            if(0 == countdowns[i])
                            {
                                // timeout !
                                eventQueue.add(TimeoutEvents.get(i));
                                countdowns[i] = -1;
                            }
                            else
                            {
                                // this timeout is still active
                                hasMoreActiveTimeouts = true;
                            }
                        }
                    }
                    if(false == hasMoreActiveTimeouts)
                    {
                        // all timeouts timed out or are inactive -> stop checking
                        TimeoutsActive = false;
                        lastTime = 0;
                    }
                }
            }
        }
        // else nothing to do
    }

    private void reportIntResult(Event e, int value)
    {
        final EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true, value));
        }
        // else nobody cares
    }

    private void reportDoubleResult(Event e, Double value)
    {
        final EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true, value));
        }
        // else nobody cares
    }

    private void reportBooleanResult(Event e, boolean value)
    {
        final EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true, value));
        }
        // else nobody cares
    }

    private void reportFailed(Event e)
    {
        final EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(false));
        }
        // else nobody cares
    }

    private void reportSuccess(Event e)
    {
        final EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true));
        }
        // else nobody cares
    }

    public boolean doAction(Action_enum theAction)
    {
        Event e;
        switch(theAction)
        {
        case doShutDown:
        case endOfMove:
        case doImmediateShutDown:
            e = new Event(theAction, null, this);
            eventQueue.add(e);
            return getResult();

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return false;
        }
    }

    public boolean doAction(Action_enum theAction, final Object param)
    {
        Event e;
        switch(theAction)
        {
        case pauseMovement:
        case relativeMove:
        case homeAxis:
            e = new Event(theAction, param, this);
            eventQueue.add(e);
            return getResult();

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return false;
        }
    }

    public boolean doAction(Action_enum theAction, Object param, Object param2)
    {
        Event e;
        switch(theAction)
        {
        case relativeMove:
        case setFanSpeed:
        case setHeaterTemperature:
            e = new Event(theAction, param, param2, this);
            eventQueue.add(e);
            return getResult();

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return false;
        }
    }

    public ActionResponse getValue(Action_enum theAction)
    {
        Event e;
        switch(theAction)
        {
        case getIsHoming:
            e = new Event(theAction, null, this);
            eventQueue.add(e);
            return getResponse();

        case getUsedSlotsClientQueue:
            e = new Event(theAction, null, this);
            eventQueue.add(e);
            final ActionResponse res = getResponse();
            if( 0 > res.getInt())
            {
                return new ActionResponse(false, res.getInt());
            }
            else
            {
                return res;
            }

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return null;
        }
    }

    public ActionResponse getValue(Action_enum theAction, Object param)
    {
        Event e;
        switch(theAction)
        {
        case getStateOfSwitch:
        case getTemperature:
            e = new Event(theAction, param, this);
            eventQueue.add(e);
            return getResponse();

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return null;
        }
    }

    public ActionResponse getValue(Action_enum theAction,
                                   Object param, Object param2, Object param3, Object param4)
    {
        Event e;
        switch(theAction)
        {
        case sendRawOrderFrame:
            e = new Event(theAction, param, param2, param3, param4, this);
            eventQueue.add(e);
            return getResponse();

        default:
            lastErrorReason = "Action " + theAction + " not implemented !";
            return null;
        }
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void close()
    {
        log.trace("Requesting Action Handler to close,...");
        this.interrupt();
        while(true == isRunning)
        {
            try
            {
                sleep(1);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        timedExecutor.shutdownNow();
        log.trace("Action Handler has closed !");
    }

    private ActionResponse getResponse()
    {
        try
        {
            return resultQueue.take();
        }
        catch(InterruptedException e)
        {
            return null;
        }
    }

    private boolean getResult()
    {
        final ActionResponse response = getResponse();
        if(null != response)
        {
            if(response.wasSuccessful())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void reportEventStatus(ActionResponse response)
    {
        resultQueue.add(response);
    }

}
