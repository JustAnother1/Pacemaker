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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Fan
{
    private final Protocol pro;
    private final int num;
    private String lastErrorReason = null;

    public Fan(final Protocol pro, final int number)
    {
        this.pro = pro;
        this.num = number;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public boolean setSpeed(Integer speed)
    {
        if(false == pro.setFanSpeedfor(num, speed))
        {
            lastErrorReason = pro.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String toString()
    {
        return "Fan num=" + num;
    }

}
