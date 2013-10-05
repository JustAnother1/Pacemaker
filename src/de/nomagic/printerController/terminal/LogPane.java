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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class LogPane
{
    private JTextArea textArea = new JTextArea(10, 80);
    private JScrollPane scrollPane = new JScrollPane(textArea);


    public LogPane()
    {
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setLineWrap(false);
    }

    public Component getPanel()
    {
        return scrollPane;
    }

    public JTextArea getTextArea()
    {
        return textArea;
    }

    public void close()
    {

    }

}
