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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;


/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class StatusWindow extends JFrame implements Hardware, ActionListener
{
    public static final int NUMBER_OF_TEMPERATURE_SENSORS = 4;
    public static final int NUMBER_OF_HEATERS = 3;

    private static final String TIMER_ACTION_COMMAND = "timer";
    private static final long serialVersionUID = 1L;
    private final JPanel StatusPanel = new JPanel();
    private final JPanel ControlPanel = new JPanel();
    private final JTextArea curStatus = new JTextArea("not available");

    private boolean isInStoppedState = true;
    private Timer timer;
    private ProtocolClient pc;

    private int[] temperature = new int[NUMBER_OF_TEMPERATURE_SENSORS];
    private int[] heaterTargetTemperature = new int[NUMBER_OF_HEATERS];
    private int[] mappedTemperatureSensor = new int[NUMBER_OF_HEATERS];

    public StatusWindow()
    {
        this.setTitle("Pacemaker Status");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Status Panel
        StatusPanel.setBorder(BorderFactory.createTitledBorder(
                              BorderFactory.createLineBorder(Color.black),
                              "Status"));
        curStatus.setEditable(false);
        StatusPanel.add(curStatus);
        // Control Panel
        ControlPanel.setBorder(BorderFactory.createTitledBorder(
                               BorderFactory.createLineBorder(Color.black),
                               "Control"));

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                    ControlPanel,
                                                    StatusPanel);
        this.add(splitPane);

        this.setResizable(true);
        this.pack();
        this.setVisible(true);
        timer = new Timer(100, this);
        timer.setInitialDelay(1);
        timer.setActionCommand(TIMER_ACTION_COMMAND);
        timer.start();
    }

    public boolean isInStoppedState()
    {
        return isInStoppedState;
    }

    public void setInStoppedState(boolean isInStoppedState)
    {
        this.isInStoppedState = isInStoppedState;
    }

    public void setProtocolClient(ProtocolClient pc)
    {
        this.pc = pc;
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
    public String getNameOfHeater(int idx)
    {
        if((-1 < idx) & (idx < NUMBER_OF_HEATERS))
        {
            return "Heater " + (idx + 1);
        }
        else
        {
            return "Invalid Heater";
        }
    }

    @Override
    public int getNumberPwm()
    {
        return 1;
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
    public int getNumberTempSensor()
    {
        return 4;
    }

    @Override
    public String getNameOfTemperatureSensor(int idx)
    {
        if((-1 < idx) & (idx < NUMBER_OF_TEMPERATURE_SENSORS))
        {
            return "Temp " + (idx + 1);
        }
        else
        {
            return "Invalid Temperature Sensor";
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
    public int getNumberBuzzer()
    {
        return 2;
    }


    @Override
    public String getNameOfBuzzer(int idx)
    {
        switch(idx)
        {
        case 0: return "Buzzer_1";
        case 1: return "Buzzer_2";
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
        final int[] res = {0, 1, 2, 3};
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
    public void actionPerformed(ActionEvent e)
    {
        final String cmd = e.getActionCommand();
        if(true == TIMER_ACTION_COMMAND.equals(cmd))
        {
            // update the status
            String statusText = "";
            if(true == isInStoppedState)
            {
                statusText = statusText + "State: Stopped !\n";
            }
            else
            {
                statusText = statusText + "State: running !\n";
            }
            if(null != pc)
            {
                if(true == pc.isConnected())
                {
                    statusText = statusText + "Host has connected !\n";
                }
                else
                {
                    statusText = statusText + "Host not connected !\n";
                }
            }
            else
            {
                statusText = statusText + "Host not connected !\n";
            }

            curStatus.setText(statusText);
        }
    }

    @Override
    public void reset()
    {
        isInStoppedState = true;
        for(int i = 0; i < heaterTargetTemperature.length; i++)
        {
            heaterTargetTemperature[i] = 0;
        }

        for(int i = 0; i < mappedTemperatureSensor.length; i++)
        {
            mappedTemperatureSensor[i] = 0xff; // not mapped
        }
    }

    @Override
    public int getTemperatureFromSensor(int idx)
    {
        if((-1 < idx) & (idx < NUMBER_OF_TEMPERATURE_SENSORS))
        {
            return temperature[idx];
        }
        else
        {
            return 0;
        }
    }

    @Override
    public byte[] getConfigurationOfHeater(int heaterIdx)
    {
        final byte[] res = new byte[2];
        res[0] = 0; // No internal Sensor
        res[2] = (byte)mappedTemperatureSensor[heaterIdx];
        return res;
    }

    @Override
    public void setConfigurationOfHeater(int heaterIdx, int tempSensor)
    {
        mappedTemperatureSensor[heaterIdx] = tempSensor;
    }

    @Override
    public void setTargetTemperatureOfHeater(int idx, int targetTemp)
    {
        if((-1 < idx) & (idx < NUMBER_OF_TEMPERATURE_SENSORS))
        {
            heaterTargetTemperature[idx] = targetTemp;
            // Fake heating:
            final int tempSensorIdx = mappedTemperatureSensor[idx];
            if((-1 < tempSensorIdx) && (tempSensorIdx < temperature.length))
            {
                temperature[tempSensorIdx] = targetTemp;
            }
        }
        // else ignore
    }

    @Override
    public int getInputValue(int devIdx)
    {
        // TODO add to GUI so that user can change this
        return 0;
    }

    @Override
    public void setOutputTo(int devIdx, int state)
    {
        // TODO add to GUI so that user sees the change
    }

    @Override
    public void setPwmTo(int devIdx, int pwm)
    {
        // TODO add to GUI so that user sees the change
    }

    @Override
    public boolean isAllowedToControlSteppers()
    {
        // true = has Stepper control Extension;
        // false = does not have that extension
        return true;
    }

    @Override
    public int getMaxStepRate()
    {
        // TODO
        return 42000;
    }

    @Override
    public int getHostTimeout()
    {
        // TODO
        return 10;
    }

}
