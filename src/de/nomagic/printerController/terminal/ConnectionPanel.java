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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ConnectionPanel implements ActionListener
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private JPanel pane;
    private ClientChannel client;
    private JButton connectDisconnect;
    private JTextField ConnectionDescription;
    private JFrame terminalWindow;
    private TerminalConfiguration cfg;

    private final static String COMMAND_SELECT_INTERFACE = "select";
    private final static String COMMAND_CONNECT = "connect";
    private final static String COMMAND_DISCONNECT = "disconnect";

    public ConnectionPanel(ClientChannel client, JFrame terminalWindow, TerminalConfiguration cfg)
    {
        this.client = client;
        this.terminalWindow = terminalWindow;
        this.cfg = cfg;

        pane = new JPanel();
        pane.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createLineBorder(Color.black),
                       "Connect to Client"));
        JButton selectInterface = new JButton("select Interface");
        selectInterface.setActionCommand(COMMAND_SELECT_INTERFACE);
        selectInterface.addActionListener(this);
        pane.add(selectInterface);

        ConnectionDescription = new JTextField(40);
        ConnectionDescription.setText(cfg.getConnectionString());
        pane.add(ConnectionDescription);

        connectDisconnect = new JButton("Connect");
        connectDisconnect.setActionCommand(COMMAND_CONNECT);
        connectDisconnect.addActionListener(this);
        pane.add(connectDisconnect);
    }

    public void close()
    {
        cfg.setConnectionString(ConnectionDescription.getText());
        client.disconnect();
    }

    public Component getPanel()
    {
        return pane;
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if(true == COMMAND_SELECT_INTERFACE.equals(arg0.getActionCommand()))
        {
            log.info("Select Interface");
            // select interface button has been pressed
            @SuppressWarnings("unused")
            ConnectionDescriptionDialog d = new ConnectionDescriptionDialog(terminalWindow, "Connect to...", false, ConnectionDescription);
        }
        else if(true == COMMAND_CONNECT.equals(arg0.getActionCommand()))
        {
            // connect to client
            log.info("Connect");
            if(true == client.connect(ConnectionDescription.getText()))
            {
                connectDisconnect.setText("Disconnect");
                connectDisconnect.setActionCommand(COMMAND_DISCONNECT);
            }
        }
        else if(true == COMMAND_DISCONNECT.equals(arg0.getActionCommand()))
        {
            // Disconnect from client
            log.info("Disconnect");
            if(true == client.disconnect())
            {
                connectDisconnect.setText("Connect");
                connectDisconnect.setActionCommand(COMMAND_CONNECT);
            }
        }
        else
        {
            log.info("Unrecognized Action Event!");
        }
    }

}
