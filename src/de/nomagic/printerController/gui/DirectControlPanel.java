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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class DirectControlPanel implements ActionListener
{
    private final JPanel myPanel = new JPanel();

    private final JButton XPlusButton = new JButton("Xplus");
    private final JButton XMinusButton = new JButton("Xminus");
    private final JButton YPlusButton = new JButton("Yplus");
    private final JButton YMinusButton = new JButton("Yminus");
    private final JButton ZPlusButton = new JButton("Zplus");
    private final JButton ZMinusButton = new JButton("Zminus");
    private final JButton EPlusButton = new JButton("Eplus");
    private final JButton EMinusButton = new JButton("Eminus");
    private final JButton MotorsOffButton = new JButton("Motors off");
    private final JButton HeaterOnButton = new JButton("Heater on");
    private final JButton HeaterOffButton = new JButton("Heater off");

    public DirectControlPanel()
    {
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Direct Control"));
        myPanel.setLayout(new GridLayout(0,2));
        addButton(XPlusButton, "xplus");
        addButton(XMinusButton, "xplus");
        addButton(YPlusButton, "xplus");
        addButton(YMinusButton, "xplus");
        addButton(ZPlusButton, "xplus");
        addButton(ZMinusButton, "xplus");
        addButton(EPlusButton, "xplus");
        addButton(EMinusButton, "xplus");
        addButton(HeaterOnButton, "xplus");
        addButton(HeaterOffButton, "xplus");
        addButton(MotorsOffButton, "xplus");
    }

    private void addButton(JButton button, String actionCommand)
    {
        button.addActionListener(this);
        button.setActionCommand(actionCommand);
        myPanel.add(button, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // TODO Auto-generated method stub
    }

    public Component getPanel()
    {
        return myPanel;
    }

}
