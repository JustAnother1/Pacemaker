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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.printerController.Cfg;

/** Ask user for ClientDeviceString directly.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceTypeExperimental extends OneNextWizardSlide
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel slide = new JPanel();
    private JTextField desscriptionField = new JTextField("", 20);

    public InterfaceTypeExperimental(Translator t)
    {
        JLabel label = new JLabel(t.t("Interface_Type_Experimental_Device"));
        slide.add(label, BorderLayout.WEST);
        slide.add(desscriptionField, BorderLayout.EAST);
    }

    @Override
    public String getName()
    {
        return "ExperimentalInterface";
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
            desscriptionField.setText(cfg.getConnectionDefinitionOfClient(0));
        }
        String text = desscriptionField.getText();
        if(1 > text.length())
        {
            // Simulator on same PC as default.
            desscriptionField.setText("TCP:127.0.0.1:12345");
        }
        desscriptionField.requestFocus();
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
        cfg.setClientDeviceString(0, desscriptionField.getText());
        ds.putObject(WizardMain.DS_CONFIGURATION_NAME, cfg);
        return ds;
    }

}
