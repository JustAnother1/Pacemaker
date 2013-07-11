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
package de.nomagic.printerController.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class DirectControlPanel implements ActionListener
{
    private final JPanel myPanel = new JPanel();
    private final PrintProcess pp;
    private final static String ACTION_COMMAND_X_PLUS = "xplus";
    private final static String ACTION_COMMAND_X_MINUS = "xminus";
    private final static String ACTION_COMMAND_Y_PLUS = "yplus";
    private final static String ACTION_COMMAND_Y_MINUS = "yminus";
    private final static String ACTION_COMMAND_Z_PLUS = "zplus";
    private final static String ACTION_COMMAND_Z_MINUS = "zminus";
    private final static String ACTION_COMMAND_E_PLUS = "eplus";
    private final static String ACTION_COMMAND_E_MINUS = "eminus";
    private final static String ACTION_COMMAND_MOTORS_OFF = "motoroff";
    private final static String ACTION_COMMAND_STOP_PRINT = "stop";
    private final static String ACTION_COMMAND_EMERGENCY_STOP = "emergency";
    private final static String ACTION_COMMAND_HOME = "home";

    private final JButton XPlusButton = new JButton("Xplus");
    private final JButton XMinusButton = new JButton("Xminus");
    private final JButton YPlusButton = new JButton("Yplus");
    private final JButton YMinusButton = new JButton("Yminus");
    private final JButton ZPlusButton = new JButton("Zplus");
    private final JButton ZMinusButton = new JButton("Zminus");
    private final JButton EPlusButton = new JButton("Eplus");
    private final JButton EMinusButton = new JButton("Eminus");
    private final JButton MotorsOffButton = new JButton("Motors off");
    private final JButton StopPrintButton = new JButton("Stop Print");
    private final JButton EmergencyStopButton = new JButton("Emergency Stop");
    private final JButton HomeButton = new JButton("Home");

    public DirectControlPanel(PrintProcess pp)
    {
        this.pp = pp;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Direct Control"));
        myPanel.setLayout(new GridLayout(0,2));
        addButton(XPlusButton, ACTION_COMMAND_X_PLUS);
        addButton(XMinusButton, ACTION_COMMAND_X_MINUS);
        addButton(YPlusButton, ACTION_COMMAND_Y_PLUS);
        addButton(YMinusButton, ACTION_COMMAND_Y_MINUS);
        addButton(ZPlusButton, ACTION_COMMAND_Z_PLUS);
        addButton(ZMinusButton, ACTION_COMMAND_Z_MINUS);
        addButton(EPlusButton, ACTION_COMMAND_E_PLUS);
        addButton(EMinusButton, ACTION_COMMAND_E_MINUS);
        addButton(MotorsOffButton, ACTION_COMMAND_MOTORS_OFF);
        addButton(StopPrintButton, ACTION_COMMAND_STOP_PRINT);
        addButton(EmergencyStopButton, ACTION_COMMAND_EMERGENCY_STOP);
        addButton(HomeButton, ACTION_COMMAND_HOME);
        // TODO change Temperatures (preheat/ change during printing)
        // TODO control Fans
    }

    public void setToOnline()
    {
        XPlusButton.setEnabled(true);
        XMinusButton.setEnabled(true);
        YPlusButton.setEnabled(true);
        YMinusButton.setEnabled(true);
        ZPlusButton.setEnabled(true);
        ZMinusButton.setEnabled(true);
        EPlusButton.setEnabled(true);
        EMinusButton.setEnabled(true);
        MotorsOffButton.setEnabled(true);
        StopPrintButton.setEnabled(false);
        EmergencyStopButton.setEnabled(false);
        HomeButton.setEnabled(false);
    }

    public void setToOffline()
    {
        XPlusButton.setEnabled(false);
        XMinusButton.setEnabled(false);
        YPlusButton.setEnabled(false);
        YMinusButton.setEnabled(false);
        ZPlusButton.setEnabled(false);
        ZMinusButton.setEnabled(false);
        EPlusButton.setEnabled(false);
        EMinusButton.setEnabled(false);
        MotorsOffButton.setEnabled(false);
        StopPrintButton.setEnabled(false);
        EmergencyStopButton.setEnabled(false);
        HomeButton.setEnabled(false);
    }

    private void addButton(JButton button, String actionCommand)
    {
        button.addActionListener(this);
        button.setActionCommand(actionCommand);
        button.setEnabled(false);
        myPanel.add(button, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if(true == ACTION_COMMAND_EMERGENCY_STOP.equals(cmd))
        {
            pp.executeGCode("M112");
        }
        else if(true == ACTION_COMMAND_STOP_PRINT.equals(cmd))
        {
            pp.executeGCode("M0");
        }
        else if(true == ACTION_COMMAND_X_PLUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 X1");
        }
        else if(true == ACTION_COMMAND_X_MINUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 X-1");
        }
        else if(true == ACTION_COMMAND_Y_PLUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 Y1");
        }
        else if(true == ACTION_COMMAND_Y_MINUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 Y-1");
        }
        else if(true == ACTION_COMMAND_Z_PLUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 Z1");
        }
        else if(true == ACTION_COMMAND_Z_MINUS.equals(cmd))
        {
            pp.executeGCode("G91"); // relative Positioning
            pp.executeGCode("G1 Z-1");
        }
        else if(true == ACTION_COMMAND_E_PLUS.equals(cmd))
        {
            pp.executeGCode("M83"); // relative Positioning
            pp.executeGCode("G1 E1");
        }
        else if(true == ACTION_COMMAND_E_MINUS.equals(cmd))
        {
            pp.executeGCode("M83"); // relative Positioning
            pp.executeGCode("G1 E-1");
        }
        else if(true == ACTION_COMMAND_MOTORS_OFF.equals(cmd))
        {
            pp.executeGCode("M18");
        }
        else if(true == ACTION_COMMAND_HOME.equals(cmd))
        {
            pp.executeGCode("G28");
        }
        // else unknown action -> ignore
    }

    public Component getPanel()
    {
        return myPanel;
    }

}
