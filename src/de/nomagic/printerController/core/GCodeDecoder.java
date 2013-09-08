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
package de.nomagic.printerController.core;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class GCodeDecoder
{
    public final static double Inch_in_Milimeter = 25.4;


    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Executor exe;

    // G-Code State
    private final double[] curPosition= new double[5]; // x,y,z,e,f
    private boolean isRelative = false;
    private boolean isMetric = true;
    private String LastErrorReason = null;

    /**
     *
     */
    public GCodeDecoder(final Executor plan)
    {
        this.exe = plan;
        for(int i = 0; i < curPosition.length; i++)
        {
            curPosition[i] = 0.0;
        }
    }

    public boolean sendLine(final String line)
    {
        LastErrorReason = null;
        if(null == line) return true;
        if(1 > line.length()) return true;
        final GCode code = new GCode(line);
        if(true == code.isEmpty()) return true;
        if(false == code.isValid())
        {
            LastErrorReason = "G-Code is invalid !";
            log.error(LastErrorReason);
            return false;
        }
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
            LastErrorReason = "Line has no G and no M Code !";
            log.error(LastErrorReason);
            return false;
        }
    }

    public String getLastErrorReason()
    {
        if(null == LastErrorReason)
        {
            return exe.getLastErrorReason();
        }
        else
        {
            return LastErrorReason;
        }
    }

    private boolean decode_Miscellaneous_Function_Code(final GCode code)
    {
        final Double Number = code.getWordValue('M');
        final int num = Number.intValue();
        switch(num)
        {
        case 0: // Stop Print
            return exe.doShutDown();

        case 17: // Enable/Power all stepper motors
            return exe.enableAllStepperMotors();

        case 18: // Disable all stepper motors
            return exe.disableAllStepperMotors();

        case 92: // Set axis_steps_per_unit
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    if(true == isMetric)
                    {
                        if(false == exe.setStepsPerMilimeter(axel, code.getWordValue(axel.getChar())))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if(false == exe.setStepsPerMilimeter(axel, code.getWordValue(axel.getChar())/Inch_in_Milimeter ))
                        {
                            return false;
                        }
                    }
                }
            }
            return true;

        case 104: // Set Extruder Temperature - no wait
            return exe.setCurrentExtruderTemperatureNoWait(code.getWordValue('S'));

        case 107: // Fan off - deprecated
            log.warn("G-Code M107 is deprecated! Use M106 S0 instead.");
            if(true == code.hasWord('P'))
            {
                return exe.setFanSpeedfor(code.getWordValue('P').intValue(), 0);
            }
            else
            {
                return exe.setFanSpeedfor(0, 0);
            }

        case 106: // set Fan Speed
            if(true == code.hasWord('P'))
            {
                return exe.setFanSpeedfor(code.getWordValue('P').intValue(), code.getWordValue('S').intValue());
            }
            else
            {
                return exe.setFanSpeedfor(0, code.getWordValue('S').intValue());
            }

        case 109: // Set Extruder Temperature and wait
            return exe.setCurrentExtruderTemperatureAndDoWait(code.getWordValue('S'));

        case 112: // Emergency Stop
            return exe.doImmediateShutDown();

        case 116: // wait for Heaters
            return exe.waitForEverythingInLimits();

        case 140: // set Bed Temperature - no wait
            return exe.setPrintBedTemperatureNoWait(code.getWordValue('S'));

        case 190: // set Bed Temperature - and do wait
            return exe.setPrintBedTemperatureAndDoWait(code.getWordValue('S'));

        case 141: // set chamber temperature - no wait
            return exe.setChamberTemperatureNoWait(code.getWordValue('S'));

        default:
            LastErrorReason = "M" + num + " not yet implemented !";
            log.error(LastErrorReason);
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
            return exe.addMoveTo(getRelativeMovefor(code));

        case 4: // Dwell
            return exe.addPauseFor(code.getWordValue('P'));

        case 10: // Set Coordinate System Data
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    curPosition[axel.ordinal()] = curPosition[axel.ordinal()] - code.getWordValue(axel.getChar());
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
            Vector<Axis_enum> homingAxis = new Vector<Axis_enum>();
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    homingAxis.add(axel);
                }
            }
            if(false == exe.startHoming(homingAxis.toArray(new Axis_enum[0])))
            {
                return false;
            }
            if(false == exe.waitForEndOfHoming())
            {
                return false;
            }
            return true;

        case 90: // Set Distance Mode : absolute
            isRelative = false;
            return true;

        case 91: // Set Distance Mode : incremental
            isRelative = true;
            return true;

        case 92: // Coordinate System Offsets
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    curPosition[axel.ordinal()] = code.getWordValue(axel.getChar());
                }
            }
            return true;

        default:
            LastErrorReason = "G" + num + " not yet implemented !";
            log.error(LastErrorReason);
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
                int index = -1;
                switch(axis)
                {
                case 'x': index = Axis_enum.X.ordinal(); break;
                case 'y': index = Axis_enum.Y.ordinal(); break;
                case 'z': index = Axis_enum.Z.ordinal(); break;
                case 'e': index = Axis_enum.E.ordinal(); break;
                case 'f': index = Axis_enum.E.ordinal() + 1; break;
                }
                if(true == isMetric)
                {
                    return code.getWordValue(axis) - curPosition[index];
                }
                else
                {
                    // Inches
                    return ((code.getWordValue(axis) - curPosition[index]) * Inch_in_Milimeter);
                }
            }
        }
        else
        {
            return 0.0;
        }
    }

    private RelativeMove getRelativeMovefor(final GCode code)
    {
        RelativeMove move = new RelativeMove(
                getRelativeMoveForAxis(code, 'x'),
                getRelativeMoveForAxis(code, 'y'),
                getRelativeMoveForAxis(code, 'z'),
                getRelativeMoveForAxis(code, 'e'),
                getRelativeMoveForAxis(code, 'f')
                );

        curPosition[0] = curPosition[0] + getRelativeMoveForAxis(code, 'x');
        curPosition[1] = curPosition[1] + getRelativeMoveForAxis(code, 'y');
        curPosition[2] = curPosition[2] + getRelativeMoveForAxis(code, 'z');
        curPosition[3] = curPosition[3] + getRelativeMoveForAxis(code, 'e');
        curPosition[4] = curPosition[4] + getRelativeMoveForAxis(code, 'f');
        return move;
    }

    public void close()
    {
    }

}