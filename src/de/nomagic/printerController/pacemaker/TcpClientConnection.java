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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TCP Communication to test protocol. Protocol Layer is UART !
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class TcpClientConnection extends ClientConnectionBase
{
    private static final Logger log = LoggerFactory.getLogger("TcpClientConnection");
    private final Socket pms;
    private final String host;
    private final String portStr;

    public static TcpClientConnection establishConnectionTo(String data)
    {
        TcpClientConnection res = new TcpClientConnection(data);
        res.connect();
        return res;
    }

    public TcpClientConnection(String data)
    {
        super("TcpClientConnection");
        if(false == data.contains(":"))
        {
            log.error("Description({}) has no \":\" !", data);
            host = "";
            portStr = "";
            pms = null;
            return;
        }
        host = data.substring(0, data.indexOf(':'));
        portStr = data.substring(data.indexOf(':') + 1);
        pms = new Socket();
    }

    public boolean connect()
    {
        if(1 > portStr.length())
        {
            return false;
        }
        final int port = Integer.parseInt(portStr);
        // connecting to Pacemaker using TCP
        log.info("Connecting to Pacemaker at {}:{} !", host, port);
        try
        {
            pms.connect(new InetSocketAddress(host, port));
            if(true == pms.isConnected())
            {
                pms.setSoTimeout(0);
                pms.setKeepAlive(true);
                pms.setTcpNoDelay(true);
                this.in = pms.getInputStream();
                this.out = pms.getOutputStream();
                this.start();
                return true;
            }
            else
            {
                pms.close();
                log.error("Could not connect !");
                return false;
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
        return false;
    }

    @Override
    public String toString()
    {
        String cn = getConnectionName();
        if(0 < cn.length())
        {
            return cn + ": TCP : " + pms.getInetAddress() + ":" + pms.getPort();
        }
        else
        {
            return "TCP : " + pms.getInetAddress() + ":" + pms.getPort();
        }
    }

    @Override
    public void disconnect()
    {
        super.disconnect();
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
