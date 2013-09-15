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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class MacroPanel implements ActionListener
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private JPanel pane;
    private final static String CONFIG_FILE_NAME = "PacemakerTerminal.cfg";
    private final static String ACTION_COMMAND_PREFIX = "Macro";
    private final static String ACTION_COMMAND_REMOVE = "remove";
    private Vector<Macro> Macros = new Vector<Macro>();
    private ClientChannel client;
    private JFrame terminalWindow;

    public MacroPanel(ClientChannel client, JFrame terminalWindow)
    {
        this.client = client;
        pane = new JPanel();
        pane.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createLineBorder(Color.black),
                       "Macros"));
        pane.setLayout(new FlowLayout(FlowLayout.LEADING));
        readConfig();
    }

    private void readConfig()
    {
        File f = new File(CONFIG_FILE_NAME);
        if(true == f.canRead())
        {
            ObjectInputStream oin = null;
            try
            {
                oin = new ObjectInputStream(new FileInputStream(f));
                Macro curM = null;
                do
                {
                    curM = (Macro)oin.readObject();
                    if(null != curM)
                    {
                        Macros.add(curM);
                    }
                } while(curM != null);
            }
            catch(EOFException e)
            {
                // ok
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(null != oin)
                {
                    try
                    {
                        oin.close();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        updatePanel();
    }

    private void updatePanel()
    {
        pane.removeAll();
        if(0 < Macros.size())
        {
            JButton remove = new JButton("removeMacro");
            remove.setActionCommand(ACTION_COMMAND_REMOVE);
            remove.addActionListener(this);
            pane.add(remove);
            for(int i = 0; i < Macros.size(); i++)
            {
                JButton curB = new JButton(Macros.get(i).getName());
                curB.setActionCommand(ACTION_COMMAND_PREFIX + i);
                curB.addActionListener(this);
                pane.add(curB);
            }
        }
        else
        {
            JLabel errorLabel = new JLabel("no macro configured");
            pane.add(errorLabel);
        }
        pane.revalidate();
    }

    private void writeConfig()
    {
        if(0 < Macros.size())
        {
            File f = new File(CONFIG_FILE_NAME);
            try
            {
                f.createNewFile();
                if(true == f.canWrite())
                {
                    ObjectOutputStream oOut = null;
                    try
                    {
                        oOut = new ObjectOutputStream(new FileOutputStream(f));
                        for(int i = 0; i < Macros.size(); i++)
                        {
                            oOut.writeObject(Macros.get(i));
                        }
                    }
                    catch(FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        if(null != oOut)
                        {
                            try
                            {
                                oOut.close();
                            }
                            catch(IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                {
                    log.error("Could not save the Macros !");
                }
            }
            catch(IOException e1)
            {
                e1.printStackTrace();
                log.error("IO Exception! Could not save the Macros !");
            }
        }
    }

    public void close()
    {
        writeConfig();
    }

    public Component getPanel()
    {
        return pane;
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();
        if(true == command.startsWith(ACTION_COMMAND_PREFIX))
        {
            int i = Integer.parseInt((command.substring(ACTION_COMMAND_PREFIX.length())));
            Macro m = Macros.get(i);
            client.sendFrame(m.getOrder(),
                             m.getParameterLength(),
                             m.getParameter());
        }
        else if(true == ACTION_COMMAND_REMOVE.equals(command))
        {
            //remove a Macro
            Vector<String> options = new Vector<String>();
            for(int i = 0; i < Macros.size(); i++)
            {
                Macro m = Macros.get(i);
                String res = "" + i + " : " + m.getName();
                options.add(res);
            }
            Object[] possibilities = options.toArray();
            String s = (String)JOptionPane.showInputDialog(
                                terminalWindow,
                                "Complete the sentence:\n"
                                + "\"Green eggs and...\"",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                possibilities,
                                "ham");
            if ((s != null) && (s.length() > 0))
            {
                String num = s.substring(0, s.indexOf(':'));
                int opt = Integer.parseInt(num.trim());
                Macros.remove(opt);
                updatePanel();
            }
        }
        // else ignore action
    }

    public void addMacro(Macro m)
    {
        Macros.add(m);
        updatePanel();
    }

}
