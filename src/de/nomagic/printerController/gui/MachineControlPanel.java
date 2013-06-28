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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class MachineControlPanel implements ActionListener
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public final static String ACTION_LOAD_CONFIGURATION = "loadConfig";
    public final static String ACTION_PRINT = "print";

    private final PrintProcess pp;
    private final JPanel myPanel = new JPanel();
    private final ClientPanel clientPane;
    private final JButton configurationButton = new JButton("load configuration");
    private final JButton printButton = new JButton("print");
    private final PrinterStatusPanel printerStatusPanel;
    final JFileChooser fc = new JFileChooser();

    public MachineControlPanel(final PrintProcess pp, PrinterStatusPanel printerStatusPanel)
    {
        this.pp = pp;
        this.printerStatusPanel = printerStatusPanel;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Printer Control"));
        configurationButton.addActionListener(this);
        configurationButton.setActionCommand(ACTION_LOAD_CONFIGURATION);
        myPanel.add(configurationButton, BorderLayout.NORTH);
        printButton.addActionListener(this);
        printButton.setActionCommand(ACTION_PRINT);
        printButton.setEnabled(false);
        myPanel.add(printButton, BorderLayout.NORTH);
        // connection to Client Panel (connect, disconnect,...)
        clientPane = new ClientPanel(pp, this);
        myPanel.add(clientPane.getPanel(), BorderLayout.PAGE_END);
    }

    public Component getPanel()
    {
        return myPanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        log.info("Action performed : " + e.getActionCommand());
        if(ClientPanel.ACTION_CLOSE_CLIENT_CONNECTION.equals(e.getActionCommand()))
        {
            log.trace("User requests to close the connection to the client!");
            pp.closeClientConnection();
            clientPane.updateButtons();
            printerStatusPanel.setToOffline();
            printButton.setEnabled(false);
        }
        else if(ClientPanel.ACTION_OPEN_CLIENT_CONNECTION.equals(e.getActionCommand()))
        {
            log.info("User requests to open the connection to the client!");
            final Cfg cfg = pp.getCfg();
            cfg.setClientDeviceString(clientPane.getConnectionDefinition());
            pp.setCfg(cfg);
            if(true == pp.connectToPacemaker())
            {
                log.trace("connection to client is now open !");
                clientPane.updateButtons();
                printerStatusPanel.setToOnline();
                printButton.setEnabled(true);
            }
            else
            {
                log.info("connection failed !");
            }
        }
        else if(ACTION_LOAD_CONFIGURATION.equals(e.getActionCommand()))
        {
            log.trace("User wants to load a configuration from a file,..");
            if(fc.showOpenDialog(myPanel) == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                Cfg c = new Cfg();
                try
                {
                    if(true == c.readFrom(new FileInputStream(file)))
                    {
                        log.trace("using the newly read configuration !");
                        pp.setCfg(c);
                        clientPane.updateConnectionDefinition();
                    }
                }
                catch (FileNotFoundException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        else if(ACTION_PRINT.equals(e.getActionCommand()))
        {
            log.trace("User wants to print a G-Code file,..");
            if(fc.showOpenDialog(myPanel) == JFileChooser.APPROVE_OPTION)
            {
                //TODO move to own task
                File file = fc.getSelectedFile();
                BufferedReader br = null;
                FileInputStream fin = null;
                try
                {
                    fin = new FileInputStream(file);
                    br = new BufferedReader(new InputStreamReader(fin, Charset.forName("UTF-8")));
                    String curLine = br.readLine();
                    while(null != curLine)
                    {
                        if(false == pp.executeGCode(curLine))
                        {
                            return;
                        }
                        curLine = br.readLine();
                    }
                }
                catch (FileNotFoundException e1)
                {
                    e1.printStackTrace();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                finally
                {
                    if(null != br)
                    {
                        try
                        {
                            br.close();
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    if(null != fin)
                    {
                        try
                        {
                            fin.close();
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        // other action would go here
    }

}
