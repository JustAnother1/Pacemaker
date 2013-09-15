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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.pacemaker.ClientConnectionFactory;
import de.nomagic.printerController.pacemaker.UartClientConnection;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class ConnectionDescriptionDialog extends JDialog implements ActionListener
{
    private final static Logger log = LoggerFactory.getLogger("ClientConnection");
    private static final long serialVersionUID = 1L;
    private String answer = "";
    private JPanel myPanel = null;
    private JButton OkButton = null;
    private JButton CancelButton = null;
    private JTextField connectionDescription;

    private JTabbedPane tabbedPane;
    private static final String TAB_TCP = "TCP";
    private static final String TAB_UART = "Serial";
    // TCP
    private JTextField tcpHost;
    private JTextField tcpPort;
    // UART
    private JTextField portName;
    // Baudrate : any number - Exception if rate not supported
    private JTextField baudInput = new JTextField("115200");
    // databits: 5,6,7,8
    private JComboBox<String> dataBitsUsed = new JComboBox<String>(UartClientConnection.bits);
    // stop bits: 1, 1.5, 2
    private JComboBox<String> stopBits = new JComboBox<String>(UartClientConnection.stop);
    // parity: None, even, odd mark, space
    private JComboBox<String> parityUsed = new JComboBox<String>(UartClientConnection.parityOptions);
    // Flowcontrol: none, RTS/CTS IN, RTS/CTS OUT, Xon/Xoff IN, Xon/Xoff OUT ( Bitmask !)
    private JCheckBox rts_cts_in = new JCheckBox("RTS/CTS IN");
    private JCheckBox rts_cts_out = new JCheckBox("RTS/CTS OUT");
    private JCheckBox xon_xoff_in = new JCheckBox("Xon/Xoff IN");
    private JCheckBox xon_xoff_out = new JCheckBox("Xon/Xoff OUT");

    public ConnectionDescriptionDialog(Frame owner, String title, boolean modal, JTextField connectionDescription)
    {
        super(owner, title, modal);
        this.connectionDescription = connectionDescription;
        myPanel = new JPanel();
        tabbedPane = new JTabbedPane();
        JPanel TcpPanel = new JPanel();
        TcpPanel.setLayout(new GridLayout(0,2));
        JLabel tcpHostLabel = new JLabel("Host");
        tcpHost = new JTextField(30);
        TcpPanel.add(tcpHostLabel, BorderLayout.WEST);
        TcpPanel.add(tcpHost, BorderLayout.EAST);
        JLabel tcpPortLabel = new JLabel("Port");
        tcpPort = new JTextField(5);
        TcpPanel.add(tcpPortLabel, BorderLayout.WEST);
        TcpPanel.add(tcpPort, BorderLayout.EAST);
        tabbedPane.add(TAB_TCP, TcpPanel);
        JPanel UartPanel = new JPanel();
        UartPanel.setLayout(new GridLayout(0,2));
        // Port
        JLabel portLabel = new JLabel("Port");
        portName = new JTextField(30);
        UartPanel.add(portLabel, BorderLayout.WEST);
        UartPanel.add(portName, BorderLayout.EAST);
        // Baudrate
        JLabel baudLabel = new JLabel("Baudrate");
        UartPanel.add(baudLabel, BorderLayout.WEST);
        UartPanel.add(baudInput, BorderLayout.EAST);
        // databits
        JLabel bitLabel = new JLabel("Data Bits");
        dataBitsUsed.setSelectedIndex(3); // 8 bits
        UartPanel.add(bitLabel, BorderLayout.WEST);
        UartPanel.add(dataBitsUsed, BorderLayout.EAST);
        // parity
        JLabel parityLabel = new JLabel("Parity");
        UartPanel.add(parityLabel, BorderLayout.WEST);
        UartPanel.add(parityUsed, BorderLayout.EAST);
        // stopbit
        JLabel stopLabel = new JLabel("Stop Bits");
        UartPanel.add(stopLabel, BorderLayout.WEST);
        UartPanel.add(stopBits, BorderLayout.EAST);
        // Flowcontrol
        JLabel flowLabel = new JLabel("FlowControl");
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.add(rts_cts_in);
        buttonPane.add(rts_cts_out);
        buttonPane.add(xon_xoff_in);
        buttonPane.add(xon_xoff_out);
        UartPanel.add(flowLabel, BorderLayout.WEST);
        UartPanel.add(buttonPane, BorderLayout.EAST);
        tabbedPane.add(TAB_UART, UartPanel);
        myPanel.add(tabbedPane);
        JPanel ButtonPanel = new JPanel();
        OkButton = new JButton("OK");
        OkButton.addActionListener(this);
        ButtonPanel.add(OkButton);
        CancelButton = new JButton("Cancel");
        CancelButton.addActionListener(this);
        ButtonPanel.add(CancelButton);
        myPanel.add(ButtonPanel);
        getContentPane().add(myPanel);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(OkButton == e.getSource())
        {
            if(true == TAB_UART.equals(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())))
            {
                log.info("user provided UART Connection data");
                answer = ClientConnectionFactory.UART_PREFIX
                + UartClientConnection.getDescriptorFor(
                portName.getText(),
                Integer.parseInt(baudInput.getText()),
                dataBitsUsed.getSelectedIndex(),
                parityUsed.getSelectedIndex(),
                stopBits.getSelectedIndex(),
                rts_cts_in.isSelected(),
                rts_cts_out.isSelected(),
                xon_xoff_in.isSelected(),
                xon_xoff_out.isSelected() );
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        connectionDescription.setText(answer);
                        connectionDescription.validate();
                    }
                });
            }
            else if(true == TAB_TCP.equals(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())))
            {
                log.info("user provided TCP Connection data");
                answer = ClientConnectionFactory.TCP_PREFIX + tcpHost.getText() + ":" + tcpPort.getText();
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        connectionDescription.setText(answer);
                        connectionDescription.validate();
                    }
                });
            }
            else
            {
                log.info("invalid Connection Type");
                answer = "";
            }
            setVisible(false);
        }
        else if(CancelButton == e.getSource())
        {
            log.info("user canceled");
            answer = "";
            setVisible(false);
        }
    }

}
