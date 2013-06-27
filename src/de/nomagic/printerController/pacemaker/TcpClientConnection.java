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
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TCP Communiocation to test protocol. Protocol Layer is UART !
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class TcpClientConnection extends ClientConnection
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Socket pms;
    private byte sequenceNumber = 0;
    private boolean isSynced = false;

    public TcpClientConnection(final Socket pms) throws IOException
    {
        pms.setSoTimeout(0);
        pms.setKeepAlive(true);
        pms.setTcpNoDelay(true);
        this.pms = pms;
        this.in = pms.getInputStream();
        this.out = pms.getOutputStream();
    }

    @Override
    public void close()
    {
        if(null != pms)
        {
            try
            {
                pms.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Reply sendRequest(final byte Order, final byte[] parameter, final boolean cached)
    {
        final byte[] buf = new byte[parameter.length + 5];
        buf[0] = Protocol.START_OF_HOST_FRAME;
        buf[1] = Order;
        buf[2] = (byte)(parameter.length + 1);
        incrementSequenceNumber();
        if(true == cached)
        {
            buf[3] = sequenceNumber;
        }
        else
        {
            buf[3] = (byte)(0x08 | sequenceNumber);
        }
        for(int i = 0; i < parameter.length; i++)
        {
            buf[4 + i] = parameter[i];
        }
        // log.trace("calculating CRC for : " + Tool.fromByteBufferToHexString(buf, 4 + parameter.length));
        buf[4 + parameter.length] = getCRCfor(buf, 4 + parameter.length);
        try
        {
            out.write(buf);
            return getReply();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Reply getReply()
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
        return null;
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

    private void incrementSequenceNumber()
    {
        sequenceNumber ++;
        if(sequenceNumber > 7)
        {
            sequenceNumber = 0;
        }
    }

}
