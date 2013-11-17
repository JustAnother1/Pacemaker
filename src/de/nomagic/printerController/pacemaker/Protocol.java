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

/**
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
    public static final byte ORDER_WRITE_FIRMWARE_CONFIGURATION                     = 0x0A;
    public static final byte ORDER_READ_FIRMWARE_CONFIGURATION                      = 0x0B;
    public static final byte ORDER_STOP_PRINT                                       = 0x0C;
    public static final byte ORDER_ACTIVATE_STEPPER_CONTROL                         = 0x0D;
    public static final byte ORDER_ENABLE_DISABLE_STEPPER_MOTORS                    = 0x0E;
    public static final byte ORDER_CONFIGURE_END_STOPS                              = 0x0F;
    public static final byte ORDER_ENABLE_DISABLE_END_STOPS                         = 0x10;
    public static final byte ORDER_REQUEST_DEVICE_COUNT                             = 0x11;
    public static final byte ORDER_QUEUE_COMMAND_BLOCKS                             = 0x12;
    public static final byte ORDER_CONFIGURE_AXIS_MOVEMENT_RATES                    = 0x13;
    public static final byte ORDER_RETRIEVE_EVENTS                                  = 0x14;
    public static final byte ORDER_GET_NUMBER_EVENT_FORMAT_IDS                      = 0x15;
    public static final byte ORDER_GET_EVENT_STRING_FORMAT_ID                       = 0x16;
    public static final byte ORDER_CLEAR_COMMAND_BLOCK_QUEUE                        = 0x17;
    public static final byte ORDER_REQUEST_DEVICE_STATUS                            = 0x18;
    public static final byte ORDER_CONFIGURE_MOVEMENT_UNDERRUN_AVOIDANCE_PARAMETERS = 0x19;
    public static final byte ORDER_GET_FIRMWARE_CONFIGURATION_VALUE_PROPERTIES      = 0x1a;
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
    public static final byte ORDERED_STOP = 0;
    public static final byte EMERGENCY_STOP = 1;
    public static final int DIRECTION_INCREASING = 1;
    public static final int DIRECTION_DECREASING = 0;
    public static final byte MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER = 0x01;
    public static final byte MOVEMENT_BLOCK_TYPE_DELAY = 0x02;
    public static final byte MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD = 0x03;

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
    public static final int RESPONSE_MAX = 0x13;

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
    private int ClientExecutedJobs = 0;
    private int CommandsSendToClient = 0;

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

    public DeviceInformation getDeviceInformation()
    {
        final DeviceInformation paceMaker = new DeviceInformation();
        try
        {
            if(true == paceMaker.readDeviceInformationFrom(this))
            {
                log.info(paceMaker.toString());
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

    public boolean sendOrderExpectOK(final byte order, final byte parameter)
    {
        byte[] help = new byte[1];
        help[0] = parameter;
        return sendOrderExpectOK(order, help);
    }

    public boolean sendOrderExpectOK(final byte order, final byte[] parameter)
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
    /*
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

    public void waitForEndSwitchTriggered(final int axis)
    {
        log.info("Waiting for homing of axis {} !", axis);
        int direction = DIRECTION_DECREASING;
        if(true == axisCfg[axis].isHomingDecreasing())
        {
            direction = DIRECTION_DECREASING;
        }
        else
        {
            direction = DIRECTION_INCREASING;
        }
        boolean isTriggered = isEndSwitchTriggered(axis, direction);
        while(false == isTriggered)
        {
            try
            {
                Thread.sleep(POLLING_TIME_END_SWITCH_MS);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
            isTriggered = isEndSwitchTriggered(axis, direction);
        }
    }
*/
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
            return -3;
        }
        if(true == r.isOKReply())
        {
            byte[] reply = r.getParameter();
            if(2 > reply.length)
            {
                // The return did not have the data
                return -4;
            }
            int reportedTemp = (reply[0] * 256) + reply[1];
            if(SENSOR_PROBLEM == reportedTemp)
            {
                return -1;
            }
            else
            {
                return reportedTemp / 10;
            }
        }
        else
        {
            // error -> try again later
            return -2;
        }
    }

    /** sets the speed of the Fan ( Fan 0 = Fan that cools the printed part).
     *
     * @param fan specifies the effected fan.
     * @param speed 0 = off; 255 = max
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
            log.warn("Client does not have the Fan {} ! It has only {} + 1 fans!", fan,  di.getNumberPwmSwitchedOutputs());
            return true;
        }
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
/*
    public void startHomeOnAxes(final Vector<Integer> listOfHomeAxes)
    {
        if(null == listOfHomeAxes)
        {
            return;
        }
        final byte[] param = new byte[3 * listOfHomeAxes.size()];
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
            byte secondStepNum = (byte)axisCfg[axis].getSecondStepper();
            if(Cfg.INVALID == secondStepNum)
            {
                secondStepNum = (byte) 0xff;
            }

            byte direction = DIRECTION_DECREASING;
            if(true == axisCfg[axis].isHomingDecreasing())
            {
                direction = DIRECTION_DECREASING;
            }
            else
            {
                direction = DIRECTION_INCREASING;
            }
            param[(i*3) +0] = stepNum;
            param[(i*3) +1] = direction;
            param[(i*3) +2] = secondStepNum;
        }
        if(false == sendOrderExpectOK(Protocol.ORDER_HOME_AXES, param))
        {
            log.error("Falied to Home the Axis !");
        }
    }
*/
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

    /** adds a pause to the Queue.
     *
     * @param ticks allowed 0..65535 (0xffff)
     * @return
     */
    public boolean addPauseToQueue(final int ticks)
    {
        if(true == di.hasExtensionQueuedCommand())
        {
            final byte[] param = new byte[3];
            param[0] = MOVEMENT_BLOCK_TYPE_DELAY;
            param[1] = (byte)(0xff & (ticks/256));
            param[2] = (byte)(ticks & 0xff);
            return enqueueCommandBlocking(param);
        }
        else
        {
            lastErrorReason = "no Queue - no chance to add to it.";
            return false;
        }
    }

    /** Enqueues the data for _one_ command into the Queue.
     *
     * CAUTION: All data that can not be send out stays in Memory. So if this
     * function fails repeatedly the memory consumption increases with every
     * call. If in this situation try calling enqueueCommandBlocking() !
     *
     * @param param Data of only one command !
     * @return true = success; false= command could not be put in the queue but
     * will be send with the next command that shall be enqueued.
     */
    private boolean enqueueCommand(byte[] param)
    {
        if(false == sendQueue.isEmpty())
        {
            // add the new command, and...
            sendQueue.add(param);
            // try to get the Queue empty again.
            if(0 == ClientQueueFreeSlots)
            {
                // send only the first command from the command queue
                byte[] firstCommand = sendQueue.get(0);
                final Reply r = cc.sendRequest(ORDER_QUEUE_COMMAND_BLOCKS, firstCommand);
                if(null == r)
                {
                    return false;
                }
                if(true == r.isOKReply())
                {
                    byte[] reply = r.getParameter();
                    ClientQueueFreeSlots = (reply[0] * 256) + reply[1];
                    ClientExecutedJobs = (reply[2] * 256) + reply[3];
                    CommandsSendToClient ++;
                    sendQueue.remove(0);
                    return true;
                }
                else
                {
                    // error -> try again later
                    return false;
                }
            }
            else
            {
                byte[] sendBuffer = new byte[QUEUE_SEND_BUFFER_SIZE];
                int writePos = 0;
                int idx = 0;
                int numBlocksInBuffer = 0;
                for(int i = 0; i < ClientQueueFreeSlots; i++)
                {
                    // add a block to the send buffer until
                    // either send Buffer if full
                    // or all commands have been put in the buffer
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
                // or the loop ends
                // then send them
                final Reply r = cc.sendRequest(ORDER_QUEUE_COMMAND_BLOCKS,
                                               sendBuffer, // data
                                               0, // offset
                                               writePos // length
                                               );
                // and see what happens.
                if(null == r)
                {
                    return false;
                }
                if(true == r.isOKReply())
                {
                    byte[] reply = r.getParameter();
                    ClientQueueFreeSlots = (reply[0] * 256) + reply[1];
                    ClientExecutedJobs = (reply[2] * 256) + reply[3];
                    CommandsSendToClient = CommandsSendToClient + numBlocksInBuffer;
                    for(int i = 0; i < numBlocksInBuffer; i++)
                    {
                        sendQueue.remove(0);
                    }
                    return true;
                }
                else if(RESPONSE_ORDER_SPECIFIC_ERROR == r.getReplyCode())
                {
                    // Order Specific Error
                    byte[] response = r.getParameter();
                    // partly Queued
                    int numberOfQueued = response[1];
                    for(int i = 0; i < numberOfQueued; i++)
                    {
                        sendQueue.remove(0);
                    }
                    ClientQueueFreeSlots = (response[2] * 256) + response[3];
                    ClientExecutedJobs = (response[4] * 256) + response[5];
                    if(0x01 != response[0])
                    {
                        // Error caused by bad Data !
                        log.error("Could not Queue Block as Client Reports invalid Data !");
                        log.error("Send Data: {} !", Tool.fromByteBufferToHexString(sendBuffer, writePos));
                        log.error("Received : {} !", Tool.fromByteBufferToHexString(response));
                        log.error("Can not recover !");
                        System.exit(1);
                        // what else can I do ?
                    }
                    // else queue full so try next time
                    return false;
                }
                else
                {
                    // error -> send commands later
                    return false;
                }
            }
        }
        else
        {
            // Fast lane. Just send it out.
            final Reply r = cc.sendRequest(ORDER_QUEUE_COMMAND_BLOCKS, param);
            if(null == r)
            {
                return false;
            }
            if(true == r.isOKReply())
            {
                byte[] reply = r.getParameter();
                ClientQueueFreeSlots = (reply[0] * 256) + reply[1];
                ClientExecutedJobs = (reply[2] * 256) + reply[3];
                CommandsSendToClient ++;
                return true;
            }
            else
            {
                // error -> send command later
                sendQueue.add(param);
                return false;
            }
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
            log.error("Tried To enque without data !");
            lastErrorReason = "Tried To enque without data !";
            return false;
        }
        if(1 < param.length)
        {
            log.error("Tried To enque with too few bytes !");
            lastErrorReason = "Tried To enque with too few bytes !";
            return false;
        }
        if(false == enqueueCommand(param))
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
                    // TODO report Error
                    return false;
                }
            }while(false == enqueueCommand(null));
        }
        // else sending succeeded !
        return true;
    }

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

    public String getLastErrorReason()
    {
        return lastErrorReason;
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

}
