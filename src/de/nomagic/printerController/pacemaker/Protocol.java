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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Tool;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.devices.Stepper;
import de.nomagic.printerController.core.devices.Switch;

/** Pacemaker protocol.
 *  as specified in https://github.com/JustAnother1/Pacemaker/blob/master/doc/Pacemaker_Protocol.asciidoc
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Protocol
{
    public static final int ORDER_POS_OF_SYNC               = 0;
    public static final int ORDER_POS_OF_LENGTH             = 1;
    public static final int ORDER_POS_OF_CONTROL            = 2;
    public static final int ORDER_POS_OF_ORDER_CODE         = 3;
    public static final int ORDER_POS_OF_START_OF_PARAMETER = 4;

    public static final int REPLY_POS_OF_SYNC               = 0;
    public static final int REPLY_POS_OF_LENGTH             = 1;
    public static final int REPLY_POS_OF_CONTROL            = 2;
    public static final int REPLY_POS_OF_REPLY_CODE         = 3;
    public static final int REPLY_POS_OF_START_OF_PARAMETER = 4;

    public static final int MAX_SEQUENCE_NUMBER = 15;
    public static final int SEQUENCE_NUMBER_MASK = 0x0f;
    public static final int RESET_COMMUNICATION_SYNC_MASK = 0x10;

    // Magic Number from Protocol Definition:
// Host
    public static final int START_OF_HOST_FRAME = 0x23;

    public static final byte ORDER_RESUME                                           = 0;
    public static final byte ORDER_REQ_INFORMATION                                  = 1;
    public static final byte ORDER_REQ_DEVICE_NAME                                  = 2;
    public static final byte ORDER_REQ_TEMPERATURE                                  = 3;
    public static final byte ORDER_GET_HEATER_CONFIGURATION                         = 4;
    public static final byte ORDER_CONFIGURE_HEATER                                 = 5;
    public static final byte ORDER_SET_HEATER_TARGET_TEMPERATURE                    = 6;
    public static final byte ORDER_REQ_INPUT                                        = 7;
    public static final byte ORDER_SET_OUTPUT                                       = 8;
    public static final byte ORDER_SET_PWM                                          = 9;
    public static final byte ORDER_WRITE_FIRMWARE_CONFIGURATION                     = 0x0A; // 10
    public static final byte ORDER_READ_FIRMWARE_CONFIGURATION                      = 0x0B; // 11
    public static final byte ORDER_STOP_PRINT                                       = 0x0C; // 12
    public static final byte ORDER_ACTIVATE_STEPPER_CONTROL                         = 0x0D; // 13
    public static final byte ORDER_ENABLE_DISABLE_STEPPER_MOTORS                    = 0x0E; // 14
    public static final byte ORDER_CONFIGURE_END_STOPS                              = 0x0F; // 15
    public static final byte ORDER_ENABLE_DISABLE_END_STOPS                         = 0x10; // 16
    public static final byte ORDER_REQUEST_DEVICE_COUNT                             = 0x11; // 17
    public static final byte ORDER_QUEUE_COMMAND_BLOCKS                             = 0x12; // 18
    public static final byte ORDER_CONFIGURE_AXIS_MOVEMENT_RATES                    = 0x13; // 19
    public static final byte ORDER_RETRIEVE_EVENTS                                  = 0x14; // 20
    public static final byte ORDER_GET_NUMBER_EVENT_FORMAT_IDS                      = 0x15; // 21
    public static final byte ORDER_GET_EVENT_STRING_FORMAT_ID                       = 0x16; // 22
    public static final byte ORDER_CLEAR_COMMAND_BLOCK_QUEUE                        = 0x17; // 23
    public static final byte ORDER_REQUEST_DEVICE_STATUS                            = 0x18; // 24
    public static final byte ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS = 0x19; // 25
    public static final byte ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES      = 0x1a; // 26
    public static final byte ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES           = 0x1b;
    public static final byte ORDER_RESET                                            = (byte)0x7f;

    public static final byte QUERY_STOPPED_STATE = 0;
    public static final byte CLEAR_STOPPED_STATE = 1;
    public static final int INFO_FIRMWARE_NAME_STRING = 0;
    public static final int INFO_SERIAL_NUMBER_STRING = 1;
    public static final int INFO_BOARD_NAME_STRING = 2;
    public static final int INFO_GIVEN_NAME_STRING = 3;
    public static final int INFO_SUPPORTED_PROTOCOL_VERSION_MAJOR = 4;
    public static final int INFO_SUPPORTED_PROTOCOL_VERSION_MINOR = 5;
    public static final int INFO_LIST_OF_SUPPORTED_PROTOCOL_EXTENSIONS = 6;

    public static final int INFO_PROTOCOL_EXTENSION_STEPPER_CONTROL = 0;
    public static final int INFO_PROTOCOL_EXTENSION_QUEUED_COMMAND = 1;
    public static final int INFO_PROTOCOL_EXTENSION_BASIC_MOVE = 2;
    public static final int INFO_PROTOCOL_EXTENSION_EVENT_REPORTING = 3;

    public static final int INFO_FIRMWARE_TYPE = 7;
    public static final int INFO_FIRMWARE_REVISION_MAJOR = 8;
    public static final int INFO_FIRMWARE_REVISION_MINOR = 9;
    public static final int INFO_HARDWARE_TYPE = 10;
    public static final int INFO_HARDWARE_REVISION = 11;
    public static final int INFO_MAX_STEP_RATE= 12;
    public static final int INFO_HOST_TIMEOUT = 13;

    public static final byte INPUT_HIGH = 1;
    public static final byte INPUT_LOW = 0;
    public static final int OUTPUT_STATE_LOW = 0;
    public static final int OUTPUT_STATE_HIGH = 0;
    public static final int OUTPUT_STATE_DISABLED = 0;
    public static final byte ORDERED_STOP = 0;
    public static final byte EMERGENCY_STOP = 1;
    public static final int DIRECTION_INCREASING = 1;
    public static final int DIRECTION_DECREASING = 0;

    public static final byte MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER     = 0x01;
    public static final byte MOVEMENT_BLOCK_TYPE_DELAY               = 0x02;
    public static final byte MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE   = 0x03;
    public static final byte MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD = 0x04;

// Client
    public static final int START_OF_CLIENT_FRAME = 0x42;
    public static final int DEBUG_FLAG = 0x80;

    public static final byte RESPONSE_FRAME_RECEIPT_ERROR = 0;
    public static final int RESPONSE_BAD_FRAME = 0;
    public static final int RESPONSE_BAD_ERROR_CHECK_CODE = 1;
    public static final int RESPONSE_UNABLE_TO_ACCEPT_FRAME = 2;

    public static final byte RESPONSE_OK = 0x10;
    public static final byte RESPONSE_GENERIC_APPLICATION_ERROR = 0x11;

    public static final int RESPONSE_UNKNOWN_ORDER = 1;
    public static final int RESPONSE_BAD_PARAMETER_FORMAT = 2;
    public static final int RESPONSE_BAD_PARAMETER_VALUE = 3;
    public static final int RESPONSE_INVALID_DEVICE_TYPE = 4;
    public static final int RESPONSE_INVALID_DEVICE_NUMBER = 5;
    public static final int RESPONSE_INCORRECT_MODE = 6;
    public static final int RESPONSE_BUSY = 7;
    public static final int RESPONSE_FAILED = 8;
    public static final int RESPONSE_FIRMWARE_ERROR = 9;
    public static final int RESPONSE_CANNOT_ACTIVATE_DEVICE = 10;

    public static final byte RESPONSE_STOPPED = 0x12;
    public static final byte STOPPED_UNACKNOWLEADGED = 0;
    public static final byte STOPPED_ACKNOWLEADGED = 1;
    public static final byte RECOVERY_CLEARED = 1;
    public static final byte RECOVERY_PERSISTS = 2;
    public static final byte RECOVERY_UNRECOVERABLE = 3;
    public static final byte CAUSE_RESET = 0;
    public static final byte CAUSE_END_STOP_HIT = 1;
    public static final byte CAUSE_MOVEMENT_ERROR = 2;
    public static final byte CAUSE_TEMPERATURE_ERROR = 3;
    public static final byte CAUSE_DEVICE_FAULT = 4;
    public static final byte CAUSE_ELECTRICAL_FAULT = 5;
    public static final byte CAUSE_FIRMWARE_FAULT = 6;
    public static final byte CAUSE_OTHER_FAULT = 7;

    public static final byte RESPONSE_ORDER_SPECIFIC_ERROR = 0x13;
    public static final int SENSOR_PROBLEM = 0x7fff;

    public static final byte RESPONSE_DEBUG_FRAME_DEBUG_MESSAGE = 0x50;
    public static final byte RESPONSE_DEBUG_FRAME_NEW_EVENT = 0x51;

    public static final byte DEVICE_TYPE_UNUSED = 0;
    public static final byte DEVICE_TYPE_INPUT = 1;
    public static final byte DEVICE_TYPE_OUTPUT = 2;
    public static final byte DEVICE_TYPE_PWM_OUTPUT = 3;
    public static final byte DEVICE_TYPE_STEPPER = 4;
    public static final byte DEVICE_TYPE_HEATER = 5;
    public static final byte DEVICE_TYPE_TEMPERATURE_SENSOR = 6;
    public static final byte DEVICE_TYPE_BUZZER = 7;

    public static final int FIRMWARE_SETTING_TYPE_VOLATILE_CONFIGURATION = 0;
    public static final int FIRMWARE_SETTING_TYPE_NON_VOLATILE_CONFIGURATION = 1;
    public static final int FIRMWARE_SETTING_TYPE_STATISTIC = 2;
    public static final int FIRMWARE_SETTING_TYPE_SWITCH = 3;
    public static final int FIRMWARE_SETTING_TYPE_DEBUG = 4;

    ////////////////////////////////////////////////////////////////////////////
    // end of Magic Number from Protocol Definition
    ////////////////////////////////////////////////////////////////////////////

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_TRY_AGAIN_LATER = 2;

    public static final int MAX_STEPS_PER_MOVE = 0xffff;

    private static final int QUEUE_SEND_BUFFER_SIZE = 200;


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String lastErrorReason = null;

    // the client needs at lest this(in milliseconds) time to free up one slot in the Queue
    private final long QUEUE_POLL_DELAY = 10;
    // blocking command will try not more than MAX_ENQUEUE_DELAY times to enqueue the block
    private final int MAX_ENQUEUE_DELAY = 100;

    private ClientConnection cc;
    private boolean isOperational = false;
    private DeviceInformation di = null;

    private final Vector<byte[]> sendQueue = new Vector<byte[]>();
    private int ClientQueueFreeSlots = 0;
    private int ClientQueueNumberOfEnqueuedCommands = 0;
    private int ClientExecutedJobs = 0;
    private int CommandsSendToClient = 0;

    public static String parse(byte[] buf)
    {
        if(null == buf)
        {
            return "no data";
        }
        if(1 > buf.length)
        {
            return "no data";
        }
        StringBuffer res = new StringBuffer();
        if(ORDER_POS_OF_SYNC < buf.length)
        {
            if(START_OF_HOST_FRAME == buf[ORDER_POS_OF_SYNC])
            {
                res.append("Order:");
                if(ORDER_POS_OF_ORDER_CODE < buf.length)
                {
                    res.append(orderCodeToString(buf[ORDER_POS_OF_ORDER_CODE]));
                    if(ORDER_POS_OF_START_OF_PARAMETER < buf.length)
                    {
                        int length = (0xff & buf[ORDER_POS_OF_LENGTH]) -2;
                        int offset = ORDER_POS_OF_START_OF_PARAMETER;
                        if(0 < length)
                        {
                            res.append(Tool.fromByteBufferToHexString(buf, length, offset));
                        }
                    }
                }
            }
        }
        if(REPLY_POS_OF_SYNC < buf.length)
        {
            if(START_OF_CLIENT_FRAME == buf[REPLY_POS_OF_SYNC])
            {
                res.append("Response:");
                if(REPLY_POS_OF_REPLY_CODE < buf.length)
                {
                    res.append(replyCodeToString(buf[REPLY_POS_OF_REPLY_CODE]));
                    if(REPLY_POS_OF_START_OF_PARAMETER < buf.length)
                    {
                        int length = (0xff & buf[REPLY_POS_OF_LENGTH]) -2;
                        int offset = REPLY_POS_OF_START_OF_PARAMETER;
                        if(0 < length)
                        {
                            res.append(Tool.fromByteBufferToHexString(buf, length, offset));
                        }
                    }
                }
            }
        }
        return res.toString();
    }

    private static String replyCodeToString(byte b)
    {
        switch(b)
        {
        case RESPONSE_FRAME_RECEIPT_ERROR: return "Frame Receipt Error";
        case RESPONSE_OK: return "ok";
        case RESPONSE_GENERIC_APPLICATION_ERROR: return "generic application Error";
        case RESPONSE_STOPPED: return "stopped";
        case RESPONSE_ORDER_SPECIFIC_ERROR: return "order specific error";
        case RESPONSE_DEBUG_FRAME_DEBUG_MESSAGE: return "debug";
        default: return "Invalid Reply Code";
        }
    }

    public static String orderCodeToString(byte b)
    {
        switch(b)
        {
        case ORDER_RESUME: return "resume";
        case ORDER_REQ_INFORMATION: return "req Information";
        case ORDER_REQ_DEVICE_NAME: return "req Device name";
        case ORDER_REQ_TEMPERATURE: return "req Temperature";
        case ORDER_GET_HEATER_CONFIGURATION: return "get Heater cfg";
        case ORDER_CONFIGURE_HEATER: return "cfg Heater";
        case ORDER_SET_HEATER_TARGET_TEMPERATURE: return "set Heater Temperature";
        case ORDER_REQ_INPUT: return "req Input";
        case ORDER_SET_OUTPUT: return "set Output";
        case ORDER_SET_PWM: return "set PWM";
        case ORDER_WRITE_FIRMWARE_CONFIGURATION: return "write FWcfg";
        case ORDER_READ_FIRMWARE_CONFIGURATION: return "read FWcfg";
        case ORDER_STOP_PRINT: return "stop print";
        case ORDER_ACTIVATE_STEPPER_CONTROL: return "activate stepper control";
        case ORDER_ENABLE_DISABLE_STEPPER_MOTORS: return "en/disable stepper";
        case ORDER_CONFIGURE_END_STOPS: return "cfg end stop";
        case ORDER_ENABLE_DISABLE_END_STOPS: return "en/disable end stops";
        case ORDER_REQUEST_DEVICE_COUNT: return "req. Device count";
        case ORDER_QUEUE_COMMAND_BLOCKS: return "add to Queue";
        case ORDER_CONFIGURE_AXIS_MOVEMENT_RATES: return "cfg Axis movement rate";
        case ORDER_RETRIEVE_EVENTS: return "retrieve Events";
        case ORDER_GET_NUMBER_EVENT_FORMAT_IDS: return "get Num. Event Format IDs";
        case ORDER_GET_EVENT_STRING_FORMAT_ID: return "get Event String Format ID";
        case ORDER_CLEAR_COMMAND_BLOCK_QUEUE: return "clear Queue";
        case ORDER_REQUEST_DEVICE_STATUS: return "req Device status";
        case ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS: return "cfg under run avoidance";
        case ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES: return "get FWcfg value Props";
        case ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES: return "traverse FWcfg Values";
        case ORDER_RESET: return "reset";
        default: return "Invalid Order Code";
        }
    }

    public Protocol(String ConnectionDefinition)
    {
        this.cc = ClientConnectionFactory.establishConnectionTo(ConnectionDefinition);
        if(null == cc)
        {
            isOperational = false;
        }
        else
        {
            // Arduino Clients with Automatic Reset need a pause of one second.(Bootloader)
            try{ Thread.sleep(1000); } catch(InterruptedException e) { }
            // take client out of Stopped Mode
            isOperational = sendOrderExpectOK(ORDER_RESUME, CLEAR_STOPPED_STATE);
        }
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    @Override
    public String toString()
    {
        return "operational =" + isOperational + " , Client Location = " + cc.toString();
    }

    /**
     *
     * @return true if everything is ready to start.
     */
    public boolean isOperational()
    {
        return isOperational;
    }

    public void closeConnection()
    {
        cc.close();
        isOperational = false;
    }

