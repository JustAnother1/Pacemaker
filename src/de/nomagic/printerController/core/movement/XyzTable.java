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
    /** used to calculate the length of the homing movement.
     * minimum Value is 1.0. Everything more is just to be sure.
     * 1.5 is 50% longer move to be sure to reach the end of the axis. */
    public static final double HOMING_MOVE_SFAETY_FACTOR = 1.5;
    /** everything shorter than this will be assumed to be 0 */
    public static final double MIN_MOVEMENT_DISTANCE = 0.00001;
    /** if the axis has steps the speed may not be 0. So this is the speed is will have at least */
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
        yMin               = cfg.getGeneralSetting(CFG_NAME_Y_MIN,                 0);
        zMin               = cfg.getGeneralSetting(CFG_NAME_Z_MIN,                 0);
        xMax               = cfg.getGeneralSetting(CFG_NAME_X_MAX,                 200);
        yMax               = cfg.getGeneralSetting(CFG_NAME_Y_MAX,                 200);
        zMax               = cfg.getGeneralSetting(CFG_NAME_Z_MAX,                 200);
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
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
        final Switch xmin = switches.get(Switch_enum.Xmin);
        if(null != xmin)
        {
            endStop_Xmin = xmin.getNumber();
        }
        final Switch xmax = switches.get(Switch_enum.Xmax);
        if(null != xmax)
        {
            endStop_Xmax = xmax.getNumber();
        }

        final Switch ymin = switches.get(Switch_enum.Ymin);
        if(null != ymin)
        {
            endStop_Ymin = ymin.getNumber();
        }
        final Switch ymax = switches.get(Switch_enum.Ymax);
        if(null != ymax)
        {
            endStop_Ymax = ymax.getNumber();
        }

        final Switch zmin = switches.get(Switch_enum.Zmin);
        if(null != zmin)
        {
            endStop_Zmin = zmin.getNumber();
        }
        final Switch zmax = switches.get(Switch_enum.Zmax);
        if(null != zmax)
        {
            endStop_Zmax = zmax.getNumber();
        }
    }

    private Vector<StepperMove> updateEndStopActivation(StepperMove move)
    {
        final Vector<Integer> stopsOn = new Vector<Integer>();
        final Vector<Integer> stopsOff = new Vector<Integer>();
        if(true == autoEndStopDisable)
        {
// X
            if(true == XisHomed)
            {
                final double posOnAxis = curPosition[X];
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
                final double posOnAxis = curPosition[Y];
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
                final double posOnAxis = curPosition[Z];
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

        final Vector<StepperMove> res = new Vector<StepperMove>();
        if((false == XisHomed) || (false == YisHomed) || (false == ZisHomed))
        {
            if(0 < stopsOn.size())
            {
                final StepperMove sm = new StepperMove();
                sm.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
                res.add(sm);
                stopsOn.clear();
            }
        }
        if(0 < stopsOff.size())
        {
            final StepperMove sm = new StepperMove();
            sm.addEndStopOnOffCommand(false, stopsOff.toArray(new Integer[0]));
            res.add(sm);
        }
        if(null != move)
        {
            res.add(move);
        }
        if(0 < stopsOn.size())
        {
            final StepperMove sm = new StepperMove();
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
       log.trace("Speed Factor = {} mm/second", SpeedPerMm);
       final StepperMove res = new StepperMove();
       for(Axis_enum ax: Axis_enum.values())
       {
           if(true == relMov.has(ax))
           {
               switch(ax)
               {
               case X:
                   curPosition[X] = curPosition[X] + relMov.get(Axis_enum.X);
                   res.setMmX(relMov.get(Axis_enum.X));
                   final double XSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   log.trace("XSpeed = {}", XSpeed);
                   if(null != X0)
                   {
                       X0.addMove(relMov.get(ax));
                       X0.setMaxSpeedMmPerSecond(XSpeed);
                       final int max = X0.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)X0.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           X0.setMaxSpeedStepsPerSecond(max);
                       }
                       res.addAxisMotors(X0);
                   }
                   if(null != X1)
                   {
                       X1.addMove(relMov.get(ax));
                       X1.setMaxSpeedMmPerSecond(XSpeed);
                       final int max = X1.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)X1.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           X1.setMaxSpeedStepsPerSecond(max);
                       }
                       res.addAxisMotors(X1);
                   }
                   break;

               case Y:
                   curPosition[Y] = curPosition[Y] + relMov.get(Axis_enum.Y);
                   res.setMmY(relMov.get(Axis_enum.Y));
                   final double YSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   log.trace("YSpeed = {}", YSpeed);
                   if(null != Y0)
                   {
                       Y0.addMove(relMov.get(ax));
                       Y0.setMaxSpeedMmPerSecond(YSpeed);
                       final int max = Y0.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)Y0.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           Y0.setMaxSpeedStepsPerSecond(max);
                       }
                       res.addAxisMotors(Y0);
                   }
                   if(null != Y1)
                   {
                       Y1.addMove(relMov.get(ax));
                       Y1.setMaxSpeedMmPerSecond(YSpeed);
                       final int max = Y1.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)Y1.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           Y1.setMaxSpeedStepsPerSecond(max);
                       }
                       res.addAxisMotors(Y1);
                   }
                   break;

               case Z:
                   curPosition[Z] = curPosition[Z] + relMov.get(Axis_enum.Z);
                   res.setMmZ(relMov.get(Axis_enum.Z));
                   final double ZSpeed = Math.abs(relMov.get(ax) * SpeedPerMm);
                   log.trace("ZSpeed = {}", ZSpeed);
                   if(null != Z0)
                   {
                       Z0.addMove(relMov.get(ax));
                       Z0.setMaxSpeedMmPerSecond(ZSpeed);
                       final int max = Z0.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)Z0.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           Z0.setMaxSpeedStepsPerSecond(max);
                       }
                       res.addAxisMotors(Z0);
                   }
                   if(null != Z1)
                   {
                       Z1.addMove(relMov.get(ax));
                       Z1.setMaxSpeedMmPerSecond(ZSpeed);
                       final int max = Z1.getMaxPossibleSpeedStepsPerSecond();
                       final int speed = (int)Z1.getMaxTravelSpeedStepsPerSecond();
                       if(speed > max)
                       {
                           Z1.setMaxSpeedStepsPerSecond(max);
                       }
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
       final StepperMove res = new StepperMove();
       res.setIsHoming(true);
       double homingDistance = 0.0;
       for(int i = 0; i < axis.length; i++)
       {
           final Axis_enum ax = axis[i];
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
       if(null != aMove)
       {
           log.trace("preparing the move : {}", aMove);
           final Vector<StepperMove> moves = updateEndStopActivation(aMove);
           for(int i = 0; i < moves.size(); i++)
           {
               PlannerQueue.add(moves.get(i));
           }
       }
       // else no new movement data, but we now know that this is the end of the move.
       sendAllPossibleMoves(isLastMove);
       if(true == isLastMove)
       {
           if(null != sender)
           {
               log.trace("flushing Queue to Client");
               sender.flushQueueToClient();
           }
           // else no sender - no movement to flush
       }
   }

   private void sendOneMove(StepperMove aMove, double endSpeedFactor)
   {
       log.trace("endSpeedFactor = {}", endSpeedFactor);
       final Integer[] steppers = aMove.getAllActiveSteppers();
       for(int j = 0; j < steppers.length; j++)
       {
           log.trace("Stepper {}:", steppers[j]);
           final double maxSpeed = aMove.getMaxSpeedMmPerSecondFor(steppers[j]);
           log.trace("maxSpeed = {}", maxSpeed);
           aMove.setMaxEndSpeedMmPerSecondFor(steppers[j], maxSpeed * endSpeedFactor);
       }
       if(null != sender)
       {
           sender.add(aMove);
       }
       else
       {
           log.error("Could not send Move - no sender available !");
       }
       PlannerQueue.finishedOneMove();
   }

   private boolean sendOneMoveThatHasAnotherFolling()
   {
       if(2 > PlannerQueue.size())
       {
           // we need at least two moves
           return false;
       }
       final StepperMove firstMove = PlannerQueue.getMove(0);
       final double[] firstVector =new double[3];
       firstVector[X] = firstMove.getMmX();
       firstVector[Y] = firstMove.getMmY();
       firstVector[Z] = firstMove.getMmZ();
       log.trace("getting the move [{},{},z]", firstVector[X], firstVector[Y]);

       final Integer[] steppers = firstMove.getAllActiveSteppers();
       for(int j = 0; j < steppers.length; j++)
       {
           log.trace("Stepper {}:", steppers[j]);
       }

       // find second move
       int i = 1;
       boolean found = false;
       double[] secondVector;
       do
       {
           final StepperMove curMove = PlannerQueue.getMove(i);
           secondVector = new double[3];
           secondVector[X] = curMove.getMmX();
           secondVector[Y] = curMove.getMmY();
           secondVector[Z] = curMove.getMmZ();
           log.trace("getting the move [{},{},z]", secondVector[X], secondVector[Y]);
           if(true == hasMovementData(secondVector))
           {
               found = true;
           }
           //else  we found a empty stepperMove with a command in it -> skip for now
           i++;
       } while((i < PlannerQueue.size()) && (false == found));

       if(true == found)
       {
           // we have two moves so send the first
           // set end Speed
           final double endSpeedFactor = getMaxEndSpeedFactorFor(firstVector, secondVector);
           // send first move
           log.trace("sending {}", firstMove);
           sendOneMove(firstMove, endSpeedFactor);
           return true;
       }
       else
       {
           // no second move found
           return false;
       }
   }

   private void sendLastMove()
   {
       final StepperMove curMove = PlannerQueue.getMove(0);
       log.trace("sending last move");
       sendOneMove(curMove, 0.0);
   }

    private void sendAllPossibleMoves(boolean isLastMove)
    {
        if(1 > PlannerQueue.size())
        {
            // no moves available to send
            return;
        }
        sendCommands();
        boolean goon = true;
        do
        {
            goon = sendOneMoveThatHasAnotherFolling();
            sendCommands();
        }while(true == goon);
        if(true == isLastMove)
        {
            sendLastMove();
        }
        log.trace("send everything");
    }

    private void sendCommands()
    {
        while(0 < PlannerQueue.size())
        {
            final StepperMove curMove = PlannerQueue.getMove(0);
            if(true == curMove.hasCommand())
            {
                // This move can be send
                if(null != sender)
                {
                    log.trace("sending command");
                    sender.add(curMove);
                }
                else
                {
                    log.error("Could not send Command - no sender available !");
                }
            }
            final double[] curVector = new double[3];
            curVector[X] = curMove.getMmX();
            curVector[Y] = curMove.getMmY();
            curVector[Z] = curMove.getMmZ();
            if(true == hasMovementData(curVector))
            {
                break;
            }
            else
            {
                // empty move -> remove from queue
                PlannerQueue.finishedOneMove();
            }
        }
    }

    private double cornerBreakFactor(double in, double out)
    {
        if((in > 0) && (out > 0))
        {
            return 1 - Math.abs(in -out);
        }
        if((in < 0) && ( out < 0))
        {
            return 1 - Math.abs(in -out);
        }
        // else one is 0 or they point in opposing directions
        return 0.0;
    }


    private double getMaxEndSpeedFactorFor(double[] vec_one, double[] vec_two)
    {
        vec_one = normalize(vec_one);
        vec_two = normalize(vec_two);
        return Math.max(Math.max(cornerBreakFactor(vec_one[X], vec_two[X]),
                                 cornerBreakFactor(vec_one[Y], vec_two[Y])),
                                 cornerBreakFactor(vec_one[Z], vec_two[Z]));

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
