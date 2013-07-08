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
package de.nomagic.printerController.printerConfigurationCreationWizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import purejavacomm.CommPortIdentifier;
import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;

/** Ask user for parameters of UART Interface.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceTypeUartSlide extends OneNextWizardSlide
{
    private JPanel slide = new JPanel();

    // ComPort Name,
    private JComboBox<String> portName;
    // Baudrate : any number - Exception if rate not supported
    private JTextField baudInput = new JTextField("115200");
    // databits: 5,6,7,8
    private String[] bits = {"5", "6", "7", "8"};
    private JComboBox<String> dataBitsUsed = new JComboBox<String>(bits);
    // stop bits: 1, 1.5, 2
    private String[] stop = {"1", "1 1/2", "2"};
    private JComboBox<String> stopBits = new JComboBox<String>(stop);
    // parity: None, even, odd mark, space
    private String[] parityOptions = {"None", "Even", "Odd", "Mark", "Space"};
    private JComboBox<String> parityUsed = new JComboBox<String>(parityOptions);
    // Flowcontrol: none, RTS/CTS IN, RTS/CTS OUT, Xon/Xoff IN, Xon/Xoff OUT ( Bitmask !)
    private JCheckBox rts_cts_in = new JCheckBox("RTS/CTS IN");
    private JCheckBox rts_cts_out = new JCheckBox("RTS/CTS OUT");
    private JCheckBox xon_xoff_in = new JCheckBox("Xon/Xoff IN");
    private JCheckBox xon_xoff_out = new JCheckBox("Xon/Xoff OUT");

    public InterfaceTypeUartSlide(Translator t)
    {
        slide.setLayout(new GridLayout(0,2));
        // Port
        JLabel portLabel = new JLabel(t.t("Interface_uart_port"));
        @SuppressWarnings("rawtypes")
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        Vector<String> portList = new Vector<String>();
        while (ports.hasMoreElements())
        {
          CommPortIdentifier port = (CommPortIdentifier)ports.nextElement();
          if(CommPortIdentifier.PORT_SERIAL == port.getPortType())
          {
              portList.add(port.getName());
          }
        }
        portName = new JComboBox<String>(portList);
        portName.setSelectedIndex(0);
        slide.add(portLabel, BorderLayout.WEST);
        slide.add(portName, BorderLayout.EAST);
        // Baudrate
        JLabel baudLabel = new JLabel(t.t("Interface_uart_baudrate"));
        slide.add(baudLabel, BorderLayout.WEST);
        slide.add(baudInput, BorderLayout.EAST);
        // databits
        JLabel bitLabel = new JLabel(t.t("Interface_uart_databits"));
        dataBitsUsed.setSelectedIndex(3); // 8 bits
        slide.add(bitLabel, BorderLayout.WEST);
        slide.add(dataBitsUsed, BorderLayout.EAST);
        // parity
        JLabel parityLabel = new JLabel(t.t("Interface_uart_parity"));
        slide.add(parityLabel, BorderLayout.WEST);
        slide.add(parityUsed, BorderLayout.EAST);
        // stopbit
        JLabel stopLabel = new JLabel(t.t("Interface_uart_stopbits"));
        slide.add(stopLabel, BorderLayout.WEST);
        slide.add(stopBits, BorderLayout.EAST);
        // Flowcontrol
        JLabel flowLabel = new JLabel(t.t("Interface_uart_flowcontrol"));
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.add(rts_cts_in);
        buttonPane.add(rts_cts_out);
        buttonPane.add(xon_xoff_in);
        buttonPane.add(xon_xoff_out);
        slide.add(flowLabel, BorderLayout.WEST);
        slide.add(buttonPane, BorderLayout.EAST);
    }

    @Override
    public String getName()
    {
        return "UartSlide";
    }

    @Override
    public Component getComponent()
    {
        return slide;
    }

    @Override
    public DataStore actionOnShow(DataStore ds)
    {
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {
        // TODO Auto-generated method stub
        return ds;
    }

}
