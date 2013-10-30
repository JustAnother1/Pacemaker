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
package de.nomagic.printerController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Cfg
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public final static String COMMENT_START = "#";
    public final static String SEPERATOR = " = ";
    public final static String CONNECTION_START = "*";
    public final static String CONNECTION_END = "*";
    public final static String STEPPER_ENALED = "enabled";

    private enum Sect {GENERAL, TEMPERATURES, HEATERS, FANS, SWITCHES, STEPPER, FIRMWARE_CONFIGURATION, INVALID}
    public final static String GENERAL_SECTION = "[general]";
    public final static String TEMPERATURES_SECTION = "[temperatures]";
    public final static String HEATERS_SECTION = "[heaters]";
    public final static String FANS_SECTION = "[fans]";
    public final static String SWITCHES_SECTION = "[switches]";
    public final static String STEPPER_SECTION = "[stepper]";
    public final static String FIRMWARE_CONFIGURATION_SECTION = "[firmware]";

    // General Section


    // Settings per Connection:
    private HashMap<Integer,String> ConnectionDefinition = new HashMap<Integer,String>();
    private HashMap<Integer,HashMap<Integer,Heater_enum>> TemperatureSensors = new HashMap<Integer,HashMap<Integer,Heater_enum>>();
    private HashMap<Integer,HashMap<Integer,Heater_enum>> Heaters = new HashMap<Integer,HashMap<Integer,Heater_enum>>();
    private HashMap<Integer,HashMap<Integer,Fan_enum>> Fans = new HashMap<Integer,HashMap<Integer,Fan_enum>>();
    private HashMap<Integer,HashMap<Integer,Switch_enum>> Switches = new HashMap<Integer,HashMap<Integer,Switch_enum>>();
    // TODO inverted Switches
    private HashMap<Integer,Boolean> useSteppers = new HashMap<Integer,Boolean>();
    private HashMap<Integer,HashMap<Axis_enum, Boolean>> movementDirectionInverted = new HashMap<Integer,HashMap<Axis_enum, Boolean>>();
    private HashMap<Integer,HashMap<Integer,Axis_enum>> Steppers = new HashMap<Integer,HashMap<Integer,Axis_enum>>();
    private HashMap<Integer,HashMap<String,String>> firmwareCfg = new HashMap<Integer,HashMap<String,String>>();
    private HashMap<Integer,HashMap<Integer,Integer>> StepperMaxAcceleration = new HashMap<Integer,HashMap<Integer,Integer>>();
    private HashMap<Integer,HashMap<Integer,Double>> StepperStepsPerMillimeter = new HashMap<Integer,HashMap<Integer,Double>>();


    public Cfg()
    {
    }

    // General


    // Connections
    public void setClientDeviceString(final Integer clientNumber, final String ClientDeviceString)
    {
        ConnectionDefinition.put(clientNumber, ClientDeviceString);
    }

    public int getNumberOfClients()
    {
        return ConnectionDefinition.size();
    }

    public String getConnectionDefinitionOfClient(Integer clientNumber)
    {
        String ClientDeviceString = ConnectionDefinition.get(clientNumber);
        if(null == ClientDeviceString)
        {
            return "";
        }
        else
        {
            return ClientDeviceString;
        }
    }

    // Temperature Sensors
    public void addTemperatureSensor(Integer ClientNumber, Integer SensorNumber, Heater_enum Function)
    {
        HashMap<Integer,Heater_enum> TempSensors = TemperatureSensors.get(ClientNumber);
        if(null == TempSensors)
        {
            TempSensors = new HashMap<Integer,Heater_enum>();
        }
        TempSensors.put(SensorNumber, Function);
        TemperatureSensors.put(ClientNumber, TempSensors);
    }

    public Heater_enum getFunctionOfTemperatureSensor(Integer ClientNumber, Integer SensorNumber)
    {
        HashMap<Integer,Heater_enum> TempSensors = TemperatureSensors.get(ClientNumber);
        if(null == TempSensors)
        {
            return null;
        }
        else
        {
            return TempSensors.get(SensorNumber);
        }
    }

    // Heaters
    public void addHeater(Integer ClientNumber, Integer HeaterNumber, Heater_enum Function)
    {
        HashMap<Integer,Heater_enum> heat = Heaters.get(ClientNumber);
        if(null == heat)
        {
            heat = new HashMap<Integer,Heater_enum>();
        }
        heat.put(HeaterNumber, Function);
        Heaters.put(ClientNumber, heat);
    }

    public Heater_enum getFunctionOfHeater(Integer ClientNumber, Integer HeaterNumber)
    {
        HashMap<Integer,Heater_enum> Heat = Heaters.get(ClientNumber);
        if(null == Heat)
        {
            return null;
        }
        else
        {
            return Heat.get(HeaterNumber);
        }
    }

    // Fans
    public void addFan(Integer ClientNumber, Integer FanNumber, Fan_enum Function)
    {
        HashMap<Integer,Fan_enum> fan = Fans.get(ClientNumber);
        if(null == fan)
        {
            fan = new HashMap<Integer,Fan_enum>();
        }
        fan.put(FanNumber, Function);
        Fans.put(ClientNumber, fan);
    }

    public Fan_enum getFunctionOfFan(Integer ClientNumber, Integer FanNumber)
    {
        HashMap<Integer,Fan_enum> fan = Fans.get(ClientNumber);
        if(null == fan)
        {
            return null;
        }
        else
        {
            return fan.get(FanNumber);
        }
    }

    // Switches
    public void addSwitch(Integer ClientNumber, Integer SwitchNumber, Switch_enum Function)
    {
        HashMap<Integer,Switch_enum> sw = Switches.get(ClientNumber);
        if(null == sw)
        {
            sw = new HashMap<Integer,Switch_enum>();
        }
        sw.put(SwitchNumber, Function);
        Switches.put(ClientNumber, sw);
    }

    public Switch_enum getFunctionOfSwitch(Integer ClientNumber, Integer SwitchNumber)
    {
        HashMap<Integer,Switch_enum> sw = Switches.get(ClientNumber);
        if(null == sw)
        {
            return null;
        }
        else
        {
            return sw.get(SwitchNumber);
        }
    }

    // Stepper

    public void setUseSteppers(int clientNumber, boolean shouldUse)
    {
        useSteppers.put(clientNumber, shouldUse);
    }

    public boolean shouldUseSteppers(int clientNumber)
    {
        return useSteppers.get(clientNumber);
    }

    public Boolean isMovementDirectionInverted(Integer ClientNumber, Axis_enum axis)
    {
        HashMap<Axis_enum, Boolean> axInv = movementDirectionInverted.get(ClientNumber);
        if(null == axInv)
        {
            return false;
        }
        else
        {
            return axInv.get(axis);
        }
    }

    public void setMovementDirectionInverted(Integer ClientNumber, Axis_enum axis, Boolean inverted)
    {
        HashMap<Axis_enum, Boolean> axInv = movementDirectionInverted.get(ClientNumber);
        if(null == axInv)
        {
            axInv = new HashMap<Axis_enum, Boolean>();
        }
        axInv.put(axis, inverted);
        movementDirectionInverted.put(ClientNumber, axInv);
    }

    public void addStepper(Integer ClientNumber, Integer StepperNumber, Axis_enum Function)
    {
        HashMap<Integer,Axis_enum> step = Steppers.get(ClientNumber);
        if(null == step)
        {
            step = new HashMap<Integer,Axis_enum>();
        }
        step.put(StepperNumber, Function);
        Steppers.put(ClientNumber, step);
    }

    public Axis_enum getFunctionOfAxis(Integer ClientNumber, Integer StepperNumber)
    {
        HashMap<Integer,Axis_enum> step = Steppers.get(ClientNumber);
        if(null == step)
        {
            return null;
        }
        else
        {
            return step.get(StepperNumber);
        }
    }

    public void setMaxAccelerationFor(int clientNumber, int stepperNumber, int acceleration)
    {
        HashMap<Integer,Integer> accel = StepperMaxAcceleration.get(clientNumber);
        if(null == accel)
        {
            accel = new HashMap<Integer,Integer>();
        }
        accel.put(stepperNumber, acceleration);
        StepperMaxAcceleration.put(clientNumber, accel);
    }

    public int getMaxAccelerationFor(int clientNumber, int stepperNumber)
    {
        HashMap<Integer,Integer> accel = StepperMaxAcceleration.get(clientNumber);
        if(null == accel)
        {
            return 0;
        }
        else
        {
            return accel.get(stepperNumber);
        }
    }

    public void setSteppsPerMillimeterFor(int clientNumber, int stepperNumber, double StepsPerMillimeter)
    {
        HashMap<Integer,Double> steps = StepperStepsPerMillimeter.get(clientNumber);
        if(null == steps)
        {
            steps = new HashMap<Integer,Double>();
        }
        steps.put(stepperNumber, StepsPerMillimeter);
        StepperStepsPerMillimeter.put(clientNumber, steps);
    }

    public double getStepsPerMillimeterFor(int clientNumber, int stepperNumber)
    {
        HashMap<Integer,Double> steps = StepperStepsPerMillimeter.get(clientNumber);
        if(null == steps)
        {
            return 0.0;
        }
        else
        {
            return steps.get(stepperNumber);
        }
    }

    // Firmware Configuration
    public void addFirmwareConfiguration(Integer ClientNumber, String Setting, String Value)
    {
        HashMap<String,String> fwSettings = firmwareCfg.get(ClientNumber);
        if(null == fwSettings)
        {
            fwSettings = new HashMap<String,String>();
        }
        fwSettings.put(Setting, Value);
        firmwareCfg.put(ClientNumber, fwSettings);
    }

    public HashMap<String,String> getAllFirmwareSettingsFor(Integer ClientNumber)
    {
        return firmwareCfg.get(ClientNumber);
    }


    // Save and Load
    public boolean saveTo(final OutputStream out)
    {
        final OutputStreamWriter ow = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        try
        {
            // ow.write(GENERAL_SECTION + "\n");

            Set<Integer> connectionSet = ConnectionDefinition.keySet();
            Iterator<Integer> connectionIterator = connectionSet.iterator();
            while(connectionIterator.hasNext())
            {
                Integer ConnectionNum = connectionIterator.next();
                ow.write(CONNECTION_START + getConnectionDefinitionOfClient(ConnectionNum) + CONNECTION_END);
                // write out the settings for this connection
                ow.write(TEMPERATURES_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Heater_enum heater : Heater_enum.values())
                {
                    ow.write(" " + heater);
                }
                ow.write("\n");
                HashMap<Integer, Heater_enum> sensors = TemperatureSensors.get(ConnectionNum);
                Set<Integer> usedSensorsSet = sensors.keySet();
                Iterator<Integer> sensorsIterator = usedSensorsSet.iterator();
                while(sensorsIterator.hasNext())
                {
                    Integer curSensor = sensorsIterator.next();
                    Heater_enum function = sensors.get(curSensor);
                    ow.write(curSensor + SEPERATOR +  function + "\n");
                }

                ow.write(HEATERS_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Heater_enum heater : Heater_enum.values())
                {
                    ow.write(" " + heater);
                }
                ow.write("\n");
                HashMap<Integer, Heater_enum> heat = Heaters.get(ConnectionNum);
                Set<Integer> usedHeatersSet = heat.keySet();
                Iterator<Integer> heatorsIterator = usedHeatersSet.iterator();
                while(heatorsIterator.hasNext())
                {
                    Integer curHeater = heatorsIterator.next();
                    Heater_enum function = sensors.get(curHeater);
                    ow.write(curHeater + SEPERATOR +  function + "\n");
                }

                ow.write(FANS_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Fan_enum ele : Fan_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                HashMap<Integer, Fan_enum> fan = Fans.get(ConnectionNum);
                Set<Integer> usedFanSet = fan.keySet();
                Iterator<Integer> FansIterator = usedFanSet.iterator();
                while(FansIterator.hasNext())
                {
                    Integer curFan = FansIterator.next();
                    Fan_enum function = fan.get(curFan);
                    ow.write(curFan + SEPERATOR +  function + "\n");
                }

                ow.write(SWITCHES_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Switch_enum ele : Switch_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                HashMap<Integer, Switch_enum> sw = Switches.get(ConnectionNum);
                Set<Integer> usedSwitchesSet = sw.keySet();
                Iterator<Integer> SwitchesIterator = usedSwitchesSet.iterator();
                while(SwitchesIterator.hasNext())
                {
                    Integer curSwitch = SwitchesIterator.next();
                    Switch_enum function = sw.get(curSwitch);
                    ow.write(curSwitch + SEPERATOR +  function + "\n");
                }

                ow.write(STEPPER_SECTION + "\n");
                ow.write(STEPPER_ENALED + SEPERATOR + useSteppers.get(ConnectionNum));
                ow.write(COMMENT_START + Boolean.TRUE + " means the traveling direction of this Axis is inverted. \n");
                HashMap<Axis_enum, Boolean> mdi =  movementDirectionInverted.get(ConnectionNum);
                Set<Axis_enum> axels = mdi.keySet();
                Iterator<Axis_enum> it = axels.iterator();
                while(it.hasNext())
                {
                    Axis_enum curAxel = it.next();
                    Boolean isInverted = mdi.get(curAxel);
                    ow.write(curAxel + SEPERATOR +  isInverted + "\n");
                }

                ow.write(COMMENT_START + " valid functions are :");
                for(Axis_enum ele : Axis_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                HashMap<Integer, Axis_enum> steps = Steppers.get(ConnectionNum);
                Set<Integer> usedSteppersSet = steps.keySet();
                Iterator<Integer> SteppersIterator = usedSteppersSet.iterator();
                while(SteppersIterator.hasNext())
                {
                    Integer curStepper = SteppersIterator.next();
                    Axis_enum axis = steps.get(curStepper);
                    ow.write(curStepper + SEPERATOR +  axis + "\n");
                }

                ow.write(FIRMWARE_CONFIGURATION_SECTION + "\n");
                HashMap<String, String> fwcfg = firmwareCfg.get(ConnectionNum);
                Set<String> keys = fwcfg.keySet();
                Iterator<String> SettingsIterator = keys.iterator();
                while(true == SettingsIterator.hasNext())
                {
                    String name = SettingsIterator.next();
                    String value = fwcfg.get(name);
                    ow.write(name + SEPERATOR + value + "\n");
                }
            }
            ow.flush();
            ow.close();
            return true;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean readFrom(final InputStream in)
    {
        final BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        try
        {
            String curLine = br.readLine();
            Sect curSection = Sect.INVALID;
            Boolean inConnection = false;
            Integer connectionNumber = -1;
            while(null != curLine)
            {
                curLine = removeCommentsFrom(curLine);
                if(0 < curLine.length())
                {
                    if(true == curLine.startsWith("["))
                    {
                        // new Section
                        if(true == GENERAL_SECTION.equals(curLine))
                        {
                            curSection = Sect.GENERAL;
                        }
                        else if(true == TEMPERATURES_SECTION.equals(curLine))
                        {
                            curSection = Sect.TEMPERATURES;
                        }
                        else if(true == HEATERS_SECTION.equals(curLine))
                        {
                            curSection = Sect.HEATERS;
                        }
                        else if(true == FANS_SECTION.equals(curLine))
                        {
                            curSection = Sect.FANS;
                        }
                        else if(true == SWITCHES_SECTION.equals(curLine))
                        {
                            curSection = Sect.SWITCHES;
                        }
                        else if(true == STEPPER_SECTION.equals(curLine))
                        {
                            curSection = Sect.STEPPER;
                        }
                        else if(true == FIRMWARE_CONFIGURATION_SECTION.equals(curLine))
                        {
                            curSection = Sect.FIRMWARE_CONFIGURATION;
                        }
                        else
                        {
                            log.error("Invalid Section : " + curLine);
                        }
                    }
                    else if(true == curLine.startsWith(CONNECTION_START))
                    {
                        connectionNumber ++;
                        String cstr = curLine.substring(CONNECTION_START.length(), curLine.indexOf(CONNECTION_END));
                        ConnectionDefinition.put(connectionNumber, cstr);
                        inConnection = true;
                    }
                    else
                    {
                        if(false == inConnection)
                        {
                            switch(curSection)
                            {
                            case GENERAL:
                                // currently no settings here
                                break;

                            default:
                                log.error("Found Text in Invalid Section ! ");
                                break;
                            }
                        }
                        else
                        {
                            switch(curSection)
                            {
                            case TEMPERATURES:
                            {
                                HashMap<Integer, Heater_enum> curMap = TemperatureSensors.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<Integer, Heater_enum>();
                                }
                                try
                                {
                                    curMap.put(getIntKeyFrom(curLine), Heater_enum.valueOf(getValueFrom(curLine)));
                                    TemperatureSensors.put(connectionNumber, curMap);
                                }
                                catch(IllegalArgumentException iae)
                                {
                                    log.error("Found invalid function : " + curLine);
                                }
                            }
                                break;
                            case HEATERS:
                            {
                                HashMap<Integer, Heater_enum> curMap = Heaters.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<Integer, Heater_enum>();
                                }
                                try
                                {
                                    curMap.put(getIntKeyFrom(curLine), Heater_enum.valueOf(getValueFrom(curLine)));
                                    Heaters.put(connectionNumber, curMap);
                                }
                                catch(IllegalArgumentException iae)
                                {
                                    log.error("Found invalid function : " + curLine);
                                }
                            }
                                break;
                            case FANS:
                            {
                                HashMap<Integer, Fan_enum> curMap = Fans.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<Integer, Fan_enum>();
                                }
                                try
                                {
                                    curMap.put(getIntKeyFrom(curLine), Fan_enum.valueOf(getValueFrom(curLine)));
                                    Fans.put(connectionNumber, curMap);
                                }
                                catch(IllegalArgumentException iae)
                                {
                                    log.error("Found invalid function : " + curLine);
                                }
                            }
                                break;
                            case SWITCHES:
                            {
                                HashMap<Integer, Switch_enum> curMap = Switches.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<Integer, Switch_enum>();
                                }
                                try
                                {
                                    curMap.put(getIntKeyFrom(curLine), Switch_enum.valueOf(getValueFrom(curLine)));
                                    Switches.put(connectionNumber, curMap);
                                }
                                catch(IllegalArgumentException iae)
                                {
                                    log.error("Found invalid function : " + curLine);
                                }
                            }
                                break;
                            case STEPPER:
                            {
                                if(true == curLine.startsWith(STEPPER_ENALED))
                                {
                                    Boolean activated = getBooleanValueFrom(curLine);
                                    useSteppers.put(connectionNumber, activated);
                                }
                                else
                                {
                                    boolean axisFound = false;
                                    for(Axis_enum ele : Axis_enum.values())
                                    {
                                        if(true == curLine.startsWith("" + ele))
                                        {
                                            HashMap<Axis_enum, Boolean> axSetting = movementDirectionInverted.get(connectionNumber);
                                            axSetting.put(ele, getBooleanValueFrom(curLine));
                                            movementDirectionInverted.put(connectionNumber, axSetting);
                                            axisFound = true;
                                        }
                                    }
                                    if(false == axisFound)
                                    {
                                        HashMap<Integer, Axis_enum> curMap = Steppers.get(connectionNumber);
                                        if(null == curMap)
                                        {
                                            curMap = new HashMap<Integer, Axis_enum>();
                                        }
                                        try
                                        {
                                            curMap.put(getIntKeyFrom(curLine), Axis_enum.valueOf(getValueFrom(curLine)));
                                            Steppers.put(connectionNumber, curMap);
                                        }
                                        catch(IllegalArgumentException iae)
                                        {
                                            log.error("Found invalid function : " + curLine);
                                        }
                                    }
                                }
                            }
                                break;
                            case FIRMWARE_CONFIGURATION:
                            {
                                HashMap<String, String> curMap = firmwareCfg.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<String, String>();
                                }
                                curMap.put(getKeyFrom(curLine), getValueFrom(curLine));
                                firmwareCfg.put(connectionNumber, curMap);
                            }
                                break;

                            default:
                                log.error("Found Text in Invalid Section ! ");
                                break;
                            }
                        }
                    }
                }
                // else -> the line is empty -> nothing to do with it !
                // We are done with this line -> Read the next line.
                curLine = br.readLine();
            }
            br.close();
            return true;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private String removeCommentsFrom(String aLine)
    {
        if(true == aLine.contains(COMMENT_START))
        {
            String res = aLine.substring(0, aLine.indexOf(COMMENT_START));
            return res.trim();
        }
        else
        {
            return aLine.trim();
        }
    }

    private String getKeyFrom(final String line)
    {
        return (line.substring(0, line.indexOf(SEPERATOR))).trim();
    }

    private String getValueFrom(final String line)
    {
        return (line.substring(line.indexOf(SEPERATOR) + 1)).trim();
    }

    private int getIntKeyFrom(final String line)
    {
        String hlp = line.substring(0, line.indexOf(SEPERATOR));
        hlp = hlp.trim();
        return Integer.parseInt(hlp);
    }

    private int getIntValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR) + 1);
        hlp = hlp.trim();
        return Integer.parseInt(hlp);
    }

    private double getDoubleValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR) + 1);
        hlp = hlp.trim();
        return Double.parseDouble(hlp);
    }

    private boolean getBooleanValueFrom(String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR) + 1);
        hlp = hlp.trim();
        return Boolean.valueOf(hlp);
    }

}
