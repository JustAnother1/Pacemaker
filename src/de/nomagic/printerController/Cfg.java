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
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.gui.Macro;
import de.nomagic.printerController.gui.MacroFactory;

/** host configuration settings.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Cfg
{

    public static final String COMMENT_START = "#";
    public static final String SEPERATOR = " = ";
    public static final char   SEPERATOR_CHAR = '=';
    public static final String CONNECTION_START = "(";
    public static final String CONNECTION_END = ")";
    public static final String STEPPER_ENALED = "enabled";
    public static final String STEPPER_INVERTED = "inverted";
    public static final String STEPPER_AXIS = "axis";
    public static final String STEPPER_MAXIMUM_ACCELLERATION = "maximum acceleration";
    public static final String STEPPER_MAXIMUM_SPEED = "maximum steps per second";
    public static final String STEPPER_STEPS_PER_MILLIMETER = "steps per millimeter";

    private enum Sect
    {GENERAL, TEMPERATURES, HEATERS, FANS, OUTPUTS, SWITCHES, STEPPER, FIRMWARE_CONFIGURATION, INVALID}
    public static final String GENERAL_SECTION = "[general]";
    public static final String TEMPERATURES_SECTION = "[temperatures]";
    public static final String HEATERS_SECTION = "[heaters]";
    public static final String FANS_SECTION = "[fans]";
    public static final String SWITCHES_SECTION = "[switches]";
    public static final String OUTPUTS_SECTION = "[outputs]";
    public static final String STEPPER_SECTION = "[steppers]";
    public static final String STEPPER_SECTION_OPEN = "[stepper";
    public static final String FIRMWARE_CONFIGURATION_SECTION = "[firmware]";
    public static final String MACRO_PREFIX = "Macro";


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    // General Section
    private HashMap<String,String> GeneralSettings
    = new HashMap<String,String>();
    private HashMap<Integer, Macro> macroMap
    = new HashMap<Integer, Macro>();

    // Settings per Connection:
    private HashMap<Integer,String> ConnectionDefinition
      = new HashMap<Integer,String>();
    private HashMap<Integer,HashMap<Integer,Heater_enum>> TemperatureSensors
      = new HashMap<Integer,HashMap<Integer,Heater_enum>>();
    private HashMap<Integer,HashMap<Integer,Heater_enum>> Heaters
      = new HashMap<Integer,HashMap<Integer,Heater_enum>>();
    private HashMap<Integer,HashMap<Integer,Fan_enum>> Fans
      = new HashMap<Integer,HashMap<Integer,Fan_enum>>();
    private HashMap<Integer,HashMap<Integer,Output_enum>> Outputs
      = new HashMap<Integer,HashMap<Integer,Output_enum>>();
    private HashMap<Integer,HashMap<Integer,Switch_enum>> Switches
      = new HashMap<Integer,HashMap<Integer,Switch_enum>>();
    private HashMap<Integer,Boolean> useSteppers
      = new HashMap<Integer,Boolean>();
    private HashMap<Integer,HashMap<Integer, Boolean>> movementDirectionInverted
      = new HashMap<Integer,HashMap<Integer, Boolean>>();
    private HashMap<Integer,HashMap<Integer,Axis_enum>> Steppers
      = new HashMap<Integer,HashMap<Integer,Axis_enum>>();
    private HashMap<Integer,HashMap<Integer,Double>> StepperMaxAcceleration
      = new HashMap<Integer,HashMap<Integer,Double>>();
    private HashMap<Integer,HashMap<Integer,Integer>> StepperMaxSpeed
    = new HashMap<Integer,HashMap<Integer,Integer>>();
    private HashMap<Integer,HashMap<Integer,Double>> StepperStepsPerMillimeter
      = new HashMap<Integer,HashMap<Integer,Double>>();

    private HashMap<Integer,Vector<Setting>> firmwareCfg = new HashMap<Integer,Vector<Setting>>();

    public Cfg()
    {
    }

    // General

    public void setValueOfSetting(String SettingName, String Value)
    {
        GeneralSettings.put(SettingName, Value);
    }

    public String getGeneralSetting(String Name, String Default)
    {
        final String value = GeneralSettings.get(Name);
        if(null == value)
        {
            GeneralSettings.put(Name, Default);
            return Default;
        }
        else
        {
            return value;
        }
    }

    public boolean getGeneralSetting(String Name, boolean Default)
    {
        final String value = GeneralSettings.get(Name);
        if(null == value)
        {
            GeneralSettings.put(Name, "" + Default);
            return Default;
        }
        else
        {
            final boolean res = Boolean.parseBoolean(value);
            return res;
        }
    }

    public int getGeneralSetting(String Name, int Default)
    {
        final String value = GeneralSettings.get(Name);
        if(null == value)
        {
            GeneralSettings.put(Name, "" + Default);
            return Default;
        }
        else
        {
            final Integer res = Integer.parseInt(value);
            return res;
        }
    }

    public double getGeneralSetting(String Name, double Default)
    {
        final String value = GeneralSettings.get(Name);
        if(null == value)
        {
            GeneralSettings.put(Name, "" + Default);
            return Default;
        }
        else
        {
            final double res = Double.parseDouble(value);
            return res;
        }
    }

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
        final String ClientDeviceString = ConnectionDefinition.get(clientNumber);
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
        final HashMap<Integer,Heater_enum> TempSensors = TemperatureSensors.get(ClientNumber);
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
        final HashMap<Integer,Heater_enum> Heat = Heaters.get(ClientNumber);
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
        final HashMap<Integer,Fan_enum> fan = Fans.get(ClientNumber);
        if(null == fan)
        {
            return null;
        }
        else
        {
            return fan.get(FanNumber);
        }
    }

    // Outputs
    public void addOutput(Integer ClientNumber, Integer OutputNumber, Output_enum Function)
    {
        HashMap<Integer,Output_enum> sw = Outputs.get(ClientNumber);
        if(null == sw)
        {
            sw = new HashMap<Integer,Output_enum>();
        }
        sw.put(OutputNumber, Function);
        Outputs.put(ClientNumber, sw);
    }

    public Output_enum getFunctionOfOutput(Integer ClientNumber, Integer OutputNumber)
    {
        final HashMap<Integer,Output_enum> sw = Outputs.get(ClientNumber);
        if(null == sw)
        {
            return null;
        }
        else
        {
            return sw.get(OutputNumber);
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
        final HashMap<Integer,Switch_enum> sw = Switches.get(ClientNumber);
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
        final Boolean res = useSteppers.get(clientNumber);
        if(null == res)
        {
            // There was no configuration for this client in the config file
            return false;
        }
        else
        {
            return res;
        }
    }

    public Boolean isMovementDirectionInverted(Integer ClientNumber, Integer stepper)
    {
        final HashMap<Integer, Boolean> axInv = movementDirectionInverted.get(ClientNumber);
        if(null == axInv)
        {
            return false;
        }
        else
        {
            return axInv.get(stepper);
        }
    }

    public void setMovementDirectionInverted(Integer ClientNumber, Integer stepper, Boolean inverted)
    {
        HashMap<Integer, Boolean> axInv = movementDirectionInverted.get(ClientNumber);
        if(null == axInv)
        {
            axInv = new HashMap<Integer, Boolean>();
        }
        axInv.put(stepper, inverted);
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
        final HashMap<Integer,Axis_enum> step = Steppers.get(ClientNumber);
        if(null == step)
        {
            return null;
        }
        else
        {
            return step.get(StepperNumber);
        }
    }

    public int getMaxSpeedFor(int clientNumber, int stepperNumber)
    {
        final HashMap<Integer,Integer> speed = StepperMaxSpeed.get(clientNumber);
        if(null == speed)
        {
            return 0;
        }
        else
        {
            return speed.get(stepperNumber);
        }
    }

    public void setMaxSpeedFor(int clientNumber, int stepperNumber, int maxSpeed)
    {
        HashMap<Integer,Integer> speed = StepperMaxSpeed.get(clientNumber);
        if(null == speed)
        {
            speed = new HashMap<Integer,Integer>();
        }
        speed.put(stepperNumber, maxSpeed);
        StepperMaxSpeed.put(clientNumber, speed);
    }

    public void setMaxAccelerationFor(int clientNumber, int stepperNumber, double acceleration)
    {
        HashMap<Integer,Double> accel = StepperMaxAcceleration.get(clientNumber);
        if(null == accel)
        {
            accel = new HashMap<Integer,Double>();
        }
        accel.put(stepperNumber, acceleration);
        StepperMaxAcceleration.put(clientNumber, accel);
    }

    public double getMaxAccelerationFor(int clientNumber, int stepperNumber)
    {
        final HashMap<Integer,Double> accel = StepperMaxAcceleration.get(clientNumber);
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
        final HashMap<Integer,Double> steps = StepperStepsPerMillimeter.get(clientNumber);
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
        Vector<Setting> fwSettings = firmwareCfg.get(ClientNumber);
        if(null == fwSettings)
        {
            fwSettings = new Vector<Setting>();
        }
        final Setting theSetting = new Setting(Setting, Value);
        fwSettings.add(theSetting);
        firmwareCfg.put(ClientNumber, fwSettings);
    }

    public Vector<Setting> getAllFirmwareSettingsFor(Integer ClientNumber)
    {
        return firmwareCfg.get(ClientNumber);
    }

    // Save and Load
    public boolean saveTo(final OutputStream out)
    {
        log.trace("Writing Configuration");
        final OutputStreamWriter ow = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        try
        {
            ow.write(GENERAL_SECTION + "\n");
            final Set<String> names = GeneralSettings.keySet();
            final Iterator<String> it = names.iterator();
            while(it.hasNext())
            {
                final String name = it.next();
                ow.write(name + SEPERATOR + GeneralSettings.get(name) + "\n");
            }

            // Macros
            Macro m = null;
            Integer idx = 0;
            do
            {
                m = macroMap.get(idx);
                if(null != m)
                {
                    ow.write(MACRO_PREFIX + idx + Macro.SEPERATOR + m.getDefinition() + "\n");
                    idx ++;
                }
            }while(null != m);

            final Set<Integer> connectionSet = ConnectionDefinition.keySet();
            final Iterator<Integer> connectionIterator = connectionSet.iterator();
            while(connectionIterator.hasNext())
            {
                final Integer ConnectionNum = connectionIterator.next();
                ow.write(CONNECTION_START + getConnectionDefinitionOfClient(ConnectionNum) + CONNECTION_END + "\n");
// write out the settings for this connection:

// Temperature Sensors:
                ow.write(TEMPERATURES_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Heater_enum heater : Heater_enum.values())
                {
                    ow.write(" " + heater);
                }
                ow.write("\n");
                final HashMap<Integer, Heater_enum> sensors = TemperatureSensors.get(ConnectionNum);
                if(null != sensors)
                {
                    final Set<Integer> usedSensorsSet = sensors.keySet();
                    final Iterator<Integer> sensorsIterator = usedSensorsSet.iterator();
                    while(sensorsIterator.hasNext())
                    {
                        final Integer curSensor = sensorsIterator.next();
                        final Heater_enum function = sensors.get(curSensor);
                        ow.write(curSensor + SEPERATOR +  function + "\n");
                    }
                }

// Heaters :
                ow.write(HEATERS_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Heater_enum heater : Heater_enum.values())
                {
                    ow.write(" " + heater);
                }
                ow.write("\n");
                final HashMap<Integer, Heater_enum> heat = Heaters.get(ConnectionNum);
                if(null != heat)
                {
                    final Set<Integer> usedHeatersSet = heat.keySet();
                    final Iterator<Integer> heatorsIterator = usedHeatersSet.iterator();
                    while(heatorsIterator.hasNext())
                    {
                        final Integer curHeater = heatorsIterator.next();
                        final Heater_enum function = sensors.get(curHeater);
                        ow.write(curHeater + SEPERATOR +  function + "\n");
                    }
                }

// Fans :
                ow.write(FANS_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Fan_enum ele : Fan_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                final HashMap<Integer, Fan_enum> fan = Fans.get(ConnectionNum);
                if(null != fan)
                {
                    final Set<Integer> usedFanSet = fan.keySet();
                    final Iterator<Integer> FansIterator = usedFanSet.iterator();
                    while(FansIterator.hasNext())
                    {
                        final Integer curFan = FansIterator.next();
                        final Fan_enum function = fan.get(curFan);
                        ow.write(curFan + SEPERATOR +  function + "\n");
                    }
                }

// Outputs :
                ow.write(OUTPUTS_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Output_enum ele : Output_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                final HashMap<Integer, Output_enum> op = Outputs.get(ConnectionNum);
                if(null != op)
                {
                    final Set<Integer> usedOutputsSet = op.keySet();
                    final Iterator<Integer> OutputsIterator = usedOutputsSet.iterator();
                    while(OutputsIterator.hasNext())
                    {
                        final Integer curOutput = OutputsIterator.next();
                        final Output_enum function = op.get(curOutput);
                        ow.write(curOutput + SEPERATOR +  function + "\n");
                    }
                }
// Switches :
                ow.write(SWITCHES_SECTION + "\n");
                ow.write(COMMENT_START + " valid functions are :");
                for(Switch_enum ele : Switch_enum.values())
                {
                    ow.write(" " + ele);
                }
                ow.write("\n");
                final HashMap<Integer, Switch_enum> sw = Switches.get(ConnectionNum);
                if(null != sw)
                {
                    final Set<Integer> usedSwitchesSet = sw.keySet();
                    final Iterator<Integer> SwitchesIterator = usedSwitchesSet.iterator();
                    while(SwitchesIterator.hasNext())
                    {
                        final Integer curSwitch = SwitchesIterator.next();
                        final Switch_enum function = sw.get(curSwitch);
                        ow.write(curSwitch + SEPERATOR +  function + "\n");
                    }
                }

// Stepper Motors :
                ow.write(STEPPER_SECTION + "\n");
                ow.write(STEPPER_ENALED + SEPERATOR + useSteppers.get(ConnectionNum));

                final int maxStepperIndex = getMaxStepperIndexfor(ConnectionNum);
                final HashMap<Integer, Boolean> invertedMap = movementDirectionInverted.get(ConnectionNum);
                final HashMap<Integer,Axis_enum> axisMap = Steppers.get(ConnectionNum);
                if(null != axisMap)
                {
                    final HashMap<Integer,Double> maxAccelMap = StepperMaxAcceleration.get(ConnectionNum);
                    final HashMap<Integer,Integer> maxSpeedMap = StepperMaxSpeed.get(ConnectionNum);
                    final HashMap<Integer,Double> StepsMap = StepperStepsPerMillimeter.get(ConnectionNum);
                    for(int i = 0; i <= maxStepperIndex; i++)
                    {
                        final Axis_enum axis = axisMap.get(i);
                        if(null == axis)
                        {
                            // this stepper is not used
                        }
                        else
                        {
                            ow.write(STEPPER_SECTION_OPEN + "." + i  + "]\n");
                            ow.write(STEPPER_INVERTED + SEPERATOR + invertedMap.get(i)  + "\n");
                            ow.write(STEPPER_AXIS + SEPERATOR + axisMap.get(i)  + "\n");
                            ow.write(STEPPER_MAXIMUM_ACCELLERATION + SEPERATOR + maxAccelMap.get(i)  + "\n");
                            ow.write(STEPPER_STEPS_PER_MILLIMETER +SEPERATOR + StepsMap.get(i)  + "\n");
                            ow.write(STEPPER_MAXIMUM_SPEED + SEPERATOR + maxSpeedMap.get(i)  + "\n");
                        }
                    }
                }

// Firmware specific configuration values :
                ow.write(FIRMWARE_CONFIGURATION_SECTION + "\n");
                final Vector<Setting> fwcfg = firmwareCfg.get(ConnectionNum);
                if(null != fwcfg)
                {
                    for(int i = 0; i < fwcfg.size(); i++)
                    {
                        final Setting curSetting = fwcfg.get(i);
                        ow.write(curSetting.getName() + SEPERATOR + curSetting.getValue() + "\n");
                    }
                }
                // else no configuration for this client
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

    private int getMaxStepperIndexfor(Integer connectionNum)
    {
        int max = 0;
        final HashMap<Integer, Axis_enum> allSteppers = Steppers.get(connectionNum);
        if(null != allSteppers)
        {
            final Iterator<Integer> it = allSteppers.keySet().iterator();
            while(it.hasNext())
            {
                final int cur = it.next();
                if(cur > max)
                {
                    max = cur;
                }
            }
        }
        return max;
    }

    public boolean readFrom(final InputStream in)
    {
        log.trace("Reading Configuration");
        final BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
        int curStepper = 0;
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
                            inConnection = false;
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
                        else if(true == OUTPUTS_SECTION.equals(curLine))
                        {
                            curSection = Sect.OUTPUTS;
                        }
                        else if(true == SWITCHES_SECTION.equals(curLine))
                        {
                            curSection = Sect.SWITCHES;
                        }
                        else if(true == STEPPER_SECTION.equals(curLine))
                        {
                            curSection = Sect.STEPPER;
                        }
                        else if(true == curLine.startsWith(STEPPER_SECTION_OPEN))
                        {
                            curSection = Sect.STEPPER;
                            final String hlp = curLine.substring(curLine.indexOf('.') + 1, curLine.indexOf(']'));
                            curStepper = Integer.parseInt(hlp);
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
                        final String cstr = curLine.substring(CONNECTION_START.length(),
                                                              curLine.indexOf(CONNECTION_END));
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
                                if(true == curLine.startsWith(MACRO_PREFIX))
                                {
                                    final String numStr = curLine.substring(MACRO_PREFIX.length(),
                                                                            curLine.indexOf(Macro.SEPERATOR));
                                    final String MacroString = curLine.substring(
                                                               curLine.indexOf(
                                                                       Macro.SEPERATOR) +Macro.SEPERATOR.length());
                                    final Macro m = MacroFactory.getMacroFromLine(MacroString);
                                    macroMap.put(Integer.parseInt(numStr), m);
                                }
                                else
                                {
                                    GeneralSettings.put(getKeyFrom(curLine), getValueFrom(curLine));
                                }
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
                                    log.error("Found invalid temperature sensor function : " + curLine);
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
                                    log.error("Found invalid heater function : " + curLine);
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
                                    log.error("Found invalid fan function : " + curLine);
                                }
                            }
                                break;
                            case OUTPUTS:
                            {
                                HashMap<Integer, Output_enum> curMap = Outputs.get(connectionNumber);
                                if(null == curMap)
                                {
                                    curMap = new HashMap<Integer, Output_enum>();
                                }
                                try
                                {
                                    curMap.put(getIntKeyFrom(curLine), Output_enum.valueOf(getValueFrom(curLine)));
                                    Outputs.put(connectionNumber, curMap);
                                }
                                catch(IllegalArgumentException iae)
                                {
                                    log.error("Found invalid output function : " + curLine);
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
                                    log.error("Found invalid switch function : " + curLine);
                                }
                            }
                                break;
                            case STEPPER:
                            {
                                if(true == curLine.startsWith(STEPPER_ENALED))
                                {
                                    final Boolean activated = getBooleanValueFrom(curLine);
                                    useSteppers.put(connectionNumber, activated);
                                }
                                else if(true == curLine.startsWith(STEPPER_INVERTED))
                                {
                                    setMovementDirectionInverted(connectionNumber,
                                                                 curStepper,
                                                                 getBooleanValueFrom(curLine));
                                }
                                else if(true == curLine.startsWith(STEPPER_AXIS))
                                {
                                    addStepper(connectionNumber,
                                               curStepper,
                                               Axis_enum.valueOf(getValueFrom(curLine)));
                                }
                                else if(true == curLine.startsWith(STEPPER_MAXIMUM_ACCELLERATION))
                                {
                                    setMaxAccelerationFor(connectionNumber,
                                                          curStepper,
                                                          getDoubleValueFrom(curLine));
                                }
                                else if(true == curLine.startsWith(STEPPER_MAXIMUM_SPEED))
                                {
                                    setMaxSpeedFor(connectionNumber,
                                                   curStepper,
                                                   getIntValueFrom(curLine));
                                }
                                else if(true == curLine.startsWith(STEPPER_STEPS_PER_MILLIMETER))
                                {
                                    setSteppsPerMillimeterFor(connectionNumber,
                                                              curStepper,
                                                              getDoubleValueFrom(curLine));
                                }
                                else
                                {
                                    log.error("Invalid stepper Setting {} !", curLine);
                                }
                            }
                                break;
                            case FIRMWARE_CONFIGURATION:
                                addFirmwareConfiguration(connectionNumber, getKeyFrom(curLine), getValueFrom(curLine));
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
            final String res = aLine.substring(0, aLine.indexOf(COMMENT_START));
            return res.trim();
        }
        else
        {
            return aLine.trim();
        }
    }

    private String getKeyFrom(final String line)
    {
        if(-1 == line.indexOf(SEPERATOR_CHAR))
        {
            return line;
        }
        else
        {
            return (line.substring(0, line.indexOf(SEPERATOR_CHAR))).trim();
        }
    }

    private String getValueFrom(final String line)
    {
        if(-1 == line.indexOf(SEPERATOR_CHAR))
        {
            return "";
        }
        else
        {
            return (line.substring(line.indexOf(SEPERATOR_CHAR) + 1)).trim();
        }
    }

    private int getIntKeyFrom(final String line)
    {
        String hlp = line.substring(0, line.indexOf(SEPERATOR_CHAR));
        hlp = hlp.trim();
        try
        {
            return Integer.parseInt(hlp);
        }
        catch(NumberFormatException e)
        {
            log.error("Failed to convert {} to a number !", hlp);
            return 0;
        }
    }

    private int getIntValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR_CHAR) + 1);
        hlp = hlp.trim();
        try
        {
            return Integer.parseInt(hlp);
        }
        catch(NumberFormatException e)
        {
            log.error("Failed to convert {} to a number !", hlp);
            return 0;
        }
    }

    private double getDoubleValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR_CHAR) + 1);
        hlp = hlp.trim();
        try
        {
            return Double.parseDouble(hlp);
        }
        catch(NumberFormatException e)
        {
            log.error("Failed to convert {} to a number !", hlp);
            return 0.0;
        }
    }

    private boolean getBooleanValueFrom(String line)
    {
        String hlp = line.substring(line.indexOf(SEPERATOR_CHAR) + 1);
        hlp = hlp.trim();
        return Boolean.valueOf(hlp);
    }

    public Vector<Macro> getMacros()
    {
        final Vector<Macro> res = new Vector<Macro>();
        Macro m = null;
        int i = 0;
        do
        {
            m = macroMap.get(i);
            if(null != m)
            {
                res.add(m);
                i++;
            }
        }while(null != m);
        return res;
    }

    public void setMacros(Vector<Macro> macros)
    {
        for(int i = 0; i < macros.size(); i++)
        {
            macroMap.put(i, macros.get(i));
        }
    }

}
