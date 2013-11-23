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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Tool;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public abstract class ClientConnection extends Thread
{
    public static final int MAX_MS_BETWEEN_TWO_BYTES = 20;
    public static final int MAX_MS_UNTIL_REPLY_ARRIVES = 100;
    public static final int MAX_TRANSMISSIONS = 2; // number of tries to send the frame
    public static final int MAX_TIMEOUT_TRANSMISSIONS = 20; // number of tries to send the frame if the reason was a timeout
    public static final int ORDER_PACKET_ENVELOPE_NUM_BYTES = 5; // Sync, length, Control, Order, CRC = 5
    public static final int RESPONSE_PACKET_ENVELOPE_NUM_BYTES = 3; // Sync, length and CRC are not included in length

    private static final Logger log = LoggerFactory.getLogger("ClientConnection");
    private static final boolean useNonBlocking = true;

    private static byte[] crc_array =
    {
        //       0           1           2           3           4           5           6           7           8           9           A           B           C           D           E           F
    /* 0*/ (byte)0x00, (byte)0xa6, (byte)0xea, (byte)0x4c, (byte)0x72, (byte)0xd4, (byte)0x98, (byte)0x3e, (byte)0xe4, (byte)0x42, (byte)0x0e, (byte)0xa8, (byte)0x96, (byte)0x30, (byte)0x7c, (byte)0xda,
    /* 1*/ (byte)0x6e, (byte)0xc8, (byte)0x84, (byte)0x22, (byte)0x1c, (byte)0xba, (byte)0xf6, (byte)0x50, (byte)0x8a, (byte)0x2c, (byte)0x60, (byte)0xc6, (byte)0xf8, (byte)0x5e, (byte)0x12, (byte)0xb4,
    /* 2*/ (byte)0xdc, (byte)0x7a, (byte)0x36, (byte)0x90, (byte)0xae, (byte)0x08, (byte)0x44, (byte)0xe2, (byte)0x38, (byte)0x9e, (byte)0xd2, (byte)0x74, (byte)0x4a, (byte)0xec, (byte)0xa0, (byte)0x06,
    /* 3*/ (byte)0xb2, (byte)0x14, (byte)0x58, (byte)0xfe, (byte)0xc0, (byte)0x66, (byte)0x2a, (byte)0x8c, (byte)0x56, (byte)0xf0, (byte)0xbc, (byte)0x1a, (byte)0x24, (byte)0x82, (byte)0xce, (byte)0x68,
    /* 4*/ (byte)0x1e, (byte)0xb8, (byte)0xf4, (byte)0x52, (byte)0x6c, (byte)0xca, (byte)0x86, (byte)0x20, (byte)0xfa, (byte)0x5c, (byte)0x10, (byte)0xb6, (byte)0x88, (byte)0x2e, (byte)0x62, (byte)0xc4,
    /* 5*/ (byte)0x70, (byte)0xd6, (byte)0x9a, (byte)0x3c, (byte)0x02, (byte)0xa4, (byte)0xe8, (byte)0x4e, (byte)0x94, (byte)0x32, (byte)0x7e, (byte)0xd8, (byte)0xe6, (byte)0x40, (byte)0x0c, (byte)0xaa,
    /* 6*/ (byte)0xc2, (byte)0x64, (byte)0x28, (byte)0x8e, (byte)0xb0, (byte)0x16, (byte)0x5a, (byte)0xfc, (byte)0x26, (byte)0x80, (byte)0xcc, (byte)0x6a, (byte)0x54, (byte)0xf2, (byte)0xbe, (byte)0x18,
    /* 7*/ (byte)0xac, (byte)0x0a, (byte)0x46, (byte)0xe0, (byte)0xde, (byte)0x78, (byte)0x34, (byte)0x92, (byte)0x48, (byte)0xee, (byte)0xa2, (byte)0x04, (byte)0x3a, (byte)0x9c, (byte)0xd0, (byte)0x76,
    /* 8*/ (byte)0x3c, (byte)0x9a, (byte)0xd6, (byte)0x70, (byte)0x4e, (byte)0xe8, (byte)0xa4, (byte)0x02, (byte)0xd8, (byte)0x7e, (byte)0x32, (byte)0x94, (byte)0xaa, (byte)0x0c, (byte)0x40, (byte)0xe6,
    /* 9*/ (byte)0x52, (byte)0xf4, (byte)0xb8, (byte)0x1e, (byte)0x20, (byte)0x86, (byte)0xca, (byte)0x6c, (byte)0xb6, (byte)0x10, (byte)0x5c, (byte)0xfa, (byte)0xc4, (byte)0x62, (byte)0x2e, (byte)0x88,
    /* A*/ (byte)0xe0, (byte)0x46, (byte)0x0a, (byte)0xac, (byte)0x92, (byte)0x34, (byte)0x78, (byte)0xde, (byte)0x04, (byte)0xa2, (byte)0xee, (byte)0x48, (byte)0x76, (byte)0xd0, (byte)0x9c, (byte)0x3a,
    /* B*/ (byte)0x8e, (byte)0x28, (byte)0x64, (byte)0xc2, (byte)0xfc, (byte)0x5a, (byte)0x16, (byte)0xb0, (byte)0x6a, (byte)0xcc, (byte)0x80, (byte)0x26, (byte)0x18, (byte)0xbe, (byte)0xf2, (byte)0x54,
    /* C*/ (byte)0x22, (byte)0x84, (byte)0xc8, (byte)0x6e, (byte)0x50, (byte)0xf6, (byte)0xba, (byte)0x1c, (byte)0xc6, (byte)0x60, (byte)0x2c, (byte)0x8a, (byte)0xb4, (byte)0x12, (byte)0x5e, (byte)0xf8,
    /* D*/ (byte)0x4c, (byte)0xea, (byte)0xa6, (byte)0x00, (byte)0x3e, (byte)0x98, (byte)0xd4, (byte)0x72, (byte)0xa8, (byte)0x0e, (byte)0x42, (byte)0xe4, (byte)0xda, (byte)0x7c, (byte)0x30, (byte)0x96,
    /* E*/ (byte)0xfe, (byte)0x58, (byte)0x14, (byte)0xb2, (byte)0x8c, (byte)0x2a, (byte)0x66, (byte)0xc0, (byte)0x1a, (byte)0xbc, (byte)0xf0, (byte)0x56, (byte)0x68, (byte)0xce, (byte)0x82, (byte)0x24,
    /* F*/ (byte)0x90, (byte)0x36, (byte)0x7a, (byte)0xdc, (byte)0xe2, (byte)0x44, (byte)0x08, (byte)0xae, (byte)0x74, (byte)0xd2, (byte)0x9e, (byte)0x38, (byte)0x06, (byte)0xa0, (byte)0xec, (byte)0x4a
    };


    protected InputStream in;
    protected OutputStream out;
    protected byte sequenceNumber = 0;
    protected boolean isSynced = false;
    private boolean isFirstOrder = true;
    private BlockingQueue<Reply> receiveQueue = new LinkedBlockingQueue<Reply>();

    private byte[] readBuffer = null;
    private int readPos = 0;
    private int lastPos = 0;
    private int numberOfTimeouts = 0;
    private int numberOfTransmissions = 0;

    public Reply sendRequest(final byte order, final byte[] parameter)
    {
        if(null == parameter)
        {
            return sendRequest(order, parameter, 0, 0);
        }
        else
        {
            return sendRequest(order, parameter, 0, parameter.length);
        }
    }

    public Reply sendRequest(final int Order, final int[] parameter, int offset, int length)
    {
        byte[] para = new byte[length];
        for(int i = 0; i < length; i++)
        {
            para[i] = (byte)(0xff & parameter[offset + i]);
        }
        return sendRequest((byte)(0xff & Order), para, 0, length);
    }

    /** sends a request frame to the client.
     *
     * @param order The Order byte.
     * @param parameter the parameter data. May be null !
     * @param offset parameter starts at this offset in the buffer.
     * @param length send only this many bytes. May be 0 !
     * @return true= success false = no reply received - timeout
     */
    public Reply sendRequest(final byte Order, final byte[] parameter, int offset, int length)
    {
        Reply r = null;
        numberOfTransmissions = 0;
        numberOfTimeouts = 0;
        boolean needsToRetransmitt = false;
        final byte[] buf = getFrameAsBuffer(Order, parameter, offset, length);
        do
        {
            try
            {
                log.trace("Sending Frame: " + Tool.fromByteBufferToHexString(buf));
                out.write(buf);
                numberOfTransmissions++;
                r =  getReply();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
                log.error("Failed to send Request - Exception !");
                return null;
            }
            needsToRetransmitt = retransmissionNeeded(r);
        } while((true == needsToRetransmitt) && (numberOfTransmissions < MAX_TRANSMISSIONS));
        logReply(r);
        return r;
    }

    private void logReply(Reply r)
    {
        if(Protocol.RESPONSE_GENERIC_APPLICATION_ERROR == r.getReplyCode())
        {
            // Do some logging
            byte[] para = r.getParameter();
            String type = "";
            switch(para[0])
            {
            case Protocol.RESPONSE_UNKNOWN_ORDER:          type = "unknown order"; break;
            case Protocol.RESPONSE_BAD_PARAMETER_FORMAT:   type = "bad parameter format"; break;
            case Protocol.RESPONSE_BAD_PARAMETER_VALUE:    type = "bad parameter value"; break;
            case Protocol.RESPONSE_INVALID_DEVICE_TYPE:    type = "wrong device type"; break;
            case Protocol.RESPONSE_INVALID_DEVICE_NUMBER:  type = "wrong device number"; break;
            case Protocol.RESPONSE_INCORRECT_MODE:         type = "wrong mode"; break;
            case Protocol.RESPONSE_BUSY:                   type = "busy"; break;
            case Protocol.RESPONSE_FAILED:                 type = "failed"; break;
            case Protocol.RESPONSE_FIRMWARE_ERROR:         type = "firmware error"; break;
            case Protocol.RESPONSE_CANNOT_ACTIVATE_DEVICE: type = "can not activate device"; break;
            default:                                       type = "" + (0xff & para[0]); break;
            }
            log.error("Generic Application Error : " + type  + " " + r.getParameterAsString(1));
        }
    }

    private byte[] getFrameAsBuffer(byte order, byte[] parameter, int offset, int length)
    {
        final byte[] buf = new byte[length + ORDER_PACKET_ENVELOPE_NUM_BYTES];
        buf[Protocol.ORDER_POS_OF_SYNC] = Protocol.START_OF_HOST_FRAME;
        buf[Protocol.ORDER_POS_OF_LENGTH] = (byte)(length + 2); // length also includes Control and Order
        incrementSequenceNumber();
        // else retransmission due to communications error
        if(false == isFirstOrder)
        {
            buf[Protocol.ORDER_POS_OF_CONTROL] = sequenceNumber;
        }
        else
        {
            // signal the client that host has reset so hat the client flushes all cached responses
            buf[Protocol.ORDER_POS_OF_CONTROL] = (byte)(Protocol.RESET_COMMUNICATION_SYNC_MASK | sequenceNumber);
            isFirstOrder = false;
        }
        buf[Protocol.ORDER_POS_OF_ORDER_CODE] = order;
        for(int i = 0; i < length; i++)
        {
            buf[Protocol.ORDER_POS_OF_START_OF_PARAMETER + i] = parameter[i + offset];
        }
        // Sync is not included in CRC
        buf[Protocol.ORDER_POS_OF_START_OF_PARAMETER + length]
                = getCRCfor(buf, Protocol.ORDER_POS_OF_START_OF_PARAMETER -1 + length, 1);
        return buf;
    }

    private boolean retransmissionNeeded(Reply r)
    {
        if(false == r.isValid())
        {
            if(true == isFirstOrder)
            {
                log.error("Received no response - Timeout!");
                // Timeout
                if(numberOfTimeouts < MAX_TIMEOUT_TRANSMISSIONS)
                {
                    // try again
                    numberOfTransmissions = 0;
                    numberOfTimeouts ++;
                    return true;
                }
                else
                {
                    // give up
                    return false;
                }
            }
            else
            {
                log.error("received invalid Frame ({})!", r);
                return true;
            }
        }
        else
        {
            // Transport error -> Retransmission ?
            if((-1 < r.getReplyCode()) && (Protocol.RESPONSE_OK > r.getReplyCode()))
            {
                // Reply codes as defined in Pacemaker Protocol
                if(Protocol.RESPONSE_FRAME_RECEIPT_ERROR == r.getReplyCode())
                {
                    byte[] para = r.getParameter();
                    switch(para[0])
                    {
                    case Protocol.RESPONSE_BAD_FRAME:
                        log.error("received Bad Frame error Frame !");
                        break;

                    case Protocol.RESPONSE_BAD_ERROR_CHECK_CODE:
                        log.error("received bad CRC error Frame !");
                        break;

                    case Protocol.RESPONSE_UNABLE_TO_ACCEPT_FRAME:
                        log.error("received unable to accept error Frame !");
                        break;

                    default:
                        log.error("received error Frame with invalid parameter !");
                        break;
                    }
                }
                // new error frames would be here with else if()
                else
                {
                    log.error("received invalid error Frame !");
                }
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public static byte getCRCfor(final byte[] buf, final int length)
    {
        return getCRCfor(buf, length -1, 1/* Byte 0 is Sync and is not included in CRC*/);
    }

    public static byte getCRCfor(final byte[] buf, int length, final int offset)
    {
        byte crc = 0;
        int pos = offset;
        while (length > 0)
        {
            crc = crc_array[0xff & (buf[pos] ^ crc)];
            pos = pos + 1;
            length = length - 1;
        }
        return crc;
    }

    @SuppressWarnings("unused")
    protected int getAByte() throws IOException, TimeoutException
    {
        if(false == useNonBlocking)
        {
            final int res =  in.read();
            if(-1 == res)
            {
                throw new IOException("Channel closed");
            }
            log.trace("Received the Byte: " + String.format("%02X", res));
            return res;
        }
        else
        {
            if(null != readBuffer)
            {
                // we have some Bytes already in the in Buffer.
                int res = 0xff & readBuffer[readPos];
                log.trace("Received the Byte: " + String.format("%02X", res));
                readPos++;
                if(readPos > lastPos)
                {
                    readBuffer = null;
                    readPos = 0;
                    lastPos = 0;
                }
                return res;
            }
            else
            {
                int numAvail = in.available();
                if(1 > numAvail)
                {
                    int timeoutCounter = 0;
                    do
                    {
                        try
                        {
                            Thread.sleep(1);
                        }
                        catch(InterruptedException e)
                        {
                        }
                        if(true == isSynced)
                        {
                            timeoutCounter++;
                            if(MAX_MS_BETWEEN_TWO_BYTES < timeoutCounter)
                            {
                                throw new TimeoutException();
                            }
                        }
                        // else pause between two frames can be as long as it wants to be.
                        numAvail = in.available();
                    }while(1 > numAvail);
                }
                // else a byte is already available
                readBuffer = new byte[numAvail];
                lastPos = (in.read(readBuffer)) -1; // Index starts with 0 -> -1
                if(-1 == lastPos)
                {
                    throw new IOException("Channel closed");
                }
                log.trace("Received the Byte: " + String.format("%02X", readBuffer[0]));
                int res = readBuffer[0];
                if(0 == lastPos)
                {
                    // just a single Byte arrived
                    readPos = 0;
                    readBuffer = null;
                }
                else
                {
                    // more than one byte arrived
                    readPos = 1;
                }
                return res;
            }
        }
    }

    protected void incrementSequenceNumber()
    {
        sequenceNumber ++;
        if(sequenceNumber > Protocol.MAX_SEQUENCE_NUMBER)
        {
            sequenceNumber = 0;
        }
    }

    protected Reply getReply()
    {
        Reply r = null;
        r = receiveQueue.poll();
        if(null != r)
        {
            return r;
        }
        int timeoutCounter = 0;
        do
        {
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException e)
            {
            }
            timeoutCounter++;
            r = receiveQueue.poll();
            if((null == r) && (MAX_MS_UNTIL_REPLY_ARRIVES < timeoutCounter))
            {
                log.error("Timeout !");
                isFirstOrder = true;
                return new Reply(null);
            }
        }while(null == r);
        return r;
    }

    @Override
    public void run()
    {
        try
        {
            while(false == isInterrupted())
            {
                // Sync
                int sync;
                do
                {
                    try
                    {
                        sync = getAByte();
                        if((sync != Protocol.START_OF_CLIENT_FRAME) && (true == isSynced))
                        {
                            // Protocol Error
                            log.error("Frame did not start with sync byte !");
                            isSynced = false;
                        }
                        else
                        {
                            if(Protocol.START_OF_CLIENT_FRAME == sync)
                            {
                                isSynced = true;
                            }
                        }
                    }
                    catch(TimeoutException e)
                    {
                        isSynced = false;
                    }
                } while (false == isSynced);
                log.trace("Synced to client!");

                final int replyLength;
                final int control;
                final byte reply;
                final byte[] buf;
                try
                {
                    // Length
                    replyLength = getAByte();
                    if(0 > replyLength)
                    {
                        isSynced = false;
                        continue;
                    }
                    buf = new byte[RESPONSE_PACKET_ENVELOPE_NUM_BYTES + replyLength];
                    buf[Protocol.ORDER_POS_OF_SYNC] = Protocol.START_OF_CLIENT_FRAME;
                    buf[Protocol.REPLY_POS_OF_LENGTH] = (byte)(replyLength & 0xff);

                    // Control
                    control =  (byte)getAByte();
                    // check control later
                    buf[Protocol.REPLY_POS_OF_CONTROL] = (byte)(control & 0xff);

                    // Reply Code
                    reply =  (byte)getAByte();
                    // check reply later
                    buf[Protocol.REPLY_POS_OF_REPLY_CODE] = reply;

                    // Parameter
                    for(int i = 0; i < replyLength-2;i++) // Control and reply code is also in the length
                    {
                        buf[Protocol.REPLY_POS_OF_START_OF_PARAMETER + i] = (byte)getAByte();
                        // log.traceSystem.out.print(" " + i);
                    }

                    // Error Check Code (CRC-8)
                    buf[2 + replyLength] = (byte)getAByte();
                }
                catch(TimeoutException e)
                {
                    isSynced = false;
                    isFirstOrder = true;
                    continue;
                }

                byte expectedCRC = getCRCfor(buf, replyLength + 2);
                if(expectedCRC != buf[2 + replyLength])
                {
                    log.error("Wrong CRC ! expected : " + String.format("%02X", expectedCRC)
                                       + " received : " + String.format("%02X", buf[2 + replyLength]));
                    isSynced = false;
                    receiveQueue.put( new Reply(null));
                    continue;
                }

                if((control & Protocol.SEQUENCE_NUMBER_MASK) != sequenceNumber)
                {
                    // debug frames might not always have the correct sequence number.
                    if(Protocol.DEBUG_FLAG == (Protocol.DEBUG_FLAG & control))
                    {
                        // ok
                    }
                    // if there has been a bit error in the transmission and
                    // the client did not receive the request frame correctly
                    // then it might answer with a wrong reply code, but the
                    // reply will be "bad crc"
                    else if(   (Protocol.RESPONSE_FRAME_RECEIPT_ERROR == reply)
                       && (Protocol.RESPONSE_BAD_ERROR_CHECK_CODE == buf[Protocol.REPLY_POS_OF_START_OF_PARAMETER]) )
                    {
                        // ok
                    }
                    else
                    {
                        // Protocol Error
                        log.error("Wrong Sequence Number !(Received: {}; Expected: {})",
                                                         (control & Protocol.SEQUENCE_NUMBER_MASK), sequenceNumber);
                        isSynced = false;
                        continue;
                    }
                }

                Reply curReply = new Reply(buf);
                if(true == curReply.isDebugFrame())
                {
                    log.info(curReply.toString());
                    if(Protocol.RESPONSE_DEBUG_FRAME_NEW_EVENT == curReply.getReplyCode())
                    {
                        //TODO react to the new event
                    }
                }
                else
                {
                    receiveQueue.put(curReply);
                }
            }
        }
        catch(InterruptedException ie)
        {
            log.info("Has been Interrupted !");
            // end the thread
        }
        catch (final IOException e)
        {
            log.error("Failed to read Reply - Exception !");
            e.printStackTrace();
        }
        log.info("Receive Thread stopped !");
    }

    public void close()
    {
        this.interrupt();
    }

}
