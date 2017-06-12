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
import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.core.RelativeMove;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.core.movement.PlannedMoves;
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
    private final TimeoutHandler to;
    private int TimeoutId;
    private Reference ref = new Reference("Movement Initialisation");

    public Movement(TimeoutHandler to, Cfg cfg)
    {
        this.to = to;
        table = new XyzTable(cfg);
        final Event e = new Event(Action_enum.endOfMove, null, null);
        TimeoutId = to.createTimeout(e, MOVE_TIMEOUT_MS);
    }

    @Override
    public String toString()
    {
        return "table : " + table.toString();
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public boolean addConnection(DeviceInformation di,
                              Cfg cfg,
                              Protocol pro,
                              int ClientNumber,
                              HashMap<Switch_enum, Switch> switches)
    {
        if(false == cfg.shouldUseSteppers(ClientNumber))
        {
            log.info("Client #{} is not allowed to use the Steppers !", ClientNumber);
            return false;
        }

        if(    (false == di.hasExtensionBasicMove())
            || (false == di.hasExtensionQueuedCommand())
            || (false == di.hasExtensionStepperControl())
            || (1 > di.getNumberSteppers()))
        {
            log.info("Skipped Move as client does not support it !");
            return false;
        }

        boolean first = true;
        int thisProtocolIdx = -1; // -1 is invalid
        for(int i = 0; i < di.getNumberSteppers(); i++)
        {
            final Axis_enum ae = cfg.getFunctionOfAxis(ClientNumber, i);
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
                        final PlannedMoves Queue = new PlannedMoves(di.getMaxSteppsPerSecond(), to);
                        Queue.addProtocol(pro);
                        table.addMovementQueue(Queue);
                        // we need this Protocol
                        protocols.put(maxProtocol, pro);
                        thisProtocolIdx = maxProtocol;
                        maxProtocol++;
                        log.debug("Using this protocol as number {} !", thisProtocolIdx);
                    }
                    // else protocol already added
                    log.trace("Using stepper number {} for axis {} !", i, ae);
                    final double maxAccelerationOfThisStepper = cfg.getMaxAccelerationFor(ClientNumber, i);
                    final int maxStepsPerSecond = cfg.getMaxSpeedFor(ClientNumber, i);
                    final Stepper motor = new Stepper(i,
                                     maxAccelerationOfThisStepper,
                                     maxStepsPerSecond,
                                     cfg.isMovementDirectionInverted(ClientNumber, i),
                                     cfg.getStepsPerMillimeterFor(ClientNumber, i),
                                     cfg.getMaxJerkMmSfor(ClientNumber, i));
                    table.addStepper(ae, motor);

                    if(false == connectEndSwitchesToStepper(ae, motor, switches, pro))
                    {
                        return false;
                    }
                    if(false == configureStepperMaxSpeed(motor, pro))
                    {
                        return false;
                    }
                    if(false == configureUnderRunAvoidance(motor, pro))
                    {
                        return false;
                    }
                    table.addEndStopSwitches(switches);
                    table.setMaxClientStepsPerSecond(di.getMaxSteppsPerSecond());
                    break;

                default:
                    // This axis is not interesting for me.
                    break;
                }
            }
        }
        if(false == pro.activateStepperControl(ref))
        {
            log.error("Could not activate Stepper control !");
            return false;
        }
        return true;
    }

    private boolean configureUnderRunAvoidance(Stepper motor, Protocol pro)
    {
        final boolean res = pro.configureUnderRunAvoidance(motor.getStepperNumber(),
                                                           motor.getMaxPossibleSpeedStepsPerSecond(),
                                                           (int)motor.getMaxAccelerationStepsPerSecond(),
                                                           ref);
        if(false == res)
        {
            log.error("Could not configure the under run avoidance !");
        }
        return res;
    }

    private boolean configureStepperMaxSpeed(Stepper motor, Protocol pro)
    {
        final boolean res = pro.configureStepperMovementRate(motor.getStepperNumber(),
                                                             motor.getMaxPossibleSpeedStepsPerSecond(),
                                                             ref);
        if(false == res)
        {
            log.error("Could not configure the Maximum Speed  of {} for the Stepper {} !",
                      motor.getMaxPossibleSpeedStepsPerSecond(), motor.getStepperNumber());
        }
        return res;
    }

    private boolean connectEndSwitchesToStepper(Axis_enum ae,
                                             Stepper motor,
                                             HashMap<Switch_enum, Switch> switches,
                                             Protocol pro)
    {
        Switch min;
        Switch max;
        boolean res = false;
        switch(ae)
        {
        case X:
            if(motor.isDirectionInverted())
            {
                min = switches.get(Switch_enum.Xmax);
                max = switches.get(Switch_enum.Xmin);
            }
            else
            {
                min = switches.get(Switch_enum.Xmin);
                max = switches.get(Switch_enum.Xmax);
            }
            res = pro.configureEndStop(motor, min, max, ref);
            break;

        case Y:
            if(motor.isDirectionInverted())
            {
                min = switches.get(Switch_enum.Ymax);
                max = switches.get(Switch_enum.Ymin);
            }
            else
            {
                min = switches.get(Switch_enum.Ymin);
                max = switches.get(Switch_enum.Ymax);
            }
            res = pro.configureEndStop(motor, min, max, ref);
            break;

        case Z:
            if(motor.isDirectionInverted())
            {
                min = switches.get(Switch_enum.Zmax);
                max = switches.get(Switch_enum.Zmin);
            }
            else
            {
                min = switches.get(Switch_enum.Zmin);
                max = switches.get(Switch_enum.Zmax);
            }
            res = pro.configureEndStop(motor, min, max, ref);
            break;

        case E:
            // No end Stops on E
            res = true;
            break;

        default:
            res = false;
            break;
        }
        if(false == res)
        {
            log.error("Could not connect the end Switch to the Stepper !");
        }
        return res;
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
                final Protocol pro = protocols.get(i);
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
     * @param ref
     *
     */
    public boolean letMovementStop(Reference ref)
    {
        to.stopTimeout(TimeoutId);
        return table.letMovementStop(ref);
    }

    public boolean addRelativeMove(RelativeMove relMov, Reference ref)
    {
        if(true == table.addRelativeMove(relMov, ref))
        {
            to.startTimeout(TimeoutId);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean homeAxis(Axis_enum[] axis, Reference ref)
    {
        to.stopTimeout(TimeoutId);
        if(false == table.homeAxis(axis, ref))
        {
            lastErrorReason = "Movement: " + table.getLastErrorReason();
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean enableAllMotors(Reference ref)
    {
        final Collection<Protocol> col = protocols.values();
        final Iterator<Protocol> it = col.iterator();
        boolean success = true;
        while(true == it.hasNext())
        {
            final Protocol pro = it.next();
            if(false == pro.enableAllStepperMotors(ref))
            {
                success = false;
            }
        }
        return success;
    }

    public boolean disableAllMotors(Reference ref)
    {
        final Collection<Protocol> col = protocols.values();
        final Iterator<Protocol> it = col.iterator();
        boolean success = true;
        while(true == it.hasNext())
        {
            final Protocol pro = it.next();
            if(false == pro.disableAllStepperMotors(ref))
            {
                success = false;
            }
        }
        return success;
    }

    public int getNumberOfUsedSlotsInClientQueue(Reference ref)
    {
        final Collection<Protocol> col = protocols.values();
        final Iterator<Protocol> it = col.iterator();
        int res = 0;
        while(true == it.hasNext())
        {
            final Protocol pro = it.next();
            final int cur = pro.getNumberOfCommandsInClientQueue(ref);
            if(0 > cur)
            {
                lastErrorReason = "Client in Stopped Mode !";
                return -1;
            }
            res = res + cur;
        }
        return res;
    }

}
