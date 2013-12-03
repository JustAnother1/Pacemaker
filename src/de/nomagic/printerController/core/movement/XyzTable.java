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
package de.nomagic.printerController.core.movement;

import java.util.HashMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.core.RelativeMove;
import de.nomagic.printerController.core.devices.Stepper;
import de.nomagic.printerController.core.devices.Switch;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class XyzTable
{
    public static final double HOMING_MOVE_SFAETY_FACTOR = 1.5;
    public static final double MIN_MOVEMENT_DISTANCE = 0.00001;
    public static final double MIN_MOVEMENT_SPEED_MM_SECOND = 0.1;

    public static final String CFG_NAME_AUTO_END_STOP_DISABLE = "automatically disable end stops";
    public static final String CFG_NAME_END_STOP_ALLOWANCE = "end stop allowance";
    public static final String CFG_NAME_X_MIN = "allowed movement area x min";
    public static final String CFG_NAME_Y_MIN = "allowed movement area y min";
    public static final String CFG_NAME_Z_MIN = "allowed movement area z min";
    public static final String CFG_NAME_X_MAX = "allowed movement area x max";
    public static final String CFG_NAME_Y_MAX = "allowed movement area y max";
    public static final String CFG_NAME_Z_MAX = "allowed movement area z max";


    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Stepper X0 = null;
    private Stepper X1 = null;
    private Stepper Y0 = null;
    private Stepper Y1 = null;
    private Stepper Z0 = null;
    private Stepper Z1 = null;
    private Stepper E0 = null;
    private Stepper E1 = null;

    private double[] curPosition = {0,0,0};
    private boolean XisHomed = false;
    private boolean YisHomed = false;
    private boolean ZisHomed = false;

    private double[] HomePosition = {0,0,0};

    private double endstopAllowance;
    private double xMin;
    private double yMin;
    private double zMin;
    private double xMax;
    private double yMax;
    private double zMax;

    private boolean autoEndStopDisable = true;
    private boolean endStopXminOn = false;
    private boolean endStopXmaxOn = false;
    private boolean endStopYminOn = false;
    private boolean endStopYmaxOn = false;
    private boolean endStopZminOn = false;
    private boolean endStopZmaxOn = false;
    private int endStop_Xmin = -1;
    private int endStop_Xmax = -1;
    private int endStop_Ymin = -1;
    private int endStop_Ymax = -1;
    private int endStop_Zmin = -1;
    private int endStop_Zmax = -1;

    private MotionSender sender;
    private MovementQueue PlannerQueue = new MovementQueue();

    private double FeedrateMmPerMinute = 0;

    private int activeToolhead = 0; // TODO

    public XyzTable(Cfg cfg)
    {
        autoEndStopDisable = cfg.getGeneralSetting(CFG_NAME_AUTO_END_STOP_DISABLE, true);
        endstopAllowance   = cfg.getGeneralSetting(CFG_NAME_END_STOP_ALLOWANCE,    0.5);
        xMin               = cfg.getGeneralSetting(CFG_NAME_X_MIN,                 0);
        yMin               = cfg.getGeneralSetting(CFG_NAME_X_MIN,                 0);
        zMin               = cfg.getGeneralSetting(CFG_NAME_X_MIN,                 0);
        xMax               = cfg.getGeneralSetting(CFG_NAME_X_MAX,                 200);
        yMax               = cfg.getGeneralSetting(CFG_NAME_X_MAX,                 200);
        zMax               = cfg.getGeneralSetting(CFG_NAME_X_MAX,                 200);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Configured Steppers:\n");
        if(null != X0)
        {
            sb.append("X0 : " + X0 + "\n");
        }
        if(null != X1)
        {
            sb.append("X1 : " + X1 + "\n");
        }
        if(null != Y0)
        {
            sb.append("Y0 : " + Y0 + "\n");
        }
        if(null != Y1)
        {
            sb.append("Y1 : " + Y1 + "\n");
        }
        if(null != Z0)
        {
            sb.append("Z0 : " + Z0 + "\n");
        }
        if(null != Z1)
        {
            sb.append("Z1 : " + Z1 + "\n");
        }
        if(null != E0)
        {
            sb.append("E0 : " + E0 + "\n");
        }
        if(null != E1)
        {
            sb.append("E1 : " + E1 + "\n");
        }
        return sb.toString();
    }

    public void addSender(MotionSender sender)
    {
        this.sender = sender;
    }

    public void addEndStopSwitches(HashMap<Switch_enum, Switch> switches)
    {
        Switch xmin = switches.get(Switch_enum.Xmin);
        if(null != xmin)
        {
            endStop_Xmin = xmin.getNumber();
        }
        Switch xmax = switches.get(Switch_enum.Xmax);
        if(null != xmax)
        {
            endStop_Xmax = xmax.getNumber();
        }

        Switch ymin = switches.get(Switch_enum.Ymin);
        if(null != ymin)
        {
            endStop_Ymin = ymin.getNumber();
        }
        Switch ymax = switches.get(Switch_enum.Ymax);
        if(null != ymax)
        {
            endStop_Ymax = ymax.getNumber();
        }

        Switch zmin = switches.get(Switch_enum.Zmin);
        if(null != zmin)
        {
            endStop_Zmin = zmin.getNumber();
        }
        Switch zmax = switches.get(Switch_enum.Zmax);
        if(null != zmax)
        {
            endStop_Zmax = zmax.getNumber();
        }
        prepareMoveForSending(null, true);
    }

    private Vector<StepperMove> updateEndStopActivation(StepperMove move)
    {
        Vector<Integer> stopsOn = new Vector<Integer>();
        Vector<Integer> stopsOff = new Vector<Integer>();
        if(true == autoEndStopDisable)
        {
// X
            if(true == XisHomed)
            {
                double posOnAxis = curPosition[X];
                if((xMin <= posOnAxis) && ((xMin + endstopAllowance)>= posOnAxis))
                {
                    // position close to min end stop -> min end stop off
                    if(true == endStopXminOn)
                    {
                        endStopXminOn = false;
                        if(-1 < endStop_Xmin)
                        {
                            stopsOff.add(endStop_Xmin);
                        }
                    }
                }
                else if((xMax >= posOnAxis) && (xMax - endstopAllowance) <= posOnAxis)
                {
                    // position close to end stop -> end stop off
                    if(true == endStopXmaxOn)
                    {
                        endStopXmaxOn = false;
                        if(-1 < endStop_Xmax)
                        {
                            stopsOff.add(endStop_Xmax);
                        }
                    }
                }
                else
                {
                    // position far away from end stops -> end Stops on
                    if(false == endStopXminOn)
                    {
                        endStopXminOn = true;
                        if(-1 < endStop_Xmin)
                        {
                            stopsOn.add(endStop_Xmin);
                        }
                    }
                    if(false == endStopXmaxOn)
                    {
                        endStopXmaxOn = true;
                        if(-1 < endStop_Xmax)
                        {
                            stopsOn.add(endStop_Xmax);
                        }
                    }
                }
            }
            else
            {
                // X not homed -> end stops on
                if(false == endStopXminOn)
                {
                    endStopXminOn = true;
                    if(-1 < endStop_Xmin)
                    {
                        stopsOn.add(endStop_Xmin);
                    }
                }
                if(false == endStopXmaxOn)
                {
                    endStopXmaxOn = true;
                    if(-1 < endStop_Xmax)
                    {
                        stopsOn.add(endStop_Xmax);
                    }
                }
            }
// Y
            if(true == YisHomed)
            {
                double posOnAxis = curPosition[Y];
                if((yMin <= posOnAxis) && ((yMin + endstopAllowance)>= posOnAxis))
                {
                    // position close to min end stop -> min end stop off
                    if(true == endStopYminOn)
                    {
                        endStopYminOn = false;
                        if(-1 < endStop_Ymin)
                        {
                            stopsOff.add(endStop_Ymin);
                        }
                    }
                }
                else if((yMax >= posOnAxis) && (yMax - endstopAllowance) <= posOnAxis)
                {
                    // position close to end stop -> end stop off
                    if(true == endStopYmaxOn)
                    {
                        endStopYmaxOn = false;
                        if(-1 < endStop_Ymax)
                        {
                            stopsOff.add(endStop_Ymax);
                        }
                    }
                }
                else
                {
                    // position far away from end stops -> end Stops on
                    if(false == endStopYminOn)
                    {
                        endStopYminOn = true;
                        if(-1 < endStop_Ymin)
                        {
                            stopsOn.add(endStop_Ymin);
                        }
                    }
                    if(false == endStopYmaxOn)
                    {
                        endStopYmaxOn = true;
                        if(-1 < endStop_Ymax)
                        {
                            stopsOn.add(endStop_Ymax);
                        }
                    }
                }
            }
            else
            {
                // X not homed -> end stops on
                if(false == endStopYminOn)
                {
                    endStopYminOn = true;
                    if(-1 < endStop_Ymin)
                    {
                        stopsOn.add(endStop_Ymin);
                    }
                }
                if(false == endStopYmaxOn)
                {
                    endStopYmaxOn = true;
                    if(-1 < endStop_Ymax)
                    {
                        stopsOn.add(endStop_Ymax);
                    }
                }
            }
// Z
            if(true == ZisHomed)
            {
                double posOnAxis = curPosition[Z];
                if((zMin <= posOnAxis) && ((zMin + endstopAllowance)>= posOnAxis))
                {
                    // position close to min end stop -> min end stop off
                    if(true == endStopZminOn)
                    {
                        endStopZminOn = false;
                        if(-1 < endStop_Zmin)
                        {
                            stopsOff.add(endStop_Zmin);
                        }
                    }
                }
                else if((zMax >= posOnAxis) && (zMax - endstopAllowance) <= posOnAxis)
                {
                    // position close to end stop -> end stop off
                    if(true == endStopZmaxOn)
                    {
                        endStopZmaxOn = false;
                        if(-1 < endStop_Zmax)
                        {
                            stopsOff.add(endStop_Zmax);
                        }
                    }
                }
                else
                {
                    // position far away from end stops -> end Stops on
                    if(false == endStopZminOn)
                    {
                        endStopZminOn = true;
                        if(-1 < endStop_Zmin)
                        {
                            stopsOn.add(endStop_Zmin);
                        }
                    }
                    if(false == endStopZmaxOn)
                    {
                        endStopZmaxOn = true;
                        if(-1 < endStop_Zmax)
                        {
                            stopsOn.add(endStop_Zmax);
                        }
                    }
                }
            }
            else
            {
                // X not homed -> end stops on
                if(false == endStopZminOn)
                {
                    endStopZminOn = true;
                    if(-1 < endStop_Zmin)
                    {
                        stopsOn.add(endStop_Zmin);
                    }
                }
                if(false == endStopZmaxOn)
                {
                    endStopZmaxOn = true;
                    if(-1 < endStop_Zmax)
                    {
                        stopsOn.add(endStop_Zmax);
                    }
                }
            }
        }
        else
        {
            // end stops always on
            if(false == endStopXminOn)
            {
                endStopXminOn = true;
                if(-1 < endStop_Xmin)
                {
                    stopsOn.add(endStop_Xmin);
                }
            }
            if(false == endStopXmaxOn)
            {
                endStopXmaxOn = true;
                if(-1 < endStop_Xmax)
                {
                    stopsOn.add(endStop_Xmax);
                }
            }
            if(false == endStopYminOn)
            {
                endStopYminOn = true;
                if(-1 < endStop_Ymin)
                {
                    stopsOn.add(endStop_Ymin);
                }
            }
            if(false == endStopYmaxOn)
            {
                endStopYmaxOn = true;
                if(-1 < endStop_Ymax)
                {
                    stopsOn.add(endStop_Ymax);
                }
            }
            if(false == endStopZminOn)
            {
                endStopZminOn = true;
                if(-1 < endStop_Zmin)
                {
                    stopsOn.add(endStop_Zmin);
                }
            }
            if(false == endStopZmaxOn)
            {
                endStopZmaxOn = true;
                if(-1 < endStop_Zmax)
                {
                    stopsOn.add(endStop_Zmax);
                }
            }
        }
        Vector<StepperMove> res = new Vector<StepperMove>();
        if(0 < stopsOff.size())
        {
            StepperMove sm = new StepperMove();
            sm.addEndStopOnOffCommand(false, stopsOff.toArray(new Integer[0]));
            res.add(sm);
        }
        if(null != move)
        {
            res.add(move);
        }
        if(0 < stopsOn.size())
        {
            StepperMove sm = new StepperMove();
            sm.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
            res.add(sm);
        }
        return res;
    }

    public void addStepper(Axis_enum ae, Stepper motor)
    {
        switch(ae)
        {
        case X:
            if(null == X0)
            {
                X0 = motor;
            }
            else
            {
                X1 = motor;
            }
            break;

        case Y:
            if(null == Y0)
            {
                Y0 = motor;
            }
            else
            {
                Y1 = motor;
            }
            break;

        case Z:
            if(null == Z0)
            {
                Z0 = motor;
            }
            else
            {
                Z1 = motor;
            }
            break;

        case E:
            if(null == E0)
            {
                E0 = motor;
            }
            else
            {
                E1 = motor;
            }
            break;

        default:
            log.error("Handling of the Axis {} not implemented !", ae);
            break;
        }
    }

    public boolean setStepsPerMillimeter(Axis_enum axis, Double steps)
    {
        // This is only used for the G-Code function.
        // That G-Code can not distinguish between the steppers for the same axis.
        // So lets assume he means both. Otherwise we would get trouble,...
        switch(axis)
        {
        case X:
            if(null != X0)
            {
                X0.setStepsPerMillimeter(steps);
            }
            if(null != X1)
            {
                X1.setStepsPerMillimeter(steps);
            }
            break;

        case Y:
            if(null != Y0)
            {
                Y0.setStepsPerMillimeter(steps);
            }
            if(null != Y1)
            {
                Y1.setStepsPerMillimeter(steps);
            }
            break;

        case Z:
            if(null != Z0)
            {
                Z0.setStepsPerMillimeter(steps);
            }
            if(null != Z1)
            {
                Z1.setStepsPerMillimeter(steps);
            }
            break;

        case E:

            if((0 == activeToolhead) && (null != E0))
            {
                E0.setStepsPerMillimeter(steps);
            }
            else if((1 == activeToolhead) && (null != E1))
            {
                E1.setStepsPerMillimeter(steps);
            }
            else
            {
                log.error("Using unconfigured Tool Head{} !", activeToolhead);
            }
            break;

        default:
            log.error("Handling of the Axis {} not implemented !", axis);
            return false;
        }
        return true;
    }

    /** This causes all axis to decelerate to a full stop.
    *
    * @return
    */
   public void letMovementStop()
   {
       log.trace("letting the movement stop");
       prepareMoveForSending(null, true);
   }

   public void addRelativeMove(RelativeMove relMov)
   {
       log.trace("adding the move {}", relMov);
       // Feedrate
       if(true == relMov.hasFeedrate())
       {
           FeedrateMmPerMinute = relMov.getFeedrate();
       }
       // else  reuse last Feedrate
       log.trace("Feedrate = {} mm/Minute", FeedrateMmPerMinute);
       double distanceOnXYZMm = 0.0;
       if(true == relMov.has(Axis_enum.X))
       {
           distanceOnXYZMm = distanceOnXYZMm + Math.abs(relMov.get(Axis_enum.X));
       }
       if(true == relMov.has(Axis_enum.Y))
       {
           distanceOnXYZMm = distanceOnXYZMm + Math.abs(relMov.get(Axis_enum.Y));
       }
       if(true == relMov.has(Axis_enum.Z))
       {
           distanceOnXYZMm = distanceOnXYZMm + Math.abs(relMov.get(Axis_enum.Z));
       }
       log.trace("distance on X Y Z = {} mm", distanceOnXYZMm);
       double SpeedPerMm = (FeedrateMmPerMinute/60) / distanceOnXYZMm;
       if(MIN_MOVEMENT_SPEED_MM_SECOND > SpeedPerMm)
       {
           log.error("Feedrate too low !");
           SpeedPerMm = MIN_MOVEMENT_SPEED_MM_SECOND;
       }
       log.trace("Speed = {} mm/second", SpeedPerMm);
       StepperMove res = new StepperMove();
       for(Axis_enum ax: Axis_enum.values())
       {
           if(true == relMov.has(ax))
           {
               switch(ax)
               {
               case X:
                   curPosition[X] = curPosition[X] + relMov.get(Axis_enum.X);
                   res.setMmX(relMov.get(Axis_enum.X));
                   double XSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   if(null != X0)
                   {
                       X0.addMove(relMov.get(ax));
                       X0.setMaxSpeedMmPerSecond(XSpeed);
                       res.addAxisMotors(X0);
                   }
                   if(null != X1)
                   {
                       X1.addMove(relMov.get(ax));
                       X1.setMaxSpeedMmPerSecond(XSpeed);
                       res.addAxisMotors(X1);
                   }
                   break;

               case Y:
                   curPosition[Y] = curPosition[Y] + relMov.get(Axis_enum.Y);
                   res.setMmY(relMov.get(Axis_enum.Y));
                   double YSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   if(null != Y0)
                   {
                       Y0.addMove(relMov.get(ax));
                       Y0.setMaxSpeedMmPerSecond(YSpeed);
                       res.addAxisMotors(Y0);
                   }
                   if(null != Y1)
                   {
                       Y1.addMove(relMov.get(ax));
                       Y1.setMaxSpeedMmPerSecond(YSpeed);
                       res.addAxisMotors(Y1);
                   }
                   break;

               case Z:
                   curPosition[Z] = curPosition[Z] + relMov.get(Axis_enum.Z);
                   res.setMmZ(relMov.get(Axis_enum.Z));
                   double ZSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   if(null != Z0)
                   {
                       Z0.addMove(relMov.get(ax));
                       Z0.setMaxSpeedMmPerSecond(ZSpeed);
                       res.addAxisMotors(Z0);
                   }
                   if(null != Z1)
                   {
                       Z1.addMove(relMov.get(ax));
                       Z1.setMaxSpeedMmPerSecond(ZSpeed);
                       res.addAxisMotors(Z1);
                   }
                   break;

               case E:
                   if(null != E0)
                   {
                       E0.addMove(relMov.get(ax));
                       E0.setMaxSpeedMmPerSecond(E0.getMaxSpeedMmPerSecond());
                       res.addAxisMotors(E0);
                   }
                   if(null != E1)
                   {
                       E1.addMove(relMov.get(ax));
                       E1.setMaxSpeedMmPerSecond(E1.getMaxSpeedMmPerSecond());
                       res.addAxisMotors(E1);
                   }
                   break;

               default:
                   log.error("Handling of the Axis {} not implemented !", ax);
                   break;
               }
           }
           // else axis not used
       }
       prepareMoveForSending(res, false);
   }

   public void homeAxis(Axis_enum[] axis)
   {
       log.trace("homing Axis");
       // TODO Homing direction (inverted = - homingDistance)
       StepperMove res = new StepperMove();
       res.setIsHoming(true);
       double homingDistance = 0.0;
       for(int i = 0; i < axis.length; i++)
       {
           Axis_enum ax = axis[i];
           switch(ax)
           {
           case X:
               XisHomed = true;
               homingDistance = (xMax - xMin) * HOMING_MOVE_SFAETY_FACTOR;
               res.setMmX(homingDistance);
               if(null != X0)
               {
                   X0.addMove(homingDistance);
                   res.addAxisMotors(X0);
               }
               if(null != X1)
               {
                   X1.addMove(homingDistance);
                   res.addAxisMotors(X1);
               }
               break;

           case Y:
               YisHomed = true;
               homingDistance = (yMax - yMin) * HOMING_MOVE_SFAETY_FACTOR;
               res.setMmY(homingDistance);
               if(null != Y0)
               {
                   Y0.addMove(homingDistance);
                   res.addAxisMotors(Y0);
               }
               if(null != Y1)
               {
                   Y1.addMove(homingDistance);
                   res.addAxisMotors(Y1);
               }
               break;

           case Z:
               ZisHomed = true;
               homingDistance = (yMax - yMin) * HOMING_MOVE_SFAETY_FACTOR;
               res.setMmZ(homingDistance);
               if(null != Z0)
               {
                   Z0.addMove(homingDistance);
                   res.addAxisMotors(Z0);
               }
               if(null != Z1)
               {
                   Z1.addMove(homingDistance);
                   res.addAxisMotors(Z1);
               }
               break;

           case E:
               // home on E not possible
               break;

           default:
               log.error("Homing not implemented for the new Axis {} !", ax);
               break;
           }
       }
       prepareMoveForSending(res, true);
   }

   private void prepareMoveForSending(StepperMove aMove, boolean isLastMove)
   {
       log.trace("preparing the move : {}", aMove);
       Vector<StepperMove> moves = updateEndStopActivation(aMove);
       for(int i = 0; i < moves.size(); i++)
       {
           PlannerQueue.add(moves.get(i));
       }
       sendAllPossibleMoves(isLastMove);
       if(true == isLastMove)
       {
           log.trace("flushing Queue to Client");
           sender.flushQueueToClient();
       }
   }

    private void sendAllPossibleMoves(boolean isLastMove)
    {
        if(1 > PlannerQueue.size())
        {
            // no moves available to send
            return;
        }
        StepperMove firstMove = null;
        double[] firstVector = null;
        int i = 0;
        do
        {
            StepperMove curMove = PlannerQueue.getMove(i);
            if((null == firstMove) && (true == curMove.hasCommand()))
            {
                // This move can be send
                sender.add(curMove);
                PlannerQueue.finishedOneMove();
                // i gets not increased
            }
            else
            {
                double[] curVector = new double[3];
                curVector[X] = curMove.getMmX();
                curVector[Y] = curMove.getMmY();
                curVector[Z] = curMove.getMmZ();
                curVector = normalize(curVector);
                if(true == hasMovementData(curVector))
                {
                    // We have a move, so if
                    if(null == firstMove)
                    {
                        // this is the first move -> wait for next move
                        firstMove = curMove;
                        firstVector = curVector;
                        i++;
                    }
                    else
                    {
                        // this is already the second move
                        // -> send the first move and this becomes the new first move

                        // set end Speed
                        double endSpeedFactor = getMaxEndSpeedFactorFor(firstVector, curVector);
                        log.trace("endSpeedFactor = {}", endSpeedFactor);
                        Integer[] steppers = firstMove.getAllActiveSteppers();
                        for(int j = 0; j < steppers.length; j++)
                        {
                            double maxSpeed = firstMove.getMaxSpeedMmPerSecondFor(steppers[j]);
                            log.trace("maxSpeed = {}", maxSpeed);
                            firstMove.setMaxEndSpeedMmPerSecondFor(steppers[j], maxSpeed * endSpeedFactor);
                        }
                        // send first move
                        sender.add(curMove);
                        PlannerQueue.finishedOneMove();
                        // the first move was at index 0 !
                        if(i != 1)
                        {
                            // we skipped a move
                            for(int s = 0; s < (i-1); s++)
                            {
                                sender.add(PlannerQueue.getMove(0));
                                PlannerQueue.finishedOneMove();
                            }
                            i = 1;
                        }
                        firstMove = curMove;
                        firstVector = curVector;
                    }
                }
                else
                {
                    // we found a stepperMove with a command in it -> skip for now
                    i++;
                }
            }
        } while(i < PlannerQueue.size());
        // do we have a first move stored?
        if(null != firstMove)
        {
            if(true == isLastMove)
            {
                Integer[] steppers = firstMove.getAllActiveSteppers();
                for(int j = 0; j < steppers.length; j++)
                {
                    // last move so stop at the end
                    firstMove.setMaxEndSpeedMmPerSecondFor(steppers[j], 0.0);
                }
                sender.add(firstMove);
                PlannerQueue.finishedOneMove();
                for(int j = 0; j < PlannerQueue.size(); j++)
                {
                    // send all command moves that are still in the Queue
                    sender.add(PlannerQueue.getMove(0));
                    PlannerQueue.finishedOneMove();
                }
            }
            // else we can not send that one move
        }
    }

    private double getMaxEndSpeedFactorFor(double[] vec_one, double[] vec_two)
    {
        return Math.abs(1 - (   Math.abs(vec_one[X] - vec_two[X])
                             +  Math.abs(vec_one[Y] - vec_two[Y])
                             +  Math.abs(vec_one[Z] - vec_two[Z])));
    }

    private boolean hasMovementData(double[] vec)
    {
        if(MIN_MOVEMENT_DISTANCE < Math.abs(vec[X]))
        {
            return true;
        }
        if(MIN_MOVEMENT_DISTANCE < Math.abs(vec[Y]))
        {
            return true;
        }
        if(MIN_MOVEMENT_DISTANCE < Math.abs(vec[Z]))
        {
            return true;
        }
        return false;
    }

    private double[] normalize(double[] vec)
    {
        double sum = 0.0;
        sum = Math.abs(vec[X]) + Math.abs(vec[Y]) + Math.abs(vec[Z]);
        vec[X] = vec[X] / sum;
        vec[Y] = vec[Y] / sum;
        vec[Z] = vec[Z] / sum;
        return vec;
    }

}
