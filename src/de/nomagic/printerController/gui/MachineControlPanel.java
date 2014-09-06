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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class MachineControlPanel implements GCodeResultStream
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final JPanel myPanel = new JPanel();
    private final ClientPanel clientPane;
    private final DirectControlPanel directControlPane;
    private final SendPanel rawFramePane;
    private final JPanel buttonPanel = new JPanel();
    private final JButton configurationButton = new JButton("load configuration");
    private final JButton printButton = new JButton("print");
    private final PrinterStatusPanel printerStatusPanel;
    private final JFileChooser fc = new JFileChooser();
    private CoreStateMachine pp;
    private final Cfg cfg;

    public MachineControlPanel(final CoreStateMachine pp,
                               final Cfg cfg,
                               PrinterStatusPanel printerStatusPanel,
                               ActionListener parent,
                               JFrame ParentWindow)
    {
        this.pp = pp;
        this.cfg = cfg;
        this.printerStatusPanel = printerStatusPanel;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Printer Control"));
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setLayout(new FlowLayout());
        configurationButton.addActionListener(parent);
        configurationButton.setActionCommand(MainWindow.ACTION_CFG_LOAD);
        buttonPanel.add(configurationButton);
        printButton.addActionListener(parent);
        printButton.setActionCommand(MainWindow.ACTION_PRINT);
        printButton.setEnabled(false);
        buttonPanel.add(printButton);
        myPanel.add(buttonPanel);

        // move motors ,control heaters,...
        directControlPane = new DirectControlPanel(pp, cfg);
        myPanel.add(directControlPane.getPanel());

        // connection to Client Panel (connect, disconnect,...)
        clientPane = new ClientPanel(pp, cfg, parent);
        myPanel.add(clientPane.getPanel());

        // Send Raw Pacemaker Order Frames to Client
        rawFramePane = new SendPanel(pp, directControlPane, ParentWindow);
        myPanel.add(rawFramePane.getPanel());
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public void updateCore(CoreStateMachine core)
    {
        pp = core;
        directControlPane.updateCore(core);
        clientPane.updateCore(core);
        rawFramePane.updateCore(core);
    }

    public void handleActionCloseClient()
    {
        log.trace("User requests to close the connection to the client!");
        pp.close();
        clientPane.updateButtons();
        printerStatusPanel.setToOffline();
        directControlPane.setToOffline();
        printButton.setEnabled(false);
    }

    public void setToOffline()
    {
        log.trace("connection to client is now closed !");
        clientPane.updateButtons();
        printerStatusPanel.setToOffline();
        directControlPane.setToOffline();
        printButton.setEnabled(false);
    }

    public void setToOnline()
    {
        log.trace("connection to client is now open !");
        clientPane.updateButtons();
        printerStatusPanel.setToOnline();
        directControlPane.setToOnline();
        printButton.setEnabled(true);
    }

    public void handleActionOpenClient()
    {
        log.info("User requests to open the connection to the client!");
        cfg.setClientDeviceString(0, clientPane.getConnectionDefinition());
        if(null != pp)
        {
            pp.close();
        }
        pp = new CoreStateMachine(cfg);
        if(true == pp.isOperational())
        {
            setToOnline();
        }
        else
        {
            log.info("connection failed !");
            setToOffline();
        }

    }

    public void handleActionPrint()
    {
        log.trace("User wants to print a G-Code file,..");
        if(fc.showOpenDialog(myPanel) == JFileChooser.APPROVE_OPTION)
        {
            final PrintWorker pw = new PrintWorker(fc.getSelectedFile(), pp, this);
            pw.execute();
        }
    }

    public void close()
    {
        directControlPane.close();
        clientPane.close();
    }

    public void setViewMode(int mode)
    {
        switch(mode)
        {
        case MainWindow.VIEW_MODE_EXPERT:
            clientPane.setVisible(true);
            rawFramePane.setVisible(false);
            break;
        case MainWindow.VIEW_MODE_DEVELOPER:
            clientPane.setVisible(true);
            rawFramePane.setVisible(true);
            break;
        case MainWindow.VIEW_MODE_STANDARD:
        default:
            clientPane.setVisible(false);
            rawFramePane.setVisible(false);
            break;
        }
    }

    public ClientPanel getClientPanel()
    {
        return clientPane;
    }

    public void updateCfg(Cfg cfg)
    {
        directControlPane.updateCfg(cfg);
        clientPane.updateCfg(cfg);
    }

    @Override
    public void write(String msg)
    {
        // We can not do a log without an end of line :-(
        log.debug(msg);
    }

    @Override
    public void writeLine(String msg)
    {
        log.debug(msg);
    }
}
