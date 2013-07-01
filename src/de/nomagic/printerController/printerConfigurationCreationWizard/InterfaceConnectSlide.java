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

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.BaseWindow;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceConnectSlide extends OneNextWizardSlide
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private JPanel slide = new JPanel();
    private JTextArea connectLog = new JTextArea();
    private BaseWindow configCreator;
    private final PrintProcess pp = new PrintProcess();

    public InterfaceConnectSlide(Translator t, BaseWindow configCreator)
    {
        this.configCreator = configCreator;
        slide.add(connectLog);
    }

    @Override
    public String getName()
    {
        return "InterfaceConnect";
    }

    @Override
    public Component getComponent()
    {
        return slide;
    }

    @Override
    public DataStore actionOnShow(DataStore ds)
    {
        connectLog.setText("trying to connect to Client,..");
        configCreator.setNextAllowed(false);
        Object obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        Cfg cfg = null;
        if(true == obj instanceof Cfg)
        {
            cfg = (Cfg)obj;
            // TODO Thread Start:
            pp.setCfg(cfg);
            if(true == pp.connectToPacemaker())
            {
                log.trace("connection to client is now open !");
                connectLog.setText(connectLog.getText() + "\nconnection to client is now open !");
                DeviceInformation di = pp.getPrinterAbilities();
                ds.putObject(WizardMain.DS_PRINT_PROCESS_NAME, pp);
                ds.putObject(WizardMain.DS_DEVICE_INFORMATION_NAME, di);
                configCreator.setNextAllowed(true);
            }
            else
            {
                log.info("connection failed !");
                connectLog.setText(connectLog.getText() + "\nconnection failed !");
            }
            // Thread end
        }
        return ds;
    }

}
