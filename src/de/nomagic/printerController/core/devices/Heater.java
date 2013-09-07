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

import de.nomagic.printerController.core.devices.TemperatureSensor;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Heater
{
    private Protocol pro;
    private int num;
    private TemperatureSensor sensor;

    public Heater()
    {
    }

    public void setHeaterNumber(int number, final Protocol pro)
    {
        this.num = number;
        this.pro = pro;
    }

    public boolean setTemperature(Double temperature)
    {
        return pro.setTemperature(num, temperature);
    }

    public void setTemperatureSenor(TemperatureSensor sensor)
    {
        this.sensor = sensor;
    }

    public TemperatureSensor getTemperatureSenor()
    {
        return sensor;
    }

}
