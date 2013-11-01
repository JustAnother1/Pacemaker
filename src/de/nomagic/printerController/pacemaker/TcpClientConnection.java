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
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TCP Communiocation to test protocol. Protocol Layer is UART !
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class TcpClientConnection extends ClientConnection
{
    private final static Logger log = LoggerFactory.getLogger("TcpClientConnection");
    private final Socket pms;

    public static ClientConnection establishConnectionTo(String data)
    {
        if(false == data.contains(":"))
        {
            log.error("Description({}) has no : !", data);
            return null;
        }
        final String host = data.substring(0, data.indexOf(':'));
        final String portStr = data.substring(data.indexOf(':') + 1);
        final int port = Integer.parseInt(portStr);
        // connecting to Pacemaker using TCP
        log.info("Connecting to Pacemaker at {}:{} !", host, port);
        try
        {
            final Socket pms = new Socket(host, port);
            if(true == pms.isConnected())
            {
                return new TcpClientConnection(pms);
            }
            else
            {
                pms.close();
                log.error("Could not connect !");
                return null;
            }
        }
        catch (final UnknownHostException e)
        {
            log.error("Unknown Host " + host + " !");
            e.printStackTrace();
        }
        catch (final IOException e)
        {
            log.error("IOException !");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "TCP : " + pms.getInetAddress() + ":" + pms.getPort();
    }

    public TcpClientConnection(final Socket pms) throws IOException
    {
        pms.setSoTimeout(0);
        pms.setKeepAlive(true);
        pms.setTcpNoDelay(true);
        this.pms = pms;
        this.in = pms.getInputStream();
        this.out = pms.getOutputStream();
        this.start();
    }

    @Override
    public void close()
    {
        super.close();
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

}
