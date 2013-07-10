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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public abstract class ClientConnection
{
    private final static Logger log = LoggerFactory.getLogger("ClientConnection");

    public final static int MAX_MS_BETWEEN_TWO_BYTES = 100;

    protected InputStream in;
    protected OutputStream out;
    protected byte sequenceNumber = 0;
    protected boolean isSynced = false;

    private static byte crc_array[] =
    {
        (byte)0x00, (byte)0x07, (byte)0x0E, (byte)0x09,
        (byte)0x1C, (byte)0x1B, (byte)0x12, (byte)0x15,
        (byte)0x38, (byte)0x3F, (byte)0x36, (byte)0x31,
        (byte)0x24, (byte)0x23, (byte)0x2A, (byte)0x2D,
        (byte)0x70, (byte)0x77, (byte)0x7E, (byte)0x79,
        (byte)0x6C, (byte)0x6B, (byte)0x62, (byte)0x65,
        (byte)0x48, (byte)0x4F, (byte)0x46, (byte)0x41,
        (byte)0x54, (byte)0x53, (byte)0x5A, (byte)0x5D,
        (byte)0xE0, (byte)0xE7, (byte)0xEE, (byte)0xE9,
        (byte)0xFC, (byte)0xFB, (byte)0xF2, (byte)0xF5,
        (byte)0xD8, (byte)0xDF, (byte)0xD6, (byte)0xD1,
        (byte)0xC4, (byte)0xC3, (byte)0xCA, (byte)0xCD,
        (byte)0x90, (byte)0x97, (byte)0x9E, (byte)0x99,
        (byte)0x8C, (byte)0x8B, (byte)0x82, (byte)0x85,
        (byte)0xA8, (byte)0xAF, (byte)0xA6, (byte)0xA1,
        (byte)0xB4, (byte)0xB3, (byte)0xBA, (byte)0xBD,
        (byte)0xC7, (byte)0xC0, (byte)0xC9, (byte)0xCE,
        (byte)0xDB, (byte)0xDC, (byte)0xD5, (byte)0xD2,
        (byte)0xFF, (byte)0xF8, (byte)0xF1, (byte)0xF6,
        (byte)0xE3, (byte)0xE4, (byte)0xED, (byte)0xEA,
        (byte)0xB7, (byte)0xB0, (byte)0xB9, (byte)0xBE,
        (byte)0xAB, (byte)0xAC, (byte)0xA5, (byte)0xA2,
        (byte)0x8F, (byte)0x88, (byte)0x81, (byte)0x86,
        (byte)0x93, (byte)0x94, (byte)0x9D, (byte)0x9A,
        (byte)0x27, (byte)0x20, (byte)0x29, (byte)0x2E,
        (byte)0x3B, (byte)0x3C, (byte)0x35, (byte)0x32,
        (byte)0x1F, (byte)0x18, (byte)0x11, (byte)0x16,
        (byte)0x03, (byte)0x04, (byte)0x0D, (byte)0x0A,
        (byte)0x57, (byte)0x50, (byte)0x59, (byte)0x5E,
        (byte)0x4B, (byte)0x4C, (byte)0x45, (byte)0x42,
        (byte)0x6F, (byte)0x68, (byte)0x61, (byte)0x66,
        (byte)0x73, (byte)0x74, (byte)0x7D, (byte)0x7A,
        (byte)0x89, (byte)0x8E, (byte)0x87, (byte)0x80,
        (byte)0x95, (byte)0x92, (byte)0x9B, (byte)0x9C,
        (byte)0xB1, (byte)0xB6, (byte)0xBF, (byte)0xB8,
        (byte)0xAD, (byte)0xAA, (byte)0xA3, (byte)0xA4,
        (byte)0xF9, (byte)0xFE, (byte)0xF7, (byte)0xF0,
        (byte)0xE5, (byte)0xE2, (byte)0xEB, (byte)0xEC,
        (byte)0xC1, (byte)0xC6, (byte)0xCF, (byte)0xC8,
        (byte)0xDD, (byte)0xDA, (byte)0xD3, (byte)0xD4,
        (byte)0x69, (byte)0x6E, (byte)0x67, (byte)0x60,
        (byte)0x75, (byte)0x72, (byte)0x7B, (byte)0x7C,
        (byte)0x51, (byte)0x56, (byte)0x5F, (byte)0x58,
        (byte)0x4D, (byte)0x4A, (byte)0x43, (byte)0x44,
        (byte)0x19, (byte)0x1E, (byte)0x17, (byte)0x10,
        (byte)0x05, (byte)0x02, (byte)0x0B, (byte)0x0C,
        (byte)0x21, (byte)0x26, (byte)0x2F, (byte)0x28,
        (byte)0x3D, (byte)0x3A, (byte)0x33, (byte)0x34,
        (byte)0x4E, (byte)0x49, (byte)0x40, (byte)0x47,
        (byte)0x52, (byte)0x55, (byte)0x5C, (byte)0x5B,
        (byte)0x76, (byte)0x71, (byte)0x78, (byte)0x7F,
        (byte)0x6A, (byte)0x6D, (byte)0x64, (byte)0x63,
        (byte)0x3E, (byte)0x39, (byte)0x30, (byte)0x37,
        (byte)0x22, (byte)0x25, (byte)0x2C, (byte)0x2B,
        (byte)0x06, (byte)0x01, (byte)0x08, (byte)0x0F,
        (byte)0x1A, (byte)0x1D, (byte)0x14, (byte)0x13,
        (byte)0xAE, (byte)0xA9, (byte)0xA0, (byte)0xA7,
        (byte)0xB2, (byte)0xB5, (byte)0xBC, (byte)0xBB,
        (byte)0x96, (byte)0x91, (byte)0x98, (byte)0x9F,
        (byte)0x8A, (byte)0x8D, (byte)0x84, (byte)0x83,
        (byte)0xDE, (byte)0xD9, (byte)0xD0, (byte)0xD7,
        (byte)0xC2, (byte)0xC5, (byte)0xCC, (byte)0xCB,
        (byte)0xE6, (byte)0xE1, (byte)0xE8, (byte)0xEF,
        (byte)0xFA, (byte)0xFD, (byte)0xF4, (byte)0xF3
    };

    public Reply sendRequest(final byte order, final byte[] parameter)
    {
        if(null == parameter)
        {
            return sendRequest(order, parameter, 0, 0, true);
        }
        else
        {
            return sendRequest(order, parameter, 0, parameter.length, true);
        }
    }

    /** sends a request frame to the client.
     *
     * @param order The Order byte.
     * @param parameter the parameter data. May be null !
     * @param offset parameter starts at this offset in the buffer.
     * @param length send only this many bytes. May be 0 !
     * @param cached true= client may send cached result; false= client must execute the order. no cached reply.
     * @return true= success false = no reply received - timeout
     */
    public Reply sendRequest(final byte Order, final byte[] parameter, int offset, int length, final boolean cached)
    {
        final byte[] buf = new byte[length + 5];
        buf[0] = Protocol.START_OF_HOST_FRAME;
        buf[1] = Order;
        buf[2] = (byte)(length + 1);
        incrementSequenceNumber();
        if(true == cached)
        {
            buf[3] = sequenceNumber;
        }
        else
        {
            buf[3] = (byte)(0x08 | sequenceNumber);
        }
        for(int i = 0; i < length; i++)
        {
            buf[4 + i] = parameter[i + offset];
        }
        // log.trace("calculating CRC for : " + Tool.fromByteBufferToHexString(buf, 4 + length));
        buf[4 + length] = getCRCfor(buf, 4 + length);
        try
        {
            out.write(buf);
            return getReply();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        log.error("Failed to send Request - Exception !");
        return null;
    }

    public static byte getCRCfor(final byte[] buf)
    {
        return getCRCfor(buf, buf.length, 0);
    }
    public static byte getCRCfor(final byte[] buf, final int length)
    {
        return getCRCfor(buf, length, 0);
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
                return null;
            }
            if(Protocol.START_OF_CLIENT_FRAME == sync)
            {
                isSynced = true;
            }
            } while (false == isSynced);
            // log.traceSystem.out.print("Sync-");

            // Reply Code
            final byte reply =  (byte)getAByte();
            if(Protocol.RESPONSE_MAX < reply)
            {
                // Protocol Error
                log.error("Invalid reply code !");
                return null;
            }
            // log.traceSystem.out.print("Order-");

            // Length
            final int replyLength = getAByte();
            if(1 > replyLength)
            {
                // Protocol Error
                log.error("Invalid length !");
                return null;
            }
            // log.traceSystem.out.print("Length(" + replyLength + ")-");

            final byte[] buf = new byte[4 + replyLength];
            buf[0] = Protocol.START_OF_CLIENT_FRAME;
            buf[1] = reply;
            buf[2] = (byte)(replyLength & 0xff);

            // Control
            buf[3] =  (byte)getAByte();
            if(buf[3] != sequenceNumber)
            {
                // Protocol Error
                log.error("Wrong Sequence Number !");
                return null;
            }
            // log.traceSystem.out.println("Control-");

            // Parameter
            for(int i = 0; i < (replyLength - 1);i++)
            {
                buf[4 + i] = (byte)getAByte();
                // log.traceSystem.out.print(" " + i);
            }
            // log.traceSystem.out.print("Parameter bytes-");

            // Error Check Code (CRC-8)
            buf[3 + replyLength] = (byte)getAByte();
            if(getCRCfor(buf, replyLength + 3) != buf[3 + replyLength])
            {
                // TODO Retransmit
                log.error("Wrong CRC !");
                return null;
            }
            // log.traceSystem.out.println("CRC !");
            return new Reply(buf);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        log.error("Failed to read Reply - Exception !");
        return null;
    }

    public void close()
    {
    }

}
