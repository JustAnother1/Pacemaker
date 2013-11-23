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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Fan_enum;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Setting;
import de.nomagic.printerController.core.devices.Fan;
import de.nomagic.printerController.core.devices.Heater;
import de.nomagic.printerController.core.devices.Movement;
import de.nomagic.printerController.core.devices.Printer;
import de.nomagic.printerController.core.devices.TemperatureSensor;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ActionHandler extends Thread implements EventSource
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Cfg cfg;
    private boolean isOperational = false;
    private String lastErrorReason = null;
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    private BlockingQueue<ActionResponse> resultQueue = new LinkedBlockingQueue<ActionResponse>();
    // Devices:
    private Movement move = new Movement();
    private HashMap<Integer, Printer> print = new HashMap<Integer, Printer>();
    private HashMap<Integer, Fan> fans = new HashMap<Integer, Fan>();
    private HashMap<Heater_enum, Heater> heaters = new HashMap<Heater_enum, Heater>();
    private HashMap<Heater_enum, TemperatureSensor> TempSensors = new HashMap<Heater_enum, TemperatureSensor>();

    public ActionHandler(Cfg cfg)
    {
        super("ActionHandler");
        this.cfg = cfg;
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
        if(null == move)
        {
            log.error("Move is missing !");
            return false;
        }
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
        StringBuffer sb = new StringBuffer();
        int numClients = cfg.getNumberOfClients();
        for(int i = 0; i < numClients; i++)
        {
            sb.append("Connection " + i + " : " + print.get(i).toString() + "\n");
        }
        sb.append("Configured Fans:\n");
        for (Fan_enum fe : Fan_enum.values())
        {
            Fan f = fans.get(fe.getValue());
            if(null != f)
            {
                sb.append(fe.toString() + " : " +  f.toString() + "\n");
            }
        }
        sb.append("Configured Heaters:\n");
        for (Heater_enum he : Heater_enum.values())
        {
            Heater h = heaters.get(he);
            if(null != h)
            {
                sb.append(he.toString() + " : " +  h.toString() + "\n");
            }
        }
        sb.append("Configured Temperature Sensors:\n");
        for (Heater_enum he : Heater_enum.values())
        {
            TemperatureSensor h = TempSensors.get(he);
            if(null != h)
            {
                sb.append(he.toString() + " : " +  h.toString() + "\n");
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
        int numClients = cfg.getNumberOfClients();
        log.info("Connecting to {} Client(s).", numClients);
        for(int i = 0; i < numClients; i++)
        {
            String clientDefinition = cfg.getConnectionDefinitionOfClient(i);
            if(null == clientDefinition)
            {
                log.error("Client Definition for Client {} is null !", i);
                return false;
            }
            else
            {
                log.info("Client Definition: " + clientDefinition);
            }
            Protocol pro = new Protocol(clientDefinition);
            if(false == pro.isOperational())
            {
                log.error("Client connection failed ! " + clientDefinition);
                return false;
            }
            log.info("Protocol is operational");
            // First send the configuration.
            // The configuration might have an effect on the other values.
            if(false == applyConfiguration(pro, i))
            {
                return false;
            }
            DeviceInformation di = pro.getDeviceInformation();
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
            move.addConnection(di, cfg, pro, i);
            readConfigurationFromClient(pro);
        }
        return true;
    }

    private void readConfigurationFromClient(Protocol pro)
    {
        Vector<String> settings = new Vector<String>();
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
            String allInfo = pro.getCompleteDescriptionForSetting(settings.get(i));
            String Value = pro.readFirmwareConfigurationValue(settings.get(i));
            log.info("" + i + " : " + allInfo + " = " + Value + " !");
        }
        log.info("end of List");
    }

    private boolean applyConfiguration(Protocol pro, int connectionNumber)
    {
        Vector<Setting> settings = cfg.getAllFirmwareSettingsFor(connectionNumber);
        if(null == settings)
        {
            // nothing to configure for this client -> successful
            return true;
        }
        else
        {
            for(int i = 0; i < settings.size(); i++)
            {
                Setting curSetting = settings.get(i);
                String setting = curSetting.getName();
                String value = curSetting.getValue();
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
            Fan_enum func = cfg.getFunctionOfFan(connectionNumber, i);
            if(null != func)
            {
                // this Fan is used
                Fan f = new Fan(pro, i);
                fans.put(func.getValue(), f);
            }
        }
    }

    private void mapTemperatureSensors(DeviceInformation di, Protocol pro, int connectionNumber)
    {
        for(int i = 0; i < di.getNumberTemperatureSensors(); i++)
        {
            Heater_enum func = cfg.getFunctionOfTemperatureSensor(connectionNumber, i);
            if(null != func)
            {
                // this Temperature Sensor is used
                TemperatureSensor s = new TemperatureSensor(pro, i);
                TempSensors.put(func, s);

                Heater h = heaters.get(func);
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
            Heater_enum func = cfg.getFunctionOfHeater(connectionNumber, i);
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

    @Override
    public void run()
    {
        Event e;
        try
        {
            while(false == isInterrupted())
            {
                e = eventQueue.take();
                if(null != e)
                {
                    // handle that
                    switch(e.getType())
                    {
                    // For data Types of parameters see Executor !
                    case doShutDown:
                    {
                        boolean success = true;
                        Set<Integer> ks = print.keySet();
                        Iterator<Integer> it = ks.iterator();
                        while(it.hasNext())
                        {
                            Printer curP =print.get(it.next());
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
                        break;

                    case doImmediateShutDown:
                    {
                        boolean success = true;
                        Set<Integer> ks = print.keySet();
                        Iterator<Integer> it = ks.iterator();
                        while(it.hasNext())
                        {
                            Printer curP =print.get(it.next());
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
                        break;

                    case pauseMovement:
                        if(false == move.addPause((Double)e.getParameter()))
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case relativeMove:
                        if(false == move.addRelativeMove((RelativeMove)e.getParameter()))
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case homeAxis:
                        if(false == move.homeAxis((Axis_enum[])e.getParameter()))
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case getIsHoming:
                        boolean res = move.isHoming();
                        reportBooleanResult(e, res);
                        break;

                    case enableMotor:
                        if(false == move.enableAllMotors())
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case disableMotor:
                        if(false == move.disableAllMotors())
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case setStepsPerMilimeter:
                        if(false == move.setStepsPerMillimeter((Axis_enum)e.getParameter(), (Double)e.getParameter2()))
                        {
                            lastErrorReason = move.getLastErrorReason();
                            reportFailed(e);
                        }
                        else
                        {
                            reportSuccess(e);
                        }
                        break;

                    case setFanSpeed:
                        int FanIdx = (Integer)e.getParameter();
                        Fan theFan = fans.get(FanIdx);
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
                        break;

                    case setHeaterTemperature:
                        {
                            Heater theHeater = heaters.get((Heater_enum)e.getParameter2());
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
                        break;

                    case getTemperature:
                        {
                            TemperatureSensor sensor = TempSensors.get((Heater_enum)e.getParameter());
                            if(null == sensor)
                            {
                                log.trace("Tried to get Heater temperature from invalid Temperature Sensor!");
                                reportDoubleResult(e, 0.0);
                            }
                            else
                            {
                                Double curTemp = sensor.getTemperature();
                                reportDoubleResult(e, curTemp);
                            }
                        }
                        break;

                    default:
                        lastErrorReason = "Invalid Event Type ! " + e.getType();
                        log.error(lastErrorReason);
                        reportFailed(e);
                    }
                }
            }
        }
        catch(InterruptedException e1)
        {
            log.info("Has been Interrupted !");
            // -> end the thread
        }
        // close all connections
        Set<Integer> conSet = print.keySet();
        Iterator<Integer> conIt = conSet.iterator();
        while(conIt.hasNext())
        {
            Integer curConn = conIt.next();
            Printer curPrinter = print.get(curConn);
            curPrinter.closeConnection();
        }
    }

    private void reportDoubleResult(Event e, Double value)
    {
        EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true, value));
        }
        // else nobody cares
    }

    private void reportBooleanResult(Event e, boolean value)
    {
        EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(true, value));
        }
        // else nobody cares
    }

    private void reportFailed(Event e)
    {
        EventSource src = e.getSrc();
        if(null != src)
        {
            src.reportEventStatus(new ActionResponse(false));
        }
        // else nobody cares
    }

    private void reportSuccess(Event e)
    {
        EventSource src = e.getSrc();
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
            e = new Event(Action_enum.getIsHoming, null, this);
            eventQueue.add(e);
            return getResponse();

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
        case getTemperature:
            e = new Event(Action_enum.getTemperature, param, this);
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
        this.interrupt(); // task will shut down after this.
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
        ActionResponse response = getResponse();
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
