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
package de.nomagic.test.pacemaker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import de.nomagic.printerController.Tool;
import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.pacemaker.Protocol;


/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ProtocolClient
{
    private final byte[] response = new byte[260];
    private byte[] cachedResponse = new byte[260];
    private int cachedResponseLength = 0;
    private int cachedResponseSequenceNumber = -1;
    private final int[] parameter = new int[256];
    private final InputStream in;
    private final OutputStream out;
    private final Hardware hw;

    private int order =  -1;
    private int length = -1;
    private int control = -1;
    private boolean hasEvent = false;
    private boolean StepperControlActive = false;
    private final int totalSlots = 500;
    private int lastWritenSlot = -1;
    private int lastExecutedSlot = -1;
    private final Slot[] orderQueue = new Slot[totalSlots];
    private boolean isConnected = false;
    private boolean inStoppedState = true;
    private boolean stoppedStateAcknowleadged = false;

    public ProtocolClient(final InputStream in, final OutputStream out, final Hardware hw)
    {
        this.in = in;
        this.out = out;
        this.hw = hw;
        response[Protocol.REPLY_POS_OF_SYNC] = Protocol.START_OF_CLIENT_FRAME;
    }

    public boolean isConnected()
    {
        return isConnected;
    }

    private int calculateChecksum(final int order, final int length, final int control, final int[] parameter)
    {
        final byte[] data = new byte[length + 2];
        data[Protocol.ORDER_POS_OF_SYNC] = Protocol.START_OF_HOST_FRAME;
        data[Protocol.ORDER_POS_OF_LENGTH] = (byte)length;
        data[Protocol.ORDER_POS_OF_CONTROL] = (byte)control;
        data[Protocol.ORDER_POS_OF_ORDER_CODE] = (byte)order;
        for(int i = 0; i < length - 2; i++)
        {
            data[i + Protocol.ORDER_POS_OF_START_OF_PARAMETER] = (byte)parameter[i];
        }
        int res =  0xff & ClientConnection.getCRCfor(data, length + Protocol.ORDER_POS_OF_START_OF_PARAMETER - 2);
        System.out.println("calculating CRC for : " + Tool.fromByteBufferToHexString(data) + " -> " + String.format("%02X", res));
        return res;
    }

    public void communicate() throws IOException
    {
        try
        {
            boolean isSynced = false;
            isConnected = true;
            for(;;)
            {
                final int sync = getAByte/*Blocking*/();
                if(sync != Protocol.START_OF_HOST_FRAME)
                {
                    if(false == isSynced)
                    {
                        // drop that byte
                    }
                    else
                    {
                        // bad bytes after frame
                        System.err.println("Received a bad Byte !");
                        isSynced = false;
                    }
                }
                else
                {
                    isSynced = true;
                    // A new frame is coming in...
                    length = getAByte();
                    control = getAByte();
                    order =  getAByte();
                    for(int i = 0; i < length -2; i++)
                    {
                        final int h = getAByte();
                        parameter[i] = h;
                    }
                    final int checksum = 0xff & getAByte();
                    int calculatedCheckSum = calculateChecksum(order, length, control, parameter);
                    if(checksum != calculatedCheckSum)
                    {
                        System.err.println("BAD CRC ! (" +checksum + " - " + calculatedCheckSum + ") !" );
                        sendReply(Protocol.RESPONSE_FRAME_RECEIPT_ERROR, Protocol.RESPONSE_BAD_ERROR_CHECK_CODE);
                    }
                    else
                    {
                        // TODO check Length
                        if(true == inStoppedState)
                        {
                            handleStoppedStateFrame();
                        }
                        else
                        {
                            handleTheFrame();
                        }
                    }
                }
            }
        }
        catch(IOException e)
        {
            isConnected = false;
        }
    }

    private void handleStoppedStateFrame() throws IOException
    {
        if((Protocol.ORDER_RESUME == order) && (parameter[0] != Protocol.QUERY_STOPPED_STATE))
        {
            switch(parameter[0])
            {
            case Protocol.CLEAR_STOPPED_STATE:
                inStoppedState = false;
                sendOK();
                break;

            default:
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_BAD_PARAMETER_VALUE);
                break;
            }
        }
        else
        {
            sendStoppedReply();
        }
    }

    private void handleTheFrame() throws IOException
    {
        if(true == shouldSendCachedResponse(control))
        {
            sendCachedResponse();
        }
        else
        {
            // Execute the Order
            switch(order)
            {
            case Protocol.ORDER_RESET:
                hw.reset();
                sendOK();
                break;

            case Protocol.ORDER_RESUME:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_REQ_INFORMATION:
                handleOrderReqInformation();
                break;

            case Protocol.ORDER_REQ_DEVICE_NAME:
                handleOrderReqDeviceName();
                break;

            case Protocol.ORDER_REQ_TEMPERATURE:
                handleOrderRequestTemperature();
                break;

            case Protocol.ORDER_GET_HEATER_CONFIGURATION:
                handleOrderGetHeaterConfiguration();
                break;

            case Protocol.ORDER_CONFIGURE_HEATER:
                handleOrderConfigureHeater();
                break;

            case Protocol.ORDER_SET_HEATER_TARGET_TEMPERATURE:
                handleOrderSetHeaterTargetTemperature();
                break;

            case Protocol.ORDER_REQ_INPUT:
                handleOrderRequestInput();
                break;

            case Protocol.ORDER_SET_OUTPUT:
                handleOrderSetOutput();
                break;

            case Protocol.ORDER_SET_PWM:
                handleOrderSetPwm();
                break;

            case Protocol.ORDER_WRITE_FIRMWARE_CONFIGURATION:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_READ_FIRMWARE_CONFIGURATION:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_STOP_PRINT:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

                // Stepper control Extension
            case Protocol.ORDER_ACTIVATE_STEPPER_CONTROL:
                handleActivateStepperControl();
                break;

            case Protocol.ORDER_ENABLE_DISABLE_STEPPER_MOTORS:
                handleOrderEnableDisableStepperMotors();
                break;

            case Protocol.ORDER_CONFIGURE_END_STOPS:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_ENABLE_DISABLE_END_STOPS:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

                // Queued Command Extension
            case Protocol.ORDER_QUEUE_COMMAND_BLOCKS:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

                // Basic Move Extension
            case Protocol.ORDER_CONFIGURE_AXIS_MOVEMENT_RATES:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

                // Event Reporting Extension
            case Protocol.ORDER_RETRIEVE_EVENTS:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_GET_NUMBER_EVENT_FORMAT_IDS:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_GET_EVENT_STRING_FORMAT_ID:
                System.err.println("Order not implemented in this state !");
                sendOK();
                break;

            case Protocol.ORDER_REQUEST_DEVICE_COUNT:
                switch(parameter[0])
                {
                case Protocol.DEVICE_TYPE_INPUT: sendByte(hw.getNumberInput());break;
                case Protocol.DEVICE_TYPE_OUTPUT: sendByte(hw.getNumberOutput());break;
                case Protocol.DEVICE_TYPE_PWM_OUTPUT: sendByte(hw.getNumberPwm());break;
                case Protocol.DEVICE_TYPE_STEPPER: sendByte(hw.getNumberSteppers());break;
                case Protocol.DEVICE_TYPE_HEATER: sendByte(hw.getNumberHeaters());break;
                case Protocol.DEVICE_TYPE_TEMPERATURE_SENSOR: sendByte(hw.getNumberTempSensor());break;
                case Protocol.DEVICE_TYPE_BUZZER: sendByte(hw.getNumberBuzzer());break;

                default:
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_BAD_PARAMETER_VALUE);
                    break;
                }
                break;

            // New Orders go here
            default:
                System.err.println("Received Invalid Order Code : " +  order);
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                               Protocol.RESPONSE_UNKNOWN_ORDER);
            }
        }
    }

    private void handleOrderEnableDisableStepperMotors() throws IOException
    {
        if(true == StepperControlActive)
        {
            sendOK();
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INCORRECT_MODE);
        }
    }

    private void handleActivateStepperControl() throws IOException
    {
        if(0 == parameter[0])
        {
            // turn off Stepper control
            if(true == StepperControlActive)
            {
                if(lastWritenSlot == lastExecutedSlot)
                {
                    // Queue is empty -> ok
                }
                else
                {
                    // busy
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_BUSY);
                }
            }
            StepperControlActive = false;
            sendOK();
        }
        else if(1 == parameter[0])
        {
            // activate Stepper control
            if(true ==hw.isAllowedToControlSteppers())
            {
                StepperControlActive = true;
                sendOK();
            }
            else
            {
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_UNKNOWN_ORDER);
            }
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_BAD_PARAMETER_VALUE);
        }
    }

    private void handleOrderSetPwm() throws IOException
    {
        // TODO handle more than one pwm at once
        int devType = parameter[0];
        int devIdx = parameter[1];
        int pwm = (parameter[2] * 256) + parameter[3];
        if(Protocol.DEVICE_TYPE_OUTPUT == devType)
        {
            if((-1 < devIdx) && (devIdx < hw.getNumberPwm()))
            {
                hw.setPwmTo(devIdx, pwm);
                sendOK();
            }
            else
            {
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
            }
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_TYPE);
        }
    }

    private void handleOrderSetOutput() throws IOException
    {
        // TODO handle more than one output at once
        int devType = parameter[0];
        int devIdx = parameter[1];
        int state = parameter[2];
        if(Protocol.DEVICE_TYPE_OUTPUT == devType)
        {
            if((-1 < devIdx) && (devIdx < hw.getNumberOutput()))
            {
                hw.setOutputTo(devIdx, state);
                sendOK();
            }
            else
            {
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
            }
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_TYPE);
        }
    }

    private void handleOrderRequestInput() throws IOException
    {
        // TODO handle more than one switch reading at once
        int devType = parameter[0];
        int devIdx = parameter[1];
        if(Protocol.DEVICE_TYPE_INPUT == devType)
        {
            if((-1 < devIdx) && (devIdx < hw.getNumberInput()))
            {
                int value = hw.getInputValue(devIdx);
                sendByte(value);
            }
            else
            {
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
            }
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_TYPE);
        }
    }

    private void handleOrderSetHeaterTargetTemperature() throws IOException
    {
        int devIdx = parameter[0];
        int targetTemp = parameter[1] * 256 + parameter[2];
        if((-1 < devIdx) &&(devIdx < hw.getNumberHeaters()))
        {
            hw.setTargetTemperatureOfHeater(devIdx, targetTemp);
            sendOK();
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
        }
    }

    private void handleOrderConfigureHeater() throws IOException
    {
        int devIdx = parameter[0];
        int tempSensor = parameter[1];
        if((-1 < devIdx) &&(devIdx < hw.getNumberHeaters()))
        {
            hw.setConfigurationOfHeater(devIdx, tempSensor);
            sendOK();
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
        }
    }

    private void handleOrderGetHeaterConfiguration() throws IOException
    {
        int devIdx = parameter[0];
        if((-1 < devIdx) &&(devIdx < hw.getNumberHeaters()))
        {
            byte[] cfg = hw.getConfigurationOfHeater(devIdx);
            sendByteArray(cfg);
        }
        else
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
        }
    }

    private void handleOrderRequestTemperature() throws IOException
    {
        // TODO handle more than one temperature requested
        int devType = parameter[0];
        int devIdx = parameter[1];
        if(Protocol.DEVICE_TYPE_TEMPERATURE_SENSOR == devType)
        {
            if((-1 < devIdx) &&(devIdx < hw.getNumberTempSensor()))
            {
                int temperature = hw.getTemperatureFromSensor(devIdx);
                sendI16(temperature);
            }
            else
            {
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
            }
        }
        else //TODO Heaters may report Temperatures (included Sensors)
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_INVALID_DEVICE_TYPE);
        }
    }

    private void handleOrderReqDeviceName() throws IOException
    {
        if(4 != length)
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                      Protocol.RESPONSE_BAD_PARAMETER_FORMAT);
        }
        else
        {
            switch(parameter[0])
            {
            case Protocol.DEVICE_TYPE_INPUT:
                if(hw.getNumberInput() > parameter[1])
                {
                    sendString(hw.getNameOfInput(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_OUTPUT:
                if(hw.getNumberOutput() > parameter[1])
                {
                    sendString(hw.getNameOfOutput(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_PWM_OUTPUT:
                if(hw.getNumberPwm() > parameter[1])
                {
                    sendString(hw.getNameOfPwm(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_STEPPER:
                if(hw.getNumberSteppers() > parameter[1])
                {
                    sendString(hw.getNameOfStepper(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_HEATER:
                if(hw.getNumberHeaters() > parameter[1])
                {
                    sendString(hw.getNameOfHeater(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_TEMPERATURE_SENSOR:
                if(hw.getNumberTempSensor() > parameter[1])
                {
                    sendString(hw.getNameOfTemperatureSensor(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            case Protocol.DEVICE_TYPE_BUZZER:
                if(hw.getNumberBuzzer() > parameter[1])
                {
                    sendString(hw.getNameOfBuzzer(parameter[1]));
                }
                else
                {
                    sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                              Protocol.RESPONSE_INVALID_DEVICE_NUMBER);
                }
                break;

            default:
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_INVALID_DEVICE_TYPE);
                break;
            }
        }
    }

    private void handleOrderReqInformation() throws IOException
    {
        if(3 != length)
        {
            sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR, Protocol.RESPONSE_BAD_PARAMETER_FORMAT);
        }
        else
        {
            switch(parameter[0])
            {
            case Protocol.INFO_FIRMWARE_NAME_STRING:sendString(hw.getFirmwareNameString()); break;
            case Protocol.INFO_SERIAL_NUMBER_STRING:sendString(hw.getSerialNumberString()); break;
            case Protocol.INFO_BOARD_NAME_STRING:sendString(hw.getBoardNameString()); break;
            case Protocol.INFO_GIVEN_NAME_STRING:sendString(hw.getGivenNameString()); break;
            case Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MAJOR:sendByte(hw.getProtocolVersionMajor()); break;
            case Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MINOR:sendByte(hw.getProtocolVersionMinor()); break;
            case Protocol.INFO_LIST_OF_SUPPORTED_PROTOCOL_EXTENSIONS:sendByteArray(hw.getListOfSupportedProtocolExtensions()); break;
            case Protocol.INFO_FIRMWARE_TYPE:sendByte(hw.getFirmwareType()); break;
            case Protocol.INFO_FIRMWARE_REVISION_MAJOR:sendByte(hw.getFirmwareRevisionMajor()); break;
            case Protocol.INFO_FIRMWARE_REVISION_MINOR:sendByte(hw.getFirmwareRevisionMinor()); break;
            case Protocol.INFO_HARDWARE_TYPE:sendByte(hw.getHardwareType()); break;
            case Protocol.INFO_HARDWARE_REVISION:sendByte(hw.getHardwareRevision()); break;
            case Protocol.INFO_MAX_STEP_RATE:sendByte(hw.getMaxStepRate()); break;
            case Protocol.INFO_HOST_TIMEOUT:sendByte(hw.getHostTimeout()); break;
            default:
                sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR,
                          Protocol.RESPONSE_BAD_PARAMETER_VALUE);
                break;
            }
        }
    }

    private int getAByte() throws IOException
    {
        final int res =  in.read();
        if(-1 == res)
        {
            throw new IOException("Channel closed");
        }
        return res;
    }

    private void sendCachedResponse() throws IOException
    {
        System.out.println("sending cached Reply !");
        out.write(cachedResponse, 0, cachedResponseLength);
        out.flush();
    }

    private boolean shouldSendCachedResponse(final int receivedControl)
    {
        if(Protocol.RESET_COMMUNICATION_SYNC_MASK == (receivedControl & Protocol.RESET_COMMUNICATION_SYNC_MASK))
        {
            // Re sync Flag set -> no cache
            return false;
        }
        if(cachedResponseSequenceNumber == (receivedControl & Protocol.SEQUENCE_NUMBER_MASK))
        {
            // same sequence number -> cached Result
            return true;
        }
        else
        {
            // different sequence Number -> different result
            return false;
        }
    }

    private void sendStoppedReply() throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_STOPPED;
        response[Protocol.REPLY_POS_OF_LENGTH] = 3 + 2; // length 3 byte parameter
        if(true == stoppedStateAcknowleadged)
        {
            response[Protocol.REPLY_POS_OF_START_OF_PARAMETER] = Protocol.STOPPED_ACKNOWLEADGED;
        }
        else
        {
            response[Protocol.REPLY_POS_OF_START_OF_PARAMETER] = Protocol.STOPPED_UNACKNOWLEADGED;
        }
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 1] = Protocol.RECOVERY_CLEARED;
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 2] = Protocol.CAUSE_RESET;
        addChecksumControlAndSend(Protocol.REPLY_POS_OF_START_OF_PARAMETER + 2);
    }

    private void sendI16(final int parameterInt) throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = 2 + 2; // length 2 byte parameter
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER] = (byte)((parameterInt >>8) & 0xff);
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 1] = (byte)(0xff & parameterInt);
        addChecksumControlAndSend(Protocol.REPLY_POS_OF_START_OF_PARAMETER + 1);
    }

    private void sendByteArray(final byte[] list) throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = (byte) (list.length + 2);
        // 3 = control
        for(int i = 0; i < list.length; i++)
        {
            response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + i] = list[i];
        }
        addChecksumControlAndSend(list.length + Protocol.REPLY_POS_OF_START_OF_PARAMETER - 1);
    }

    private void sendByteArray(final int[] list) throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = (byte)(list.length + 2);
        // 3 = control
        for(int i = 0; i < list.length; i++)
        {
            response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + i] = (byte)list[i];
        }
        addChecksumControlAndSend(list.length + (Protocol.REPLY_POS_OF_START_OF_PARAMETER - 1));
    }

    private void sendByte(final int parameterByte) throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = 1 + 2; // length 1 byte parameter
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER] = (byte)parameterByte;
        addChecksumControlAndSend(Protocol.REPLY_POS_OF_START_OF_PARAMETER);
    }

    private void sendOK() throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = 0 + 2; // length 0 byte parameter
        addChecksumControlAndSend(Protocol.REPLY_POS_OF_START_OF_PARAMETER -1);
    }

    private void sendString(final String theString) throws IOException
    {
        final byte[] str = theString.getBytes(Charset.forName("UTF-8"));
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        response[Protocol.REPLY_POS_OF_LENGTH] = (byte)(str.length + 2);
        // 3 = control
        for(int i = 0; i < str.length; i++)
        {
            response[Protocol.REPLY_POS_OF_START_OF_PARAMETER + i] = str[i];
        }
        addChecksumControlAndSend(str.length + (Protocol.REPLY_POS_OF_START_OF_PARAMETER -1));
    }

    private void sendReply(final byte replyCode, final int parameterByte) throws IOException
    {
        response[Protocol.REPLY_POS_OF_REPLY_CODE] = replyCode;
        response[Protocol.REPLY_POS_OF_LENGTH] = 1 + 2; // length 1 byte parameter
        response[Protocol.REPLY_POS_OF_START_OF_PARAMETER] = (byte)parameterByte;
        addChecksumControlAndSend(Protocol.REPLY_POS_OF_START_OF_PARAMETER);
    }

    private void addChecksumControlAndSend(final int lastUsedIndex) throws IOException
    {
        final int cspos = lastUsedIndex + 1;
        final int bytesToSend = lastUsedIndex + 2;
        if(true == hasEvent)
        {
            response[Protocol.REPLY_POS_OF_CONTROL]
                    = (byte)(Protocol.RESET_COMMUNICATION_SYNC_MASK
                          | (Protocol.SEQUENCE_NUMBER_MASK & control));
        }
        else
        {
            response[Protocol.REPLY_POS_OF_CONTROL] = (byte)(Protocol.SEQUENCE_NUMBER_MASK & control);
        }
        response[cspos] = ClientConnection.getCRCfor(response, cspos);
        System.out.println("sending : " + Tool.fromByteBufferToHexString(response, bytesToSend));
        out.write(response, 0, bytesToSend);
        out.flush();
        cachedResponse = response;
        cachedResponseLength = bytesToSend;
        cachedResponseSequenceNumber = control & Protocol.SEQUENCE_NUMBER_MASK;
    }

}
