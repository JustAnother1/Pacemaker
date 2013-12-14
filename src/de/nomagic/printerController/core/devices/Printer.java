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
package de.nomagic.printerController.core.devices;

import de.nomagic.printerController.pacemaker.Protocol;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Printer
{
    private String lastErrorReason = null;
    private final Protocol pro;

    public Printer(final Protocol pro)
    {
        this.pro = pro;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void closeConnection()
    {
        pro.closeConnection();
    }

    @Override
    public String toString()
    {
        return "Ptotocol=" + pro.toString();
    }

    public Reply sendRawOrderFrame(int Order, int[] parameterBytes, int length)
    {
        return pro.sendRawOrder(Order, parameterBytes, length);
    }

    public boolean doShutDown()
    {
        return pro.doStopPrint();
    }

    public boolean doImmediateShutDown()
    {
        return pro.doEmergencyStopPrint();
    }

}
