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
            log.error("No Configuration available !");
            return null;
        }
        final String connectionDescription = cfg.getClientDeviceString();
        if(null == connectionDescription)
        {
            log.error("No Connection Description available !");
            return null;
        }
        if(true == connectionDescription.startsWith(UART_PREFIX))
        {
            if(connectionDescription.length() < UART_PREFIX.length())
            {
                log.error("Description({}) too short !", connectionDescription);
                return null;
            }
            final String data = connectionDescription.substring(UART_PREFIX.length());
            if(false == data.contains(":"))
            {
                log.error("Description({}) has no : !", connectionDescription);
                return null;
            }
            return UartClientConnection.establishConnectionTo(data);
        }
        else if(true == connectionDescription.startsWith(TCP_PREFIX))
        {
            if(connectionDescription.length() < TCP_PREFIX.length())
            {
                log.error("Description({}) too short !", connectionDescription);
                return null;
            }
            final String data = connectionDescription.substring(TCP_PREFIX.length());
            return TcpClientConnection.establishConnectionTo(data);
        }
        log.error("Description({}) has unknown prefix !", connectionDescription);
        return null;
    }
}
