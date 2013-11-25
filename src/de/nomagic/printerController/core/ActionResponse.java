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
package de.nomagic.printerController.core;

/** result of an executed Action.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ActionResponse
{
    private final boolean status;
    private final double temperature;
    private final boolean boolValue;
    private final int intValue;


    public ActionResponse(boolean success)
    {
        this.status = success;
        temperature = 0.0;
        intValue = 0;
        boolValue = false;
    }

    public ActionResponse(boolean success, double Temperature)
    {
        this.status = success;
        this.temperature = Temperature;
        intValue = 0;
        boolValue = false;
    }

    public ActionResponse(boolean success, boolean boolValue)
    {
        this.status = success;
        this.temperature = 0.0;
        intValue = 0;
        this.boolValue = boolValue;
    }

    public ActionResponse(boolean success, int intValue)
    {
        this.status = success;
        this.temperature = 0.0;
        this.intValue = intValue;
        this.boolValue = false;
    }

    public boolean wasSuccessful()
    {
        return status;
    }

    public double getTemperature()
    {
        return temperature;
    }

    public boolean getBoolean()
    {
        return boolValue;
    }

    public int getInt()
    {
        return intValue;
    }
}
