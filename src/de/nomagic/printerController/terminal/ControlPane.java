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

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ControlPane
{
    private JPanel MainPanel;
    private ConnectionPanel ConnectPanel;
    private SendPanel SendPanel;
    private MacroPanel MacroPanel;
    private ClientChannel Client;

    public ControlPane(JFrame terminalWindow)
    {
        MainPanel = new JPanel();
        MainPanel.setLayout(new BoxLayout(MainPanel, BoxLayout.PAGE_AXIS));
        Client = new ClientChannel();
        ConnectPanel = new ConnectionPanel(Client, terminalWindow);
        MainPanel.add(ConnectPanel.getPanel());
        MacroPanel = new MacroPanel(Client, terminalWindow);
        SendPanel = new SendPanel(Client, MacroPanel, terminalWindow);
        MainPanel.add(SendPanel.getPanel());
        MainPanel.add(MacroPanel.getPanel());
    }

    public Component getPanel()
    {
        return MainPanel;
    }

    public void close()
    {
        MacroPanel.close();
        ConnectPanel.close();
        Client.close();
    }

}
