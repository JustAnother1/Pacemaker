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
package de.nomagic.printerController.terminal;

import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.pacemaker.ClientConnectionFactory;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ClientChannel
{
    private ClientConnection cc = null;

    public ClientChannel()
    {
    }

    public void close()
    {
        if(null != cc)
        {
            cc.close();
        }
    }

    public void sendFrame(int order, int parameterLength, int[] parameter)
    {
        if(null != cc)
        {
            Reply r = cc.sendRequest(order, parameter, 0, parameterLength);
            if(null != r)
            {
                r.toString();
            }
        }
    }

    public boolean connect(String connectionDescription)
    {
        cc = ClientConnectionFactory.establishConnectionTo(connectionDescription);
        if(null == cc)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean disconnect()
    {
        if(null != cc)
        {
            cc.close();
        }
        return true;
    }

}
