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
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import de.nomagic.printerController.GuiAppender;
import de.nomagic.printerController.core.Executor;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class PrinterStatusPanel
{
    public static final String OFFLINE_MESSAGE = "Status Information not available !\n";
    public static final int FONT_SIZE = 12;
    private final JPanel myPanel = new JPanel();
    private final JTextArea statusText = new JTextArea(20, 50);
    private final JScrollPane scrollPane = new JScrollPane();
    private Executor exe;

    public PrinterStatusPanel(Executor exe)
    {
        this.exe = exe;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Printer Status"));
        statusText.setEditable(false);
        statusText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        statusText.setLineWrap(false);
        statusText.setText(OFFLINE_MESSAGE);
        final DefaultCaret caret = (DefaultCaret)statusText.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane.setViewportView(statusText);
        myPanel.add(scrollPane, BorderLayout.CENTER);
        GuiAppender.setTextArea(statusText);
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public void setToOffline()
    {
        statusText.setText(OFFLINE_MESSAGE);
    }

    public void setToOnline()
    {
        statusText.setText("Connected to Pacemaker client !\n");
    }

    public void close()
    {

    }

    public void setViewMode(int mode)
    {
        switch(mode)
        {
        case MainWindow.VIEW_MODE_EXPERT:
            break;
        case MainWindow.VIEW_MODE_DEVELOPER:
            break;
        case MainWindow.VIEW_MODE_STANDARD:

        default:
            break;
        }
    }

    public void updateExecutor(Executor executor)
    {
        exe = executor;
    }

}
