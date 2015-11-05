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
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Switch_enum;

/** Decodes Strings and gives the result to the Executor.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class GCodeDecoder
{
    public static final double Inch_in_Milimeter = 25.4;

    public static final int RESULT_OK    = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_VALUE = 2;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Executor exe;

    // G-Code State
    private final double[] curPosition= new double[Axis_enum.size + 1]; // last entry is Feedrate
    private boolean isRelative = false;
    private boolean isExtruderRelative = false;
    private boolean isMetric = true;
    private String lastErrorReason = null;
    private String ResultValue = "";
    private String selectedSDCardFile = "";
    private boolean FileTransferModeActive = false;

    private boolean firstLine = true;
    private int lastLineNumber = 0;
    private final SDCardSimulation sdCard;
    private SDCardPrinter sdPrinterWorker;
    private GCode masterCode;

    public GCodeDecoder(final Executor plan, SDCardSimulation sdCard)
    {
        this.exe = plan;
        this.sdCard = sdCard;
        masterCode = new GCodeImpl("");
        for(int i = 0; i < curPosition.length; i++)
        {
            curPosition[i] = 0.0;
        }
    }

    public void addSDCardPrinter(SDCardPrinter printer)
    {
        sdPrinterWorker = printer;
    }

    public void setGCodeImplementation(GCode masterCode)
    {
        this.masterCode = masterCode;
    }

    public String sendLine(final String line, final GCodeResultStream resultStream)
    {
        lastErrorReason = null;
        if(null == line) {return "";}
        if(1 > line.length()) {return "";}
        final GCode code = masterCode.getGCodeFrom(line);
        if(true == FileTransferModeActive)
        {
            final Double M = code.getWordValue('M');
            if(29 == M.intValue()) // M29: Stop writing to SD Card
            {
                sdCard.closeFile();
                FileTransferModeActive = false;
                return "ok";
            }
            else
            {
                final boolean res = sdCard.appendToFile(line);
                if(false == res)
                {
                    lastErrorReason = sdCard.getLastErrorReason();
                    return "!! " + lastErrorReason;
                }
                else
                {
                    return "ok";
                }
            }
        }
        if(true == code.isEmpty()) {return "";}
        if(false == code.isValid())
        {
            lastErrorReason = "G-Code is invalid !";
            log.error(lastErrorReason);
            return "!! " + lastErrorReason;
        }

        int result = RESULT_ERROR;
        if(true == code.hasWord('N'))
        {
            // This line has a Line Number and Checksum

            // read checksum
            final int readCheckSum = (code.getWordValue('*')).intValue();
            // calculate checksum
            final int calculatedCheckSum = getCalculatedChecksum(line);
            // compare
            if(readCheckSum != calculatedCheckSum)
            {
                return "rs " + lastLineNumber + 1;
            }
            // read line Number
            final int lineNumber = (code.getWordValue('N')).intValue();

            // check line Number
            if(false == firstLine)
            {
                if(lineNumber != lastLineNumber + 1)
                {
                    return "rs " + lastLineNumber + 1;
                }
                else
                {
                    lastLineNumber = lineNumber;
                }
            }
            else
            {
                // this is the first line
                firstLine = false;
                lastLineNumber = lineNumber;
            }
        }
        if(true == code.hasWord('G'))
        {
            result = decode_General_Function_Code(code);
        }
        else if(true == code.hasWord('M'))
        {
            result = decode_Miscellaneous_Function_Code(code, resultStream);
        }
        else if(true == code.hasWord('T'))
        {
            result = decode_Tool_Function_Code(code);
        }
        else
        {
            lastErrorReason = "Line has no G, M or T Code !";
            log.error(lastErrorReason);
            return "!! " + lastErrorReason;
        }
        if(RESULT_OK == result)
        {
            return "ok";
        }
        else if(RESULT_ERROR == result)
        {
            return "!! " + lastErrorReason;
        }
        else if(RESULT_VALUE == result)
        {
            return ResultValue;
        }
        else
        {
            // should not happen
            return "";
        }
    }

    public String getLastErrorReason()
    {
        if(null == lastErrorReason)
        {
            return exe.getLastErrorReason();
        }
        else
        {
            return lastErrorReason;
        }
    }

    public void close()
    {
    }

    private int getCalculatedChecksum(String line)
    {
        // Definition on http://reprap.org/wiki/G-code#N_and_.2A:
        //int cs = 0;
        // for(i = 0; cmd[i] != '*' && cmd[i] != NULL; i++)
        //   cs = cs ^ cmd[i];
        //cs &= 0xff;  // Defensive programming...

        int cs = 0;
        for(int i = 0; line.charAt(i) != '*' && i < line.length(); i++)
        {
           cs = cs ^ line.charAt(i);
        }
        cs &= 0xff;  // Defensive programming...

        return cs;
    }

    private int decode_Miscellaneous_Function_Code(final GCode code, GCodeResultStream resultStream)
    {
        final Double Number = code.getWordValue('M');
        final int num = Number.intValue();
        switch(num)
        {
        case 0: // Stop Print
            if(false == exe.doShutDown()){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 17: // Enable/Power all stepper motors
            if(false == exe.enableAllStepperMotors()){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 20: // List SD Card Files
            final String[] files = sdCard.getListOfFiles();
            if(1 >files.length)
            {
                ResultValue = "ok Files: {}";
            }
            else
            {
                final StringBuffer sb = new StringBuffer();
                sb.append("Files:{ ");
                for(int i = 0; i < files.length; i++)
                {
                    sb.append(files[i] + ",");
                }
                sb.append("}");
                ResultValue = sb.toString();
            }
            return RESULT_OK;

        case 21: // Initialize SD Card (   mount the SD Card
        case 22: // Release the SD card (unmount the SD Card))
            return RESULT_OK;

        case 23: // select SD File
            selectedSDCardFile = code.getLineWithoutCommentWithoutWord('M');
            return RESULT_OK;

        case 24: // start / resume SD print
            if(false == sdPrinterWorker.startResumePrinting(selectedSDCardFile)){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 25: // pause SD print
            sdPrinterWorker.pausePrinting();
            return RESULT_OK;

        case 26: // set SD position
            Double bytePosition = code.getWordValue('S');
            if(false == sdPrinterWorker.setSDCardPosition(bytePosition.longValue())){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 27: // report SD print status
            ResultValue = sdPrinterWorker.getPrintStatus();
            return RESULT_OK;

        case 28: // begin write to SD Card
            if(false == sdCard.createAndOpenNewFile(code.getLineWithoutCommentWithoutWord('M')))
            {
                lastErrorReason = sdCard.getLastErrorReason();
                return RESULT_ERROR;
            }
            else
            {
                FileTransferModeActive = true;
                return RESULT_OK;
            }

        case 29: // Stop writing to SD Card
            return RESULT_ERROR; // not started

        case 30: // delete a file on the SD Card
            if(false == sdCard.deleteFile(code.getLineWithoutCommentWithoutWord('M')))
            {
                lastErrorReason = sdCard.getLastErrorReason();
                return RESULT_ERROR;
            }
            else
            {
                return RESULT_OK;
            }

        case 32: // select file and start printing
            selectedSDCardFile = code.getLineWithoutCommentWithoutWord('M');
            if(false == sdPrinterWorker.startResumePrinting(selectedSDCardFile)){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 82: // set Extruder to absolute Mode
            isExtruderRelative = false;
            return RESULT_OK;

        case 83: // set Extruder to relative mode
            isExtruderRelative = true;
            return RESULT_OK;

        case 18: // Disable all stepper motors
        case 84: // Stop Idle hold
            if(false == exe.disableAllStepperMotors()){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 92: // Set axis_steps_per_unit
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    if(true == isMetric)
                    {
                        if(false == exe.setStepsPerMilimeter(axel, code.getWordValue(axel.getChar())))
                        {
                            return RESULT_ERROR;
                        }
                    }
                    else
                    {
                        if(false == exe.setStepsPerMilimeter(axel, code.getWordValue(axel.getChar())/Inch_in_Milimeter ))
                        {
                            return RESULT_ERROR;
                        }
                    }
                }
            }
            return RESULT_OK;

        case 104: // Set Extruder Temperature - no wait
            if(false == exe.setCurrentExtruderTemperatureNoWait(code.getWordValue('S'))){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 105: // Get Extruder Temperature
            ResultValue = "T:" + exe.getCurrentExtruderTemperature() + " B:" + exe.getHeatedBedTemperature();
            return RESULT_VALUE;

        case 106: // set Fan Speed
            // The fan Speed in S is 0..255 with 0=off 255=full speed.
            // The Fan speed in Pacemaker is 0=off 0xffff = full speed
            int speed = (code.getWordValue('S').intValue() * 256) + code.getWordValue('S').intValue();
            if(true == code.hasWord('P'))
            {
                if(false == exe.setFanSpeedfor(code.getWordValue('P').intValue(), speed)){ return RESULT_ERROR;} else {return RESULT_OK;}
            }
            else
            {
                if(false == exe.setFanSpeedfor(0, speed)){ return RESULT_ERROR;} else {return RESULT_OK;}
            }

        case 107: // Fan off - deprecated
            log.warn("G-Code M107 is deprecated! Use M106 S0 instead.");
            if(true == code.hasWord('P'))
            {
                if(false == exe.setFanSpeedfor(code.getWordValue('P').intValue(), 0)){ return RESULT_ERROR;} else {return RESULT_OK;}
            }
            else
            {
                if(false == exe.setFanSpeedfor(0, 0)){ return RESULT_ERROR;} else {return RESULT_OK;}
            }

        case 109: // Set Extruder Temperature and wait
            if(false == exe.setCurrentExtruderTemperatureAndDoWait(code.getWordValue('S'), resultStream))
            {
                return RESULT_ERROR;
            }
            else
            {
                return RESULT_OK;
            }

        case 110: // Set current Line Number
            if(true == code.hasWord('N'))
            {
                lastLineNumber = (code.getWordValue('N')).intValue();
                return RESULT_OK;
            }
            else
            {
                return RESULT_ERROR;
            }

        case 112: // Emergency Stop
            if(false == exe.doImmediateShutDown()){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 115: // get Firmware Version and capabilities
        {
            final StringBuffer sb = new StringBuffer();
            sb.append("FIRMWARE_NAME:Pacemaker");
            sb.append(" FIRMWARE_VERSION:0.1");
            sb.append(" FIRMWARE_URL:https://github.com/JustAnother1/Pacemaker");
            ResultValue = sb.toString();
        }
            return RESULT_VALUE;

        case 116: // wait for Heaters
            if(false == exe.waitForEverythingInLimits(resultStream))
            {
                return RESULT_ERROR;
            }
            else
            {
                return RESULT_OK;
            }

        case 117: // Display Message on LCD
            log.info("Message for Display: " + code.getLineWithoutCommentWithoutWord('M'));
            return RESULT_OK;

        case 119: // interpreted status of end stop switches
        {
            final StringBuffer sb = new StringBuffer();
            sb.append("Reporting endstop status\r\n");
            // X min
            sb.append("x_min: ");
            int swstate = exe.getStateOfSwitch(Switch_enum.Xmin);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            // X max
            sb.append("x_max: ");
            swstate = exe.getStateOfSwitch(Switch_enum.Xmax);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            // Y min
            sb.append("y_min: ");
            swstate = exe.getStateOfSwitch(Switch_enum.Ymin);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            // Y max
            sb.append("y_max: ");
            swstate = exe.getStateOfSwitch(Switch_enum.Ymax);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            // Z min
            sb.append("z_min: ");
            swstate = exe.getStateOfSwitch(Switch_enum.Zmin);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            // Z max
            sb.append("z_max: ");
            swstate = exe.getStateOfSwitch(Switch_enum.Zmax);
            sb.append(getDescriptionOfSwitchState(swstate));
            sb.append("\r\n");
            sb.append("ok\r\n");
            ResultValue = sb.toString();
        }
            return RESULT_VALUE;

        case 140: // set Bed Temperature - no wait
            if(false == exe.setPrintBedTemperatureNoWait(code.getWordValue('S'))){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 141: // set chamber temperature - no wait
            if(false == exe.setChamberTemperatureNoWait(code.getWordValue('S'))){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 190: // set Bed Temperature - and do wait
            if(false == exe.setPrintBedTemperatureAndDoWait(code.getWordValue('S'), resultStream))
            {
                return RESULT_ERROR;
            }
            else
            {
                return RESULT_OK;
            }
        case 303:
            Heater_enum ext = Heater_enum.Extruder_0;
            double extruder = code.getWordValue('E', 1.0);
            if(0 > extruder)
            {
                ext = Heater_enum.Print_Bed;
            }
            if(1.5 > extruder)
            {
                ext = Heater_enum.Extruder_1;
            }
            if(2.5 > extruder)
            {
                ext = Heater_enum.Extruder_2;
            }

            if(false == exe.runPIDautotune(ext,
                                           code.getWordValue('S'),
                                           (int)Math.round(code.getWordValue('C', 3.0)),
                                           resultStream))
            {
                return RESULT_ERROR;
            }
            else
            {
                return RESULT_OK;
            }

        default:
            lastErrorReason = "M" + num + " not yet implemented !";
            log.error(lastErrorReason);
            return RESULT_ERROR;
        }
    }

    private int decode_General_Function_Code(final GCode code)
    {
        final Double Number = code.getWordValue('G');
        final int num = Number.intValue();
        switch(num)
        {
        case 0: // Rapid Linear Motion
        case 1: // Linear Motion at Feed Rate
            if(false == exe.addMoveTo(getRelativeMovefor(code))){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 4: // Dwell
            if(false == exe.addPauseFor(code.getWordValue('P'))){ return RESULT_ERROR;} else {return RESULT_OK;}

        case 10: // Set Coordinate System Data
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    curPosition[axel.ordinal()] = curPosition[axel.ordinal()] - code.getWordValue(axel.getChar());
                }
            }
            return RESULT_OK;

        case 20: // Length Units : Inches
            isMetric = false;
            return RESULT_OK;

        case 21: // Length Units : millimeters
            isMetric = true;
            return RESULT_OK;

        case 28: // Home
            final Vector<Axis_enum> homingAxis = new Vector<Axis_enum>();
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    homingAxis.add(axel);
                }
            }
            if(true == homingAxis.isEmpty())
            {
                homingAxis.add(Axis_enum.X);
                homingAxis.add(Axis_enum.Y);
                homingAxis.add(Axis_enum.Z);
            }
            if(false == exe.startHoming(homingAxis.toArray(new Axis_enum[0])))
            {
                lastErrorReason = exe.getLastErrorReason();
                return RESULT_ERROR;
            }
            for(int i = 0; i < homingAxis.size(); i++)
            {
                curPosition[homingAxis.get(i).ordinal()] = 0;
            }
            return RESULT_OK;

        case 90: // Set Distance Mode : absolute
            isRelative = false;
            return RESULT_OK;

        case 91: // Set Distance Mode : incremental
            isRelative = true;
            return RESULT_OK;

        case 92: // Coordinate System Offsets
            for(Axis_enum axel : Axis_enum.values())
            {
                if(true == code.hasWord(axel.getChar()))
                {
                    curPosition[axel.ordinal()] = code.getWordValue(axel.getChar());
                }
            }
            return RESULT_OK;

        default:
            lastErrorReason = "G" + num + " not yet implemented !";
            log.error(lastErrorReason);
            return RESULT_ERROR;
        }
    }

    private int decode_Tool_Function_Code(GCode code)
    {
        final Double Number = code.getWordValue('T');
        final int num = Number.intValue();
        if(true == exe.switchExtruderTo(num))
        {
            return RESULT_OK;
        }
        else
        {
            return RESULT_ERROR;
        }
    }

    private double getRelativeMoveForAxisE(final GCode code, final Character axis)
    {
        if(true == isExtruderRelative)
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
            final int index = Axis_enum.E.ordinal();
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

    private double getRelativeMoveForOtherAxis(final GCode code, final Character axis)
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
            case 'X': index = Axis_enum.X.ordinal(); break;
            case 'Y': index = Axis_enum.Y.ordinal(); break;
            case 'Z': index = Axis_enum.Z.ordinal(); break;
            case 'E': index = Axis_enum.E.ordinal(); break;
            case 'F': index = curPosition.length -1; break;
            default:
                log.error("Requested Move for Illigal Axis {} !", axis);
                return 0.0;
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

    private double getRelativeMoveForAxis(final GCode code, final Character axis)
    {
        if(true == code.hasWord(axis))
        {
            if('E' == axis)
            {
                return getRelativeMoveForAxisE(code, axis);
            }
            else
            {
                return getRelativeMoveForOtherAxis(code, axis);
            }
        }
        else
        {
            return 0.0;
        }
    }

    private RelativeMove getRelativeMovefor(final GCode code)
    {
        final RelativeMove move = new RelativeMove();
        if(true == code.hasWord('X'))
        {
            move.setX(getRelativeMoveForAxis(code, 'X'));
            curPosition[Axis_enum.X.ordinal()] = curPosition[Axis_enum.X.ordinal()] + getRelativeMoveForAxis(code, 'X');
        }
        if(true == code.hasWord('Y'))
        {
            move.setY(getRelativeMoveForAxis(code, 'Y'));
            curPosition[Axis_enum.Y.ordinal()] = curPosition[Axis_enum.Y.ordinal()] + getRelativeMoveForAxis(code, 'Y');
        }
        if(true == code.hasWord('Z'))
        {
            move.setZ(getRelativeMoveForAxis(code, 'Z'));
            curPosition[Axis_enum.Z.ordinal()] = curPosition[Axis_enum.Z.ordinal()] + getRelativeMoveForAxis(code, 'Z');
        }
        if(true == code.hasWord('E'))
        {
            move.setE(getRelativeMoveForAxis(code, 'E'));
            curPosition[Axis_enum.E.ordinal()] = curPosition[Axis_enum.E.ordinal()] + getRelativeMoveForAxis(code, 'E');
        }
        if(true == code.hasWord('F'))
        {
            move.setF(getRelativeMoveForAxis(code, 'F'));
            curPosition[curPosition.length -1] = curPosition[curPosition.length -1] + getRelativeMoveForAxis(code, 'F');
        }
        return move;
    }

    private String getDescriptionOfSwitchState(int switchState)
    {
        switch(switchState)
        {
        case Executor.SWITCH_STATE_OPEN:
            return "open";
        case Executor.SWITCH_STATE_CLOSED:
            return"TRIGGERED";
        default:
            return "unknown";
        }
    }
}
