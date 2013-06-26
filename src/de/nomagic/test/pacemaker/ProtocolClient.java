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
    private final boolean hasEvent = false;
    private final int totalSlots = 500;
    private final int lastWritenSlot = -1;
    private final int lastExecutedSlot = -1;
    private final Slot[] orderQueue = new Slot[totalSlots];

    public ProtocolClient(final InputStream in, final OutputStream out, final Hardware hw)
    {
        this.in = in;
        this.out = out;
        this.hw = hw;
        response[0] = Protocol.START_OF_CLIENT_FRAME;
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

    public void communicate() throws IOException
    {
        boolean isSynced = false;
        for(;;)
        {
            final int sync = getAByte/*Blocking*/();
            // System.out.println("Frame Incomming,...");
            if(sync != Protocol.START_OF_HOST_FRAME)
            {
                if(false == isSynced)
                {
                    // drop that byte
                }
                else
                {
                    // bad bytes after frame
                    System.err.println("Received a Byd Byte !");
                    isSynced = false;
                }
            }
            else
            {
                isSynced = true;
                // A new frame is coming in...
                order =  getAByte();
                length = getAByte();
                control = getAByte();
                for(int i = 0; i < (length - 1); i++) // length includes control
                {
                    final int h = getAByte();
                    parameter[i] = h;
                }
                final int checksum = getAByte();
                if(checksum != calculateChecksum(order, length, control, parameter))
                {
                    System.err.println("BAD CRC ! (" +checksum + " - " + calculateChecksum(order, length, control, parameter) + ") !" );
                    sendReply(Protocol.RESPONSE_FRAME_RECEIPT_ERROR, Protocol.RESPONSE_BAD_ERROR_CHECK_CODE);
                }
                else
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
                        case Protocol.ORDER_REQ_INFORMATION:
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
                            case Protocol.INFO_NUMBER_STEPPERS:sendByte(hw.getNumberSteppers()); break;
                            case Protocol.INFO_NUMBER_HEATERS:sendByte(hw.getNumberHeaters()); break;
                            case Protocol.INFO_NUMBER_PWM:sendByte(hw.getNumberPwm()); break;
                            case Protocol.INFO_NUMBER_TEMP_SENSOR:sendByte(hw.getNumberTempSensor()); break;
                            case Protocol.INFO_NUMBER_INPUT:sendByte(hw.getNumberInput()); break;
                            case Protocol.INFO_NUMBER_OUTPUT:sendByte(hw.getNumberOutput()); break;
                            case Protocol.INFO_NUMBER_BUZZER:sendByte(hw.getNumberBuzzer()); break;
                            case Protocol.INFO_QUEUE_TOTAL_SLOTS:sendI16(totalSlots); break;
                            case Protocol.INFO_QUEUE_USED_SLOTS:
                                int usedSlots = -1;
                                if(lastWritenSlot >= lastExecutedSlot)
                                {
                                    usedSlots = lastWritenSlot  - lastExecutedSlot;
                                }
                                else
                                {
                                    usedSlots = (lastWritenSlot + totalSlots) - lastExecutedSlot;
                                }
                                sendI16(usedSlots);
                                break;
                            default: sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR, Protocol.RESPONSE_BAD_PARAMETER_VALUE); break;
                            }
                            break;
                        // New Orders go here
                        default: sendReply(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR, Protocol.RESPONSE_UNKNOWN_ORDER);
                        }
                    }
                }
            }

        }
    }

    private void sendCachedResponse() throws IOException
    {
    	System.out.println("sending cached Reply !");
        out.write(cachedResponse, 0, cachedResponseLength);
        out.flush();
    }

    private boolean shouldSendCachedResponse(final int receivedControl)
    {
        if(0x10 == (receivedControl & 0x10))
        {
            // Re sync Flag set -> no cache
            return false;
        }
        if(cachedResponseSequenceNumber == (receivedControl & 0x0f))
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

    private void sendI16(final int parameterInt) throws IOException
    {
        response[1] = Protocol.RESPONSE_OK;
        response[2] = 3; // length 2 byte parameter
        response[4] = (byte)((parameterInt >>8) & 0xff);
        response[5] = (byte)(0xff & parameterInt);
        addChecksumControlAndSend(5);
    }

    private void sendByteArray(final int[] list) throws IOException
    {
        response[1] = Protocol.RESPONSE_OK;
        response[2] = (byte)(list.length + 1);
        // 3 = control
        for(int i = 0; i < list.length; i++)
        {
            response[4 + i] = (byte)list[i];
        }
        addChecksumControlAndSend(list.length + 3);
    }

    private void sendByte(final int parameterByte) throws IOException
    {
        response[1] = Protocol.RESPONSE_OK;
        response[2] = 2; // length 1 byte parameter
        response[4] = (byte)parameterByte;
        addChecksumControlAndSend(4);
    }

    private void sendString(final String theString) throws IOException
    {
        final byte[] str = theString.getBytes(Charset.forName("UTF-8"));
        response[1] = Protocol.RESPONSE_OK;
        response[2] = (byte)(str.length + 1);
        // 3 = control
        for(int i = 0; i < str.length; i++)
        {
            response[4 + i] = str[i];
        }
        addChecksumControlAndSend(str.length + 3);
    }

    private void sendReply(final byte replyCode, final int parameterByte) throws IOException
    {
        response[1] = replyCode;
        response[2] = 2; // length 1 byte parameter
        response[4] = (byte)parameterByte;
        addChecksumControlAndSend(4);
    }

    private void addChecksumControlAndSend(final int lastUsedIndex) throws IOException
    {
        final int cspos = lastUsedIndex + 1;
        final int bytesToSend = lastUsedIndex + 2;
        if(true == hasEvent)
        {
            response[3] = (byte)(0x10 | (0x0f & control));
        }
        else
        {
            response[3] = (byte)(0x0f & control);
        }
        response[cspos] = ClientConnection.getCRCfor(response, cspos);
        System.out.println("sending : " + Tool.fromByteBufferToHexString(response, bytesToSend));
        out.write(response, 0, bytesToSend);
        out.flush();
        cachedResponse = response;
        cachedResponseLength = bytesToSend;
        cachedResponseSequenceNumber = control & 0x0f;
    }

    private int calculateChecksum(final int order, final int length, final int control, final int[] parameter)
    {
        final byte[] data = new byte[length + 3];
        data[0] = Protocol.START_OF_HOST_FRAME;
        data[1] = (byte)order;
        data[2] = (byte)length;
        data[3] = (byte)control;
        for(int i = 0; i < (length - 1); i++)
        {
            data[i + 4] = (byte)parameter[i];
        }
        System.out.println("calculating CRC for : " + Tool.fromByteBufferToHexString(data));
        return ClientConnection.getCRCfor(data);
    }

}
