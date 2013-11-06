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
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
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
    public final static double TOLLERANCE_SPEED_IN_MILLIMETER = 0.001;
    public final static double SECONDS_TO_UNITS_FACTOR = 10000; // 1 unit = 10uS
    public final static double MAX_UNITS_PER_COMMAND = 65535;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String lastErrorReason = null;

    private Vector<RelativeMove> MovementQueue = new Vector<RelativeMove>();
    private HashMap<Integer, Protocol> protocols = new HashMap<Integer, Protocol>();
    private HashMap<Axis_enum, Stepper> Steppers = new HashMap<Axis_enum, Stepper>();
    private Integer maxProtocol = 0;

    private int maxAccellerationX = Integer.MAX_VALUE;
    private int maxAccellerationY = Integer.MAX_VALUE;
    private int maxAccellerationZ = Integer.MAX_VALUE;
    private int maxAccellerationXY = Integer.MAX_VALUE;
    private int maxAccellerationXYZ = Integer.MAX_VALUE;


    private double Feedrate = 0;

    public Movement()
    {
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Configured Steppers:\n");
        for(Axis_enum ax: Axis_enum.values())
        {
            Stepper step = Steppers.get(ax);
            if(null != step)
            {
                sb.append("Axis " + ax.toString() + " : " + step.toString());
            }
        }
        return sb.toString();
    }

    public String getLastErrorReason()
    {
        return lastErrorReason;
    }

    public void addConnection(DeviceInformation di, Cfg cfg, Protocol pro, int ClientNumber)
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
                                log.trace("Using this protocol as number {} !", thisProtocolIdx);
                            }
                            // else protocol already added
                            log.trace("Using stepper number {} for axis {} !", i, ae);
                            Stepper motor = Steppers.get(ae);
                            if(null == motor)
                            {
                                // first Motor for this axis
                                motor = new Stepper();
                            }
                            int maxAccelerationOfThisStepper = cfg.getMaxAccelerationFor(ClientNumber, i);
                            motor.addStepper(i,
                                             thisProtocolIdx,
                                             maxAccelerationOfThisStepper,
                                             cfg.isMovementDirectionInverted(ClientNumber, i),
                                             cfg.getStepsPerMillimeterFor(ClientNumber, i));
                            Steppers.put(ae, motor);
                            // update max Acceleration Values
                            if(Axis_enum.X == ae)
                            {
                                if(maxAccellerationX > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationX = maxAccelerationOfThisStepper;
                                }
                                if(maxAccellerationXY > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationXY = maxAccelerationOfThisStepper;
                                }
                                if(maxAccellerationXYZ > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationXYZ = maxAccelerationOfThisStepper;
                                }
                            }
                            else if(Axis_enum.Y == ae)
                            {
                                if(maxAccellerationY > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationY = maxAccelerationOfThisStepper;
                                }
                                if(maxAccellerationXY > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationXY = maxAccelerationOfThisStepper;
                                }
                                if(maxAccellerationXYZ > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationXYZ = maxAccelerationOfThisStepper;
                                }
                            }
                            else if(Axis_enum.Z == ae)
                            {
                                if(maxAccellerationZ > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationZ = maxAccelerationOfThisStepper;
                                }
                                if(maxAccellerationXYZ > maxAccelerationOfThisStepper)
                                {
                                    maxAccellerationXYZ = maxAccelerationOfThisStepper;
                                }
                            }
                            break;

                        default:
                            // This axis is not interesting for me.
                            break;
                        }
                    }
                }
            }
            else
            {
                log.trace("Skipped Move as printer does not support it !");
                return;
            }
        }
        else
        {
            log.trace("Client is not allowed to use the Steppers !");
            return;
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

    private int getMaxAccelerationFor(boolean Xmoves, boolean Ymoves, boolean Zmoves)
    {
        int maxAccelleration = 0;
        if((true == Xmoves) && (false == Ymoves) && (false == Zmoves))
        {
            maxAccelleration = maxAccellerationX;
        }
        else if((false == Xmoves) && (true == Ymoves) && (false == Zmoves))
        {
            maxAccelleration = maxAccellerationY;
        }
        else if((false == Xmoves) && (false == Ymoves) && (true == Zmoves))
        {
            maxAccelleration = maxAccellerationZ;
        }
        else if((true == Xmoves) && (true == Ymoves) && (false == Zmoves))
        {
            maxAccelleration = maxAccellerationXY;
        }
        else
        {
            maxAccelleration = maxAccellerationXYZ;
        }
        return maxAccelleration;
    }


    private boolean sendMoveCommand(boolean isLastMove)
    {
        RelativeMove relMov = MovementQueue.get(0);
        MovementQueue.remove(0);
        if(true == relMov.has(Axis_enum.F))
        {
            Feedrate = relMov.get(Axis_enum.F);
        }
        // calculate the speed
        // The speed on all active axis must be the same. If the speed would be
        // different then a diagonal line would not be straight but be a curve.
        // Three speeds are interesting:
        double start_speed = 0.0;
        double travel_speed = 0.0;
        double end_speed = 0.0;
        // The _start speed_ is given from the last movement.
        // Initial start speed is 0.
        // The _travel speed_ is the highest speed possible that is still below
        // the Feedrate.
        // The _end speed_ is the speed to that the move needs to decelerate in
        // order to be able to do the next move. The end speed is also the
        // start speed of the next move.
        // The Feedrate is the speed of the print head.
        // So X Y and Z speed add up to the Feedrate.
        // The defined Feedrate from the G-Code is the absolute maximum speed.

        // axis moving in this move:
        boolean Xmoves= false;
        boolean Ymoves= false;
        boolean Zmoves= false;

        // for x,y,z,e
        boolean first = true;
        Vector<Integer> prots = new Vector<Integer>();
        for (Axis_enum axis : Axis_enum.values())
        {
            Stepper motor = Steppers.get(axis);
            if(null != motor)
            {
                motor.clearMove();
                // calculate the steps for the axes
                if(true == relMov.has(axis))
                {
                    motor.addMove(relMov.get(axis));
                    double lastSpeed = motor.getLastSpeedInMillimeterperSecond();
                    if(true == first)
                    {
                        start_speed = lastSpeed;
                        first = false;
                    }
                    else
                    {
                        if(   (start_speed + TOLLERANCE_SPEED_IN_MILLIMETER < lastSpeed)
                           || (start_speed - TOLLERANCE_SPEED_IN_MILLIMETER > lastSpeed) )
                        {
                            log.error("Axis had different Start Speeds ({} and {}) !", start_speed, lastSpeed);
                        }
                        // else ok
                    }
                    if(Axis_enum.X == axis){ Xmoves = true; }
                    if(Axis_enum.Y == axis){ Ymoves = true; }
                    if(Axis_enum.Z == axis){ Zmoves = true; }
                    // find the protocols affected by this move
                    prots = motor.addActiveProtocolIndexes(prots);
                }
            }
            // else no Motor on this axis (F,..)
        }

        int maxAcceleration = getMaxAccelerationFor(Xmoves, Ymoves, Zmoves);

        //TODO travel_speed
        log.error("TODO");
        // TODO end_speed
        log.error("TODO");

        // with every protocol
        int numProts = prots.size();
        log.info("Move effects {} protocols !", numProts);
        for(int i = 0; i < numProts; i++)
        {
            // check if a split of the move is needed
            int minParts = 0;
            for (Axis_enum axis : Axis_enum.values())
            {
                Stepper motor = Steppers.get(axis);
                int requestedParts = motor.getMinimumPossiblePartialMoves(i);
                if(requestedParts > minParts)
                {
                    minParts = requestedParts;
                }
            }
            // for each part
            for(int p = 0; p < minParts; p++)
            {
                // send the command
                // TODO
                log.error("TODO");
            }
        }
        return true;
    }

    public boolean addRelativeMove(RelativeMove relMov)
    {
        MovementQueue.add(relMov);
        if(1 < MovementQueue.size())
        {
            return sendMoveCommand(false);
        }
        else
        {
            return true;
        }
    }

    /** This causes all axis to decelerate to a full stop.
     *
     * @return
     */
    public boolean letMovementStop()
    {
        // TODO needs to get called !
        return sendMoveCommand(true);
    }

    public boolean homeAxis(Axis_enum[] parameter)
    {
        // Empty Array -> home all Axis
        // else home all Axis in Array
        // TODO Auto-generated method stub
        log.error("TODO");
        return false;
    }


    public boolean isHoming()
    {
        // TODO
        log.error("TODO");
        return false;
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

    public boolean setStepsPerMillimeter(Axis_enum axis, Double steps)
    {
        Stepper motor = Steppers.get(axis);
        if(null != axis)
        {
            motor.setStepsPerMillimeter(steps);
            return true;
        }
        else
        {
            log.error("Received Stepse per Millimeter for invalid Axis {} !", axis);
            return false;
        }
    }
}
