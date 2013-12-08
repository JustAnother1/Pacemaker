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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.core.Action_enum;
import de.nomagic.printerController.core.Event;
import de.nomagic.printerController.core.RelativeMove;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.core.movement.MotionSender;
import de.nomagic.printerController.core.movement.XyzTable;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Movement
{
    public static final double SECONDS_TO_UNITS_FACTOR = 10000; // 1 unit = 10uS
    public static final double MAX_UNITS_PER_COMMAND = 65535;
    public static final int MOVE_TIMEOUT_MS = 20;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String lastErrorReason = null;

    private HashMap<Integer, Protocol> protocols = new HashMap<Integer, Protocol>();

    private Integer maxProtocol = 0;
    private XyzTable table;
    private MotionSender sender;
    private TimeoutHandler to;
    private int TimeoutId;

    public Movement(TimeoutHandler to, Cfg cfg)
    {
        this.to = to;
        table = new XyzTable(cfg);
        Event e = new Event(Action_enum.endOfMove, null, null);
        TimeoutId = to.createTimeout(e, MOVE_TIMEOUT_MS);
    }

    @Override
    public String toString()
    {
        return "table : " + table.toString() + " sender : " + sender.toString();
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void addConnection(DeviceInformation di,
                              Cfg cfg,
                              Protocol pro,
                              int ClientNumber,
                              HashMap<Switch_enum, Switch> switches)
    {
        if(true == cfg.shouldUseSteppers(ClientNumber))
        {
            if(    (true == di.hasExtensionBasicMove())
                && (true == di.hasExtensionQueuedCommand())
                && (true == di.hasExtensionStepperControl())
                && (0 < di.getNumberSteppers()))
            {
                boolean first = true;
                int thisProtocolIdx = -1; // -1 is invalid
                for(int i = 0; i < di.getNumberSteppers(); i++)
                {
                    Axis_enum ae = cfg.getFunctionOfAxis(ClientNumber, i);
                    if(null != ae)
                    {
                        switch(ae)
                        {
                        case X:
                        case Y:
                        case Z:
                        case E:
                            if(true == first)
                            {
                                first = false;
                                // we need this Protocol
                                protocols.put(maxProtocol, pro);
                                thisProtocolIdx = maxProtocol;
                                maxProtocol++;
                                sender = new MotionSender();
                                sender.setProtocol(pro);
                                table.addSender(sender);
                                log.debug("Using this protocol as number {} !", thisProtocolIdx);
                            }
                            // else protocol already added
                            log.trace("Using stepper number {} for axis {} !", i, ae);
                            double maxAccelerationOfThisStepper = cfg.getMaxAccelerationFor(ClientNumber, i);
                            int maxStepsPerSecond = cfg.getMaxSpeedFor(ClientNumber, i);
                            Stepper motor = new Stepper(i,
                                             maxAccelerationOfThisStepper,
                                             maxStepsPerSecond,
                                             cfg.isMovementDirectionInverted(ClientNumber, i),
                                             cfg.getStepsPerMillimeterFor(ClientNumber, i));
                            table.addStepper(ae, motor);
                            connectEndSwitchesToStepper(ae, motor, switches, pro);
                            configureStepperMaxSpeed(motor, pro);
                            configureUnderRunAvoidance(motor, pro);
                            table.addEndStopSwitches(switches);
                            break;

                        default:
                            // This axis is not interesting for me.
                            break;
                        }
                    }
                }
                if(false == pro.activateStepperControl())
                {
                    log.error("Could not activate Stepper control !");
                }

            }
            else
            {
                log.trace("Skipped Move as client does not support it !");
                return;
            }
        }
        else
        {
            log.trace("Client is not allowed to use the Steppers !");
            return;
        }
    }

    private void configureUnderRunAvoidance(Stepper motor, Protocol pro)
    {
        boolean res = pro.configureUnderRunAvoidance(motor.getStepperNumber(),
                                                     motor.getMaxPossibleSpeedStepsPerSecond(),
                                                     (int)motor.getMaxAccelerationStepsPerSecond());
        if(false == res)
        {
            log.error("Could not configure the under run avoidance !");
        }
    }

    private void configureStepperMaxSpeed(Stepper motor, Protocol pro)
    {
        boolean res = pro.configureStepperMovementRate(motor.getStepperNumber(),
                                                       motor.getMaxPossibleSpeedStepsPerSecond());
        if(false == res)
        {
            log.error("Could not configure the Maximum Speed  of {} for the Stepper {} !",
                      motor.getMaxPossibleSpeedStepsPerSecond(), motor.getStepperNumber());
        }
    }

    private void connectEndSwitchesToStepper(Axis_enum ae,
                                             Stepper motor,
                                             HashMap<Switch_enum, Switch> switches,
                                             Protocol pro)
    {
        Switch min;
        Switch max;
        boolean res = true;
        switch(ae)
        {
        case X:
            min = switches.get(Switch_enum.Xmin);
            max = switches.get(Switch_enum.Xmax);
            res = pro.configureEndStop(motor, min, max);
            break;

        case Y:
            min = switches.get(Switch_enum.Ymin);
            max = switches.get(Switch_enum.Ymax);
            res = pro.configureEndStop(motor, min, max);
            break;

        case Z:
            min = switches.get(Switch_enum.Zmin);
            max = switches.get(Switch_enum.Zmax);
            res = pro.configureEndStop(motor, min, max);
            break;

        default:
            // No end Stops on E
            break;
        }
        if(false == res)
        {
            log.error("Could not connect the end Switch to the Stepper !");
        }
    }

    public boolean addPause(double seconds)
    {
        double units = seconds * SECONDS_TO_UNITS_FACTOR;
        do
        {
            int pauseLength;
            if(units > MAX_UNITS_PER_COMMAND)
            {
                pauseLength = (int)MAX_UNITS_PER_COMMAND;
            }
            else
            {
                pauseLength = ((int)units) + 1;
            }
            for(int i = 0; i < protocols.size(); i++)
            {
                Protocol pro = protocols.get(i);
                if(false == pro.addPauseToQueue(pauseLength))
                {
                    return false;
                }
                else
                {
                    units = units - pauseLength;
                }
            }
        } while(units > 0);
        return true;
    }

    public boolean setStepsPerMillimeter(Axis_enum axis, Double steps)
    {
        return table.setStepsPerMillimeter(axis, steps);
    }

    /** This causes all axis to decelerate to a full stop.
     *
     * @return
     */
    public boolean letMovementStop()
    {
        to.stopTimeout(TimeoutId);
        table.letMovementStop();
        return true;
    }

    public boolean addRelativeMove(RelativeMove relMov)
    {
        table.addRelativeMove(relMov);
        to.startTimeout(TimeoutId);
        return true;
    }

    public boolean homeAxis(Axis_enum[] axis)
    {
        table.homeAxis(axis);
        to.stopTimeout(TimeoutId);
        return true;
    }


    public boolean isHoming()
    {
        // TODO check if homing is finished - all end stops triggered,...
        boolean isFinished = sender.hasAllMovementFinished();
        if(false == isFinished)
        {
            return true;
        }
        else
        {

            return false;
        }
    }

    public boolean enableAllMotors()
    {
        Collection<Protocol> col = protocols.values();
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
        Collection<Protocol> col = protocols.values();
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

    public int getNumberOfUsedSlotsInClientQueue()
    {
        Collection<Protocol> col = protocols.values();
        Iterator<Protocol> it = col.iterator();
        int res = 0;
        while(true == it.hasNext())
        {
            Protocol pro = it.next();
            res = res + pro.getNumberOfCommandsInClientQueue();
        }
        return res;
    }

}
