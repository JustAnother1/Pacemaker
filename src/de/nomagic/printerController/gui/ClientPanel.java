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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientPanel
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final JPanel myPanel = new JPanel();
    private final JPanel myDescriptionPanel = new JPanel();
    private final JPanel myButtonPanel = new JPanel();
    private final JLabel label = new JLabel("Connection Description : ");
    private final JTextField desscriptionField = new JTextField("", 40);
    private final JButton selectInterfaceButton = new JButton("select Interface");
    private final JButton connectButton = new JButton("Connect");
    private final JButton disconnectButton = new JButton("Disconnect");
    private CoreStateMachine pp;
    private Cfg cfg;

    public ClientPanel(final CoreStateMachine pp, Cfg cfg, ActionListener parent)
    {
        this.pp = pp;
        this.cfg = cfg;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Client Connection"));

        selectInterfaceButton.setActionCommand(MainWindow.ACTION_CLIENT_ADD);
        selectInterfaceButton.addActionListener(parent);
        myPanel.add(selectInterfaceButton);
        myDescriptionPanel.add(label, BorderLayout.WEST);
        updateConnectionDefinition("");
        myDescriptionPanel.add(desscriptionField, BorderLayout.EAST);

        updateButtons();
        connectButton.addActionListener(parent);
        disconnectButton.addActionListener(parent);
        connectButton.setActionCommand(MainWindow.ACTION_CLIENT_CONNECT);
        disconnectButton.setActionCommand(MainWindow.ACTION_CLIENT_DISCONNECT);
        myButtonPanel.add(connectButton, BorderLayout.NORTH);
        myButtonPanel.add(disconnectButton, BorderLayout.SOUTH);

        myPanel.add(myDescriptionPanel, BorderLayout.EAST);
        myPanel.add(myButtonPanel, BorderLayout.WEST);
    }


    public void updateCore(CoreStateMachine core)
    {
        pp = core;
    }

    public void updateButtons()
    {
        if(null == pp)
        {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(false);
        }
        else if(true == pp.isOperational())
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

    public void updateConnectionDefinition(String connection)
    {
        if(1 > connection.length())
        {
            if(null == cfg)
            {
                log.warn("No Configuration available !");
                desscriptionField.setText("");
            }
            else
            {
                log.trace("Connection set from Configuration !");
                desscriptionField.setText(cfg.getConnectionDefinitionOfClient(0));
            }
        }
        else
        {
            log.trace("Connection set explicitly !");
            desscriptionField.setText(connection);
        }
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public String getConnectionDefinition()
    {
        return desscriptionField.getText();
    }

    public void close()
    {
        log.trace("Storing ClientConnection Definition ({}) in Configuration File !", desscriptionField.getText());
        cfg.setClientDeviceString(0 /* TODO add support for more than one connection*/,
                                  desscriptionField.getText());
    }

    public void setVisible(boolean b)
    {
        myPanel.setVisible(b);
    }


    public void updateCfg(Cfg cfg)
    {
        this.cfg = cfg;
        if(null != cfg)
        {
            desscriptionField.setText(cfg.getConnectionDefinitionOfClient(0));
        }
    }

}
