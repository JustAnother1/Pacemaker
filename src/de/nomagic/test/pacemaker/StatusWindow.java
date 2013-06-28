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
package de.nomagic.test.pacemaker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class StatusWindow extends JFrame implements Hardware
{
    private static final long serialVersionUID = 1L;
    private final JPanel StatusPanel = new JPanel();
    private final JTextArea curStatus = new JTextArea("not available");

    public StatusWindow()
    {
        this.setTitle("Pacemaker Status");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StatusPanel.add(curStatus);
        this.add(StatusPanel);
        this.pack();
        this.setVisible(true);
    }

    @Override
    public int getNumberSteppers()
    {
        return 5;
    }

    @Override
    public String getNameOfStepper(int idx)
    {
        switch(idx)
        {
        case 0: return "Stepper_1";
        case 1: return "Stepper_2";
        case 2: return "Stepper_3";
        case 3: return "Stepper_4";
        case 4: return "Stepper_5";
        default: return "Invalid Stepper Motor";
        }
    }

    @Override
    public int getNumberHeaters()
    {
        return 3;
    }

    @Override
    public String getNameOfPwm(int idx)
    {
        switch(idx)
        {
        case 0: return "PWM_1";
        case 1: return "PWM_2";
        case 2: return "PWM_3";
        default: return "Invalid PWM Output";
        }
    }

    @Override
    public String getNameOfHeater(int idx)
    {
        switch(idx)
        {
        case 0: return "Heater_1";
        case 1: return "Heater_2";
        case 2: return "Heater_3";
        default: return "Invalid Heater";
        }
    }

    @Override
    public int getNumberPwm()
    {
        return 1;
    }

    @Override
    public int getNumberTempSensor()
    {
        return 4;
    }

    @Override
    public String getNameOfTemperatureSensor(int idx)
    {
        switch(idx)
        {
        case 0: return "Temp_1";
        case 1: return "Temp_2";
        case 2: return "Temp_3";
        case 3: return "Temp_4";
        default: return "Invalid Temperature Sensor";
        }
    }

    @Override
    public int getNumberInput()
    {
        return 10;
    }

    @Override
    public String getNameOfInput(int idx)
    {
        switch(idx)
        {
        case 0: return "Input_1";
        case 1: return "Input_2";
        case 2: return "Input_3";
        case 3: return "Input_4";
        case 4: return "Input_5";
        case 5: return "Input_6";
        case 6: return "Input_7";
        case 7: return "Input_8";
        case 8: return "Input_9";
        case 9: return "Input_10";
        default: return "Invalid Input";
        }
    }

    @Override
    public int getNumberOutput()
    {
        return 13;
    }

    @Override
    public String getNameOfOutput(int idx)
    {
        switch(idx)
        {
        case 0: return "Output_1";
        case 1: return "Output_2";
        case 2: return "Output_3";
        case 3: return "Output_4";
        case 4: return "Output_5";
        case 5: return "Output_6";
        case 6: return "Output_7";
        case 7: return "Output_8";
        case 8: return "Output_9";
        case 9: return "Output_10";
        case 10: return "Output_11";
        case 11: return "Output_12";
        case 12: return "Output_13";
        default: return "Invalid Output";
        }
    }

    @Override
    public int getHardwareType()
    {
        return 0;
    }

    @Override
    public int getHardwareRevision()
    {
        return 0;
    }

    @Override
    public String getFirmwareNameString()
    {
        return "Simulated Pacemaker";
    }

    @Override
    public int[] getListOfSupportedProtocolExtensions()
    {
        final int[] res = {0, 1, 2};
        return res;
    }

    @Override
    public int getFirmwareType()
    {
        return 0;
    }

    @Override
    public String getSerialNumberString()
    {
        return "0815-4711-007";
    }

    @Override
    public String getBoardNameString()
    {
        return "Pacemaker Simulator";
    }

    @Override
    public String getGivenNameString()
    {
        return "SimOne";
    }

    @Override
    public byte getProtocolVersionMajor()
    {
        return 1;
    }

    @Override
    public byte getProtocolVersionMinor()
    {
        return 0;
    }

    @Override
    public int getFirmwareRevisionMajor()
    {
        return 7;
    }

    @Override
    public int getFirmwareRevisionMinor()
    {
        return 3;
    }

    @Override
    public int getNumberBuzzer()
    {
        return 2;
    }

}
