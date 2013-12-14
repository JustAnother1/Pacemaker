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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.RelativeMove;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ExecutorMacro extends Macro
{
    public static final int FUNC_DO_SHUT_DOWN = 0;
    public static final int FUNC_DO_IMMEDIATE_SHUT_DOWN = 1;
    public static final int FUNC_ADD_PAUSE_FOR = 2;
    public static final int FUNC_ADD_MOVE_TO = 3;
    public static final int FUNC_LET_MOVEMENT_STOP = 4;
    public static final int FUNC_START_HOMING = 5;
    public static final int FUNC_WAIT_FOR_END_OF_HOMING = 6;
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
    private int[] function;
    private Object[] parameter;
    private Object[] parameter2;

    public ExecutorMacro(int[] function, Object[] parameter, Object[] parameter2)
    {
        this.function = function;
        this.parameter = parameter;
        this.parameter2 = parameter2;
    }

    public ExecutorMacro(int function)
    {
        this.function = new int[1];
        this.function[0] = function;
        this.parameter = new Object[1];
        this.parameter[0] = null;
        this.parameter2 = new Object[1];
        this.parameter2[0] = null;
    }

    public ExecutorMacro(int function, Object parameter)
    {
        this.function = new int[1];
        this.function[0] = function;
        this.parameter = new Object[1];
        this.parameter[0] = parameter;
        this.parameter2 = new Object[1];
        this.parameter2[0] = null;
    }

    public ExecutorMacro(int function, Object parameter, Object parameter2)
    {
        this.function = new int[1];
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
                res = exe.doShutDown();
                break;

            case FUNC_DO_IMMEDIATE_SHUT_DOWN:
                res = exe.doImmediateShutDown();
                break;

            case FUNC_ADD_PAUSE_FOR:
                res = exe.addPauseFor((Double)parameter[i]);
                break;

            case FUNC_ADD_MOVE_TO:
                res = exe.addMoveTo((RelativeMove)parameter[i]);
                break;

            case FUNC_LET_MOVEMENT_STOP:
                res = exe.letMovementStop();
                break;

            case FUNC_START_HOMING:
                res = exe.startHoming((Axis_enum[])parameter[i]);
                break;

            case FUNC_WAIT_FOR_END_OF_HOMING:
                res = exe.waitForEndOfHoming();
                break;

            case FUNC_DISABLE_ALL_STEPPER_MOTORS:
                res = exe.disableAllStepperMotors();
                break;

            case FUNC_ENABLE_ALL_STEPPER_MOTORS:
                res = exe.enableAllStepperMotors();
                break;

            case FUNC_SET_STEPS_PER_MILIMETER:
                res = exe.setStepsPerMilimeter((Axis_enum)parameter[i], (Double)parameter2[i]);
                break;

            case FUNC_SET_FAN_SPEED_FOR:
                res = exe.setFanSpeedfor((Integer)parameter[i], (Integer)parameter2[i]);
                break;

            case FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_NO_WAIT:
                res = exe.setCurrentExtruderTemperatureNoWait((Double)parameter[i]);
                break;

            case FUNC_SET_CURRENT_EXTRUDER_TEMPERATURE_AND_DO_WAIT:
                res = exe.setCurrentExtruderTemperatureAndDoWait((Double)parameter[i]);
                break;

            case FUNC_WAIT_FOR_EVERYTHING_IN_LIMITS:
                res = exe.waitForEverythingInLimits();
                break;

            case FUNC_SET_PRINT_BED_TEMPERATURE_NO_WAIT:
                res = exe.setPrintBedTemperatureNoWait((Double)parameter[i]);
                break;

            case FUNC_SET_CHAMBER_TEMPERATURE_NO_WAIT:
                res = exe.setChamberTemperatureNoWait((Double)parameter[i]);
                break;

            case FUNC_SET_PRINT_BED_TEMPERATURE_AND_DO_WAIT:
                res = exe.setPrintBedTemperatureAndDoWait((Double)parameter[i]);
                break;

            case FUNC_GET_CURRENT_EXTRUDER_TEMPERATURE:
                log.info(exe.getCurrentExtruderTemperature());
                res = true;
                break;

            case FUNC_GET_HEATED_BED_TEMPERATURE:
                log.info(exe.getHeatedBedTemperature());
                res = true;
                break;

            case FUNC_GET_STATE_OF_SWITCH:
                final int state = exe.getStateOfSwitch((Switch_enum)parameter[i]);
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
                res = exe.switchExtruderTo((Integer)parameter[i]);
                break;

            case FUNC_WAIT_FOR_CLIENT_QUEUE_EMPTY:
                exe.waitForEverythingInLimits();
                res = true;
                break;

            default:
                log.error("Invalid Function {} !", function);
            }
            if(false == res)
            {
                log.error("Execution failed !");
                log.error(exe.getLastErrorReason());
            }
        }
    }

}
