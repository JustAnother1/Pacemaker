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
package de.nomagic.printerController.printer;

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

import de.nomagic.printerController.planner.AxisConfiguration;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Cfg
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public final static String COMMENT_START = "#";

    private enum Sect {GENERAL, AXIS, HEATERS, TEMPERATURES, FIRMWARE_CONFIGURATION, INVALID}
    public final static String GENERAL_SECTION = "[general]";
    public final static String SETTING_CLIENT_DEVICE_STRING = "CLientDeviceString";
    public final static String SETTING_USE_STEPPER_CONTROL = "control stepper motors";
    public final static String AXIS_SECTION = "[axis]";
    public final static String SETTING_STEPS_PER_MILLIMETER = "steps per millimeter";
    public final static String SETTING_MIN_SWITCH = "index of minimal switch";
    public final static String SETTING_MAX_SWITCH = "index of maximal switch";
    public final static String SETTING_STEPPER_ONE = "index of stepper motor";
    public final static String SETTING_STEPPER_TWO = "index of second stepper motor";
    public final static String SETTING_HOMING_DIRECTION = "direction of homing is decreasing";
    public final static String HEATERS_SECTION = "[heaters]";
    public final static String SETTING_CHAMBER_HEATER_STRING = "chamber heater";
    public final static String SETTING_PRINT_BED_HEATER_STRING = "print bed heater";
    public final static String SETTING_EXTRUDER_ONE_HEATER_STRING = "extruder one heater";
    public final static String SETTING_EXTRUDER_TWO_HEATER_STRING = "extruder two heater";
    public final static String SETTING_EXTRUDER_THREE_HEATER_STRING = "extruder three heater";
    public final static String TEMPERATURES_SECTION = "[temperatures]";
    public final static String SETTING_CHAMBER_TEMP_SENSOR_STRING = "chamber temperature sensor";
    public final static String SETTING_PRINT_BED_TEMP_SENSOR_STRING = "print bed temperature sensor";
    public final static String SETTING_EXTRUDER_ONE_TEMP_SENSOR_STRING = "extruder one temperature sensor";
    public final static String SETTING_EXTRUDER_TWO_TEMP_SENSOR_STRING = "extruder two temperature sensor";
    public final static String SETTING_EXTRUDER_THREE_TEMP_SENSOR_STRING = "extruder three temperature sensor";
    public final static String FIRMWARE_CONFIGURATION_SECTION = "[firmware]";

    private String ClientDeviceString = "";
    private boolean useSteppers = false;
    private final AxisConfiguration[] axisMapping = new AxisConfiguration[NUMBER_OF_AXIS];
    private final int[] temperatureSensorMapping = new int[NUMBER_OF_HEATERS];
    private final int[] heaterMapping = new int[NUMBER_OF_HEATERS];
    private HashMap<String,String> firmwareCfg = new HashMap<String,String>();

    public final static int POS_X = 0;
    public final static int POS_Y = 1;
    public final static int POS_Z = 2;
    public final static int POS_A = 3;
    public final static int POS_B = 4;
    public final static int POS_C = 5;
    public final static int POS_E = 6;
    public final static int NUMBER_OF_AXIS = 7; // X, Y, Z, A, B, C (from RS274/NGC) and E
    public final static Character[] axisNames = {'X', 'Y', 'Z', 'A', 'B', 'C', 'E'};

    // Heaters
    public final static int CHAMBER    = 0;
    public final static int PRINT_BED  = 1;
    public final static int EXTRUDER_1 = 2;
    public final static int EXTRUDER_2 = 3;
    public final static int EXTRUDER_3 = 4;
    public final static int NUMBER_OF_HEATERS = 5;

    public static final int INVALID = -1;

    public Cfg()
    {
        for(int i = 0; i < NUMBER_OF_AXIS; i++)
        {
            axisMapping[i] = new AxisConfiguration();
        }
        for(int i = 0; i < NUMBER_OF_HEATERS; i++)
        {
            heaterMapping[i] = -1;
            temperatureSensorMapping[i] = -1;
        }
    }

    public boolean saveTo(final OutputStream out)
    {
        final OutputStreamWriter ow = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        try
        {
            ow.write(GENERAL_SECTION + "\n");
            ow.write(SETTING_CLIENT_DEVICE_STRING + " = " + ClientDeviceString + "\n");
            ow.write(SETTING_USE_STEPPER_CONTROL + " = " + useSteppers + "\n");
            ow.write(HEATERS_SECTION + "\n");
            ow.write(SETTING_CHAMBER_HEATER_STRING + " = " + heaterMapping[CHAMBER] + "\n");
            ow.write(SETTING_PRINT_BED_HEATER_STRING + " = " + heaterMapping[PRINT_BED] + "\n");
            ow.write(SETTING_EXTRUDER_ONE_HEATER_STRING + " = " + heaterMapping[EXTRUDER_1] + "\n");
            ow.write(SETTING_EXTRUDER_TWO_HEATER_STRING + " = " + heaterMapping[EXTRUDER_2] + "\n");
            ow.write(SETTING_EXTRUDER_THREE_HEATER_STRING + " = " + heaterMapping[EXTRUDER_3] + "\n");

            ow.write(TEMPERATURES_SECTION + "\n");
            ow.write(SETTING_CHAMBER_TEMP_SENSOR_STRING +  " = " + temperatureSensorMapping[CHAMBER] + "\n");
            ow.write(SETTING_PRINT_BED_TEMP_SENSOR_STRING +  " = " + temperatureSensorMapping[PRINT_BED] + "\n");
            ow.write(SETTING_EXTRUDER_ONE_TEMP_SENSOR_STRING +  " = " + temperatureSensorMapping[EXTRUDER_1] + "\n");
            ow.write(SETTING_EXTRUDER_TWO_TEMP_SENSOR_STRING +  " = " + temperatureSensorMapping[EXTRUDER_2] + "\n");
            ow.write(SETTING_EXTRUDER_THREE_TEMP_SENSOR_STRING +  " = " + temperatureSensorMapping[EXTRUDER_3] + "\n");

            ow.write(AXIS_SECTION + "\n");
            for(int i = 0; i < NUMBER_OF_AXIS; i++)
            {
                ow.write(axisNames[i] + " " + SETTING_STEPS_PER_MILLIMETER + " = " + axisMapping[i].getStepsPerMillimeter() + "\n");
                ow.write(axisNames[i] + " " + SETTING_MIN_SWITCH + " = " + axisMapping[i].getMinSwitch() + "\n");
                ow.write(axisNames[i] + " " + SETTING_MAX_SWITCH + " = " + axisMapping[i].getMaxSwitch() + "\n");
                ow.write(axisNames[i] + " " + SETTING_STEPPER_ONE + " = " + axisMapping[i].getStepperNumber() + "\n");
                ow.write(axisNames[i] + " " + SETTING_STEPPER_TWO + " = " + axisMapping[i].getSecondStepper() + "\n");
                ow.write(axisNames[i] + " " + SETTING_HOMING_DIRECTION + " = " + axisMapping[i].isHomingDecreasing() + "\n");
            }

            if(false == firmwareCfg.isEmpty())
            {
                ow.write(FIRMWARE_CONFIGURATION_SECTION + "\n");
                Set<String> keys = firmwareCfg.keySet();
                Iterator<String> it = keys.iterator();
                while(true == it.hasNext())
                {
                    String name = it.next();
                    String value = firmwareCfg.get(name);
                    ow.write(name + " = " + value + "\n");
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
                        else if(true == AXIS_SECTION.equals(curLine))
                        {
                            curSection = Sect.AXIS;
                        }
                        else if(true == HEATERS_SECTION.equals(curLine))
                        {
                            curSection = Sect.HEATERS;
                        }
                        else if(true == TEMPERATURES_SECTION.equals(curLine))
                        {
                            curSection = Sect.TEMPERATURES;
                        }
                        else if(true == FIRMWARE_CONFIGURATION_SECTION.equals(curLine))
                        {
                            curSection = Sect.FIRMWARE_CONFIGURATION;
                        }
                    }
                    else
                    {
                        switch(curSection)
                        {
                        case GENERAL:
                            if(true == curLine.startsWith(SETTING_CLIENT_DEVICE_STRING))
                            {
                                ClientDeviceString = getValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_USE_STEPPER_CONTROL))
                            {
                                useSteppers = getBooleanValueFrom(curLine);
                            }
                            break;

                        case AXIS:
                            char axis = curLine.charAt(0);
                            axis = Character.toUpperCase(axis);
                            int axisIdx = -1;
                            for(int i = 0; i < NUMBER_OF_AXIS; i++)
                            {
                                if(axis == axisNames[i])
                                {
                                    axisIdx = i;
                                    break;
                                }
                            }
                            if(-1 == axisIdx)
                            {
                                log.warn("Found Configuration for undefined axis {} !", axis);
                            }
                            else
                            {
                                final String axissettingLine = curLine.substring(2);
                                if(true == axissettingLine.startsWith(SETTING_STEPS_PER_MILLIMETER))
                                {
                                    axisMapping[axisIdx].setStepsPerMillimeter(getDoubleValueFrom(axissettingLine));
                                }
                                else if(true == axissettingLine.startsWith(SETTING_MIN_SWITCH))
                                {
                                    axisMapping[axisIdx].setMinSwitch((byte)getIntValueFrom(axissettingLine));
                                }
                                else if(true == axissettingLine.startsWith(SETTING_MAX_SWITCH))
                                {
                                    axisMapping[axisIdx].setMaxSwitch((byte)getIntValueFrom(axissettingLine));
                                }
                                else if(true == axissettingLine.startsWith(SETTING_STEPPER_ONE))
                                {
                                    axisMapping[axisIdx].setStepperNumber((byte)getIntValueFrom(axissettingLine));
                                }
                                else if(true == axissettingLine.startsWith(SETTING_STEPPER_TWO))
                                {
                                    axisMapping[axisIdx].setSecondStepperNumber((byte)getIntValueFrom(axissettingLine));
                                }
                                else if(true == axissettingLine.startsWith(SETTING_HOMING_DIRECTION))
                                {
                                    axisMapping[axisIdx].setHomingIsDecreasing(getBooleanValueFrom(axissettingLine));
                                }

                            }
                            break;

                        case HEATERS:
                            if(true == curLine.startsWith(SETTING_CHAMBER_HEATER_STRING))
                            {
                                heaterMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_PRINT_BED_HEATER_STRING))
                            {
                                heaterMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_ONE_HEATER_STRING))
                            {
                                heaterMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_TWO_HEATER_STRING))
                            {
                                heaterMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_THREE_HEATER_STRING))
                            {
                                heaterMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            break;

                        case TEMPERATURES:
                            if(true == curLine.startsWith(SETTING_CHAMBER_TEMP_SENSOR_STRING))
                            {
                                temperatureSensorMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_PRINT_BED_TEMP_SENSOR_STRING))
                            {
                                temperatureSensorMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_ONE_TEMP_SENSOR_STRING))
                            {
                                temperatureSensorMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_TWO_TEMP_SENSOR_STRING))
                            {
                                temperatureSensorMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            else if(true == curLine.startsWith(SETTING_EXTRUDER_THREE_TEMP_SENSOR_STRING))
                            {
                                temperatureSensorMapping[CHAMBER]  = getIntValueFrom(curLine);
                            }
                            break;

                        case FIRMWARE_CONFIGURATION:
                            if(true == curLine.contains("="))
                            {
                                firmwareCfg.put(getKeyFrom(curLine), getValueFrom(curLine));
                            }
                            break;

                        default:
                            log.error("Found Text in Invalid Section ! ");
                            break;
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
        return (line.substring(0, line.indexOf('='))).trim();
    }

    private String getValueFrom(final String line)
    {
        return (line.substring(line.indexOf('=') + 1)).trim();
    }

    private int getIntValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf('=') + 1);
        hlp = hlp.trim();
        return Integer.parseInt(hlp);
    }

    private double getDoubleValueFrom(final String line)
    {
        String hlp = line.substring(line.indexOf('=') + 1);
        hlp = hlp.trim();
        return Double.parseDouble(hlp);
    }

    private boolean getBooleanValueFrom(String line)
    {
        String hlp = line.substring(line.indexOf('=') + 1);
        hlp = hlp.trim();
        return Boolean.valueOf(hlp);
    }

    public void setClientDeviceString(final String ClientDeviceString)
    {
        this.ClientDeviceString = ClientDeviceString;
    }

    public String getClientDeviceString()
    {
        if(null == ClientDeviceString)
        {
            return "";
        }
        else
        {
            return ClientDeviceString;
        }
    }

    public AxisConfiguration[] getAxisMapping()
    {
        return axisMapping;
    }

    public int[] getTemperatureSensorMapping()
    {
        return temperatureSensorMapping;
    }

    public int[] getHeaterMapping()
    {
        return heaterMapping;
    }

    public String getFirmwareSetting(String Name)
    {
        return firmwareCfg.get(Name);
    }

    public String[] getAllFirmwareKeys()
    {
        Set<String> keys = firmwareCfg.keySet();
        return keys.toArray(new String[0]);
    }

    public boolean shouldUseSteppers()
    {
        return useSteppers;
    }

    public void setUseSteppers(boolean useSteppers)
    {
        this.useSteppers = useSteppers;
    }

}
