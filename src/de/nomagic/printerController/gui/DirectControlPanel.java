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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.RelativeMove;


/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class DirectControlPanel implements ActionListener
{
    public static final String ACTION_COMMAND_PREFIX = "Macro";
    public static final String ACTION_COMMAND_REMOVE = "remove";

    private final JPanel myPanel = new JPanel();
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private CoreStateMachine pp;
    private Vector<Macro> macros = new Vector<Macro>();
    private Vector<JButton> buttons = new Vector<JButton>();
    private Cfg cfg;

    public DirectControlPanel(CoreStateMachine pp, Cfg cfg)
    {
        this.pp = pp;
        this.cfg = cfg;
        myPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black),
                "Direct Control"));
        myPanel.setLayout(new GridLayout(0,2));
        final Vector<Macro> cfgMacros = cfg.getMacros();
        if(1 > cfgMacros.size())
        {
            // default Buttons
            addDefaultMacros();
        }
        else
        {
            macros = cfgMacros;
            updateCore(pp);
        }
        updateButtons();
        if(null != pp)
        {
            if(true == pp.isOperational())
            {
                setToOnline();
            }
        }
    }


    public void updateCfg(Cfg cfg)
    {
        this.cfg = cfg;
        final Vector<Macro> cfgMacros = cfg.getMacros();
        if(1 > cfgMacros.size())
        {
            // No Macros defined in the configuration so, ...
            // lets leave the Macros as they are.
        }
        else
        {
            macros = cfgMacros;
            updateCore(pp);
        }
        updateButtons();
    }

    public void addMacro(Macro m)
    {
        m.updateCore(pp);
        macros.add(m);
        updateButtons();
    }

    private void addDefaultMacros()
    {
        RelativeMove rm = new RelativeMove();
        rm.setX(1.0);
        rm.setF(9000.0);
        ExecutorMacro em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("X+");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setX(-1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("X-");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setY(1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("Y+");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setY(-1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("Y-");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setZ(1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("Z+");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setZ(-1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("Z-");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setE(1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("E+");
        em.updateCore(pp);
        macros.add(em);

        rm = new RelativeMove();
        rm.setE(-1.0);
        rm.setF(9000.0);
        em = new ExecutorMacro(ExecutorMacro.FUNC_ADD_MOVE_TO, rm);
        em.setName("E-");
        em.updateCore(pp);
        macros.add(em);

        GCodeMacro gm = new GCodeMacro("M18");
        gm.setName("Motors off");
        gm.updateCore(pp);
        macros.add(gm);

        gm = new GCodeMacro("M0");
        gm.setName("Stop Print");
        gm.updateCore(pp);
        macros.add(gm);

        gm = new GCodeMacro("M112");
        gm.setName("Emergency Stop");
        gm.updateCore(pp);
        macros.add(gm);

        gm = new GCodeMacro("G28");
        gm.setName("Home");
        gm.updateCore(pp);
        macros.add(gm);
    }

    private void updateButtons()
    {
        myPanel.removeAll();
        buttons.clear();
        for(int i = 0; i < macros.size(); i++)
        {
            addButton(macros.get(i), ACTION_COMMAND_PREFIX + i);
        }
    }

    private void addButton(Macro m, String actionCommand)
    {
        final JButton button = new JButton(m.getName());
        button.addActionListener(this);
        button.setActionCommand(actionCommand);
        button.setEnabled(false);
        myPanel.add(button, BorderLayout.NORTH);
        buttons.add(button);
    }

    public void updateCore(CoreStateMachine core)
    {
        log.trace("updating core");
        pp = core;
        for(int i = 0; i < macros.size(); i++)
        {
            final Macro m = macros.get(i);
            m.updateCore(pp);
            macros.set(i, m);
        }
        if(true == core.isOperational())
        {
            setToOnline();
        }
        else
        {
            setToOffline();
        }
    }

    public void setToOnline()
    {
        log.trace("Setting {} Buttons to Online !", buttons.size());
        for(int i = 0; i < buttons.size(); i++)
        {
            buttons.get(i).setEnabled(true);
        }
    }

    public void setToOffline()
    {
        log.trace("Setting Buttons to Offline !");
        for(int i = 0; i < buttons.size(); i++)
        {
            buttons.get(i).setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final String command = e.getActionCommand();
        if(true == command.startsWith(ACTION_COMMAND_PREFIX))
        {
            final int i = Integer.parseInt((command.substring(ACTION_COMMAND_PREFIX.length())));
            final Macro m = macros.get(i);
            m.execute();
        }
        else if(true == ACTION_COMMAND_REMOVE.equals(command))
        {
            //remove a Macro
            final Vector<String> options = new Vector<String>();
            for(int i = 0; i < macros.size(); i++)
            {
                final Macro m = macros.get(i);
                final String res = "" + i + " : " + m.getName();
                options.add(res);
            }
            final Object[] possibilities = options.toArray();
            //TODO remove the ham and eggs
            final String s = (String)JOptionPane.showInputDialog(
                                myPanel,
                                "Complete the sentence:\n"
                                + "\"Green eggs and...\"",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                possibilities,
                                "ham");
            if ((s != null) && (s.length() > 0))
            {
                final String num = s.substring(0, s.indexOf(':'));
                final int opt = Integer.parseInt(num.trim());
                macros.remove(opt);
                cfg.setMacros(macros);
                updateButtons();
            }
        }
        // else ignore action
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public void close()
    {
        cfg.setMacros(macros);
    }

}
