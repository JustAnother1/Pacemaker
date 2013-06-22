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

import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientConnectionFactory
{
    private final static Logger log = LoggerFactory.getLogger("ClientConnectionFactory");
    public final static String TCP_PREFIX = "TCP:";
    public final static String UART_PREFIX = "UART:";

    private ClientConnectionFactory()
    {
    }

    public static ClientConnection establishConnectionTo(final Cfg cfg)
    {
        if(null == cfg)
        {
            return null;
        }
        final String connectionDescription = cfg.getClientDeviceString();

        if(true == connectionDescription.startsWith(UART_PREFIX))
        {
            // TODO
            return null;
        }
        else if(true == connectionDescription.startsWith(TCP_PREFIX))
        {
            if(connectionDescription.length() < TCP_PREFIX.length())
            {
                return null;
            }
            final String data = connectionDescription.substring(TCP_PREFIX.length());
            if(false == data.contains(":"))
            {
                return null;
            }
            final String host = data.substring(0, data.indexOf(':'));
            final String port = data.substring(data.indexOf(':') + 1);
            return establishTcpConnectionTo(host, Integer.parseInt(port));
        }
        return null;
    }

    private static ClientConnection establishTcpConnectionTo(final String host, final int port)
    {
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
            e.printStackTrace();
        }
        return null;
    }
}