// Information

    public Reply sendInformationRequest(final int which) throws IOException
    {
        final byte[] request = new byte[1];
        request[0] = (byte)(0xff & which);
        return cc.sendRequest(ORDER_REQ_INFORMATION, request);
    }

    public Reply sendDeviceCountRequest(final int device) throws IOException
    {
        final byte[] request = new byte[1];
        request[0] = (byte)(0xff & device);
        return cc.sendRequest(ORDER_REQUEST_DEVICE_COUNT, request);
    }

    public Reply sendDeviceNameRequest(final byte type, final int index) throws IOException
    {
        final byte[] request = new byte[2];
        request[0] = type;
        request[1] = (byte)(0xff & index);
        return cc.sendRequest(ORDER_REQ_DEVICE_NAME, request);
    }

    public static String getDeviceTypeName(int deviceType)
    {
        switch(deviceType)
        {
        case DEVICE_TYPE_UNUSED:             return "unused";
        case DEVICE_TYPE_INPUT:              return "input";
        case DEVICE_TYPE_OUTPUT:             return "output";
        case DEVICE_TYPE_PWM_OUTPUT:         return "PWM output(fan,..)";
        case DEVICE_TYPE_STEPPER:            return "stepper motor";
        case DEVICE_TYPE_HEATER:             return "heater";
        case DEVICE_TYPE_TEMPERATURE_SENSOR: return "temperature sensor";
        case DEVICE_TYPE_BUZZER:             return "buzzer";
        default:                             return "undefined";
        }
    }

    public DeviceInformation getDeviceInformation()
    {
        final DeviceInformation paceMaker = new DeviceInformation();
        try
        {
            if(true == paceMaker.readDeviceInformationFrom(this))
            {
                di = paceMaker;
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

// Input

    public int getSwitchState(final int num)
    {
        final byte[] param = new byte[2];
        param[0] = DEVICE_TYPE_INPUT;
        param[1] = (byte) num;
        final int reply = sendOrderExpectInt(Protocol.ORDER_REQ_INPUT, param);
        switch(reply)
        {
        case INPUT_HIGH: return Executor.SWITCH_STATE_CLOSED;
        case INPUT_LOW:  return Executor.SWITCH_STATE_OPEN;
        default :        log.error("Get Switch State returned {} !", reply);
                         return Executor.SWITCH_STATE_NOT_AVAILABLE;
        }
    }

// Temperature Sensor - Heater

    public boolean setTemperature(final int heaterNum, Double temperature)
    {
        temperature = temperature * 10; // Client expects Temperature in 0.1 degree units.
        final int tempi = temperature.intValue();
        final byte[] param = new byte[3];
        param[0] = (byte) heaterNum;
        param[1] = (byte)(0xff & (tempi/256));
        param[2] = (byte)(tempi & 0xff);
        return sendOrderExpectOK(Protocol.ORDER_SET_HEATER_TARGET_TEMPERATURE, param);
    }

    public double readTemperatureFrom(int sensorNumber)
    {
        byte[] param = new byte[2];
        param[0] = DEVICE_TYPE_TEMPERATURE_SENSOR;
        param[1] = (byte) sensorNumber;
        final Reply r = cc.sendRequest(ORDER_REQ_TEMPERATURE, param);
        if(null == r)
        {
            return -10000.3;
        }
        if(true == r.isOKReply())
        {
            byte[] reply = r.getParameter();
            if(2 > reply.length)
            {
                // The return did not have the data
                return -1000.4;
            }
            int reportedTemp = ((0xff &reply[0]) * 256) + ( 0xff & reply[1]);
            if(SENSOR_PROBLEM == reportedTemp)
            {
                return -1000.1;
            }
            else
            {
                return reportedTemp / (double)10;
            }
        }
        else
        {
            // error -> try again later
            return -1000.2;
        }
    }

// Fans

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 65535 = max
     */
    public boolean setFanSpeedfor(final int fan, final int speed)
    {
        if((-1 < fan) && (fan < di.getNumberPwmSwitchedOutputs()))
        {
            final byte[] param = new byte[4];
            param[0] = DEVICE_TYPE_PWM_OUTPUT;
            param[1] = (byte)fan;
            param[2] = (byte)(speed/256);
            param[3] = (byte)(0xff & speed);
            if(false == sendOrderExpectOK(ORDER_SET_PWM, param))
            {
                log.error("Falied to set Speed on the Fan !");
                lastErrorReason = "Falied to set Speed on the Fan !";
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            log.warn("Client does not have the Fan {} ! It has only {} + 1 fans!",
                    fan,  di.getNumberPwmSwitchedOutputs());
            return true;
        }
    }

// Output

    /** sets the state of the Output.
     *
     * @param output specifies the effected output.
     * @param state 0 = low; 1 = high; 2 = disabled / high-Z
     */
    public boolean setOutputState(final int output, final int state)
    {
        if((-1 < output) && (output < di.getNumberOutputSignals()))
        {
            final byte[] param = new byte[3];
            param[0] = DEVICE_TYPE_OUTPUT;
            param[1] = (byte)output;
            param[2] = (byte)(0xff & state);
            if(false == sendOrderExpectOK(ORDER_SET_OUTPUT, param))
            {
                log.error("Falied to set output state !");
                lastErrorReason = "Falied to set output state !";
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            log.warn("Client does not have the output {} ! It has only {} + 1 output!",
                    output,  di.getNumberOutputSignals());
            return true;
        }
    }

// Stepper Motors

    public boolean activateStepperControl()
    {
        final byte[] param = new byte[1];
        param[0] = 0x01;
        return sendOrderExpectOK(Protocol.ORDER_ACTIVATE_STEPPER_CONTROL, param);
    }

    public boolean configureUnderRunAvoidance(int stepperNumber, int maxSpeedStepsPerSecond, int maxAccelleration)
    {
        final byte[] param = new byte[9];
        param[0] = (byte)(0xff & stepperNumber);
        param[1] = (byte)(0xff & (maxSpeedStepsPerSecond>>24));
        param[2] = (byte)(0xff & (maxSpeedStepsPerSecond>>16));
        param[3] = (byte)(0xff & (maxSpeedStepsPerSecond>>8));
        param[4] = (byte)(0xff & maxSpeedStepsPerSecond);
        param[5] = (byte)(0xff & (maxAccelleration>>24));
        param[6] = (byte)(0xff & (maxAccelleration>>16));
        param[7] = (byte)(0xff & (maxAccelleration>>8));
        param[8] = (byte)(0xff & maxAccelleration);
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS, param);
    }

    public boolean configureStepperMovementRate(int stepperNumber, int maxSpeedStepsPerSecond)
    {
        final byte[] param = new byte[5];
        param[0] = (byte)(0xff & stepperNumber);
        param[1] = (byte)(0xff & (maxSpeedStepsPerSecond>>24));
        param[2] = (byte)(0xff & (maxSpeedStepsPerSecond>>16));
        param[3] = (byte)(0xff & (maxSpeedStepsPerSecond>>8));
        param[4] = (byte)(0xff & maxSpeedStepsPerSecond);
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_AXIS_MOVEMENT_RATES, param);
    }

    public boolean configureEndStop(Stepper motor, Switch min, Switch max)
    {
        if((null == min) && (null == max))
        {
            // No switch configured -> nothing to do -> But that was successful
            return true;
        }
        final byte[] param;
        if((null == min) || (null == max))
        {
            // only one switch configured
            param = new byte[3];
        }
        else
        {
            param = new byte[5];
        }
        param[0] = (byte)motor.getStepperNumber();
        int startpos = 1;
        if(null != min)
        {
            param[startpos] = (byte)min.getNumber();
            param[startpos + 1] = 0; // MIN
            startpos = startpos + 2;
        }
        if(null != max)
        {
            param[startpos] = (byte)max.getNumber();
            param[startpos + 1] = 1; // MAX
            startpos = startpos + 2;
        }
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_END_STOPS, param);
    }

    /** only needed to implement M17
     *
     * @return
     */
    public boolean enableAllStepperMotors()
    {
        final int numSteppers = di.getNumberSteppers();
        byte[] parameter = new byte[2];
        parameter[1] = 0x01; // Enabled
        for(int i = 0; i < numSteppers; i++)
        {
            parameter[0] = (byte)i;
            if(false == sendOrderExpectOK(Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS, parameter))
            {
                log.error("Falied to enable the Steppers !");
                lastErrorReason = "Falied to enable the Steppers !";
                return false;
            }
        }
        return true;
    }

    public boolean disableAllStepperMotors()
    {
        if(false == sendOrderExpectOK((byte)Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS, null))
        {
            log.error("Falied to disable the Steppers !");
            lastErrorReason = "Falied to disable the Steppers !";
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean doStopPrint()
    {
        final byte[] param = new byte[1];
        param[0] = ORDERED_STOP;
        return sendOrderExpectOK(Protocol.ORDER_STOP_PRINT, param);
    }

    public boolean doEmergencyStopPrint()
    {
        final byte[] param = new byte[1];
        param[0] = EMERGENCY_STOP;
        return sendOrderExpectOK(Protocol.ORDER_STOP_PRINT, param);
    }

// Queue handling:

    public int getNumberOfCommandsInClientQueue()
    {
        return ClientQueueNumberOfEnqueuedCommands;
    }

    public boolean hasFreeQueueSlots()
    {
        if(0 < ClientQueueFreeSlots)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void parseQueueReply(byte[] reply)
    {
        parseQueueReply(reply, 0);
    }

    private void parseQueueReply(byte[] reply, int offset)
    {
        if((5 + offset)< reply.length)
        {
            ClientQueueFreeSlots =                ((0xff & reply[0 + offset]) * 256) + (0xff & reply[1 + offset]);
            ClientQueueNumberOfEnqueuedCommands = ((0xff & reply[2 + offset]) * 256) + (0xff & reply[3 + offset]);
            ClientExecutedJobs =                  ((0xff & reply[4 + offset]) * 256) + (0xff & reply[5 + offset]);
        }
    }

    public void ClearQueue()
    {
        final Reply r = cc.sendRequest(ORDER_CLEAR_COMMAND_BLOCK_QUEUE, null);
        if(true == r.isOKReply())
        {
            parseQueueReply(r.getParameter());
        }
        else
        {
            log.error("Could not clear the Command Queue !");
        }
    }

    public boolean addBasicLinearMove(Integer[] activeSteppers,
                                      boolean isHoming,
                                      int speed,
                                      int endSpeed,
                                      int accellerationSteps,
                                      int DecellerationSteps,
                                      boolean[] axisDirectionIsIncreasing,
                                      int primaryAxis,
                                      Integer[] steps)
    {
        if(   (true == di.hasExtensionQueuedCommand())
           && (true == di.hasExtensionBasicMove()) )
        {
            // detect mode
            int maxStepperIDx = 0;
            for(int i = 0; i < activeSteppers.length; i++)
            {
                if(activeSteppers[i] > maxStepperIDx)
                {
                    maxStepperIDx = activeSteppers[i];
                }
            }
            int maxStep = accellerationSteps;
            if(DecellerationSteps > maxStep)
            {
                maxStep = DecellerationSteps;
            }
            for(int i = 0; i < steps.length; i++)
            {
                if(steps[i] > maxStep)
                {
                    maxStep = steps[i];
                }
            }
            int bytesPerStep;
            if(255 < maxStep)
            {
                bytesPerStep = 1;
            }
            else
            {
                bytesPerStep = 2;
            }
            // Prepare data
            final byte[] param;
            int steppsStart;
            if(maxStepperIDx < 7)
            {
                // 1 byte Axis selection mode
                param = new byte[7 + (bytesPerStep * (2 + steps.length))];
                param[0] = (byte)(0xff & param.length - 1); // Length
                param[1] = MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE; // Type
                // active Steppers
                int ActiveSteppersMap = 0;
                for(int i = 0; i < activeSteppers.length; i++)
                {
                    ActiveSteppersMap = ActiveSteppersMap | 1 << activeSteppers[i];
                }
                param[2] = (byte)(0x7f & ActiveSteppersMap);
                // Byte per steps
                if(1 == bytesPerStep)
                {
                    param[3] = 0;
                }
                else
                {
                    param[3] = (byte) 0x80;
                }
                // directions
                int DirectionMap = 0;
                for(int i = 0; i < axisDirectionIsIncreasing.length; i++)
                {
                    if(true == axisDirectionIsIncreasing[i])
                    {
                        DirectionMap = DirectionMap | (1 << activeSteppers[i]);
                    }
                }
                param[3] =  (byte)(param[3] | (0x7f | DirectionMap));
                // Homing
                if(true == isHoming)
                {
                    param[4] = 0x10;
                }
                // Primary Axis
                param[4] =(byte)(param[4] | (0x0f & primaryAxis));
                // Nominal Speed
                param[5] = (byte)(0xff &speed);
                // end Speed
                param[6] = (byte)(0xff &endSpeed);
                steppsStart = 7;
            }
            else if(maxStepperIDx < 15)
            {
                // 2 byte Axis selection mode
                param = new byte[9 + (bytesPerStep * (2 + steps.length))];
                param[0] = (byte)(0xff & param.length - 1); // Length
                param[1] = MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE; // Type
                // Active Steppers
                int ActiveSteppersMap = 0;
                for(int i = 0; i < activeSteppers.length; i++)
                {
                    ActiveSteppersMap = ActiveSteppersMap | 1 << activeSteppers[i];
                }
                param[3] = (byte)(0xff & ActiveSteppersMap);
                param[2] = (byte)(0x80 | (0x7f & (ActiveSteppersMap >> 8)));
                // Byte per steps
                if(1 == bytesPerStep)
                {
                    param[4] = 0;
                }
                else
                {
                    param[4] = (byte) 0x80;
                }
                // directions
                int DirectionMap = 0;
                for(int i = 0; i < axisDirectionIsIncreasing.length; i++)
                {
                    if(true == axisDirectionIsIncreasing[i])
                    {
                        DirectionMap = DirectionMap | (1 << activeSteppers[i]);
                    }
                }
                param[5] =  (byte)(0xff | DirectionMap);
                param[4] =  (byte)(param[4] | (0x7f | (DirectionMap>>8)));
                // Homing
                if(true == isHoming)
                {
                    param[6] = 0x10;
                }
                // Primary Axis
                param[6] =(byte)(param[6] | (0x0f & primaryAxis));
                // Nominal Speed
                param[7] = (byte)(0xff &speed);
                // end Speed
                param[8] = (byte)(0xff &endSpeed);
                steppsStart = 9;
            }
            else
            {
                lastErrorReason = "Too Many Steppers - Can only handle 15 !";
                log.error(lastErrorReason);
                return false;
            }
            // Add Steps
            if(1 == bytesPerStep)
            {
                param[steppsStart    ] = (byte)(0xff & accellerationSteps);
                param[steppsStart + 1] = (byte)(0xff & DecellerationSteps);
                for(int i = 0; i < steps.length; i++)
                {
                    param[steppsStart + 2 + i] = (byte)(0xff & steps[i]);
                }
            }
            else
            {
                // 2 bytes
                param[steppsStart    ] = (byte)(0xff & (accellerationSteps>>8));
                param[steppsStart + 1] = (byte)(0xff & accellerationSteps);
                param[steppsStart + 2] = (byte)(0xff & (DecellerationSteps>>8));
                param[steppsStart + 3] = (byte)(0xff & DecellerationSteps);
                for(int i = 0; i < steps.length; i++)
                {
                    param[steppsStart + 4 + i*2] = (byte)(0xff & (steps[i]>>8));
                    param[steppsStart + 5 + i*2] = (byte)(0xff & steps[i]);
                }
            }
            // Send the data
            if(RESULT_ERROR == enqueueCommand(param))
            {
                return false;
            }
            else
            {
                // success or try again later and data queued
                return true;
            }
        }
        else
        {
            lastErrorReason = "no Queue - no chance to add to it.";
            return false;
        }
    }

    public boolean addSetActiveToolHeadToQueue(final int activeToolHead)
    {
        if(   (true == di.hasExtensionQueuedCommand())
           && (true == di.hasExtensionBasicMove()) )
        {
            final byte[] param = new byte[3];
            param[0] = 2;
            param[1] = MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD;
            param[2] = (byte)(0xff & activeToolHead);
            return enqueueCommandBlocking(param);
        }
        else
        {
            lastErrorReason = "no Queue - no chance to add to it.";
            return false;
        }
    }

    public boolean endStopOnOff(boolean on, Integer[] switches)
    {
        final byte[] param = new byte[3 + (switches.length * 2)];
        param[0] = (byte)(param.length - 1);
        param[1] = MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER;
        param[2] = Protocol.ORDER_ENABLE_DISABLE_END_STOPS;
        byte onOff;
        if(true == on)
        {
            onOff = 0x01; // on
        }
        else
        {
            onOff = 0; // off
        }
        for(int i = 0; i < switches.length; i++)
        {
            param[3 + (i* 2)] = (byte)(0xff & switches[i]);
            param[3 + (i* 2) + 1] = onOff;
        }
        return enqueueCommandBlocking(param);
    }

    /** adds a pause to the Queue.
     *
     * @param ticks allowed 0..65535 (0xffff)
     * @return
     */
    public boolean addPauseToQueue(final int ticks)
    {
        if(true == di.hasExtensionQueuedCommand())
        {
            final byte[] param = new byte[4];
            param[0] = 3;
            param[1] = MOVEMENT_BLOCK_TYPE_DELAY;
            param[2] = (byte)(0xff & (ticks/256));
            param[3] = (byte)(ticks & 0xff);
            return enqueueCommandBlocking(param);
        }
        else
        {
            lastErrorReason = "no Queue - no chance to add to it.";
            return false;
        }
    }

    private int sendDataToClientQueue(byte[] param, int length, int numBlocksInBuffer)
    {
        final Reply r = cc.sendRequest(ORDER_QUEUE_COMMAND_BLOCKS,
                                       param, // data
                                       0, // offset
                                       length // length
                                       );
        // and see what happens.
        if(null == r)
        {
            lastErrorReason = "Received No Reply from Client !";
            log.error(lastErrorReason);
            return RESULT_ERROR;
        }
        if(true == r.isOKReply())
        {
            parseQueueReply(r.getParameter());
            CommandsSendToClient = CommandsSendToClient + numBlocksInBuffer;
            for(int i = 0; i < numBlocksInBuffer; i++)
            {
                if(0 < sendQueue.size())
                {
                    sendQueue.remove(0);
                }
            }
            return RESULT_SUCCESS;
        }
        else if(RESPONSE_ORDER_SPECIFIC_ERROR == r.getReplyCode())
        {
            // Order Specific Error
            byte[] response = r.getParameter();
            // partly Queued
            int numberOfQueued = (0xff & response[1]);
            for(int i = 0; i < numberOfQueued; i++)
            {
                if(0 < sendQueue.size())
                {
                    sendQueue.remove(0);
                }
            }
            parseQueueReply(response, 2);
            if(0x01 != response[0])
            {
                // Error caused by bad Data !
                lastErrorReason = "Could not Queue Block as Client Reports invalid Data !";
                log.error(lastErrorReason);
                log.error("Error Reply Code : " + (0xff & response[8]));
                if(9 < response.length)
                {
                    log.error("Description : " + r.getParameterAsString(9));
                }
                log.error("Send Data: {} !", Tool.fromByteBufferToHexString(param, length));
                log.error("Received : {} !", Tool.fromByteBufferToHexString(response));
                return RESULT_ERROR;
            }
            // else queue full so try next time
            return RESULT_TRY_AGAIN_LATER;
        }
        else
        {
            // error -> send commands later
            lastErrorReason = "Protocol violation - Unexpected Reply !";
            log.error(lastErrorReason);
            return RESULT_ERROR;
        }
    }

    /** Enqueues the data for _one_ command into the Queue.
     *
     * CAUTION: All data that can not be send out stays in Memory. So if this
     * function fails repeatedly the memory consumption increases with every
     * call. If in this situation try calling enqueueCommandBlocking() !
     *
     * @param param Data of only one command !
     * @return RESULT_SUCCESS,  RESULT_ERROR or RESULT_TRY_AGAIN_LATER
     */
    private int enqueueCommand(byte[] param)
    {
        // add the new command, and...
        sendQueue.add(param);
        // TODO wait for enough bytes in Buffer ?
        // try to get the Queue empty again.
        if(0 == sendQueue.size())
        {
            // nothing to send
            return RESULT_SUCCESS;
        }
        if(0 == ClientQueueFreeSlots)
        {
            // send only the first command from the command queue
            byte[] firstCommand = sendQueue.get(0);
            return sendDataToClientQueue(firstCommand, firstCommand.length, 1);
        }
        else
        {
            byte[] sendBuffer = new byte[QUEUE_SEND_BUFFER_SIZE];
            int writePos = 0;
            int numBlocksInBuffer = 0;
            int idx = 0;
            for(int i = 0; i < ClientQueueFreeSlots; i++)
            {
                // add a block to the send buffer until
                // either send Buffer if full
                // or all commands have been put in the buffer
                // or the number of free slots on the client has been reached
                if(idx < sendQueue.size())
                {
                    byte[] buf = sendQueue.get(idx);
                    if(null != buf)
                    {
                        if(buf.length < QUEUE_SEND_BUFFER_SIZE - writePos)
                        {
                            for(int j = 0; j < buf.length; j++)
                            {
                                sendBuffer[writePos + j] = buf[j];
                            }
                            writePos = writePos + buf.length;
                            numBlocksInBuffer ++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                    idx++;
                }
                else
                {
                    break;
                }
            }
            // then send them
            return sendDataToClientQueue(sendBuffer, writePos, numBlocksInBuffer);
        }
    }


    /** Enqueues the data for _one_ command into the Queue.
     *
     * If the Queue is full this function waits until a free spot becomes
     * available again.
     *
     * @param param Data of only one command !
     * @return true = success; false= command could not be put in the queue.
     */
    private boolean enqueueCommandBlocking(byte[] param)
    {
        // prepare data
        if(null == param)
        {
            lastErrorReason = "Tried To enque without data !";
            log.error(lastErrorReason);
            return false;
        }
        if(2 > param.length)
        {
            lastErrorReason = "Tried To enque with too few bytes !";
            log.error(lastErrorReason);
            return false;
        }
        int Result = enqueueCommand(param);
        if(RESULT_TRY_AGAIN_LATER == Result)
        {
            int delayCounter = 0;
            do
            {
                if((delayCounter < MAX_ENQUEUE_DELAY))
                {
                    try
                    {
                        Thread.sleep(QUEUE_POLL_DELAY);
                    }
                    catch(InterruptedException e)
                    {
                    }
                    delayCounter++;
                }
                else
                {
                    lastErrorReason = "Could not enque - Timeout !";
                    log.error(lastErrorReason);
                    return false;
                }
                Result = enqueueCommand(null);
            }while(RESULT_TRY_AGAIN_LATER == Result);
        }
        if(RESULT_SUCCESS == Result)
        {
            // sending succeeded !
            return true;
        }
        else
        {
            return false;
        }
    }

// Firmware specific configuration:

    public boolean writeFirmwareConfigurationValue(String name, String value)
    {
        byte[] nameBuf = name.getBytes(Charset.forName("UTF-8"));
        byte[] valueBuf = value.getBytes(Charset.forName("UTF-8"));
        byte[] parameter = new byte[nameBuf.length + valueBuf.length + 1];
        parameter[0] = (byte)nameBuf.length;
        for(int i = 0; i < nameBuf.length; i++)
        {
            parameter[i+1] = nameBuf[i];
        }
        for(int i = 0; i < valueBuf.length; i++)
        {
            parameter[i+nameBuf.length + 1] = valueBuf[i];
        }
        if(false == sendOrderExpectOK(ORDER_WRITE_FIRMWARE_CONFIGURATION, parameter))
        {
            log.error("Failed to write Firmware Setting {} = {} !", name, value);
            lastErrorReason = "Failed to write Firmware Setting " + name + " = " + value + " !";
            return false;
        }
        else
        {
            return true;
        }
    }

    public String getCompleteDescriptionForSetting(String curSetting)
    {
        if(null == curSetting)
        {
            return "";
        }
        byte[] strbuf;
        if(0 < curSetting.length())
        {
            strbuf = curSetting.getBytes(Charset.forName("UTF-8"));
        }
        else
        {
            return "";
        }
        final Reply r = cc.sendRequest(ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES, strbuf);
        if(null == r)
        {
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            return "";
        }
        byte[] res = r.getParameter();
        if(null == res)
        {
            return "";
        }
        if(res.length < 4)
        {
            return "";
        }
        int ElementType = 0xff & res[0];
        int ElementModes = 0xff & res[1];
        int DeviceType = 0xff & res[2];
        int DeviceNumber = 0xff & res[3];
        StringBuffer description = new StringBuffer();
        description.append(curSetting);
        description.append("(");
        switch(ElementType)
        {
        case FIRMWARE_SETTING_TYPE_VOLATILE_CONFIGURATION:description.append("volatile configuration"); break;
        case FIRMWARE_SETTING_TYPE_NON_VOLATILE_CONFIGURATION:description.append("non volatile configuration"); break;
        case FIRMWARE_SETTING_TYPE_STATISTIC:description.append("status"); break;
        case FIRMWARE_SETTING_TYPE_SWITCH:description.append("feature switch"); break;
        case FIRMWARE_SETTING_TYPE_DEBUG:description.append("diagnostic"); break;
        default:
            description.append("invalid Type");
            break;
        }
        description.append(",");
        if(1 == (ElementModes & 0x01))
        {
            description.append("r");
        }
        if(2 == (ElementModes & 0x02))
        {
            description.append("w");
        }
        if(4 == (ElementModes & 0x04))
        {
            description.append("d");
        }
        description.append(",");
        description.append(getDeviceTypeName(DeviceType));
        description.append(",");
        description.append(DeviceNumber);
        return description.toString();
    }

    public String traverseFirmwareConfiguration(String curSetting)
    {
        if(null == curSetting)
        {
            curSetting = "";
        }
        byte[] strbuf;
        if(0 < curSetting.length())
        {
            strbuf = curSetting.getBytes(Charset.forName("UTF-8"));
        }
        else
        {
            strbuf = new byte[0];
        }
        final Reply r = cc.sendRequest(ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES, strbuf);
        if(null == r)
        {
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            return "";
        }
        byte[] res = r.getParameter();
        if(null == res)
        {
            return "";
        }
        try
        {
            return new String(res, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public String readFirmwareConfigurationValue(String curSetting)
    {
        if(null == curSetting)
        {
            return "";
        }
        byte[] strbuf;
        if(0 < curSetting.length())
        {
            strbuf = curSetting.getBytes(Charset.forName("UTF-8"));
        }
        else
        {
            return "";
        }
        final Reply r = cc.sendRequest(ORDER_READ_FIRMWARE_CONFIGURATION, strbuf);
        if(null == r)
        {
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            return "";
        }
        byte[] res = r.getParameter();
        if(null == res)
        {
            return "";
        }
        try
        {
            return new String(res, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private boolean sendOrderExpectOK(final byte order, final byte parameter)
    {
        byte[] help = new byte[1];
        help[0] = parameter;
        return sendOrderExpectOK(order, help);
    }

    private boolean sendOrderExpectOK(final byte order, final byte[] parameter)
    {
        final Reply r = cc.sendRequest(order, parameter);
        if(null == r)
        {
            log.error("Received no Reply !");
            lastErrorReason = "Received no Reply !";
            return false;
        }
        return r.isOKReply();
    }

    private int sendOrderExpectInt(final byte order, final byte[] parameter)
    {
        final Reply r = cc.sendRequest(order, parameter);
        if(null == r)
        {
            return -1;
        }
        if(false  == r.isOKReply())
        {
            log.error("Reply is not an OK ! " + r);
            return -2;
        }
        final byte[] reply = r.getParameter();
        switch(reply.length)
        {
        case 1: return (0xff & reply[0]); // 8 bit int
        case 2: return (0xff & reply[0])* 256 + (0xff & reply[1]); // 16 bit int
        default: return -3;
        }
    }

}
