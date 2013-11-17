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
    private static final String ACTION_COMMAND_PREFIX = "Macro";
    private static final String ACTION_COMMAND_REMOVE = "remove";

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel pane;
    private Vector<Macro> Macros;
    private ClientChannel client;
    private JFrame terminalWindow;
    private TerminalConfiguration cfg;

    public MacroPanel(ClientChannel client, JFrame terminalWindow, TerminalConfiguration cfg)
    {
        this.client = client;
        this.terminalWindow = terminalWindow;
        pane = new JPanel();
        pane.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createLineBorder(Color.black),
                       "Macros"));
        pane.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.cfg = cfg;
        Macros = cfg.getMacros();
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

    public void close()
    {
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
            log.info(client.sendFrame(m.getOrder(),
                             m.getParameterLength(),
                             m.getParameter()));
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
                cfg.updateMacros(Macros);
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
