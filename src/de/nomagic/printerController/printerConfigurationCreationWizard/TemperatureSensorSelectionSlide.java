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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.WizardDialog.SlideTableModel;
import de.nomagic.printerController.pacemaker.DeviceInformation;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TemperatureSensorSelectionSlide extends OneNextWizardSlide
{
    private JPanel slide = new JPanel();
    private SlideTableModel tableData = new SlideTableModel();

    public TemperatureSensorSelectionSlide(Translator t)
    {
        //Columns
        tableData.addColumn("Sensor", false, Integer.class);
        tableData.addColumn("Name", false, String.class);
        tableData.addColumn("current Temperature", false, String.class);
        tableData.addColumn("use Sensor", true, Boolean.class);

        JTable tab = new JTable(tableData);
        JScrollPane tabelePane = new JScrollPane(tab);
        tab.setFillsViewportHeight(true);

        JLabel Instruction = new JLabel("Please tick all Sensors that shall be used.");
        slide.setLayout(new BoxLayout(slide, BoxLayout.PAGE_AXIS));
        slide.add(Instruction);
        slide.add(tabelePane);
    }

    @Override
    public String getName()
    {
        return "Temperature Sensor Slecetion";
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
            // TODO Thread Start:
            for(int i = 0; i < di.getNumberTemperatureSensors(); i++)
            {
                tableData.setValueAt(new Integer(i), i, 0);
                tableData.setValueAt("Temp1", i, 1); // TODO
                tableData.setValueAt("24.1 °C", i, 2); // TODO
                tableData.setValueAt(new Boolean(true), i, 3);
            }
            // Thread end
        }
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {

        return ds;
    }

}
