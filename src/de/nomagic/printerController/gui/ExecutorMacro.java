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
package de.nomagic.printerController.gui;

import java.util.Scanner;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.core.RelativeMove;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ExecutorMacro extends Macro implements GCodeResultStream
{
    public static final String TYPE_DEFINITION = "Executor";

    public static final String OBJECT_TYPE_DOUBLE = "Double";
    public static final String OBJECT_TYPE_INTEGER = "Integer";
    public static final String OBJECT_TYPE_HEATER_ENUM = "Heater_enum";
    public static final String OBJECT_TYPE_SWITCH_ENUM = "Switch_enum";
    public static final String OBJECT_TYPE_AXIS_ENUM = "Axis_enum";
    public static final String OBJECT_TYPE_RELATIVEMOVE = "RelativeMove";
    public static final String OBJECT_TYPE_INTEGER_ARRAY = "Integer[]";
    public static final String OBJECT_TYPE_AXIS_ENUM_ARRAY = "Axis_enum[]";

    public static final int FUNC_DO_SHUT_DOWN = 0;
    public static final int FUNC_DO_IMMEDIATE_SHUT_DOWN = 1;
    public static final int FUNC_ADD_PAUSE_FOR = 2;
    public static final int FUNC_ADD_MOVE_TO = 3;
    public static final int FUNC_LET_MOVEMENT_STOP = 4;
    public static final int FUNC_START_HOMING = 5;
    public static final int FUNC_DISABLE_ALL_STEPPER_MOTORS = 7;
    public static final int FUNC_ENABLE_ALL_STEPPER_MOTORS = 8;
    public static final int FUNC_SET_STEPS_PER_MILIMETER = 9;
    public static final int FUNC_SET_FAN_SPEED_FOR = 10;
    public static final int FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_NO_WAIT = 11;
    public static final int FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_AND_DO_WAIT = 12;
    public static final int FUNC_WAIT_FOR_EVERYTHING_IN_LIMITS = 13;
    public static final int FUNC_SET_PRINT_BED_TEMPERATURE_NO_WAIT = 14;
    public static final int FUNC_SET_CHAMBER_TEMPERATURE_NO_WAIT = 15;
    public static final int FUNC_SET_PRINT_BED_TEMPERATURE_AND_DO_WAIT = 16;
    public static final int FUNC_GET_CURRENT_EXTRUDER_TEMPERATURE = 17;
    public static final int FUNC_GET_HEATED_BED_TEMPERATURE = 18;
    public static final int FUNC_GET_STATE_OF_SWITCH = 19;
    public static final int FUNC_SWITCH_EXTRUDER_TO = 20;
    public static final int FUNC_WAIT_FOR_CLIENT_QUEUE_EMPTY = 21;

    private static final long serialVersionUID = 1L;
    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private transient Executor exe;
    private Integer[] function;
    private Object[] parameter;
    private Object[] parameter2;
    private Reference ref = new Reference(this.getSource());

    public ExecutorMacro(Integer[] function, Object[] parameter, Object[] parameter2)
    {
        this.function = function;
        this.parameter = parameter;
        this.parameter2 = parameter2;
    }

    public ExecutorMacro(int function)
    {
        this.function = new Integer[1];
        this.function[0] = function;
        this.parameter = new Object[1];
        this.parameter[0] = null;
        this.parameter2 = new Object[1];
        this.parameter2[0] = null;
    }

    public ExecutorMacro(int function, Object parameter)
    {
        this.function = new Integer[1];
        this.function[0] = function;
        this.parameter = new Object[1];
        this.parameter[0] = parameter;
        this.parameter2 = new Object[1];
        this.parameter2[0] = null;
    }

    public ExecutorMacro(int function, Object parameter, Object parameter2)
    {
        this.function = new Integer[1];
        this.function[0] = function;
        this.parameter = new Object[1];
        this.parameter[0] = parameter;
        this.parameter2 = new Object[1];
        this.parameter2[0] = parameter2;
    }

    @Override
    public void updateCore(CoreStateMachine core)
    {
        if(null != core)
        {
            exe = core.getExecutor();
        }
    }

    @Override
    public void execute()
    {
        if(null == exe)
        {
            log.error("Could not execute Macro as Core/Executor is null !");
            return;
        }
        boolean res = false;
        for(int i = 0; i < function.length; i++)
        {
            switch(function[i])
            {
            case FUNC_DO_SHUT_DOWN:
                res = exe.doShutDown(ref);
                break;

            case FUNC_DO_IMMEDIATE_SHUT_DOWN:
                res = exe.doImmediateShutDown(ref);
                break;

            case FUNC_ADD_PAUSE_FOR:
                res = exe.addPauseFor((Double)parameter[i], ref);
                break;

            case FUNC_ADD_MOVE_TO:
                res = exe.addMoveTo((RelativeMove)parameter[i], ref);
                break;

            case FUNC_LET_MOVEMENT_STOP:
                res = exe.letMovementStop(ref);
                break;

            case FUNC_START_HOMING:
                res = exe.startHoming((Axis_enum[])parameter[i], ref);
                break;

            case FUNC_DISABLE_ALL_STEPPER_MOTORS:
                res = exe.disableAllStepperMotors(ref);
                break;

            case FUNC_ENABLE_ALL_STEPPER_MOTORS:
                res = exe.enableAllStepperMotors(ref);
                break;

            case FUNC_SET_STEPS_PER_MILIMETER:
                res = exe.setStepsPerMilimeter((Axis_enum)parameter[i], (Double)parameter2[i]);
                break;

            case FUNC_SET_FAN_SPEED_FOR:
                res = exe.setFanSpeedfor((Integer)parameter[i], (Integer)parameter2[i], ref);
                break;

            case FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_NO_WAIT:
                res = exe.setCurrentExtruderTemperatureNoWait((Double)parameter[i], ref);
                break;

            case FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_AND_DO_WAIT:
                res = exe.setCurrentExtruderTemperatureAndDoWait((Double)parameter[i], this, ref);
                break;

            case FUNC_WAIT_FOR_EVERYTHING_IN_LIMITS:
                res = exe.waitForEverythingInLimits(this, ref);
                break;

            case FUNC_SET_PRINT_BED_TEMPERATURE_NO_WAIT:
                res = exe.setPrintBedTemperatureNoWait((Double)parameter[i], ref);
                break;

            case FUNC_SET_CHAMBER_TEMPERATURE_NO_WAIT:
                res = exe.setChamberTemperatureNoWait((Double)parameter[i], ref);
                break;

            case FUNC_SET_PRINT_BED_TEMPERATURE_AND_DO_WAIT:
                res = exe.setPrintBedTemperatureAndDoWait((Double)parameter[i], this, ref);
                break;

            case FUNC_GET_CURRENT_EXTRUDER_TEMPERATURE:
                log.info(exe.getCurrentExtruderTemperature(ref));
                res = true;
                break;

            case FUNC_GET_HEATED_BED_TEMPERATURE:
                log.info(exe.getHeatedBedTemperature(ref));
                res = true;
                break;

            case FUNC_GET_STATE_OF_SWITCH:
                final int state = exe.getStateOfSwitch((Switch_enum)parameter[i], ref);
                switch(state)
                {
                case Executor.SWITCH_STATE_CLOSED:
                    log.info("Switch is closed");
                    res = true;
                    break;

                case Executor.SWITCH_STATE_OPEN:
                    log.info("Switch is open");
                    res = true;
                    break;

                case Executor.SWITCH_STATE_NOT_AVAILABLE:
                    res = false;
                    break;

                default:
                    res = false;
                    break;
                }
                break;

            case FUNC_SWITCH_EXTRUDER_TO:
                res = exe.switchExtruderTo((Integer)parameter[i], ref);
                break;

            case FUNC_WAIT_FOR_CLIENT_QUEUE_EMPTY:
                exe.waitForEverythingInLimits(this, ref);
                res = true;
                break;

            default:
                log.error("Invalid Function {} !", function[i]);
            }
            if(false == res)
            {
                log.error("Execution failed !");
                log.error(exe.getLastErrorReason());
            }
        }
    }

    private static String objectToString(Object obj)
    {
        if(null == obj)
        {
            return "null";
        }
        if(obj instanceof Double)
        {
            return OBJECT_TYPE_DOUBLE + " " + obj.toString();
        }
        else if(obj instanceof Integer)
        {
            return OBJECT_TYPE_INTEGER + " " + obj.toString();
        }
        else if(obj instanceof Heater_enum)
        {
            return OBJECT_TYPE_HEATER_ENUM + " " + obj.toString();
        }
        else if(obj instanceof Switch_enum)
        {
            return OBJECT_TYPE_SWITCH_ENUM + " " + obj.toString();
        }
        else if(obj instanceof Axis_enum)
        {
            return OBJECT_TYPE_AXIS_ENUM + " " + obj.toString();
        }
        else if(obj instanceof RelativeMove)
        {
            return OBJECT_TYPE_RELATIVEMOVE + " " + obj.toString();
        }
        else if(obj instanceof Integer[])
        {
            final Integer[] arr = (Integer[]) obj;
            final StringBuffer sb = new StringBuffer();
            sb.append(OBJECT_TYPE_INTEGER_ARRAY);
            for(int i = 0; i < arr.length; i++)
            {
                sb.append(" ");
                sb.append(arr[i].toString());
            }
            return sb.toString();
        }
        else if(obj instanceof Axis_enum[])
        {
            final Axis_enum[] arr = (Axis_enum[]) obj;
            final StringBuffer sb = new StringBuffer();
            sb.append(OBJECT_TYPE_AXIS_ENUM_ARRAY);
            for(int i = 0; i < arr.length; i++)
            {
                sb.append(" ");
                sb.append(arr[i].toString());
            }
            return sb.toString();
        }
        else
        {
            return "Invalid Object " + obj.getClass();
        }
    }

    private static Object stringToObject(String str)
    {
        if(null == str)
        {
            return null;
        }
        if(1 > str.length())
        {
            return null;
        }
        if(false == str.contains(" "))
        {
            return null;
        }
        final String value = str.substring(str.indexOf(' ') + 1);
        if(true == str.startsWith(OBJECT_TYPE_DOUBLE))
        {
            return new Double(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_INTEGER))
        {
            return new Double(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_HEATER_ENUM))
        {
            return Heater_enum.valueOf(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_SWITCH_ENUM))
        {
            return Switch_enum.valueOf(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_AXIS_ENUM))
        {
            return Axis_enum.valueOf(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_RELATIVEMOVE))
        {
            return RelativeMove.getFromDefinition(value);
        }
        else if(true == str.startsWith(OBJECT_TYPE_INTEGER_ARRAY))
        {
            final Scanner sc = new Scanner(value);
            final Vector<Integer> vec = new Vector<Integer>();
            while(sc.hasNext())
            {
                final String cur = sc.next();
                vec.add(Integer.parseInt(cur));
            }
            sc.close();
            return vec.toArray(new Integer[0]);
        }
        else if(true == str.startsWith(OBJECT_TYPE_AXIS_ENUM_ARRAY))
        {
            final Scanner sc = new Scanner(value);
            final Vector<Axis_enum> vec = new Vector<Axis_enum>();
            while(sc.hasNext())
            {
                final String cur = sc.next();
                vec.add(Axis_enum.valueOf(cur));
            }
            sc.close();
            return vec.toArray(new Axis_enum[0]);
        }
        // new Data Types go in here
        else
        {
            return null;
        }

    }

    @Override
    public String getDefinition()
    {
        final StringBuffer sb = new StringBuffer();
        for(int i = 0; i < function.length; i++)
        {
            sb.append(function[i]);
            sb.append(SEPERATOR);
            sb.append(objectToString(parameter[i]));
            sb.append(SEPERATOR);
            sb.append(objectToString(parameter2[i]));
            sb.append(SEPERATOR);
        }
        final String help = sb.toString();
        return TYPE_DEFINITION + SEPERATOR + getPrefix() + SEPERATOR + help;
    }


    public static Macro getMacroFromDefinition(String macroString)
    {
        if(null == macroString)
        {
            return null;
        }
        if(1 > macroString.length())
        {
            return null;
        }
        if(false == macroString.startsWith(TYPE_DEFINITION))
        {
            return null;
        }
        String help = macroString.substring(macroString.indexOf(SEPERATOR) + SEPERATOR.length());
        final String prefix = help.substring(0, help.indexOf(SEPERATOR));
        help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        final Vector<String> vec = new  Vector<String>();
        while(true == help.contains(SEPERATOR))
        {
            final String aLine = help.substring(0, help.indexOf(SEPERATOR));
            vec.add(aLine);
            help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        }
        final int numCalls = vec.size() / 3;
        if(0 == numCalls)
        {
            return null;
        }
        final int usedParameters = 3 * numCalls;
        if(usedParameters != vec.size())
        {
            return null;
        }
        final Vector<Integer> func = new Vector<Integer>();
        final Vector<Object> para1 = new Vector<Object>();
        final Vector<Object> para2 = new Vector<Object>();
        for(int i = 0; i < numCalls; i++)
        {
            func.add(Integer.parseInt(vec.get((i*3) +0)));
            para1.add(stringToObject( vec.get((i*3) +1)));
            para2.add(stringToObject( vec.get((i*3) +2)));
        }
        final ExecutorMacro res = new ExecutorMacro(func.toArray(new Integer[0]),
                                                    para1.toArray(new Object[0]),
                                                    para2.toArray(new Object[0]));
        res.setValuesFromPrefix(prefix);
        return res;
    }

    @Override
    public void write(String msg)
    {
        // We can not do a log without an end of line :-(
        log.debug(msg);
    }

    @Override
    public void writeLine(String msg)
    {
        log.debug(msg);
    }

	@Override
	public String getSource() 
	{
		return "Macro";
	}
}
