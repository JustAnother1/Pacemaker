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
package de.nomagic.printerController.pacemaker;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.planner.AxisConfiguration;
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Protocol
{
    // Magic Number from Protocol Definition:
// Host
    public final static int START_OF_HOST_FRAME = 0x23;

    public final static byte ORDER_RESET = (byte)0x7f;
    public final static byte ORDER_RESUME = 0;
    public final static byte ORDER_REQ_INFORMATION = 1;

    public final static int INFO_FIRMWARE_NAME_STRING = 0;
    public final static int INFO_SERIAL_NUMBER_STRING = 1;
    public final static int INFO_BOARD_NAME_STRING = 2;
    public final static int INFO_GIVEN_NAME_STRING = 3;
    public final static int INFO_SUPPORTED_PROTOCOL_VERSION_MAJOR = 4;
    public final static int INFO_SUPPORTED_PROTOCOL_VERSION_MINOR = 5;
    public final static int INFO_LIST_OF_SUPPORTED_PROTOCOL_EXTENSIONS = 6;

    public final static int INFO_PROTOCOL_EXTENSION_STEPPER_CONTROL = 0;
    public final static int INFO_PROTOCOL_EXTENSION_QUEUED_COMMAND = 1;
    public final static int INFO_PROTOCOL_EXTENSION_BASIC_MOVE = 2;
    public final static int INFO_PROTOCOL_EXTENSION_EVENT_REPORTING = 3;

    public final static int INFO_FIRMWARE_TYPE = 7;
    public final static int INFO_FIRMWARE_REVISION_MAJOR = 8;
    public final static int INFO_FIRMWARE_REVISION_MINOR = 9;
    public final static int INFO_HARDWARE_TYPE = 10;
    public final static int INFO_HARDWARE_REVISION = 11;
    public final static int INFO_NUMBER_STEPPERS = 12;
    public final static int INFO_NUMBER_HEATERS = 13;
    public final static int INFO_NUMBER_PWM = 14;
    public final static int INFO_NUMBER_TEMP_SENSOR = 15;
    public final static int INFO_NUMBER_INPUT = 16;
    public final static int INFO_NUMBER_OUTPUT = 17;
    public final static int INFO_NUMBER_BUZZER = 18;

    public final static byte ORDER_REQ_DEVICE_NAME = 2;
    public final static byte ORDER_REQ_TEMPERATURE = 3;
    public final static byte ORDER_GET_HEATER_CONFIGURATION = 4;
    public final static byte ORDER_CONFIGURE_HEATER = 5;
    public final static byte ORDER_SET_HEATER_TARGET_TEMPERATURE = 6;
    public final static byte ORDER_REQ_INPUT = 7;
    public final static byte INPUT_HIGH = 1;
    public final static byte INPUT_LOW = 0;
    public final static byte ORDER_SET_OUTPUT = 8;
    public final static byte ORDER_SET_PWM = 9;
    public final static byte ORDER_WRITE_FIRMWARE_CONFIGURATION = 0x0A;
    public final static byte ORDER_READ_FIRMWARE_CONFIGURATION = 0x0B;
    public final static byte ORDER_STOP_PRINT = 0x0C;
    public final static byte ORDERED_STOP = 0;
    public final static byte EMERGENCY_STOP = 1;

    // Extension Stepper Control
    public final static byte ORDER_ACTIVATE_STEPPER_CONTROL = 0x0D;
    public final static byte ORDER_ENABLE_DISABLE_STEPPER_MOTORS = 0x0E;
    public final static byte ORDER_CONFIGURE_END_STOPS = 0x0F;
    public final static byte ORDER_ENABLE_DISABLE_END_STOPS = 0x10;
    public final static byte ORDER_HOME_AXES = 0x11;
    public final static int DIRECTION_INCREASING = 1;
    public final static int DIRECTION_DECREASING = 0;

    // Extension Queue Command
    public final static byte ORDER_QUEUE_COMMAND_BLOCKS = 0x12;

    // Extension: basic move
    public final static byte ORDER_CONFIGURE_AXIS_MOVEMENT_RATES = 0x13;

    // Extension: Event Reporting
    public final static byte ORDER_RETRIEVE_EVENTS = 0x14;
    public final static byte ORDER_GET_NUMBER_EVENT_FORMAT_IDS = 0x15;
    public final static byte ORDER_GET_EVENT_STRING_FORMAT_ID = 0x16;

    public final static byte ORDER_MAX_ORDER = 0x16;

// Client
    public final static int START_OF_CLIENT_FRAME = 0x42;

    public final static byte RESPONSE_FRAME_RECEIPT_ERROR = 0;
    public final static int RESPONSE_BAD_FRAME = 0;
    public final static int RESPONSE_BAD_ERROR_CHECK_CODE = 1;
    public final static int RESPONSE_UNABLE_TO_ACCEPT_FRAME = 2;

    public final static byte RESPONSE_OK = 0x10;
    public final static byte RESPONSE_GENERIC_APPLICATION_ERROR = 0x11;

    public final static int RESPONSE_UNKNOWN_ORDER = 0;
    public final static int RESPONSE_BAD_PARAMETER_FORMAT = 1;
    public final static int RESPONSE_BAD_PARAMETER_VALUE = 2;
    public final static int RESPONSE_INVALID_DEVICE_TYPE = 3;
    public final static int RESPONSE_INVALID_DEVICE_NUMBER = 4;
    public final static int RESPONSE_INCORRECT_MODE = 5;
    public final static int RESPONSE_BUSY = 6;
    public final static int RESPONSE_FAILED = 7;

    public final static byte RESPONSE_STOPPED = 0x12;
    public final static byte RESPONSE_ORDER_SPECIFIC_ERROR = 0x13;

    public final static int RESPONSE_MAX = 0x13;

    public final static byte DEVICE_TYPE_UNUSED = 0;
    public final static byte DEVICE_TYPE_INPUT = 1;
    public final static byte DEVICE_TYPE_OUTPUT = 2;
    public final static byte DEVICE_TYPE_PWM_OUTPUT = 3;
    public final static byte DEVICE_TYPE_STEPPER = 4;
    public final static byte DEVICE_TYPE_HEATER = 5;
    public final static byte DEVICE_TYPE_TEMPERATURE_SENSOR = 6;
    public final static byte DEVICE_TYPE_BUZZER = 7;

    ////////////////////////////////////////////////////////////////////////////
    // end of Magic Number from Protocol Definition
    ////////////////////////////////////////////////////////////////////////////


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    // time between two polls in milliseconds.
    private final int POLLING_TIME_MS = 100;

    // allowed difference to target temperature in 1/10 degree Celsius.
    private final int ACCEPTED_TEMPERATURE_DEVIATION = 10;


    private ClientConnection cc;
    private AxisConfiguration[] axisCfg;
    private int[] temperatureSensors;
    private int[] heaters;

    public Protocol()
    {
    }

    public void ConnectToChannel(final ClientConnection cc)
    {
        this.cc = cc;
    }

    public void setCfg(final Cfg cfg)
    {
        axisCfg = cfg.getAxisMapping();
        temperatureSensors = cfg.getTemperatureSensorMapping();
        heaters = cfg.getHeaterMapping();
    }

    public Reply sendInformationRequest(final int which) throws IOException
    {
        final byte[] request = new byte[1];
        request[0] = (byte)(0xff & which);
        return cc.sendRequest(ORDER_REQ_INFORMATION, request);
    }

    public Reply sendDeviceNameRequest(final byte type, final int index) throws IOException
    {
        final byte[] request = new byte[2];
        request[0] = type;
        request[1] = (byte)(0xff & index);
        return cc.sendRequest(ORDER_REQ_DEVICE_NAME, request);
    }

    public DeviceInformation getDeviceInformation()
    {
        final DeviceInformation paceMaker = new DeviceInformation();
        try
        {
            if(true == paceMaker.readDeviceInformationFrom(this))
            {
                log.info(paceMaker.toString());
                return paceMaker;
            }
            else
            {
                return null;
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean sendOrderExpectOK(final byte order, final byte[] parameter)
    {
        final Reply r = cc.sendRequest(order, parameter);
        if(null == r)
        {
            return false;
        }
        return r.isOKReply();
    }

    public int sendOrderExpectInt(final byte order, final byte[] parameter)
    {
        final Reply r = cc.sendRequest(order, parameter);
        if(null == r)
        {
            return -1;
        }
        if(false  != r.isOKReply())
        {
            return -1;
        }
        final byte[] reply = r.getParameter();
        switch(reply.length)
        {
        case 1: return reply[0]; // 8 bit int
        case 2: return reply[0]* 256 + reply[1]; // 16 bit int
        default: return -1;
        }
    }

    public boolean isEndSwitchTriggered(final int axis, final int direction)
    {
        int SwitchNum = -1;
        boolean inverted = true;

        if(DIRECTION_INCREASING == direction)
        {
            SwitchNum = axisCfg[axis].getMaxSwitch();
            inverted = axisCfg[axis].getMaxSwitchInverted();
        }
        else
        {
            SwitchNum = axisCfg[axis].getMinSwitch();
            inverted = axisCfg[axis].getMinSwitchInverted();
        }
        if(Cfg.INVALID == SwitchNum)
        {
            log.error("Can not read status of an Invalid switch !");
            return false;
        }
        final byte[] param = new byte[2];
        param[0] = DEVICE_TYPE_INPUT;
        param[1] = (byte) SwitchNum;
        final int reply = sendOrderExpectInt(Protocol.ORDER_REQ_INPUT, param);
        if(false == inverted)
        {
            if(INPUT_HIGH == reply)
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
            if(INPUT_LOW == reply)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public void waitForEndSwitchTriggered(final int axis, final int direction)
    {
        log.info("Waiting for homing of axis {} !", axis);
        boolean isTriggered = isEndSwitchTriggered(axis, direction);
        while(false == isTriggered)
        {
            try
            {
                Thread.sleep(POLLING_TIME_MS);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
            isTriggered = isEndSwitchTriggered(axis, direction);
        }
    }

    public boolean setTemperature(final int heaterNum, final Double temperature)
    {
        /* TODO
        temperature = temperature * 10;
        final int tempi = temperature.intValue();
        final byte[] param = new byte[4];
        param[0] = (byte)heaters[heaterNum];
        param[1] = (byte)(0xff & (tempi/256));
        param[2] = (byte)(tempi & 0xff);
        param[3] = (byte)temperatureSensors[heaterNum];
        return sendOrderExpectOK(Protocol.ORDER_SET_HEATER_TARGET_TEMPERATURE, param);
        */
        return false;
    }

    public void waitForHeaterInLimits(final int heaterNumber, final Double temperature)
    {
        /*
        if(Cfg.INVALID == heaterNumber)
        {
            log.warn("Ignoring waiting for reaching the temperature on a missing Heater !");
            return;
        }
        if(0.0 < temperature)
        {
            log.info("waiting for Temperature to reach {} on heater {}", temperature, heaterNumber);
            if(Cfg.INVALID == temperatureSensors[heaterNumber])
            {
                log.error("No Temperature Sensor available for Heater {} !", heaterNumber);
                return;
            }
            int curTemp = readTemperatureFrom(temperatureSensors[heaterNumber]);
            while(   (curTemp < (temperature - ACCEPTED_TEMPERATURE_DEVIATION))
                  || (curTemp > (temperature + ACCEPTED_TEMPERATURE_DEVIATION)) )
            {
                try
                {
                    Thread.sleep(POLLING_TIME_MS);
                }
                catch (final InterruptedException e)
                {
                    e.printStackTrace();
                }
                curTemp = readTemperatureFrom(temperatureSensors[heaterNumber]);
                log.info("cur Temperature is {}", curTemp);
            }
        }
        // else heater is not active -> already in limit
        return;
        */
    }

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 255 = max
     */
    public boolean setFanSpeedfor(final int fan, final int speed)
    {
        /* TODO
        if((-1 < fan) && (fan < printerAbilities.getNumberPwmFan()))
        {
            final byte[] param = new byte[3];
            param[0] = (byte) fan;
            param[1] = (byte) speed;
            param[2] = 0;
            if(false == sendOrderExpectOK(Protocol.ORDER_SET_FAN_PWM, param))
            {
                log.error("Falied to enable the Steppers !");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            log.warn("Client does not have the Fan {} !", fan);
            return true;
        }
        */
        return false;
    }

    public boolean enableAllStepperMotors()
    {
        /* TODO
        final int numSteppers = printerAbilities.getNumberSteppers();
        final byte[] parameter = new byte[numSteppers * 2];
        for(int i = 0; i < numSteppers; i++)
        {
            parameter[2 * i] = (byte)i;
            parameter[(2 * i) + 1] = (byte)Protocol.ENABLED;
        }
        if(false == proto.sendOrderExpectOK(Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS, parameter))
        {
            log.error("Falied to enable the Steppers !");
            return false;
        }
        else
        {
            return true;
        }
        */
        return false;
    }

    public boolean disableAllStepperMotors()
    {
        /* TODO
        if(false == sendOrderExpectOK((byte)Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS))
        {
            log.error("Falied to disable the Steppers !");
            return false;
        }
        else
        {
            return true;
        }
        */
        return false;
    }

    public void startHomeOnAxes(final Vector<Integer> listOfHomeAxes)
    {
        if(null == listOfHomeAxes)
        {
            return;
        }
        final byte[] param = new byte[6 * listOfHomeAxes.size()];
        for(int i = 0; i < listOfHomeAxes.size(); i++)
        {
            final int axis = listOfHomeAxes.get(i);
            if(Cfg.INVALID == axis)
            {
                log.error("Tried to home an invalid axis !");
                return;
            }
            final byte stepNum = (byte)axisCfg[axis].getStepperNumber();
            if(Cfg.INVALID == stepNum)
            {
                log.error("Tried to home an axis with invalid stepper motor! ");
                return;
            }
            param[(i*6) + 0] = stepNum;
            // TODO
        }

        if(false == sendOrderExpectOK(Protocol.ORDER_HOME_AXES, param))
        {
            log.error("Falied to Home the Axis !");
        }
    }

    public boolean doEmergencyStopPrint()
    {
        final byte[] param = new byte[1];
        param[0] = EMERGENCY_STOP;
        return sendOrderExpectOK(Protocol.ORDER_STOP_PRINT, param);
    }

    public boolean addPauseToQueue(final Double seconds)
    {
        /*
        final Double ticks = seconds / DELAY_TICK_LENGTH;
        final int tick = ticks.intValue();
        final byte[] param = new byte[2];
        param[0] = (byte)(0xff & (tick/256));
        param[1] = (byte)(tick & 0xff);
        return sendOrderExpectOK(Protocol.ORDER_ENQUEUE_DELAY, param);
        */
        return false;
    }

}
