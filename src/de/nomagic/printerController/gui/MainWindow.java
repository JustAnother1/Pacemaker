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

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class MainWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private final PrinterStatusPanel printerStatusPanel;
    private final MachineControlPanel machineControlPanel;

    public MainWindow(final Cfg cfg)
    {
        // set up the printer
        final CoreStateMachine core = new CoreStateMachine(cfg);
        // TODO check if core is operational !
        final Executor exe = core.getExecutor();
        // set up the window
        this.setTitle("Pacemaker - printerController");
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO close Core !
        // add all sub Panes
        // Printer Status Panel (cur extruder, cur Temperature, cur Position of print head,....)
        printerStatusPanel = new PrinterStatusPanel(exe);
        // Machine Control Panel
        machineControlPanel = new MachineControlPanel(core, cfg, printerStatusPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              machineControlPanel.getPanel(),
                                              printerStatusPanel.getPanel());
        this.add(splitPane);
        // End of Panels
        this.pack();
        this.setVisible(true);
    }

}
