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
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class WizardMain implements CancelAction
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public final static String DS_CONFIGURATION_NAME = "cfg";
    public final static String DS_PRINT_PROCESS_NAME = "pp";

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
        InterfaceTypeSelectionSlide interfaceSelect= new InterfaceTypeSelectionSlide(t);
        InterfaceTypeUartSlide iUart = new InterfaceTypeUartSlide(t);
        InterfaceTypeExperimental iExperimental = new InterfaceTypeExperimental(t);
        InterfaceConnectSlide iConnect = new InterfaceConnectSlide(t, ConfigCreator);
        // more slides created here

        //2. Link the Slides
        hello.addNextSlide(interfaceSelect);
        interfaceSelect.addInterfaceTypeSlide(t.t("Interface_Type_Name_Uart"), iUart);
        interfaceSelect.addInterfaceTypeSlide(t.t("Interface_Type_Name_Experimental"), iExperimental);
        iUart.addNextSlide(iConnect);
        iExperimental.addNextSlide(iConnect);
        // more linking here

        //3. register the Slides
        ConfigCreator.setFirstSlide(hello);
        ConfigCreator.addSlide(hello.getComponent(), hello.getName());
        ConfigCreator.addSlide(interfaceSelect.getComponent(), interfaceSelect.getName());
        ConfigCreator.addSlide(iUart.getComponent(), iUart.getName());
        ConfigCreator.addSlide(iExperimental.getComponent(), iExperimental.getName());
        ConfigCreator.addSlide(iConnect.getComponent(), iConnect.getName());
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
    }

}
