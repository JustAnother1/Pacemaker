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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HeaterSelectionSlide extends OneNextWizardSlide
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final static int USED_FOR_COLUMN = 3;
    private final static int USED_SENSOR_COLUMN = 4;

    private JPanel slide = new JPanel();
    private SlideTableModel tableData = new SlideTableModel();
    private JTable tab;

    public HeaterSelectionSlide()
    {
        //Columns
        tableData.addColumn("Heater", false, Integer.class);
        tableData.addColumn("Name", false, String.class);
        tableData.addColumn("use it?", false, Boolean.class);
        tableData.addColumn("used for", true, String.class);
        tableData.addColumn("used Sensor", true, String.class);

        JTable tab = new JTable(tableData);
        TableColumn usageColumn = tab.getColumnModel().getColumn(USED_FOR_COLUMN);
        JComboBox<String> usageBox = new JComboBox<String>();
        usageBox.addItem("unknown");
        usageBox.addItem("chamber");
        usageBox.addItem("print bed");
        usageBox.addItem("extruder one");
        usageBox.addItem("extruder two");
        usageBox.addItem("extruder three");
        usageColumn.setCellEditor(new DefaultCellEditor(usageBox));

        JScrollPane tabelePane = new JScrollPane(tab);
        tab.setFillsViewportHeight(true);

        JLabel Instruction = new JLabel("Please tick all Heaters that shall be used.");
        slide.setLayout(new BoxLayout(slide, BoxLayout.PAGE_AXIS));
        slide.add(Instruction);
        slide.add(tabelePane);
    }

    @Override
    public String getName()
    {
        return "active Heater Selection";
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
            obj = ds.getObject(WizardMain.DS_ACTIVE_TEMPERATURE_SENSORS_NAME);
            if(true == obj instanceof Vector)
            {
                @SuppressWarnings("unchecked")
                Vector<Integer> activeTempSensors = (Vector<Integer>) obj;
                // TODO Thread Start:
                int firstSensor = 0;
                if(activeTempSensors.size() > 0)
                {
                    firstSensor = activeTempSensors.get(0);
                    TableColumn sensorColumn = tab.getColumnModel().getColumn(USED_SENSOR_COLUMN);
                    JComboBox<String> sensorBox = new JComboBox<String>();
                    sensorBox.addItem("unknown");
                    for(int i = 0; i < activeTempSensors.size(); i++)
                    {
                        int sensorIdx = activeTempSensors.get(i);
                        sensorBox.addItem("" + sensorIdx);
                    }
                    sensorColumn.setCellEditor(new DefaultCellEditor(sensorBox));
                }
                for(int i = 0; i < di.getNumberHeaters(); i++)
                {
                    tableData.setValueAt(new Integer(i), i, 0);
                    tableData.setValueAt(di.getHeaterConnectorName(i), i, 1);
                    tableData.setValueAt(new Boolean(true), i, 2);
                    tableData.setValueAt("unknown", i, USED_FOR_COLUMN);
                    tableData.setValueAt("" + firstSensor, i, USED_SENSOR_COLUMN);
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
        /*
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
        */
        return ds;
    }

}
