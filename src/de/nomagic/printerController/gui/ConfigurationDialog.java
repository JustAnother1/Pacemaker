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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.planner.AxisConfiguration;
import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ConfigurationDialog implements ActionListener
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final PrintProcess pp;
    private final Cfg cfg;
    private final JPanel ButtonPanel = new JPanel();
    private final JButton loadButton = new JButton("load");
    private final JButton saveButton = new JButton("save");
    private final JButton cancelButton = new JButton("cancel");
    private final JTabbedPane OptionsPanel = new JTabbedPane();
    private final JPanel cfgPanel = new JPanel();
    private final JDialog wnd = new JDialog();
    private final JFileChooser fc = new JFileChooser();
    private final JTabbedPane AxisTab = new JTabbedPane();
    private final JPanel XaxisTab = new JPanel(new SpringLayout());
    private final JSpinner X_HomingSwitch = createSpinner("homing switch ", XaxisTab);
    private final JSpinner X_StepperNumber = createSpinner("Stepper motor index", XaxisTab);
    private final JSpinner X_SecondStepperNumber = createSpinner("Index of second tepper motor", XaxisTab);
    private final JPanel YaxisTab = new JPanel(new SpringLayout());
    private final JSpinner Y_HomingSwitch = createSpinner("homing switch ", YaxisTab);
    private final JSpinner Y_StepperNumber = createSpinner("Stepper motor index", YaxisTab);
    private final JSpinner Y_SecondStepperNumber = createSpinner("Index of second tepper motor", YaxisTab);
    private final JPanel ZaxisTab = new JPanel(new SpringLayout());
    private final JSpinner Z_HomingSwitch = createSpinner("homing switch ", ZaxisTab);
    private final JSpinner Z_StepperNumber = createSpinner("Stepper motor index", ZaxisTab);
    private final JSpinner Z_SecondStepperNumber = createSpinner("Index of second tepper motor", ZaxisTab);
    private final JPanel AaxisTab = new JPanel(new SpringLayout());
    private final JSpinner A_HomingSwitch = createSpinner("homing switch ", AaxisTab);
    private final JSpinner A_StepperNumber = createSpinner("Stepper motor index", AaxisTab);
    private final JSpinner A_SecondStepperNumber = createSpinner("Index of second tepper motor", AaxisTab);
    private final JPanel BaxisTab = new JPanel(new SpringLayout());
    private final JSpinner B_HomingSwitch = createSpinner("homing switch ", BaxisTab);
    private final JSpinner B_StepperNumber = createSpinner("Stepper motor index", BaxisTab);
    private final JSpinner B_SecondStepperNumber = createSpinner("Index of second tepper motor", BaxisTab);
    private final JPanel CaxisTab = new JPanel(new SpringLayout());
    private final JSpinner C_HomingSwitch = createSpinner("homing switch ", CaxisTab);
    private final JSpinner C_StepperNumber = createSpinner("Stepper motor index", CaxisTab);
    private final JSpinner C_SecondStepperNumber = createSpinner("Index of second tepper motor", CaxisTab);
    private final JPanel EaxisTab = new JPanel(new SpringLayout());
    private final JSpinner E_HomingSwitch = createSpinner("homing switch ", EaxisTab);
    private final JSpinner E_StepperNumber = createSpinner("Stepper motor index", EaxisTab);
    private final JSpinner E_SecondStepperNumber = createSpinner("Index of second tepper motor", EaxisTab);

    private final JPanel HeaterTab = new JPanel(new SpringLayout());
    private final JSpinner ChamberHeater = createSpinner("chamber heater", HeaterTab);
    private final JSpinner PrintBedHeater = createSpinner("print bed heater", HeaterTab);
    private final JSpinner ExtruderOneHeater = createSpinner("Extruder 1 heater", HeaterTab);
    private final JSpinner ExtruderTwoHeater = createSpinner("Extruder 2 heater", HeaterTab);
    private final JSpinner ExtruderThreeHeater = createSpinner("Extruder 3 heater", HeaterTab);

    private final JPanel TemperatureTab = new JPanel(new SpringLayout());
    private final JSpinner ChamberSensor = createSpinner("chamber sensor", TemperatureTab);
    private final JSpinner PrintBedSensor = createSpinner("print bed sensor", TemperatureTab);
    private final JSpinner ExtruderOneSensor = createSpinner("Extruder 1 sensor", TemperatureTab);
    private final JSpinner ExtruderTwoSensor = createSpinner("Extruder 2 sensor", TemperatureTab);
    private final JSpinner ExtruderThreeSensor = createSpinner("Extruder 3 sensor", TemperatureTab);


    private ConfigurationDialog(final PrintProcess pp)
    {
        this.pp = pp;
        cfg = pp.getCfg();
        // Axis
        final AxisConfiguration[] acfg = cfg.getAxisMapping();

        X_HomingSwitch.setValue(acfg[Cfg.POS_X].getMinSwitch()); // TODO
        X_StepperNumber.setValue(acfg[Cfg.POS_X].getStepperNumber());
        X_SecondStepperNumber.setValue(acfg[Cfg.POS_X].getSecondStepper());
        // TODO Homing Direction
        // TODO Steps per mm
        makeCompactGrid(XaxisTab,
                5,  2,  // rows, cols
               10, 10,  // initX, initY
                6, 10); // xPad, yPad

        // TODO Y, Z, A, B, C, E

        AxisTab.addTab("X", XaxisTab);
        AxisTab.addTab("Y", YaxisTab);
        AxisTab.addTab("Z", ZaxisTab);
        AxisTab.addTab("A", AaxisTab);
        AxisTab.addTab("B", BaxisTab);
        AxisTab.addTab("C", CaxisTab);
        AxisTab.addTab("E", EaxisTab);
        // Temperature Sensors
        final int[] temperaturemap = cfg.getTemperatureSensorMapping();
        ChamberSensor.setValue(temperaturemap[Cfg.CHAMBER]);
        PrintBedSensor.setValue(temperaturemap[Cfg.PRINT_BED]);
        ExtruderOneSensor.setValue(temperaturemap[Cfg.EXTRUDER_1]);
        ExtruderTwoSensor.setValue(temperaturemap[Cfg.EXTRUDER_2]);
        ExtruderThreeSensor.setValue(temperaturemap[Cfg.EXTRUDER_3]);
        makeCompactGrid(TemperatureTab,
                5,  2,  // rows, cols
               10, 10,  // initX, initY
                6, 10); // xPad, yPad
        // Heaters
        final int[] heatermap = cfg.getHeaterMapping();
        ChamberHeater.setValue(heatermap[Cfg.CHAMBER]);
        PrintBedHeater.setValue(heatermap[Cfg.PRINT_BED]);
        ExtruderOneHeater.setValue(heatermap[Cfg.EXTRUDER_1]);
        ExtruderTwoHeater.setValue(heatermap[Cfg.EXTRUDER_2]);
        ExtruderThreeHeater.setValue(heatermap[Cfg.EXTRUDER_3]);
        makeCompactGrid(HeaterTab,
                 5,  2,  // rows, cols
                10, 10,  // initX, initY
                 6, 10); // xPad, yPad
        // Configuration options in tabbed Pane
        OptionsPanel.addTab("Axis", AxisTab);
        OptionsPanel.addTab("Heaters", HeaterTab);
        OptionsPanel.addTab("Temperature Sensors", TemperatureTab);
        // Buttons in lower part of Window
        loadButton.addActionListener(this);
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        loadButton.setActionCommand("load");
        saveButton.setActionCommand("save");
        cancelButton.setActionCommand("cancel");
        ButtonPanel.add(loadButton, BorderLayout.EAST);
        ButtonPanel.add(saveButton, BorderLayout.CENTER);
        ButtonPanel.add(cancelButton, BorderLayout.WEST);
        // Whole window
        cfgPanel.add(OptionsPanel,BorderLayout.NORTH);
        cfgPanel.add(ButtonPanel,BorderLayout.SOUTH);
        wnd.setTitle("Edit Printer Configuration");
        wnd.setContentPane(cfgPanel);
        wnd.pack();
        wnd.setVisible(true);
    }


    /* Used by makeCompactGrid. */
    private SpringLayout.Constraints getConstraintsForCell(final int row,
                                                           final int col,
                                                           final Container parent,
                                                           final int cols)
    {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        final Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public void makeCompactGrid(final Container parent,
                                final int rows,
                                final int cols,
                                final int initialX,
                                final int initialY,
                                final int xPad,
                                final int yPad)
    {
        SpringLayout layout;
        try
        {
            layout = (SpringLayout)parent.getLayout();
        }
        catch (final ClassCastException exc)
        {
            log.error("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++)
        {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++)
            {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).getWidth() );
            }
            for (int r = 0; r < rows; r++)
            {
                final SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++)
        {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++)
            {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++)
            {
                final SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        final SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    private static JSpinner createSpinner(final String label, final Container c)
    {
        final JLabel l = new JLabel(label);
        c.add(l);
        final SpinnerModel indexModel = new SpinnerNumberModel( -1,   // initial value
                                                          -1,   // min
                                                         255,   // max
                                                           1);  // step
        final JSpinner spinner = new JSpinner(indexModel);
        l.setLabelFor(spinner);
        c.add(spinner);

        return spinner;
    }

    public static void showDialog(final PrintProcess pp)
    {
        @SuppressWarnings("unused")
        final ConfigurationDialog cd = new ConfigurationDialog(pp);
    }

    @Override
    public void actionPerformed(final ActionEvent ae)
    {
        if ("load".equals(ae.getActionCommand()))
        {
            final int returnVal = fc.showOpenDialog(wnd);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                final File f = fc.getSelectedFile();
                try
                {
                    final FileInputStream fin = new FileInputStream(f);
                    cfg.readFrom(fin);
                    updateWindowFromConfig();
                }
                catch (final FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            // else use pressed cancel in file chooser
        }
        else if ("save".equals(ae.getActionCommand()))
        {
            final int returnVal = fc.showSaveDialog(wnd);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                final File f = fc.getSelectedFile();
                try
                {
                    final FileOutputStream fout = new FileOutputStream(f, false);
                    updateConfigFromWindow();
                    cfg.saveTo(fout);
                }
                catch (final FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            // else use pressed cancel in file chooser

        }
        else if ("cancel".equals(ae.getActionCommand()))
        {
        }
        pp.setCfg(cfg);
        wnd.dispose();
    }

    private void updateConfigFromWindow()
    {
        // TODO Auto-generated method stub

    }

    private void updateWindowFromConfig()
    {
        // TODO Auto-generated method stub

    }

}
