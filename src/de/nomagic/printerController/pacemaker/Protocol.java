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
import de.nomagic.printerController.core.ActionResponse;
import de.nomagic.printerController.core.Action_enum;
import de.nomagic.printerController.core.Event;
import de.nomagic.printerController.core.EventSource;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.core.devices.Stepper;
import de.nomagic.printerController.core.devices.Switch;
import de.nomagic.printerController.core.devices.TemperatureSensor;
import de.nomagic.printerController.core.movement.BasicLinearMove;

/** Pacemaker protocol.
 *  as specified in https://github.com/JustAnother1/Pacemaker/blob/master/doc/Pacemaker_Protocol.asciidoc
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Protocol implements EventSource
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
    public static final byte ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES           = 0x1b; // 27
    public static final byte ORDER_RESET                                            = (byte)0x7f; // 127

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
    public static final byte CAUSE_USER_REQUESTED = 7;
    public static final byte CAUSE_HOST_TIMEOUT = 8;
    public static final byte CAUSE_OTHER_FAULT = 9;

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

    public static final byte MOVEMENT_BLOCK_QUEUE_FULL = 0x01;
    public static final byte MOVEMENT_BLOCK_UNKNOWN_BLOCK = 0x02;
    public static final byte MOVEMENT_BLOCK_MALFORMED_BLOCK = 0x03;
    public static final byte MOVEMENT_BLOCK_ERROR_IN_BLOCK = 0x04;

    ////////////////////////////////////////////////////////////////////////////
    // end of Magic Number from Protocol Definition
    ////////////////////////////////////////////////////////////////////////////

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_TRY_AGAIN_LATER = 2;

    public static final int MAX_STEPS_PER_MOVE = 0xffff;
    public static final int QUEUE_TIMEOUT_MS  = 500;

    public static final double LOWEST_POSSIBLE_TEMPERATURE           = -1000.0;
    public static final double TEMPERATURE_ERROR_SENSOR_PROBLEM      = -1000.1;
    public static final double TEMPERATURE_ERROR_REPLY_NOT_OK        = -1000.2;
    public static final double TEMPERATURE_ERROR_NO_REPLY            = -1000.3;
    public static final double TEMPERATURE_ERROR_REPLY_WITHOUT_DATA  = -1000.4;

    private static final int QUEUE_SEND_BUFFER_SIZE = 200;

    // The client switches on the power after a resume command. Give the client this time for the powered on components to boot.
    private static final int CLIENT_POWER_UP_DELAY_MS = 50;

    // the client needs at lest this(in milliseconds) time to free up one slot in the Queue
    private final long QUEUE_POLL_DELAY = 100;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private static final Logger Log = LoggerFactory.getLogger("static:Protocol");
    private final ClientConnection cc;

    // TODO needs Syncronisation:
    private volatile String lastErrorReason = null;
    private volatile boolean isOperational = false;

    private DeviceInformation di = null;

    private final Vector<byte[]> sendQueue = new Vector<byte[]>();
    private volatile int ClientQueueFreeSlots = 6;
    private volatile int ClientQueueKeepFreeSlots = 5;
    private volatile int ClientQueueNumberOfEnqueuedCommands = 0;
    private volatile int ClientExecutedJobs = 0;
    private volatile int CommandsSendToClient = 0;
    private volatile long timeofLastClientQueueUpdate;
    private volatile int hostTimeout = 2;
    private final TimeoutHandler timeout;
    private int timeoutId;

    public Protocol(ClientConnection Client, TimeoutHandler timeout)
    {
        this.cc = Client;
        if(null == cc)
        {
            log.error("Received no Client!");
            isOperational = false;
        }
        else
        {
            // take client out of Stopped Mode
            isOperational = sendOrderExpectOK(ORDER_RESUME, CLEAR_STOPPED_STATE, new Reference("Protocol Initialisation"));
            try
            {
                Thread.sleep(CLIENT_POWER_UP_DELAY_MS);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        this.timeout = timeout;
        if(null != timeout)
        {
            final Event e = new Event(Action_enum.timeOut, null, this);
            timeoutId = timeout.createTimeout(e, hostTimeout/2 * 1000);
            if(TimeoutHandler.ERROR_FAILED_TO_CREATE_TIMEOUT == timeoutId)
            {
                log.error("No Timeout available - can not send Keep Alives to Client!");
            }
            else
            {
                timeout.startTimeout(timeoutId);
            }
        }
    }

    @Override
    public void reportEventStatus(ActionResponse response, Reference Ref)
    {
        if(true == isOperational)
        {
            final long now = System.currentTimeMillis();
            long lastHearedOfClient = cc.getTimeOfLastSuccessfulReply();
            lastHearedOfClient = lastHearedOfClient + (hostTimeout/2 * 1000) - 500;
            if(lastHearedOfClient < now) // we haven't heard from the client since the last run
            {
                sendKeepAliveSignal();
            }
            // else communication active -> no need for keep alive signal.
            timeout.startTimeout(timeoutId);
        }
    }

    private static String pareParameter(byte[] buf, int length, int offset)
    {
        if((null == buf) || (0 == offset) || (0 == length))
        {
            return "";
        }
        final StringBuffer res = new StringBuffer();
        switch(buf[offset -1]) // order
        {
        case ORDER_CONFIGURE_END_STOPS:
            if(3 > length)
            {
                return "";
            }
            else
            {
                if(0 == buf[offset + 2])
                {
                    res.append("[ stepper " + buf[offset] + " has switch " + buf[offset + 1] + " as min end stop]");
                }
                else if(1 == buf[offset + 2])
                {
                    res.append("[ stepper " + buf[offset] + " has switch " + buf[offset + 1] + " as max end stop]");
                }
                else
                {
                    res.append("[ stepper " + buf[offset] + " has switch " + buf[offset + 1] + " as something wrong!]");
                }
            }
            break;

        case ORDER_ENABLE_DISABLE_END_STOPS:
            res.append("[");
            int i = 2;
            while(length >= i)
            {
                if(0 == buf[offset + (i - 1)])
                {
                    // disable
                    res.append(" disabling end stop switch " + buf[offset + (i - 2)]);
                }
                else if(1 == buf[offset + (i -1)])
                {
                    // enabled
                    res.append(" enabling end stop switch " + buf[offset + (i - 2)]);
                }
                else
                {
                    // invalid state
                    res.append(" invalid state for end stop switch " + buf[offset + (i - 2)] + "!! ");
                }
                i = i + 2;
            }
            res.append(" ]");
            break;

        case ORDER_CONFIGURE_HEATER:
            if(2 == length)
            {
                res.append("[Heater " + buf[offset] + " should use the temperature sensor " + buf[offset + 1] + "  ]");
            }
            else
            {
                 res.append("[Invalid : " + Tool.fromByteBufferToHexString(buf, length, offset) + "]");
            }
            break;

            // TODO:
        case ORDER_RESUME:
        case ORDER_REQ_INFORMATION:
        case ORDER_REQ_DEVICE_NAME:
        case ORDER_REQ_TEMPERATURE:
        case ORDER_GET_HEATER_CONFIGURATION:
        case ORDER_SET_HEATER_TARGET_TEMPERATURE:
        case ORDER_REQ_INPUT:
        case ORDER_SET_OUTPUT:
        case ORDER_SET_PWM:
        case ORDER_WRITE_FIRMWARE_CONFIGURATION:
        case ORDER_READ_FIRMWARE_CONFIGURATION:
        case ORDER_STOP_PRINT:
        case ORDER_ACTIVATE_STEPPER_CONTROL:
        case ORDER_ENABLE_DISABLE_STEPPER_MOTORS:
        case ORDER_REQUEST_DEVICE_COUNT:
        case ORDER_QUEUE_COMMAND_BLOCKS:
        case ORDER_CONFIGURE_AXIS_MOVEMENT_RATES:
        case ORDER_RETRIEVE_EVENTS:
        case ORDER_GET_NUMBER_EVENT_FORMAT_IDS:
        case ORDER_GET_EVENT_STRING_FORMAT_ID:
        case ORDER_CLEAR_COMMAND_BLOCK_QUEUE:
        case ORDER_REQUEST_DEVICE_STATUS:
        case ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS:
        case ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES:
        case ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES:
        case ORDER_RESET:

        default:
            res.append(Tool.fromByteBufferToHexString(buf, length, offset));
            break;
        }
        return res.toString();
    }


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
        final StringBuffer res = new StringBuffer();
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
                        final int length = (0xff & buf[ORDER_POS_OF_LENGTH]) -2;
                        final int offset = ORDER_POS_OF_START_OF_PARAMETER;
                        if(0 < length)
                        {
                            if(ORDER_QUEUE_COMMAND_BLOCKS == buf[ORDER_POS_OF_ORDER_CODE])
                            {
                                res.append(parseQueueBlock(buf, length, offset));
                            }
                            else
                            {
                                res.append(pareParameter(buf, length, offset));
                            }
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
                        final int length = (0xff & buf[REPLY_POS_OF_LENGTH]) -2;
                        final int offset = REPLY_POS_OF_START_OF_PARAMETER;
                        if(0 < length)
                        {
                            res.append(parseReplyParameter(buf[REPLY_POS_OF_REPLY_CODE], buf, length, offset));
                        }
                    }
                }
            }
        }
        return res.toString();
    }

    private static String parseReplyParameter(byte replyCode, byte[] buf, int length, int offset)
    {
        switch(replyCode)
        {
        case RESPONSE_STOPPED:
            StringBuffer sb = new StringBuffer();
            sb.append("Recovery: ");
            switch(buf[offset])
            {
            case RECOVERY_CLEARED       : sb.append("cleared"); break;
            case RECOVERY_PERSISTS      : sb.append("persists"); break;
            case RECOVERY_UNRECOVERABLE : sb.append("unrecoverable"); break;
            default                     : sb.append("invalid : (" + buf[offset] + ")");
            }
            sb.append(" Cause: ");
            switch(buf[offset + 1])
            {
            case CAUSE_RESET             : sb.append("Reset"); break;
            case CAUSE_END_STOP_HIT      : sb.append("unexpected end stop hit"); break;
            case CAUSE_MOVEMENT_ERROR    : sb.append("movement error"); break;
            case CAUSE_TEMPERATURE_ERROR : sb.append("temperature error"); break;
            case CAUSE_DEVICE_FAULT      : sb.append("device fault"); break;
            case CAUSE_ELECTRICAL_FAULT  : sb.append("electrical fault"); break;
            case CAUSE_FIRMWARE_FAULT    : sb.append("firmware fault"); break;
            case CAUSE_USER_REQUESTED    : sb.append("user requested"); break;
            case CAUSE_HOST_TIMEOUT      : sb.append("host timeout"); break;
            case CAUSE_OTHER_FAULT       : sb.append("other fault"); break;
            default                      : sb.append("invalid : (" + buf[offset] + ")");
            }
            return sb.toString();

        case RESPONSE_FRAME_RECEIPT_ERROR:
            switch(buf[offset])
            {
            case RESPONSE_BAD_FRAME: return "(bad frame)";
            case RESPONSE_BAD_ERROR_CHECK_CODE: return "(bad error check code)";
            case RESPONSE_UNABLE_TO_ACCEPT_FRAME: return "(unable to accept frame)";
            default: return "(Invalid : " + Tool.fromByteBufferToHexString(buf, length, offset) + ")";
            }

        case RESPONSE_GENERIC_APPLICATION_ERROR:
            String res;
            switch(buf[offset])
            {
            case RESPONSE_UNKNOWN_ORDER: res = "(unknown order)"; break;
            case RESPONSE_BAD_PARAMETER_FORMAT: res = "(bad parameter format)"; break;
            case RESPONSE_BAD_PARAMETER_VALUE: res = "(bad parameter value)"; break;
            case RESPONSE_INVALID_DEVICE_TYPE: res = "(invalid device type)"; break;
            case RESPONSE_INVALID_DEVICE_NUMBER: res = "(invalid device number)"; break;
            case RESPONSE_INCORRECT_MODE: res = "(wrong mode)"; break;
            case RESPONSE_BUSY: res = "(busy)"; break;
            case RESPONSE_FAILED: res = "(failed)"; break;
            case RESPONSE_FIRMWARE_ERROR: res = "(firmware error)"; break;
            case RESPONSE_CANNOT_ACTIVATE_DEVICE: res = "(device unavailable)"; break;
            default: res = "(Invalid : " + Tool.fromByteBufferToHexString(buf, length, offset) + ")"; break;
            }
            if(length > 1)
            {
                // a UTF-8 String follows
                res = res + " " + Tool.fromByteBufferToUtf8String(buf, length - 1, offset + 1);
            }
            return res;

        case RESPONSE_DEBUG_FRAME_DEBUG_MESSAGE:
            return Tool.fromByteBufferToUtf8String(buf, length, offset);

        default:
            return Tool.fromByteBufferToHexString(buf, length, offset) + " == " + Tool.fromByteBufferToUtf8String(buf, length, offset);
        }
    }

    private static String parseQueueBlock(byte[] buf, int length, int offset)
    {
        if(length < 2)
        {
            Log.error("Too little data to parse!");
            return "[no parameter]";
        }
        if(offset + length > buf.length)
        {
            Log.error("Invalid Parameters that would cause out of Bounds! buf.length= {}, length = {}, offset = {}", buf.length, length, offset);
            return "[error while parsing parameter]";
        }
        final StringBuffer res = new StringBuffer();
        int bytesToGo = length;
        do
        {
            final int blockLength = buf[offset];
            final int BlockType = buf[offset + 1];
            switch(BlockType)
            {
            case MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER:
                res.append("[order:" + orderCodeToString(buf[offset + 2]));
                res.append(pareParameter(buf, blockLength -3, offset + 3));
                break;

            case MOVEMENT_BLOCK_TYPE_DELAY:
                res.append("[delay " + (((0xff & buf[offset + 2])*256 + (0xff & buf[offset + 3]))*10) + "us]");
                break;

            case MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE:
                res.append(parseBasicLinearMove(buf, blockLength -2, offset +2));
                break;

            case MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD:
                res.append("[use Toolhead " + (0xff & buf[offset + 2]) + "]");
                break;

            default:
                res.append(Tool.fromByteBufferToHexString(buf, blockLength, offset));
                break;
            }
            offset = offset + blockLength;
            bytesToGo = bytesToGo - blockLength;
        } while(1 >bytesToGo);
        return res.toString();
    }

    private static String parseBasicLinearMove(byte[] data, int length, int offset)
    {
        final StringBuffer res = new StringBuffer();
        res.append("[");
        boolean twoByteAxisFormat;
        if(0 == (0x80 & data[offset]))
        {
            twoByteAxisFormat = false;
        }
        else
        {
            twoByteAxisFormat = true;
        }
        int AxisSelection;
        int nextByte;
        if(false == twoByteAxisFormat)
        {
            AxisSelection = (0x7f & data[offset]);
            nextByte = offset + 1;
        }
        else
        {
            AxisSelection = (0x7f & data[offset])<<8 + (0xff & data[offset + 1]);
            nextByte = offset + 2;
        }
        boolean twoByteStepCount;
        if(0 == (0x80 & data[nextByte]))
        {
            twoByteStepCount = false;
        }
        else
        {
            twoByteStepCount = true;
        }
        int AxisDirection;
        if(false == twoByteAxisFormat)
        {
            AxisDirection = (0x7f & data[nextByte]);
            nextByte = nextByte + 1;
        }
        else
        {
            AxisDirection = (0x7f & data[nextByte])<<8 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        res.append("AxisDirections=" + AxisDirection);
        final int primaryAxis = (0x0f & data[nextByte]);
        res.append(" primaryAxis=" + primaryAxis);
        if(0 == (0x10 & data[nextByte]))
        {
            // normal move
        }
        else
        {
            // homing move
            res.append(" homing");
        }
        nextByte++;
        final int nominalSpeed = (0xff & data[nextByte]);
        res.append(" nominalSpeed=" + nominalSpeed);
        nextByte++;
        final int endSpeed = (0xff & data[nextByte]);
        res.append(" endSpeed=" + endSpeed);
        nextByte++;
        int accelerationSteps;
        if(true == twoByteStepCount)
        {
            accelerationSteps = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        else
        {
            accelerationSteps = (0xff & data[nextByte]);
            nextByte ++;
        }
        res.append(" accelSteps=" + accelerationSteps);
        int decelerationSteps;
        if(true == twoByteStepCount)
        {
            decelerationSteps = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        else
        {
            decelerationSteps = (0xff & data[nextByte]);
            nextByte ++;
        }
        res.append(" decelSteps=" + decelerationSteps);
        for(int i = 0; i < 16; i++)
        {
            final int pattern = 0x1<<i;
            if(pattern == (AxisSelection & pattern))
            {
                int StepsOnAxis;
                if(true == twoByteStepCount)
                {
                    StepsOnAxis = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
                    nextByte = nextByte + 2;
                }
                else
                {
                    StepsOnAxis = (0xff & data[nextByte]);
                    nextByte ++;
                }
                res.append("(" + StepsOnAxis + " Steps on Axis " + i);
                if(pattern == (AxisDirection & pattern))
                {
                    res.append(" direction increasing)");
                }
                else
                {
                    res.append(" direction decreasing)");
                }
            }
            // else this axis is not selected
        }
        res.append("]");
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

    public String getDescriptionOfStopped(byte[] stoppedMessage)
    {
        if(2 > stoppedMessage.length)
        {
            return "invalid Message";
        }
        String res = "";
        // recovery Options
        switch(stoppedMessage[0])
        {
        case RECOVERY_CLEARED:
            res = res + "(one time event- cleared)";
            break;

        case RECOVERY_PERSISTS:
            res = res + "(persisting problem)";
            break;

        case RECOVERY_UNRECOVERABLE:
            res = res + "(unrecoverable)";
            break;

        default:
            res = res + "(invalid recovery)";
            break;
        }
        // Cause
        switch(stoppedMessage[1])
        {
        case CAUSE_RESET:
            res = res + "reset";
            break;

        case CAUSE_END_STOP_HIT:
            res = res + "end stop has triggered";
            break;

        case CAUSE_MOVEMENT_ERROR:
            res = res + "movement error";
            break;

        case CAUSE_TEMPERATURE_ERROR:
            res = res + "temperature error";
            break;

        case CAUSE_DEVICE_FAULT:
            res = res + "device fault";
            break;

        case CAUSE_ELECTRICAL_FAULT:
            res = res + "electrical fault";
            break;

        case CAUSE_FIRMWARE_FAULT:
            res = res + "firmware fault";
            break;

        case CAUSE_USER_REQUESTED:
            res = res + "user request";
            break;

        case CAUSE_HOST_TIMEOUT:
            res = res + "host timeout";
            break;

        case CAUSE_OTHER_FAULT:
            res = res + "other cause";
            break;

        default:
            res = res + "invalid cause !";
            break;
        }
        // Reason
        if(2 < stoppedMessage.length)
        {
             res = res + " " + new String(stoppedMessage,
                                    2, (stoppedMessage.length - 2),
                                    Charset.forName("UTF-8"));
        }
        return res;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    @Override
    public String toString()
    {
        return "operational =" + isOperational + " , Client Location = " + cc;
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
        if(null != cc)
        {
            cc.disconnect();
        }
        isOperational = false;
    }

// Information

    public Reply sendInformationRequest(final int which, Reference ref) throws IOException
    {
        final byte[] request = new byte[1];
        request[0] = (byte)(0xff & which);
        Reply res = cc.sendRequest(ORDER_REQ_INFORMATION, request, ref);
        if(null == res)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
        }
        if(false == res.isOKReply())
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
        }
        return res;
    }

    public Reply sendDeviceCountRequest(final int device, Reference ref) throws IOException
    {
        final byte[] request = new byte[1];
        request[0] = (byte)(0xff & device);
        Reply res = cc.sendRequest(ORDER_REQUEST_DEVICE_COUNT, request, ref);
        if(null == res)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
        }
        if(false == res.isOKReply())
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
        }
        return res;
    }

    public Reply sendDeviceNameRequest(final byte type, final int index, Reference ref) throws IOException
    {
        final byte[] request = new byte[2];
        request[0] = type;
        request[1] = (byte)(0xff & index);
        Reply res = cc.sendRequest(ORDER_REQ_DEVICE_NAME, request, ref);
        if(null == res)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
        }
        if(false == res.isOKReply())
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
        }
        return res;
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

    public DeviceInformation getDeviceInformation(Reference ref)
    {
        final DeviceInformation paceMaker = new DeviceInformation();
        try
        {
            if(true == paceMaker.readDeviceInformationFrom(this, ref))
            {
                di = paceMaker;
                hostTimeout = di.getHostTimeoutSeconds();
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

    public int getSwitchState(final int num, Reference ref)
    {
        final byte[] param = new byte[2];
        param[0] = DEVICE_TYPE_INPUT;
        param[1] = (byte) num;
        final int reply = sendOrderExpectUnsignedByte(Protocol.ORDER_REQ_INPUT, param, ref);
        switch(reply)
        {
        case INPUT_HIGH: return Executor.SWITCH_STATE_CLOSED;
        case INPUT_LOW:  return Executor.SWITCH_STATE_OPEN;
        default :        log.error("({}):Get Switch State returned {} !", ref, reply);
                         return Executor.SWITCH_STATE_NOT_AVAILABLE;
        }
    }

// Temperature Sensor - Heater


    public boolean associateTemperatureSensorToHeater(int heater, int sensor, Reference ref)
    {
        final byte[] param = new byte[2];
        param[0] = (byte) heater;
        param[1] = (byte) sensor;
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_HEATER, param, ref);
    }

    public boolean setTemperature(final int heaterNum, Double temperature, Reference ref)
    {
        temperature = temperature * 10; // Client expects Temperature in 0.1 degree units.
        final int tempi = temperature.intValue();
        final byte[] param = new byte[3];
        param[0] = (byte) heaterNum;
        param[1] = (byte)(0xff & (tempi/256));
        param[2] = (byte)(tempi & 0xff);
        return sendOrderExpectOK(Protocol.ORDER_SET_HEATER_TARGET_TEMPERATURE, param, ref);
    }

    public double readTemperatureFrom(int sensorNumber, Reference ref)
    {
        final byte[] param = new byte[2];
        param[0] = DEVICE_TYPE_TEMPERATURE_SENSOR;
        param[1] = (byte) sensorNumber;
        final Reply r = cc.sendRequest(ORDER_REQ_TEMPERATURE, param, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return TEMPERATURE_ERROR_NO_REPLY;
        }
        if(false == r.isOKReply())
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
        }
        if(true == r.isOKReply())
        {
            final byte[] reply = r.getParameter();
            if(2 > reply.length)
            {
                // The return did not have the data
                return TEMPERATURE_ERROR_REPLY_WITHOUT_DATA;
            }
            final int reportedTemp = ((0xff &reply[0]) * 256) + ( 0xff & reply[1]);
            if(SENSOR_PROBLEM == reportedTemp)
            {
                return TEMPERATURE_ERROR_SENSOR_PROBLEM;
            }
            else
            {
                return reportedTemp / (double)10;
            }
        }
        else
        {
            // error -> try again later
            // TODO remove ?
            return TEMPERATURE_ERROR_REPLY_NOT_OK;
        }
    }

// Fans

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 65535 = max
     * @param ref
     */
    public boolean setFanSpeedfor(final int fan, final int speed, Reference ref)
    {
        if((-1 < fan) && (fan < di.getNumberPwmSwitchedOutputs()))
        {
            final byte[] param = new byte[4];
            param[0] = DEVICE_TYPE_PWM_OUTPUT;
            param[1] = (byte)fan;
            param[2] = (byte)(speed/256);
            param[3] = (byte)(0xff & speed);
            if(false == sendOrderExpectOK(ORDER_SET_PWM, param, ref))
            {
                log.error("Falied to set speed on the Fan !");
                lastErrorReason = "Falied to set speed on the Fan !";
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            log.warn("({}): Client does not have the Fan {} ! It has only {} + 1 fans!",
                    ref, fan,  di.getNumberPwmSwitchedOutputs());
            return true;
        }
    }

// Output

    /** sets the state of the Output.
     *
     * @param output specifies the effected output.
     * @param state 0 = low; 1 = high; 2 = disabled / high-Z
     * @param ref
     */
    public boolean setOutputState(final int output, final int state, Reference ref)
    {
        if((-1 < output) && (output < di.getNumberOutputSignals()))
        {
            final byte[] param = new byte[3];
            param[0] = DEVICE_TYPE_OUTPUT;
            param[1] = (byte)output;
            param[2] = (byte)(0xff & state);
            if(false == sendOrderExpectOK(ORDER_SET_OUTPUT, param, ref))
            {
                log.error("({}): Falied to set output state !", ref);
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
            log.warn("({}): Client does not have the output {} ! It has only {} + 1 output!",
                       ref, output,  di.getNumberOutputSignals());
            return true;
        }
    }

// Stepper Motors

    public boolean activateStepperControl(Reference ref)
    {
        final byte[] param = new byte[1];
        param[0] = 0x01;
        return sendOrderExpectOK(Protocol.ORDER_ACTIVATE_STEPPER_CONTROL, param, ref);
    }

    public boolean configureUnderRunAvoidance(int stepperNumber, int maxSpeedStepsPerSecond, int maxAccelleration, Reference ref)
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
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS, param, ref);
    }

    public boolean configureStepperMovementRate(int stepperNumber, int maxSpeedStepsPerSecond, Reference ref)
    {
        final byte[] param = new byte[5];
        param[0] = (byte)(0xff & stepperNumber);
        param[1] = (byte)(0xff & (maxSpeedStepsPerSecond>>24));
        param[2] = (byte)(0xff & (maxSpeedStepsPerSecond>>16));
        param[3] = (byte)(0xff & (maxSpeedStepsPerSecond>>8));
        param[4] = (byte)(0xff & maxSpeedStepsPerSecond);
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_AXIS_MOVEMENT_RATES, param, ref);
    }

    public boolean configureEndStop(Stepper motor, Switch min, Switch max, Reference ref)
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
        return sendOrderExpectOK(Protocol.ORDER_CONFIGURE_END_STOPS, param, ref);
    }

    /** only needed to implement M17
     * @param ref
     *
     */
    public boolean enableAllStepperMotors(Reference ref)
    {
        final int numSteppers = di.getNumberSteppers();
        final byte[] parameter = new byte[2];
        parameter[1] = 0x01; // Enabled
        for(int i = 0; i < numSteppers; i++)
        {
            parameter[0] = (byte)i;
            if(false == sendOrderExpectOK(Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS, parameter, ref))
            {
                log.error("({}): Falied to enable the Steppers !", ref);
                lastErrorReason = "Falied to enable the Steppers !";
                return false;
            }
        }
        return true;
    }

    public boolean disableAllStepperMotors(Reference ref)
    {
        if(false == sendOrderExpectOK((byte)Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS, null, ref))
        {
            log.error("({}): Falied to disable the Steppers !", ref);
            lastErrorReason = "Falied to disable the Steppers !";
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean doStopPrint(Reference ref)
    {
        final byte[] param = new byte[1];
        param[0] = ORDERED_STOP;
        return sendOrderExpectOK(Protocol.ORDER_STOP_PRINT, param, ref);
    }

    public boolean doEmergencyStopPrint(Reference ref)
    {
        final byte[] param = new byte[1];
        param[0] = EMERGENCY_STOP;
        return sendOrderExpectOK(Protocol.ORDER_STOP_PRINT, param, ref);
    }

// Queue handling:

    public int getNumberOfCommandsInClientQueue(Reference ref)
    {
        final long now = System.currentTimeMillis();
        if((now - timeofLastClientQueueUpdate) > QUEUE_TIMEOUT_MS)
        {
            log.trace("polling client for Queue status");
            if(RESULT_ERROR == enqueueCommand(null))
            {
                return -1;
            }
        }
        return ClientQueueNumberOfEnqueuedCommands;
    }

    public boolean hasFreeQueueSlots()
    {
        if(0 + ClientQueueKeepFreeSlots < ClientQueueFreeSlots)
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
            timeofLastClientQueueUpdate = System.currentTimeMillis();
            log.trace("Queue Free Slots = {}", ClientQueueFreeSlots);
            log.trace("Queue Number of Enqueued Commands = {}", ClientQueueNumberOfEnqueuedCommands);
            log.trace("Executed Jobs = {}", ClientExecutedJobs);
        }
    }

    public void ClearQueue(Reference ref)
    {
        final Reply r = cc.sendRequest(ORDER_CLEAR_COMMAND_BLOCK_QUEUE, null, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
        }
        if(true == r.isOKReply())
        {
            parseQueueReply(r.getParameter());
        }
        else
        {
            log.error("Could not clear the Command Queue !");
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
        }
    }

    public boolean addBasicLinearMove(BasicLinearMove[] aMove)
    {
        if(null == aMove)
        {
            return true;
        }

        if(   (false == di.hasExtensionQueuedCommand())
           || (false == di.hasExtensionBasicMove()) )
        {
            lastErrorReason = "no Queue - no chance to add to it.";
            return false;
        }

        for(int i = 0; i < aMove.length; i++)
        {
            log.trace("Sending move {} - {}", aMove[i].getId(), aMove[i]);

            // Send the data
            byte[] data = aMove[i].getMoveData();
            // TODO remove
            log.trace("Sending move data {}", Tool.fromByteBufferToHexString(data));
            if(false == enqueueCommandBlocking(data))
            {
                return false;
            }
        }
        return true;
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
            // no response:
            lastErrorReason = "Received No Reply from Client !";
            log.error(lastErrorReason);
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return RESULT_ERROR;
        }
        if(true == r.isOKReply())
        {
            // OK reply
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
            final byte[] response = r.getParameter();
            // partly Queued
            final int numberOfQueued = (0xff & response[1]);
            if(0 != numberOfQueued)
            {
                log.warn("partly Queued !");
                for(int i = 0; i < numberOfQueued; i++)
                {
                    if(0 < sendQueue.size())
                    {
                        sendQueue.remove(0);
                    }
                }
            }
            parseQueueReply(response, 2);
            if(MOVEMENT_BLOCK_QUEUE_FULL != response[0]) // First Parameter Byte = Cause
            {
                // Error caused by bad Data !
                switch(response[0])
                {
                case MOVEMENT_BLOCK_UNKNOWN_BLOCK:
                    log.error("Unknown or unsupported Block Type");
                    break;

                case MOVEMENT_BLOCK_MALFORMED_BLOCK:
                    log.error("malformed block");
                    break;

                case MOVEMENT_BLOCK_ERROR_IN_BLOCK:
                    log.error("error in Block");
                    break;

                default:
                    log.error("Invalid Cause ({}) !", response[0]);
                    break;
                }
                lastErrorReason = "Could not Queue Block as Client Reports invalid Data !";
                log.error(lastErrorReason);
                log.error("Error Reply Code : " + (0xff & response[8]));
                if(9 < response.length)
                {
                    log.error("Description : " + r.getParameterAsString(9));
                }
                log.error("Send Data: {} !", Tool.fromByteBufferToHexString(param, length));
                log.error("Send Data: {} !", parseQueueBlock(param, length ,0));
                log.error("Received : {} !", Tool.fromByteBufferToHexString(response));
                log.error("Client reports error! Recovery not possible!");
                System.exit(1);
                return RESULT_ERROR;
            }
            // else queue full so try next time
            return RESULT_TRY_AGAIN_LATER;
        }
        else if(RESPONSE_STOPPED == r.getReplyCode())
        {
            //TODO handle stopped - Tell User ? - Auto Resume ?
            final byte[] stoppedMessage = r.getParameter();
            final String stoppedDescription = getDescriptionOfStopped(stoppedMessage);
            lastErrorReason = "Client entered Stopped Mode (" + stoppedDescription + ")!";
            log.error(lastErrorReason);
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
            return RESULT_ERROR;
        }
        else
        {
            // error -> send commands later
            lastErrorReason = "Protocol violation - Unexpected Reply !";
            log.error(lastErrorReason);
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
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
     * @param ref
     * @return RESULT_SUCCESS,  RESULT_ERROR or RESULT_TRY_AGAIN_LATER
     */
    private int enqueueCommand(byte[] param)
    {
        log.trace("putting to sendqueue {}", Tool.fromByteBufferToHexString(param));
        if(null != param)
        {
            // add the new command, and...
            sendQueue.add(param);
        }
        // TODO wait for enough bytes in Buffer ?
        // try to get the Queue empty again.
        if(null == param)
        {
            if((0 == sendQueue.size()) || (false == hasFreeQueueSlots()))
            {
                // nothing to send -> poll client to get number of Slots used
                // _OR_ client has no free slot -> poll client to get number of Slots used
                return sendDataToClientQueue(new byte[0], 0, 0);
            }
        }
        // else we use this call to flush the queue
        if(1 > (ClientQueueFreeSlots - ClientQueueKeepFreeSlots))
        {
            // client queue is full so wait for next slot to become available
            sendDataToClientQueue(new byte[0], 0, 0);
            return RESULT_TRY_AGAIN_LATER;
        }
        else
        {
            final byte[] sendBuffer = new byte[QUEUE_SEND_BUFFER_SIZE];
            int writePos = 0;
            int numBlocksInBuffer = 0;
            int idx = 0;
            for(int i = 0; i < (ClientQueueFreeSlots - ClientQueueKeepFreeSlots); i++)
            {
                // add a block to the send buffer until
                // either send Buffer if full
                // or all commands have been put in the buffer
                // or the number of free slots on the client has been reached
                if(idx < sendQueue.size())
                {
                    final byte[] buf = sendQueue.get(idx);
                    if(null != buf)
                    {
                        // TODO remove
                        log.trace("received from send queue {}", Tool.fromByteBufferToHexString(buf));
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
            do
            {
                try
                {
                    Thread.sleep(QUEUE_POLL_DELAY);
                }
                catch(InterruptedException e)
                {
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

    public boolean writeFirmwareConfigurationValue(String name, String value, Reference ref)
    {
        final byte[] nameBuf = name.getBytes(Charset.forName("UTF-8"));
        final byte[] valueBuf = value.getBytes(Charset.forName("UTF-8"));
        final byte[] parameter = new byte[nameBuf.length + valueBuf.length + 1];
        parameter[0] = (byte)nameBuf.length;
        for(int i = 0; i < nameBuf.length; i++)
        {
            parameter[i+1] = nameBuf[i];
        }
        for(int i = 0; i < valueBuf.length; i++)
        {
            parameter[i+nameBuf.length + 1] = valueBuf[i];
        }
        if(false == sendOrderExpectOK(ORDER_WRITE_FIRMWARE_CONFIGURATION, parameter, ref))
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

    public String getCompleteDescriptionForSetting(String curSetting, Reference ref)
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
        final Reply r = cc.sendRequest(ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES, strbuf, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
            return "";
        }
        final byte[] res = r.getParameter();
        if(null == res)
        {
            return "";
        }
        if(res.length < 4)
        {
            return "";
        }
        final int ElementType = 0xff & res[0];
        final int ElementModes = 0xff & res[1];
        final int DeviceType = 0xff & res[2];
        final int DeviceNumber = 0xff & res[3];
        final StringBuffer description = new StringBuffer();
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

    public String traverseFirmwareConfiguration(String curSetting, Reference ref)
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
        final Reply r = cc.sendRequest(ORDER_TRAVERSE_FIRMWARE_CONFIGURATION_VALUES, strbuf, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
            return "";
        }
        final byte[] res = r.getParameter();
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

    public String readFirmwareConfigurationValue(String curSetting, Reference ref)
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
        final Reply r = cc.sendRequest(ORDER_READ_FIRMWARE_CONFIGURATION, strbuf, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return "";
        }
        if((false == r.isOKReply()) || (false == r.isValid()))
        {
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
            return "";
        }
        final byte[] res = r.getParameter();
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

    private boolean sendOrderExpectOK(final byte order, final byte parameter, Reference ref)
    {
        final byte[] help = new byte[1];
        help[0] = parameter;
        return sendOrderExpectOK(order, help, ref);
    }

    private boolean sendOrderExpectOK(final byte order, final byte[] parameter, Reference ref)
    {
        final Reply r = cc.sendRequest(order, parameter, ref);
        if(null == r)
        {
            log.error("({}): Received no Reply !", ref);
            lastErrorReason = "Received no Reply !";
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return false;
        }
        return r.isOKReply();
    }

    private int sendOrderExpectInt(final byte order, final byte[] parameter, Reference ref)
    {
        final Reply r = cc.sendRequest(order, parameter, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return -1;
        }
        if(false  == r.isOKReply())
        {
            log.error("({}): Reply is not an OK ! " + r, ref);
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
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

    private int sendOrderExpectUnsignedByte(final byte order, final byte[] parameter, Reference ref)
    {
        final Reply r = cc.sendRequest(order, parameter, ref);
        if(null == r)
        {
            log.error("Client does not reply! Recovery not possible!");
            System.exit(1);
            return -1;
        }
        if(false  == r.isOKReply())
        {
            log.error("({}): Reply is not an OK ! " + r, ref);
            log.error("Client reports error! Recovery not possible!");
            System.exit(1);
            return -2;
        }
        final byte[] reply = r.getParameter();
        return (0xff & reply[0]);
    }

    public Reply sendRawOrder(int order, Integer[] parameterBytes, int length)
    {
        return cc.sendRequest(order, parameterBytes, 0, length);
    }

    private void sendKeepAliveSignal()
    {
        final int timeout = sendOrderExpectInt(ORDER_REQ_INFORMATION, new byte[]{INFO_HOST_TIMEOUT}, new Reference("Host Timeout Poll"));
        if(0 < timeout)
        {
            // read a vaild value
            hostTimeout = timeout;
        }
    }

}
