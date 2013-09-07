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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.core.RelativeMove;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Movement
{
    private String lastErrorReason = null;
    private final HashMap<Integer, Protocol> pros = new HashMap<Integer, Protocol>();
    private Integer numPros = 0;
    private boolean isHoming = false;
    private final HashMap<Axis_enum, Double> stepsPerMM = new HashMap<Axis_enum, Double>();

    // private final boolean[] homingAxis = new boolean[Cfg.NUMBER_OF_AXIS];

    public Movement()
    {
    }

    public int addProtocol(final Protocol pro)
    {
        if(null != pro)
        {
            pros.put(numPros, pro);
            numPros++;
            return numPros -1;
        }
        else
        {
            return numPros;
        }
    }

    public boolean addPause(double parameter)
    {
        // TODO Auto-generated method stub
        return true;
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public boolean addRelativeMove(RelativeMove parameter)
    {
        // TODO Auto-generated method stub
        /*
        if(true == printerAbilities.hasExtensionBasicMove())
        {
            log.error("Falied to send a linear move (Not implemented)!");
            return false;
        }
        else
        {
            log.error("Skipped Move as printer does not support it !");
            return false;
        }
        */
        return false;
    }

    public boolean homeAxis(Axis_enum[] parameter)
    {
        // Empty Array -> home all Axis
        // else home all Axis in Array
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isHoming()
    {
        return isHoming;
    }

    public boolean enableAllMotors()
    {
        /* TODO
        if(true == cfg.shouldUseSteppers())
        {
        }
        else
        {
            // TODO alternative to Stepper Control Extension
            log.error("Found disable Stepper Command but Client is not allowed to use the Steppers !");
            return false;
        }
        */
        Collection<Protocol> col = pros.values();
        Iterator<Protocol> it = col.iterator();
        boolean success = true;
        while(true == it.hasNext())
        {
            Protocol pro = it.next();
            if(false == pro.enableAllStepperMotors())
            {
                success = false;
            }
        }
        return success;
    }

    public boolean disableAllMotors()
    {
        /* TODO
        if(true == cfg.shouldUseSteppers())
        {
        }
        else
        {
            // TODO alternative to Stepper Control Extension
            log.error("Found disable Stepper Command but Client is not allowed to use the Steppers !");
            return false;
        }
        */
        Collection<Protocol> col = pros.values();
        Iterator<Protocol> it = col.iterator();
        boolean success = true;
        while(true == it.hasNext())
        {
            Protocol pro = it.next();
            if(false == pro.disableAllStepperMotors())
            {
                success = false;
            }
        }
        return success;
    }

    public boolean setStepsPerMillimeter(Axis_enum axis, Double steps)
    {
        stepsPerMM.put(axis, steps);
        return true;
    }

    public void addConnection(DeviceInformation di, Cfg cfg, Protocol pro, int i)
    {
        //TODO
    }

}
