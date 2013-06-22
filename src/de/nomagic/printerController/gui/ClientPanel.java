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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientPanel implements ActionListener
{
    private final JPanel myPanel = new JPanel();
    private final JPanel myDescriptionPanel = new JPanel();
    private final JPanel myButtonPanel = new JPanel();
    private final JLabel label = new JLabel("Connection Description : ");
    private final JTextField desscriptionField = new JTextField("TCP:localhost:12345");
    private final JButton connectButton = new JButton("Connect");
    private final JButton disconnectButton = new JButton("Disconnect");
    private final PrintProcess pp;

    public ClientPanel(final PrintProcess pp)
    {
        this.pp = pp;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Client Connection"));

        myDescriptionPanel.add(label, BorderLayout.WEST);
        myDescriptionPanel.add(desscriptionField, BorderLayout.EAST);

        updateButtons();
        connectButton.addActionListener(this);
        disconnectButton.addActionListener(this);
        connectButton.setActionCommand("enable");
        disconnectButton.setActionCommand("disable");
        myButtonPanel.add(connectButton, BorderLayout.NORTH);
        myButtonPanel.add(disconnectButton, BorderLayout.SOUTH);

        myPanel.add(myDescriptionPanel, BorderLayout.EAST);
        myPanel.add(myButtonPanel, BorderLayout.WEST);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if ("disable".equals(e.getActionCommand()))
        {
            pp.closeClientConnection();
        }
        else
        {
            // enable
            final Cfg cfg = pp.getCfg();
            cfg.setClientDeviceString(desscriptionField.getText());
            pp.setCfg(cfg);
            pp.connectToPacemaker();
        }
        updateButtons();
    }


    private void updateButtons()
    {
        if(true == pp.isClientConnected())
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

    public Component getPanel()
    {
        return myPanel;
    }

}
