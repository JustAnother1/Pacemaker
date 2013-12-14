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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class SendPanel implements ActionListener
{
    private static final String COMMAND_SAVE_AS_MACRO = "macro";
    private static final String COMMAND_SEND = "send";

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private JPanel pane;
    private Executor exe;
    private JTextField Order;
    private JTextField ParaLength;
    private JTextField[] parameterData;
    private DirectControlPanel MacroPanel;
    private JFrame terminalWindow;

    public SendPanel(CoreStateMachine core, DirectControlPanel macroPanel, JFrame terminalWindow)
    {
        if(null != core)
        {
            exe = core.getExecutor();
        }
        this.MacroPanel = macroPanel;
        this.terminalWindow = terminalWindow;
        pane = new JPanel();
        pane.setBorder(BorderFactory.createTitledBorder(
                       BorderFactory.createLineBorder(Color.black),
                       "Send to Client"));
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        final JPanel frame = new JPanel();
        final JLabel OrderLabel = new JLabel("Order: ");
        frame.add(OrderLabel);
        Order = new JTextField(2);
        frame.add(Order);
        final JLabel ParaLengthLabel = new JLabel(" Length of Parameter: ");
        frame.add(ParaLengthLabel);
        ParaLength = new JTextField(2);
        ParaLength.setText("0");
        frame.add(ParaLength);

        final JPanel parameterPane = new JPanel();
        parameterPane.setLayout(new BoxLayout(parameterPane, BoxLayout.PAGE_AXIS));
        parameterData = new JTextField[256];
        int pos = 0;
        for(int i = 0; i < 16; i++)
        {
            final JPanel curP = new JPanel();
            for(int j = 0; j < 16; j++)
            {
                final JTextField Data = new JTextField(2);
                parameterData[pos] = Data;
                if(255 == pos)
                {
                    Data.setText("--");
                    Data.setEditable(false);
                }
                curP.add(Data);
                pos ++;
            }
            parameterPane.add(curP);
        }

        final JPanel ButtonPane = new JPanel();
        final JButton MacroButton = new JButton("save as Macro");
        MacroButton.setActionCommand(COMMAND_SAVE_AS_MACRO);
        MacroButton.addActionListener(this);
        ButtonPane.add(MacroButton);
        final JButton SendButton = new JButton("send");
        SendButton.setActionCommand(COMMAND_SEND);
        SendButton.addActionListener(this);
        ButtonPane.add(SendButton);

        pane.add(frame);
        pane.add(parameterPane);
        pane.add(ButtonPane);
    }

    public void updateCore(CoreStateMachine core)
    {
        if(null != core)
        {
            exe = core.getExecutor();
        }
    }

    public Component getPanel()
    {
        return pane;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(true == COMMAND_SAVE_AS_MACRO.equals(e.getActionCommand()))
        {
            // save as macro
            final String s = (String)JOptionPane.showInputDialog(
                    terminalWindow,
                    "Under what name should this be stored ?",
                    "save as Macro",
                    JOptionPane.PLAIN_MESSAGE);
            if((s != null) && (s.length() > 0))
            {
                final OrderFrameMacro m = new OrderFrameMacro();
                m.setName(s);
                final String orderS = Order.getText();
                int orderI = 0;
                if(0 < orderS.length())
                {
                    orderI = Integer.parseInt(orderS);
                }
                m.setOrder(orderI);
                final String paraLengthS = ParaLength.getText();
                int paraLengthI = 0;
                if(0 < paraLengthS.length())
                {
                    paraLengthI = Integer.parseInt(paraLengthS);
                }
                m.setParameterLength(paraLengthI);
                final int[] data = new int[255];
                for(int i = 0; i < 255; i++)
                {
                    final String help = parameterData[i].getText();
                    if(0 < help.length())
                    {
                        data[i] = Integer.parseInt(help);
                    }
                    else
                    {
                        data[i] = 0;
                    }
                }
                m.setParameter(data);
                MacroPanel.addMacro(m);
            }
        }
        else if(true == COMMAND_SEND.equals(e.getActionCommand()))
        {
            final int[] data = new int[255];
            for(int i = 0; i < 255; i++)
            {
                final String help = parameterData[i].getText();
                if(0 < help.length())
                {
                    data[i] = Integer.parseInt(help);
                }
                else
                {
                    data[i] = 0;
                }
            }
            final String orderS = Order.getText();
            int orderI = 0;
            if(0 < orderS.length())
            {
                orderI = Integer.parseInt(orderS);
            }

            final String paraLengthS = ParaLength.getText();
            int paraLengthI = 0;
            if(0 < paraLengthS.length())
            {
                paraLengthI = Integer.parseInt(paraLengthS);
            }
            if(null != exe)
            {
                final Reply r = exe.sendRawOrderFrame(0, orderI, data, paraLengthI);
                log.info(r.toString());
            }
            else
            {
                log.warn("No Executor available !");
            }
        }
        // else ignore command
    }

    public void setVisible(boolean b)
    {
        pane.setVisible(b);
    }

}
