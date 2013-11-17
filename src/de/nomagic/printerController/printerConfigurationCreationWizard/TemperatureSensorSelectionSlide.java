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

import java.awt.Component;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.WizardDialog.SlideTableModel;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TemperatureSensorSelectionSlide extends OneNextWizardSlide
{
    public static final double MIN_TEMPERATURE = 15.0;
    public static final double MAX_TEMPERATURE = 600.0;
    private static final int SENSOR_ACTIVE_COLUMN = 3;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel slide = new JPanel();
    private SlideTableModel tableData = new SlideTableModel();

    public TemperatureSensorSelectionSlide(Translator t)
    {
        //Columns
        tableData.addColumn(t.t("TemperatureSensor_sensor"), false, Integer.class);
        tableData.addColumn(t.t("TemperatureSensor_name"), false, String.class);
        tableData.addColumn(t.t("TemperatureSensor_cur_temp"), false, String.class);
        tableData.addColumn(t.t("TemperatureSensor_use"), true, Boolean.class);

        JTable tab = new JTable(tableData);
        JScrollPane tabelePane = new JScrollPane(tab);
        tab.setFillsViewportHeight(true);

        JLabel Instruction = new JLabel(t.t("TemperatureSensor_select"));
        slide.setLayout(new BoxLayout(slide, BoxLayout.PAGE_AXIS));
        slide.add(Instruction);
        slide.add(tabelePane);
    }

    @Override
    public String getName()
    {
        return "Temperature Sensor Selection";
    }

    @Override
    public Component getComponent()
    {
        return slide;
    }

    @Override
    public DataStore actionOnShow(DataStore ds)
    {
        Object obj = ds.getObject(WizardMain.DS_DEVICE_INFORMATION_NAME);
        DeviceInformation di = null;
        if(true == obj instanceof DeviceInformation)
        {
            di = (DeviceInformation)obj;
            obj = ds.getObject(WizardMain.DS_PROTOCOL_NAME);
            if(true == obj instanceof Protocol)
            {
                Protocol proto = (Protocol) obj;
                // TODO Thread Start:
                for(int i = 0; i < di.getNumberTemperatureSensors(); i++)
                {
                    tableData.setValueAt(new Integer(i), i, 0);
                    tableData.setValueAt(di.getTemperatureSensorConnectorName(i), i, 1);
                    double measuredTemp = proto.readTemperatureFrom(i);
                    tableData.setValueAt(String.format("%3.1f °C", measuredTemp) , i, 2);
                    if((MIN_TEMPERATURE < measuredTemp) && (MAX_TEMPERATURE > measuredTemp))
                    {
                        tableData.setValueAt(new Boolean(true), i, SENSOR_ACTIVE_COLUMN);
                    }
                    else
                    {
                        tableData.setValueAt(new Boolean(false), i, SENSOR_ACTIVE_COLUMN);
                    }
                }
                // Thread end
            }
            else
            {
                log.error("Protocol Object missing from Data Store !");
            }
        }
        else
        {
            log.error("DeviceInformation Object missing from Data Store !");
        }
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {
        Vector<Integer> activeTempSensors = new Vector<Integer>();
        for(int i = 0; i < tableData.getRowCount(); i++)
        {
            Boolean active = (Boolean)tableData.getValueAt(i, SENSOR_ACTIVE_COLUMN);
            if(true == active.booleanValue())
            {
                activeTempSensors.add(i);
            }
        }
        ds.putObject(WizardMain.DS_ACTIVE_TEMPERATURE_SENSORS_NAME, activeTempSensors);
        return ds;
    }

}
