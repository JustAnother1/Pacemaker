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

    private MotionSender sender;
    private MovementQueue PlannerQueue = new MovementQueue("XyzTable");

    private double FeedrateMmPerMinute = 0;

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

    private Vector<StepperMove> updateEndStopActivation(StepperMove move)
    {
        stopsOn = new Vector<Integer>();
        stopsOff = new Vector<Integer>();

        calculateEndStopsThatNeedToChangeTheirEnabledState();

        final Vector<StepperMove> res = new Vector<StepperMove>();
        if(   (false == isHomed[Axis_enum.X.ordinal()])
           || (false == isHomed[Axis_enum.Y.ordinal()])
           || (false == isHomed[Axis_enum.Z.ordinal()]) )
        {
            if(0 < stopsOn.size())
            {
                log.trace("Adding stops On !(not homed)");
                final StepperMove sm = new StepperMove();
                sm.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
                res.add(sm);
                stopsOn.clear();
            }
        }
        if(0 < stopsOff.size())
        {
            log.trace("Adding stops Off !");
            final StepperMove sm = new StepperMove();
            sm.addEndStopOnOffCommand(false, stopsOff.toArray(new Integer[0]));
            res.add(sm);
        }
        if(null != move)
        {
            log.trace("Adding the move!");
            res.add(move);
        }
        if(0 < stopsOn.size())
        {
            log.trace("Adding stops On !");
            final StepperMove sm = new StepperMove();
            sm.addEndStopOnOffCommand(true, stopsOn.toArray(new Integer[0]));
            res.add(sm);
        }
        log.trace("Returning {} moves", res.size());
        return res;
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
               curPosition[ax.ordinal()] = curPosition[ax.ordinal()] + relMov.get(ax);
               res.setMm(relMov.get(ax), ax);
               final double Speed = Math.abs(relMov.get(ax) * SpeedPerMm);
               log.trace("{}Speed = {}", ax, Speed);
               for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
               {
                   if(null != Steppers[ax.ordinal()][i])
                   {
                       Steppers[ax.ordinal()][i].addMove(relMov.get(ax));
                       Steppers[ax.ordinal()][i].setMaxSpeedMmPerSecond(Speed);
                       res.addAxisMotors(Steppers[ax.ordinal()][i]);
                   }
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

       for(int a = 0; a < axis.length; a++)
       {
           final Axis_enum ax = axis[a];
           isHomed[ax.ordinal()] = true;
           if(ax != Axis_enum.E)
           {
               homingDistance = (Max[ax.ordinal()] - Min[ax.ordinal()]) * HOMING_MOVE_SFAETY_FACTOR;
               res.setMm(homingDistance, ax);
               for(int i = 0; i < MAX_STEPPERS_PER_AXIS; i++)
               {
                   if(null != Steppers[ax.ordinal()][i])
                   {
                       Steppers[ax.ordinal()][i].addMove(homingDistance);
                       res.addAxisMotors(Steppers[ax.ordinal()][i]);
                   }
               }
           }
           // else nothing to do to home E
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
           log.trace("PlannerQueue.size = {}", PlannerQueue.size());
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
       final double[] firstVector =new double[Axis_enum.size];
       for(Axis_enum axis: Axis_enum.values())
       {
           firstVector[axis.ordinal()] = firstMove.getMm(axis);
       }
       log.trace("getting the move [{},{},z]", firstVector[Axis_enum.X.ordinal()], firstVector[Axis_enum.Y.ordinal()]);

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
           secondVector = new double[Axis_enum.size];
           for(Axis_enum axis: Axis_enum.values())
           {
               secondVector[axis.ordinal()] = curMove.getMm(axis);
           }
           log.trace("getting the move [{},{},z]", secondVector[Axis_enum.X.ordinal()],
                                                   secondVector[Axis_enum.Y.ordinal()]);
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
            final double[] curVector = new double[Axis_enum.size];
            for(Axis_enum axis: Axis_enum.values())
            {
                curVector[axis.ordinal()] = curMove.getMm(axis);
            }
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
        double max = 0.0;
        for(Axis_enum axis: Axis_enum.values())
        {
            max = Math.max(max, cornerBreakFactor(vec_one[axis.ordinal()], vec_two[axis.ordinal()]));
        }
        return max;
    }

    private boolean hasMovementData(double[] vec)
    {
        for(Axis_enum axis: Axis_enum.values())
        {
            if(MIN_MOVEMENT_DISTANCE < Math.abs(vec[axis.ordinal()]))
            {
                return true;
            }
        }
        return false;
    }

    private double[] normalize(double[] vec)
    {
        double sum = 0.0;
        for(Axis_enum axis: Axis_enum.values())
        {
            sum = sum + Math.abs(vec[axis.ordinal()]);
        }
        for(Axis_enum axis: Axis_enum.values())
        {
            vec[axis.ordinal()] = vec[axis.ordinal()] / sum;
        }
        return vec;
    }

}
