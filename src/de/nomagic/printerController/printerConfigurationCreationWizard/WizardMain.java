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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;

import de.nomagic.Translator.ResourceBundleTranslator;
import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.BaseWindow;
import de.nomagic.WizardDialog.CancelAction;
import de.nomagic.WizardDialog.WizardSlide;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.printerController.printer.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class WizardMain implements CancelAction
{
    public final static String DS_CONFIGURATION_NAME = "cfg";

    final JFileChooser fc = new JFileChooser();
    // Slides
    private WizardSlide hello;

    public WizardMain()
    {
        final Translator t = new ResourceBundleTranslator(null);
        hello = new WelcomeSlide(t);
        // more slides created here

        // Link the Slides


        final BaseWindow ConfigCreator = new BaseWindow(t, hello, this);
        ConfigCreator.addSlide(hello.getComponent(), hello.getName());
        // more slides connected here

        javax.swing.SwingUtilities.invokeLater(ConfigCreator);
    }


    public static void main(String[] args)
    {
        @SuppressWarnings("unused")
        WizardMain wm = new WizardMain();
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
