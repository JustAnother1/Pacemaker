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

import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TemperatureSensor
{
    private final Protocol pro;
    private final int num;

    public TemperatureSensor(Protocol pro, int number)
    {
        this.pro = pro;
        this.num = number;
    }

    public Double getTemperature(Reference ref)
    {
        return pro.readTemperatureFrom(num, ref);
    }

    @Override
    public String toString()
    {
        return "Sensor num=" + num;
    }

}
