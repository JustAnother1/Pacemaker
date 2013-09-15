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
    private final static Logger log = LoggerFactory.getLogger("ClientConnection");

    public final static int MAX_MS_BETWEEN_TWO_BYTES = 100;
    public final static int MAX_TRANSMISSIONS = 4;

    protected InputStream in;
    protected OutputStream out;
    protected byte sequenceNumber = 0;
    protected boolean isSynced = false;
    private boolean isFirstOrder = true;
    private BlockingQueue<Reply> receiveQueue = new LinkedBlockingQueue<Reply>();

    private static byte crc_array[] =
    {
        (byte)0x00, (byte)0xa6, (byte)0xea, (byte)0x4c, (byte)0x72, (byte)0xd4, (byte)0x98, (byte)0x3e,
        (byte)0xe4, (byte)0x42, (byte)0x0e, (byte)0xa8, (byte)0x96, (byte)0x30, (byte)0x7c, (byte)0xda,
        (byte)0x6e, (byte)0xc8, (byte)0x84, (byte)0x22, (byte)0x1c, (byte)0xba, (byte)0xf6, (byte)0x50,
        (byte)0x8a, (byte)0x2c, (byte)0x60, (byte)0xc6, (byte)0xf8, (byte)0x5e, (byte)0x12, (byte)0xb4,
        (byte)0xdc, (byte)0x7a, (byte)0x36, (byte)0x90, (byte)0xae, (byte)0x08, (byte)0x44, (byte)0xe2,
        (byte)0x38, (byte)0x9e, (byte)0xd2, (byte)0x74, (byte)0x4a, (byte)0xec, (byte)0xa0, (byte)0x06,
        (byte)0xb2, (byte)0x14, (byte)0x58, (byte)0xfe, (byte)0xc0, (byte)0x66, (byte)0x2a, (byte)0x8c,
        (byte)0x56, (byte)0xf0, (byte)0xbc, (byte)0x1a, (byte)0x24, (byte)0x82, (byte)0xce, (byte)0x68,
        (byte)0x1e, (byte)0xb8, (byte)0xf4, (byte)0x52, (byte)0x6c, (byte)0xca, (byte)0x86, (byte)0x20,
        (byte)0xfa, (byte)0x5c, (byte)0x10, (byte)0xb6, (byte)0x88, (byte)0x2e, (byte)0x62, (byte)0xc4,
        (byte)0x70, (byte)0xd6, (byte)0x9a, (byte)0x3c, (byte)0x02, (byte)0xa4, (byte)0xe8, (byte)0x4e,
        (byte)0x94, (byte)0x32, (byte)0x7e, (byte)0xd8, (byte)0xe6, (byte)0x40, (byte)0x0c, (byte)0xaa,
        (byte)0xc2, (byte)0x64, (byte)0x28, (byte)0x8e, (byte)0xb0, (byte)0x16, (byte)0x5a, (byte)0xfc,
        (byte)0x26, (byte)0x80, (byte)0xcc, (byte)0x6a, (byte)0x54, (byte)0xf2, (byte)0xbe, (byte)0x18,
        (byte)0xac, (byte)0x0a, (byte)0x46, (byte)0xe0, (byte)0xde, (byte)0x78, (byte)0x34, (byte)0x92,
        (byte)0x48, (byte)0xee, (byte)0xa2, (byte)0x04, (byte)0x3a, (byte)0x9c, (byte)0xd0, (byte)0x76,
        (byte)0x3c, (byte)0x9a, (byte)0xd6, (byte)0x70, (byte)0x4e, (byte)0xe8, (byte)0xa4, (byte)0x02,
        (byte)0xd8, (byte)0x7e, (byte)0x32, (byte)0x94, (byte)0xaa, (byte)0x0c, (byte)0x40, (byte)0xe6,
        (byte)0x52, (byte)0xf4, (byte)0xb8, (byte)0x1e, (byte)0x20, (byte)0x86, (byte)0xca, (byte)0x6c,
        (byte)0xb6, (byte)0x10, (byte)0x5c, (byte)0xfa, (byte)0xc4, (byte)0x62, (byte)0x2e, (byte)0x88,
        (byte)0xe0, (byte)0x46, (byte)0x0a, (byte)0xac, (byte)0x92, (byte)0x34, (byte)0x78, (byte)0xde,
        (byte)0x04, (byte)0xa2, (byte)0xee, (byte)0x48, (byte)0x76, (byte)0xd0, (byte)0x9c, (byte)0x3a,
        (byte)0x8e, (byte)0x28, (byte)0x64, (byte)0xc2, (byte)0xfc, (byte)0x5a, (byte)0x16, (byte)0xb0,
        (byte)0x6a, (byte)0xcc, (byte)0x80, (byte)0x26, (byte)0x18, (byte)0xbe, (byte)0xf2, (byte)0x54,
        (byte)0x22, (byte)0x84, (byte)0xc8, (byte)0x6e, (byte)0x50, (byte)0xf6, (byte)0xba, (byte)0x1c,
        (byte)0xc6, (byte)0x60, (byte)0x2c, (byte)0x8a, (byte)0xb4, (byte)0x12, (byte)0x5e, (byte)0xf8,
        (byte)0x4c, (byte)0xea, (byte)0xa6, (byte)0x00, (byte)0x3e, (byte)0x98, (byte)0xd4, (byte)0x72,
        (byte)0xa8, (byte)0x0e, (byte)0x42, (byte)0xe4, (byte)0xda, (byte)0x7c, (byte)0x30, (byte)0x96,
        (byte)0xfe, (byte)0x58, (byte)0x14, (byte)0xb2, (byte)0x8c, (byte)0x2a, (byte)0x66, (byte)0xc0,
        (byte)0x1a, (byte)0xbc, (byte)0xf0, (byte)0x56, (byte)0x68, (byte)0xce, (byte)0x82, (byte)0x24,
        (byte)0x90, (byte)0x36, (byte)0x7a, (byte)0xdc, (byte)0xe2, (byte)0x44, (byte)0x08, (byte)0xae,
        (byte)0x74, (byte)0xd2, (byte)0x9e, (byte)0x38, (byte)0x06, (byte)0xa0, (byte)0xec, (byte)0x4a
    };

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
        int numberOfTransmissions = 0;
        boolean needsToRetransmitt = false;
        do
        {
            final byte[] buf = new byte[length + 5];
            buf[0] = Protocol.START_OF_HOST_FRAME;
            buf[1] = Order;
            if(false == needsToRetransmitt)
            {
                incrementSequenceNumber();
            }
            // else retransmission due to communications error
            if(false == isFirstOrder)
            {
                buf[2] = sequenceNumber;
            }
            else
            {
                buf[2] = (byte)(0x08 | sequenceNumber);
                isFirstOrder = false;
            }
            buf[3] = (byte)(length);
            for(int i = 0; i < length; i++)
            {
                buf[4 + i] = parameter[i + offset];
            }
            // log.trace("calculating CRC for : " + Tool.fromByteBufferToHexString(buf, 4 + length));
            buf[4 + length] = getCRCfor(buf, 4 + length);
            try
            {
                log.info("Sending Frame: " + Tool.fromByteBufferToHexString(buf));
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
            if(null == r)
            {
                needsToRetransmitt = true;
            }
            else
            {
                // Transport error -> Retransmission ?
                if((-1 < r.getReplyCode()) && (0x10 > r.getReplyCode()))
                {
                    needsToRetransmitt = true;
                }
                else
                {
                    needsToRetransmitt = false;
                }
            }
        } while((true == needsToRetransmitt) && (numberOfTransmissions < MAX_TRANSMISSIONS));
        return r;
    }

    public static byte getCRCfor(final byte[] buf)
    {
        return getCRCfor(buf, buf.length, 1/* Byte 0 is Sync and is not included in CRC*/);
    }

    public static byte getCRCfor(final byte[] buf, final int length)
    {
        return getCRCfor(buf, length, 1/* Byte 0 is Sync and is not included in CRC*/);
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

    protected int getAByte() throws IOException
    {
        final int res =  in.read();
        if(-1 == res)
        {
            throw new IOException("Channel closed");
        }
        log.info("Received the Byte: " + String.format("%02X", res));
        return res;
    }
    /* non blocking:
    protected int getAByte() throws IOException
    {
        if(1 > in.available())
        {
            int timeoutCounter = 0;
            do{
                try
                {
                    Thread.sleep(1);
                }
                catch(InterruptedException e)
                {
                }
                timeoutCounter++;
                if(MAX_MS_BETWEEN_TWO_BYTES < timeoutCounter)
                {
                    log.error("Timeout !");
                    throw new TimeoutException();
                }
            }while(1 > in.available());
        }
        // else a byte is already available
        final int res =  in.read();
        if(-1 == res)
        {
            throw new IOException("Channel closed");
        }
        return res;
    }
    */

    protected void incrementSequenceNumber()
    {
        sequenceNumber ++;
        if(sequenceNumber > 7)
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
        do{
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException e)
            {
            }
            timeoutCounter++;
            r = receiveQueue.poll();
            if((null == r) && (MAX_MS_BETWEEN_TWO_BYTES < timeoutCounter))
            {
                log.error("Timeout !");
                // throw new TimeoutException();
                return r;
            }
        }while(null == r);
        return r;
    }

    public void run()
    {
        try
        {
            while(false == isInterrupted())
            {
                try
                {
                    // Sync
                    do{
                        final int sync = getAByte();
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
                    } while (false == isSynced);

                    // Reply Code
                    final byte reply =  (byte)getAByte();
                    if(Protocol.RESPONSE_MAX < reply)
                    {
                        // Protocol Error
                        log.error("Invalid reply code !");
                        continue;
                    }

                    // Control
                    final int control =  (byte)getAByte();
                    if(control != sequenceNumber)
                    {
                        // Protocol Error
                        log.error("Wrong Sequence Number !(Received: {}; Expected: {})", control, sequenceNumber);
                        continue;
                    }

                    // Length
                    final int replyLength = getAByte();
                    final byte[] buf = new byte[5 + replyLength];
                    buf[0] = Protocol.START_OF_CLIENT_FRAME;
                    buf[1] = reply;
                    buf[2] = (byte)(control & 0xff);
                    buf[3] = (byte)(replyLength & 0xff);

                    // Parameter
                    for(int i = 0; i < replyLength;i++)
                    {
                        buf[4 + i] = (byte)getAByte();
                        // log.traceSystem.out.print(" " + i);
                    }

                    // Error Check Code (CRC-8)
                    buf[4 + replyLength] = (byte)getAByte();
                    if(getCRCfor(buf, replyLength + 4) != buf[4 + replyLength])
                    {
                        log.error("Wrong CRC ! expected : " + String.format("%02X", getCRCfor(buf, replyLength + 4))
                                           + " received : " + String.format("%02X", buf[4 + replyLength]));
                        continue;
                    }
                    Reply curReply = new Reply(buf);
                    if(true == curReply.isDebugFrame())
                    {
                        log.info(curReply.toString());
                    }
                    else
                    {
                        receiveQueue.put(curReply);
                    }
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
                log.error("Failed to read Reply - Exception !");
                return;
            }
        }
        catch(InterruptedException ie)
        {
            log.info("Has been Interrupted !");
            // end the thread
        }
    }

    public void close()
    {
    }

}
