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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.ResourceBundleTranslator;
import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.BaseWindow;
import de.nomagic.WizardDialog.CancelAction;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class WizardMain implements CancelAction
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public final static String DS_CONFIGURATION_NAME = "cfg";
    public final static String DS_PROTOCOL_NAME = "proto";
    public final static String DS_DEVICE_INFORMATION_NAME = "di";
    public static final String DS_G_CODE_DECODER_NAME = "decoder";
    public static final String DS_PLANNER_NAME = "plan";
    public static final String DS_CLIENT_CONNECTION_NAME = "cc";
    public static final String DS_ACTIVE_TEMPERATURE_SENSORS_NAME = "activeTempSensors";

    private final JFileChooser fc = new JFileChooser();
    private final BaseWindow ConfigCreator;
    // Slides
    private WelcomeSlide hello;

    public WizardMain()
    {
        final Translator t = new ResourceBundleTranslator(null);
        ConfigCreator = new BaseWindow(t, this);

        //1. create the slides
        hello = new WelcomeSlide(t);
        // Client Interface
        InterfaceTypeSelectionSlide interfaceSelect= new InterfaceTypeSelectionSlide(t);
        InterfaceTypeUartSlide iUart = new InterfaceTypeUartSlide(t);
        InterfaceTypeExperimental iExperimental = new InterfaceTypeExperimental(t);
        InterfaceConnectSlide iConnect = new InterfaceConnectSlide(t, ConfigCreator);
        // Temperature Sensors
        TemperatureSensorSelectionSlide tempSensorSlide = new TemperatureSensorSelectionSlide(t);
        // more slides created here

        //2. Link the Slides
        hello.addNextSlide(interfaceSelect);
        interfaceSelect.addInterfaceTypeSlide(t.t("Interface_Type_Name_Uart"), iUart);
        interfaceSelect.addInterfaceTypeSlide(t.t("Interface_Type_Name_Experimental"), iExperimental);
        iUart.addNextSlide(iConnect);
        iExperimental.addNextSlide(iConnect);
        iConnect.addNextSlide(tempSensorSlide);
        // more linking here

        //3. register the Slides
        ConfigCreator.setFirstSlide(hello);
        ConfigCreator.addSlide(hello);
        ConfigCreator.addSlide(interfaceSelect);
        ConfigCreator.addSlide(iUart);
        ConfigCreator.addSlide(iExperimental);
        ConfigCreator.addSlide(iConnect);
        ConfigCreator.addSlide(tempSensorSlide);
        // more slides connected here
    }

    public void run()
    {
        javax.swing.SwingUtilities.invokeLater(ConfigCreator);
    }

    public void addConfigurationFrom(InputStream in)
    {
        log.info("Reading provided Configuration !");
        Cfg c = new Cfg();
        c.readFrom(in);
        DataStore ds = new DataStore();
        ds.putObject(DS_CONFIGURATION_NAME, c);
        ConfigCreator.setDatatStore(ds);
    }


    public static void main(String[] args)
    {
        WizardMain wm = new WizardMain();
        for(int i = 0; i < args.length; i++)
        {
            String h = args[i];
            File f = new File(h);
            if(true == f.canRead())
            {
                FileInputStream fin;
                try
                {
                    fin = new FileInputStream(f);
                    wm.addConfigurationFrom(fin);
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            // else ignore parameter
        }
        wm.run();
    }


    @Override
    public void userClickedCancel(DataStore ds)
    {
        // If there is a Configuration then save it.
        Object obj = ds.getObject(DS_CONFIGURATION_NAME);
        if(true == obj instanceof Cfg)
        {
            Cfg cfg = (Cfg)obj;
            if(fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                File file = fc.getSelectedFile();
                try
                {
                    FileOutputStream fout = new FileOutputStream(file);
                    cfg.saveTo(fout);
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
        // If there is a connection then we close it.
        obj = ds.getObject(DS_CLIENT_CONNECTION_NAME);
        if(true == obj instanceof ClientConnection)
        {
            ClientConnection cc = (ClientConnection)obj;
            cc.close();
        }
    }

}
