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
public class MachineControlPanel implements ActionListener
{
    private final PrintProcess pp;
    private final JPanel myPanel = new JPanel();
    private final JButton configurationButton = new JButton("edit configuration");

    public MachineControlPanel(final PrintProcess pp)
    {
        this.pp = pp;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Printer Control"));
        configurationButton.addActionListener(this);
        configurationButton.setActionCommand("editConfig");
        myPanel.add(configurationButton, BorderLayout.NORTH);
    }

    public Component getPanel()
    {
        return myPanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if ("editConfig".equals(e.getActionCommand()))
        {
            ConfigurationDialog.showDialog(pp);
        }
        // other action would go here
    }

}
