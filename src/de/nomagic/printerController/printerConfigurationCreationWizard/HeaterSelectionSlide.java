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
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.WizardDialog.SlideTableModel;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.Heater_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class HeaterSelectionSlide extends OneNextWizardSlide
{

    private static final int USED_COLUMN = 2;
    private static final int USED_FOR_COLUMN = 3;
    private static final int USED_SENSOR_COLUMN = 4;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel slide = new JPanel();
    private SlideTableModel tableData = new SlideTableModel();
    private JTable tab;
    private String unknown;

    private String[] functionNames = new String[5];

    public HeaterSelectionSlide(Translator t)
    {
        //Columns
        tableData.addColumn(t.t("HeaterSelector_heater"), false, Integer.class);
        tableData.addColumn(t.t("HeaterSelector_name"), false, String.class);
        tableData.addColumn(t.t("HeaterSelector_use"), true, Boolean.class);
        tableData.addColumn(t.t("HeaterSelector_used_for"), true, String.class);
        tableData.addColumn(t.t("HeaterSelector_sensor"), true, String.class);

        functionNames[Heater_enum.Chamber.ordinal()] = t.t("HeaterSelector_func_chamber");
        functionNames[Heater_enum.Print_Bed.ordinal()] = t.t("HeaterSelector_func_bed");
        functionNames[Heater_enum.Extruder_0.ordinal()] = t.t("HeaterSelector_func_e1");
        functionNames[Heater_enum.Extruder_1.ordinal()] = t.t("HeaterSelector_func_e2");
        functionNames[Heater_enum.Extruder_2.ordinal()] = t.t("HeaterSelector_func_e3");

        tab = new JTable(tableData);
        final TableColumn usageColumn = tab.getColumnModel().getColumn(USED_FOR_COLUMN);
        final JComboBox<String> usageBox = new JComboBox<String>();
        unknown = t.t("HeaterSelector_unknown");
        usageBox.addItem(unknown);
        for(int i = 0; i< functionNames.length; i++)
        {
            usageBox.addItem(functionNames[i]);
        }
        usageColumn.setCellEditor(new DefaultCellEditor(usageBox));

        final JScrollPane tabelePane = new JScrollPane(tab);
        tab.setFillsViewportHeight(true);

        final JLabel Instruction = new JLabel(t.t("HeaterSelector_select"));
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
                final Vector<Integer> activeTempSensors = (Vector<Integer>) obj;
                // TODO Thread Start:
                if(activeTempSensors.size() > 0)
                {
                    final TableColumnModel tcm = tab.getColumnModel();
                    final TableColumn sensorColumn = tcm.getColumn(USED_SENSOR_COLUMN);
                    final JComboBox<String> sensorBox = new JComboBox<String>();
                    sensorBox.addItem(unknown);
                    for(int i = 0; i < activeTempSensors.size(); i++)
                    {
                        final int sensorIdx = activeTempSensors.get(i);
                        sensorBox.addItem("" + sensorIdx);
                    }
                    sensorColumn.setCellEditor(new DefaultCellEditor(sensorBox));
                }
                for(int i = 0; i < di.getNumberHeaters(); i++)
                {
                    tableData.setValueAt(new Integer(i), i, 0);
                    tableData.setValueAt(di.getHeaterConnectorName(i), i, 1);
                    tableData.setValueAt(new Boolean(true), i, USED_COLUMN);
                    tableData.setValueAt(unknown, i, USED_FOR_COLUMN);
                    tableData.setValueAt(unknown, i, USED_SENSOR_COLUMN);
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
        final Object obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        Cfg cfg = null;
        if(true == obj instanceof Cfg)
        {
            cfg = (Cfg)obj;
            final Vector<Integer> activeHeaters = new Vector<Integer>();
            for(int i = 0; i < tableData.getRowCount(); i++)
            {
                final Boolean active = (Boolean)tableData.getValueAt(i, USED_COLUMN);
                if(true == active.booleanValue())
                {
                    activeHeaters.add(i);
                    final String function = (String)tableData.getValueAt(i, USED_FOR_COLUMN);
                    cfg.addHeater(0, i, Heater_enum.valueOf(function));
                    // if we can map the function then we can also map the sensor if the sensor is given
                    final String Sensor = (String)tableData.getValueAt(i, USED_SENSOR_COLUMN);
                    try
                    {
                        final int sensorIdx = Integer.parseInt(Sensor);
                        cfg.addTemperatureSensor(0, sensorIdx,  Heater_enum.valueOf(function));
                    }
                    catch(NumberFormatException e)
                    {
                        // unknown sensor -> no mapping
                    }
                }
            }
            ds.putObject(WizardMain.DS_ACTIVE_HEATERS_NAME, activeHeaters);
        }
        else
        {
            log.error("Configuration Object missing from Data Store !");
        }
        return ds;
    }

}
