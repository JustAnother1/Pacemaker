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
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purejavacomm.CommPortIdentifier;
import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.printerController.pacemaker.ClientConnectionFactory;
import de.nomagic.printerController.pacemaker.UartClientConnection;
import de.nomagic.printerController.printer.Cfg;

/** Ask user for parameters of UART Interface.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceTypeUartSlide extends OneNextWizardSlide
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel slide = new JPanel();

    // ComPort Name,
    private Vector<String> portList = new Vector<String>();
    private JComboBox<String> portName;
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

    public InterfaceTypeUartSlide(Translator t)
    {
        slide.setLayout(new GridLayout(0,2));
        // Port
        JLabel portLabel = new JLabel(t.t("Interface_uart_port"));
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("jna.nosys", "true");
        @SuppressWarnings("rawtypes")
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
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
        Object obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        Cfg cfg = null;
        if(true == obj instanceof Cfg)
        {
            cfg = (Cfg)obj;
            String configuredConnection = cfg.getClientDeviceString();
            if(true == configuredConnection.startsWith(ClientConnectionFactory.UART_PREFIX))
            {
                final String data = configuredConnection.substring(ClientConnectionFactory.UART_PREFIX.length());
                String pn = UartClientConnection.getPortNameFromDescriptor(data);
                int idx = portList.indexOf(pn);
                if(0 > idx)
                {
                    // Port is not in the list so select the first in the list,...
                    idx = 0;
                }
                portName.setSelectedIndex(idx);
                baudInput.setText("" + UartClientConnection.getBaudrateFromDescriptor(data));
                dataBitsUsed.setSelectedIndex(UartClientConnection.getDataBitIdxFromDescriptor(data));
                parityUsed.setSelectedIndex(UartClientConnection.getParityIdxFromDescriptor(data));
                stopBits.setSelectedIndex(UartClientConnection.getStopBitIdxFromDescriptor(data));
                rts_cts_in.setSelected(UartClientConnection.getRtsCtsInFromDescriptor(data));
                rts_cts_out.setSelected(UartClientConnection.getRtsCtsOutFromDescriptor(data));
                xon_xoff_in.setSelected(UartClientConnection.getXonXoffInFromDescriptor(data));
                xon_xoff_out.setSelected(UartClientConnection.getXonXoffOutFromDescriptor(data));
            }
            // else a different connection type was in the configuration
        }
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {
        Object obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        Cfg cfg = null;
        if(true == obj instanceof Cfg)
        {
            cfg = (Cfg)obj;
        }
        else
        {
            log.error("No Configuration in Data Store ! Creating new Configuration !");
            cfg = new Cfg();
        }
        cfg.setClientDeviceString(ClientConnectionFactory.UART_PREFIX
                + UartClientConnection.getDescriptorFor(
                (String)portName.getSelectedItem(),
                Integer.parseInt(baudInput.getText()),
                dataBitsUsed.getSelectedIndex(),
                parityUsed.getSelectedIndex(),
                stopBits.getSelectedIndex(),
                rts_cts_in.isSelected(),
                rts_cts_out.isSelected(),
                xon_xoff_in.isSelected(),
                xon_xoff_out.isSelected() ));
        ds.putObject(WizardMain.DS_CONFIGURATION_NAME, cfg);
        return ds;
    }

}
