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

    public boolean doShutDown()
    {
        // TODO Auto-generated method stub
        /*
        for(int i = 0; i < Cfg.NUMBER_OF_HEATER_FUNCTIONS; i++)
        {
            activeHeaters[i] = false;
            heatersTargetTemperatures[i] = 0;
        }
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            homingAxis[i] = false;
        }
        if(false == proto.doStopPrint())
        {
            log.error("Falied to Stop the Print !");
            disableAllStepperMotors();
            return false;
        }
        else
        {
            return disableAllStepperMotors();
        }
        */
        return false;
    }

    public boolean doImmediateShutDown()
    {
        // TODO Auto-generated method stub
        /*
        for(int i = 0; i < Cfg.NUMBER_OF_HEATER_FUNCTIONS; i++)
        {
            activeHeaters[i] = false;
            heatersTargetTemperatures[i] = 0;
        }
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            homingAxis[i] = false;
        }
        if(false == proto.doEmergencyStopPrint())
        {
            log.error("Falied to Stop the Print !");
            disableAllStepperMotors();
            return false;
        }
        else
        {
            return disableAllStepperMotors();
        }
        */
        return false;
    }

}
