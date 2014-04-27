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
    /** switch off end stops if print head prints closer than this to the end stop
     *  Reason: The end stop might trigger to early. This should be avoided. */
    public static final double DEFAULT_END_STOP_ALLOWANCE = 0.5;
    /** maximum supported number of Steppers on a axis */
    public static final int MAX_STEPPERS_PER_AXIS = 2;

    public static final String CFG_NAME_AUTO_END_STOP_DISABLE = "automatically disable end stops";
    public static final String CFG_NAME_END_STOP_ALLOWANCE = "end stop allowance";
    public static final String CFG_NAME_MIN = "allowed movement area min";
    public static final String CFG_NAME_MAX = "allowed movement area max";


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Stepper[][] Steppers;


    private double[] curPosition;
    private boolean[] isHomed;

    private double endstopAllowance;
    private double[] Min;
    private double[] Max;

    private boolean autoEndStopDisable = true;
    private boolean[] endStopminOn;
    private boolean[] endStopmaxOn;
    private int[] endStop_min;
    private int[] endStop_max;

    private Vector<Integer> stopsOn;
    private Vector<Integer> stopsOff;

    private PlannedMoves planner;

    private double FeedrateMmPerMinute = 0;
    private int MaxClientStepsPerSecond = 0;

    public XyzTable(Cfg cfg)
    {
        Steppers = new Stepper[Axis_enum.size][2];
        curPosition = new double[Axis_enum.size];
        isHomed = new boolean[Axis_enum.size];
        endStopminOn = new boolean[Axis_enum.size];
        endStopmaxOn = new boolean[Axis_enum.size];
        Min = new double[Axis_enum.size];
        Max = new double[Axis_enum.size];
        endStop_min = new int[Axis_enum.size];
        endStop_max = new int[Axis_enum.size];
        for(Axis_enum axis: Axis_enum.values())
        {
            curPosition[axis.ordinal()] = 0.0;
            isHomed[axis.ordinal()] = false;
            Min[axis.ordinal()] = cfg.getGeneralSetting(CFG_NAME_MIN + axis, 0);
            Max[axis.ordinal()] = cfg.getGeneralSetting(CFG_NAME_MAX + axis, 0);
            endStopminOn[axis.ordinal()] = false;
            endStopmaxOn[axis.ordinal()] = false;
            endStop_min[axis.ordinal()] = -1;
            endStop_max[axis.ordinal()] = -1;
        }
        autoEndStopDisable = cfg.getGeneralSetting(CFG_NAME_AUTO_END_STOP_DISABLE, true);
        endstopAllowance   = cfg.getGeneralSetting(CFG_NAME_END_STOP_ALLOWANCE,    DEFAULT_END_STOP_ALLOWANCE);
    }

    public void addMovementQueue(PlannedMoves queue)
    {
        log.info("Adding Queue !");
        planner = queue;
    }

    public void setMaxClientStepsPerSecond(int maxSteppsPerSecond)
    {
        MaxClientStepsPerSecond = maxSteppsPerSecond;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("Configured Steppers:\n");
        for(Axis_enum axis: Axis_enum.values())
        {
            for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
            {
                if(null != Steppers[axis.ordinal()][i])
                {
                    sb.append("" +axis + i + " : " + Steppers[axis.ordinal()][i] + "\n");
                }
            }
        }
        sb.append("Queue:" + planner + "\n");
        return sb.toString();
    }

    public void addEndStopSwitches(HashMap<Switch_enum, Switch> switches)
    {
        final Switch xmin = switches.get(Switch_enum.Xmin);
        if(null != xmin)
        {
            endStop_min[Axis_enum.X.ordinal()] = xmin.getNumber();
        }
        final Switch xmax = switches.get(Switch_enum.Xmax);
        if(null != xmax)
        {
            endStop_max[Axis_enum.X.ordinal()] = xmax.getNumber();
        }

        final Switch ymin = switches.get(Switch_enum.Ymin);
        if(null != ymin)
        {
            endStop_min[Axis_enum.Y.ordinal()] = ymin.getNumber();
        }
        final Switch ymax = switches.get(Switch_enum.Ymax);
        if(null != ymax)
        {
            endStop_max[Axis_enum.Y.ordinal()] = ymax.getNumber();
        }

        final Switch zmin = switches.get(Switch_enum.Zmin);
        if(null != zmin)
        {
            endStop_min[Axis_enum.Z.ordinal()] = zmin.getNumber();
        }
        final Switch zmax = switches.get(Switch_enum.Zmax);
        if(null != zmax)
        {
            endStop_max[Axis_enum.Z.ordinal()] = zmax.getNumber();
        }
    }

    private void setEndstopMinEnabled(Axis_enum axis, boolean on)
    {
        if(on != endStopminOn[axis.ordinal()])
        {
            endStopminOn[axis.ordinal()] = on;
            if(-1 < endStop_min[axis.ordinal()])
            {
                if(false == on)
                {
                    stopsOff.add(endStop_min[axis.ordinal()]);
                }
                else
                {
                    stopsOn.add(endStop_min[axis.ordinal()]);
                }
            }
        }
    }

    private void setEndstopMaxEnabled(Axis_enum axis, boolean on)
    {
        if(on != endStopmaxOn[axis.ordinal()])
        {
            endStopmaxOn[axis.ordinal()] = on;
            if(-1 < endStop_max[axis.ordinal()])
            {
                if(false == on)
                {
                    stopsOff.add(endStop_max[axis.ordinal()]);
                }
                else
                {
                    stopsOn.add(endStop_max[axis.ordinal()]);
                }
            }
        }
    }

    private void calculateEndStopsThatNeedToChangeTheirEnabledState()
    {
        if(true == autoEndStopDisable)
        {
            for(Axis_enum axis: Axis_enum.values())
            {
                if(Axis_enum.E == axis)
                {
                    continue;
                }

                if(true == isHomed[axis.ordinal()])
                {
                    final double posOnAxis = curPosition[axis.ordinal()];
                    if((Min[axis.ordinal()] <= posOnAxis) && ((Min[axis.ordinal()] + endstopAllowance)>= posOnAxis))
                    {
                        // position close to min end stop -> min end stop off
                        setEndstopMinEnabled(axis, false);
                    }
                    else if((Max[axis.ordinal()] >= posOnAxis) && (Max[axis.ordinal()] - endstopAllowance) <= posOnAxis)
                    {
                        // position close to end stop -> end stop off
                        setEndstopMaxEnabled(axis, false);
                    }
                    else
                    {
                        // position far away from end stops -> end Stops on
                        setEndstopMinEnabled(axis, true);
                        setEndstopMaxEnabled(axis, true);
                    }
                }
                else
                {
                    // not homed -> end stops on
                    setEndstopMinEnabled(axis, true);
                    setEndstopMaxEnabled(axis, true);
                }
            }
        }
        else
        {
            // end stops always on
            for(Axis_enum axis: Axis_enum.values())
            {
                setEndstopMinEnabled(axis, true);
                setEndstopMaxEnabled(axis, true);
            }
        }
    }

    private void updateEndStopActivation(BasicLinearMove move)
    {
        if(null == planner)
        {
            log.error("Cann not move as no steppers available !");
            return;
        }
        stopsOn = new Vector<Integer>();
        stopsOff = new Vector<Integer>();

        calculateEndStopsThatNeedToChangeTheirEnabledState();
        if(   (false == isHomed[Axis_enum.X.ordinal()])
           || (false == isHomed[Axis_enum.Y.ordinal()])
           || (false == isHomed[Axis_enum.Z.ordinal()]) )
        {
            if(0 < stopsOn.size())
            {
                log.trace("Adding stops On !(not homed)");
                planner.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
                stopsOn.clear();
            }
        }
        if(0 < stopsOff.size())
        {
            log.trace("Adding stops Off !");
            planner.addEndStopOnOffCommand(false, stopsOff.toArray(new Integer[0]));
        }
        if(null != move)
        {
            log.trace("Adding the move!");
            planner.addMove(move);
        }
        if(0 < stopsOn.size())
        {
            log.trace("Adding stops On !");
            planner.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
        }
    }

    public void addStepper(Axis_enum ae, Stepper motor)
    {
        for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
        {
            if(null == Steppers[ae.ordinal()][i])
            {
                Steppers[ae.ordinal()][i] = motor;
                break;
            }
        }
    }

    public boolean setStepsPerMillimeter(Axis_enum axis, Double steps)
    {
        // This is only used for the G-Code function.
        // That G-Code can not distinguish between the steppers for the same axis.
        // So lets assume he means both. Otherwise we would get trouble,...
        for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
        {
            if(null != Steppers[axis.ordinal()][i])
            {
                Steppers[axis.ordinal()][i].setStepsPerMillimeter(steps);
            }
        }
        return true;
    }

    /** This causes all axis to decelerate to a full stop.
    *
    */
   public void letMovementStop()
   {
       log.trace("letting the movement stop");
       if(null == planner)
       {
           log.warn("No Steppers configured ! No movement to stop !");
       }
       else
       {
           planner.flushQueueToClient();
       }
   }

   public void addRelativeMove(RelativeMove relMov)
   {
       log.trace("adding the move {}", relMov);
       final BasicLinearMove aMove = new BasicLinearMove(MaxClientStepsPerSecond);
       log.trace("created Move({}) to hold the move{}.", aMove.getId(), relMov);
       // Feedrate
       if(true == relMov.hasFeedrate())
       {
           FeedrateMmPerMinute = relMov.getFeedrate();
       }
       // else  reuse last Feedrate
       log.trace("Feedrate = {} mm/Minute", FeedrateMmPerMinute);
       aMove.setFeedrateMmPerMinute(FeedrateMmPerMinute);

       for(Axis_enum ax: Axis_enum.values())
       {
           if(true == relMov.has(ax))
           {
               log.debug("adding Axis {}", ax);
               aMove.setDistanceMm(ax, relMov.get(ax));
               curPosition[ax.ordinal()] = curPosition[ax.ordinal()] + relMov.get(ax);
               for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
               {
                   if(null != Steppers[ax.ordinal()][i])
                   {
                       aMove.addMovingAxis(Steppers[ax.ordinal()][i], ax);
                   }
               }
           }
           // else axis not used
       }
       updateEndStopActivation(aMove);
   }

   public void homeAxis(Axis_enum[] axis)
   {
       if(null == planner)
       {
           log.error("Cann not home as no steppers available !");
           return;
       }
       log.trace("homing Axis");
       // TODO Homing direction (inverted = - homingDistance)
       final BasicLinearMove aMove = new BasicLinearMove(MaxClientStepsPerSecond);
       log.trace("created Move({}) to hold the homing move", aMove.getId());
       aMove.setIsHoming(true);
       double homingDistance = 0.0;

       for(int a = 0; a < axis.length; a++)
       {
           final Axis_enum ax = axis[a];
           isHomed[ax.ordinal()] = true;
           if(ax != Axis_enum.E)
           {
               homingDistance = (Max[ax.ordinal()] - Min[ax.ordinal()]) * HOMING_MOVE_SFAETY_FACTOR;
               aMove.setDistanceMm(ax, homingDistance);
               for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
               {
                   if(null != Steppers[ax.ordinal()][i])
                   {
                       aMove.addMovingAxis(Steppers[ax.ordinal()][i], ax);
                   }
               }
           }
           // else nothing to do to home E
       }
       updateEndStopActivation(aMove);
       planner.flushQueueToClient();
   }

    public boolean hasAllMovementFinished()
    {
        if(null == planner)
        {
            return true;
        }
        else
        {
            return planner.hasAllMovementFinished();
        }
    }
}
