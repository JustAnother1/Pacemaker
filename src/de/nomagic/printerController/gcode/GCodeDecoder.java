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
package de.nomagic.printerController.gcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.planner.Planner;
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class GCodeDecoder
{
    public final static double Inch_in_Milimeter = 25.4;


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Planner plan;

    // G-Code State
    private final double[] curPosition;
    private boolean isRelative = false;
    private boolean isMetric = true;


    /**
     *
     */
    public GCodeDecoder(final Planner plan)
    {
        this.plan = plan;
        curPosition = new double[Cfg.NUMBER_OF_AXIS];
        for(int i = 0; i < curPosition.length; i++)
        {
            curPosition[i] = 0.0;
        }
    }

    public boolean sendLine(final String line)
    {
        if(null == line) return true;
        if(1 > line.length()) return true;
        final GCode code = new GCode(line);
        if(true == code.isEmpty()) return true;
        if(false == code.isValid()) return false;
        if(true == code.hasWord('G'))
        {
            return decode_General_Function_Code(code);
        }
        else if(true == code.hasWord('M'))
        {
            return decode_Miscellaneous_Function_Code(code);
        }
        else
        {
            log.error("Line has no G and no M Code !");
            return false;
        }
    }

    private boolean decode_Miscellaneous_Function_Code(final GCode code)
    {
        final Double Number = code.getWordValue('M');
        final int num = Number.intValue();
        switch(num)
        {
        case 0: // Stop Print
            return plan.dohutDown();

        case 17: // Enable/Power all stepper motors
            return plan.enableAllStepperMotors();

        case 18: // Disable all stepper motors
            return plan.disableAllStepperMotors();

        case 92: // Set axis_steps_per_unit
            for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
            {
                if(true == code.hasWord(Cfg.axisNames[i]))
                {
                    if(true == isMetric)
                    {
                        plan.setStepsPerMilimeter(i, code.getWordValue(Cfg.axisNames[i]));
                    }
                    else
                    {
                        plan.setStepsPerMilimeter(i, code.getWordValue(Cfg.axisNames[i])/Inch_in_Milimeter );
                    }
                }
            }
            return true;

        case 104: // Set Extruder Temperature - no wait
            return plan.setCurrentExtruderTemperatureNoWait(code.getWordValue('S'));

        case 107: // Fan off - deprecated
            log.warn("G-Code M107 is deprecated! Use M106 S0 instead.");
            if(true == code.hasWord('P'))
            {
                return plan.setFanSpeedfor(code.getWordValue('P').intValue(), 0);
            }
            else
            {
                return plan.setFanSpeedfor(0, 0);
            }

        case 106: // set Fan Speed
            if(true == code.hasWord('P'))
            {
                return plan.setFanSpeedfor(code.getWordValue('P').intValue(), code.getWordValue('S').intValue());
            }
            else
            {
                return plan.setFanSpeedfor(0, code.getWordValue('S').intValue());
            }

        case 109: // Set Extruder Temperature and wait
            return plan.setCurrentExtruderTemperatureAndDoWait(code.getWordValue('S'));

        case 112: // Emergency Stop
            return plan.doImmediateShutDown();

        case 116: // wait for Heaters
            return plan.waitForEverythingInLimits();

        case 140: // set Bed Temperature - no wait
            return plan.setPrintBedTemperatureNoWait(code.getWordValue('S'));

        case 190: // set Bed Temperature - and do wait
            return plan.setPrintBedTemperatureAndDoWait(code.getWordValue('S'));

        case 141: // set chamber temperature - no wait
            return plan.setChamberTemperatureNoWait(code.getWordValue('S'));

        default:
            log.error("M{} not yet implemented !", num);
            return false;
        }
    }

    private boolean decode_General_Function_Code(final GCode code)
    {
        final Double Number = code.getWordValue('G');
        final int num = Number.intValue();
        switch(num)
        {
        case 0: // Rapid Linear Motion
        case 1: // Linear Motion at Feed Rate
            return plan.addMoveTo(getRelativeMovefor(code));

        case 4: // Dwell
            return plan.addPauseFor(code.getWordValue('P'));

        case 10: // Set Coordinate System Data
            for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
            {
                if(true == code.hasWord(Cfg.axisNames[i]))
                {
                    curPosition[i] = curPosition[i] - code.getWordValue(Cfg.axisNames[i]);
                }
            }
            return true;

        case 20: // Length Units : Inches
            isMetric = false;
            return true;

        case 21: // Length Units : millimeters
            isMetric = true;
            return true;

        case 28: // Home
        case 30:
            boolean home_all = true;
            for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
            {
                if(true == code.hasWord(Cfg.axisNames[i]))
                {
                    if(false == plan.addHomeAxis(i))
                    {
                        return false;
                    }
                    home_all = false;
                }
            }
            if(true == home_all)
            {
                for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
                {
                    if(false == plan.addHomeAxis(i))
                    {
                        return false;
                    }
                }
            }
            plan.startHoming();
            plan.waitForEndOfHoming();
            return true;

        case 90: // Set Distance Mode : absolute
            isRelative = false;
            return true;

        case 91: // Set Distance Mode : incremental
            isRelative = true;
            return true;

        case 92: // Coordinate System Offsets
            for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
            {
                if(true == code.hasWord(Cfg.axisNames[i]))
                {
                    curPosition[i] = code.getWordValue(Cfg.axisNames[i]);
                }
            }
            return true;

        default:
            log.error("G{} not yet implemented !", num);
            return false;
        }
    }

    private double getRelativeMoveForAxis(final GCode code, final Character axis)
    {
        if(true == code.hasWord(axis))
        {
            if(true == isRelative)
            {
                if(true == isMetric)
                {
                    return code.getWordValue(axis);
                }
                else
                {
                    // Inches
                    return (code.getWordValue(axis) * Inch_in_Milimeter);
                }
            }
            else
            {
                if(true == isMetric)
                {
                    return code.getWordValue(axis) - curPosition[Cfg.POS_X];
                }
                else
                {
                    // Inches
                    return ((code.getWordValue(axis) - curPosition[Cfg.POS_X]) * Inch_in_Milimeter);
                }
            }
        }
        else
        {
            return 0.0;
        }
    }

    private double[] getRelativeMovefor(final GCode code)
    {
        final double[] move = new double[Cfg.NUMBER_OF_AXIS];
        for(int i = 0; i < Cfg.NUMBER_OF_AXIS; i++)
        {
            move[i] = getRelativeMoveForAxis(code, Cfg.axisNames[i]);
            curPosition[i] =  curPosition[i] + move[i];
        }
        return move;
    }

}
