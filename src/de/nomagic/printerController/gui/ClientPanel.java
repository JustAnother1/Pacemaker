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
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientPanel
{
    public final static String ACTION_OPEN_CLIENT_CONNECTION = "connect";
    public final static String ACTION_CLOSE_CLIENT_CONNECTION = "disconnect";

    private final JPanel myPanel = new JPanel();
    private final JPanel myDescriptionPanel = new JPanel();
    private final JPanel myButtonPanel = new JPanel();
    private final JLabel label = new JLabel("Connection Description : ");
    private final JTextField desscriptionField = new JTextField("", 20);
    private final JButton connectButton = new JButton("Connect");
    private final JButton disconnectButton = new JButton("Disconnect");
    private final CoreStateMachine pp;
    private final Cfg cfg;

    public ClientPanel(final CoreStateMachine pp, Cfg cfg, ActionListener parent)
    {
        this.pp = pp;
        this.cfg = cfg;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Client Connection"));

        myDescriptionPanel.add(label, BorderLayout.WEST);
        updateConnectionDefinition();
        myDescriptionPanel.add(desscriptionField, BorderLayout.EAST);

        updateButtons();
        connectButton.addActionListener(parent);
        disconnectButton.addActionListener(parent);
        connectButton.setActionCommand(ACTION_OPEN_CLIENT_CONNECTION);
        disconnectButton.setActionCommand(ACTION_CLOSE_CLIENT_CONNECTION);
        myButtonPanel.add(connectButton, BorderLayout.NORTH);
        myButtonPanel.add(disconnectButton, BorderLayout.SOUTH);

        myPanel.add(myDescriptionPanel, BorderLayout.EAST);
        myPanel.add(myButtonPanel, BorderLayout.WEST);
    }

    public void updateButtons()
    {
        if(true == pp.isOperational())
        {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        }
        else
        {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
    }

    public void updateConnectionDefinition()
    {
        desscriptionField.setText(cfg.getConnectionDefinitionOfClient(0));
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public String getConnectionDefinition()
    {
        return desscriptionField.getText();
    }

}
