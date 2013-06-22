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

import javax.swing.JFrame;

import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class MainWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private final ClientPanel clientPane;
    private final PrinterStatusPanel printerStatusPanel;
    private final MachineControlPanel machineControlPanel;

    public MainWindow(final Cfg cfg)
    {
        // set up the printer
        final PrintProcess pp = new PrintProcess();
        pp.setCfg(cfg);
        // set up the window
        this.setTitle("Pacemaker - printerController");
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add all sub Panes

        // connection to Client Panel (connect, disconnect,...)
        clientPane = new ClientPanel(pp);
        this.add(clientPane.getPanel(), BorderLayout.PAGE_END);

        // Printer Status Panel (cur extruder, cur Temperature, cur Position of print head,....)
        printerStatusPanel = new PrinterStatusPanel(pp);
        this.add(printerStatusPanel.getPanel(), BorderLayout.EAST);

        // Machine Control Panel
        machineControlPanel = new MachineControlPanel(pp);
        this.add(machineControlPanel.getPanel(), BorderLayout.CENTER);

        // End of Panels
        this.pack();
        this.setVisible(true);
    }

}
