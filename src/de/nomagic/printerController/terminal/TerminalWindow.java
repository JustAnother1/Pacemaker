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
package de.nomagic.printerController.terminal;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import de.nomagic.printerController.CloseApplication;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TerminalWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    protected CloseApplication Closer;
    private ControlPane control;
    private LogPane logging;
    private TerminalConfiguration cfg;

    public TerminalWindow(CloseApplication Close)
    {
        this.Closer = Close;
        this.setTitle("Pacemaker - Terminal");
        this.setResizable(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Closer.close();
            }
        });

        cfg = new TerminalConfiguration();

        // add all sub Panes
        control = new ControlPane(this, cfg);
        logging = new LogPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              control.getPanel(),
                                              logging.getPanel());
        this.add(splitPane);
        // End of Panels
        this.pack();
        this.setVisible(true);
    }

    public void close()
    {
        control.close();
        logging.close();
        cfg.close();
    }

    public JTextArea getTextArea()
    {
        return logging.getTextArea();
    }

}
