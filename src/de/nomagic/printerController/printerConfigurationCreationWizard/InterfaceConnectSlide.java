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
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.pacemaker.TimeoutException;
import de.nomagic.printerController.Cfg;

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
        // If already connected then close connection
        Object obj = ds.getObject(WizardMain.DS_CLIENT_CONNECTION_NAME);
        if(true == obj instanceof ClientConnection)
        {
            ClientConnection cc = (ClientConnection)obj;
            cc.close();
            cc = null;
        }
        connectLog.setText("trying to connect to Client,..");
        configCreator.setNextAllowed(false);
        obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        Cfg cfg = null;
        if(true == obj instanceof Cfg)
        {
            cfg = (Cfg)obj;
            // TODO Thread Start:
            CoreStateMachine pp = null;
            try
            {
                pp = new CoreStateMachine(cfg);
                if(true == pp.isOperational())
                {
                    log.info("connection to client is now open !");
                    connectLog.setText(connectLog.getText() + "\nconnection to client is now open !");
                    configCreator.setNextAllowed(true);
                }
                else
                {
                    log.error("Could not open connection to client!");
                }
            }
            catch(TimeoutException e)
            {
                log.error("Timeout from client!");
                connectLog.setText(connectLog.getText() + "\nTimeout from client !");
                if(null != pp)
                {
                    pp.close();
                    pp = null;
                }
            }
            // Thread end
        }
        else
        {
            log.error("Configuration Object missing from Data Store !");
        }
        return ds;
    }

}
