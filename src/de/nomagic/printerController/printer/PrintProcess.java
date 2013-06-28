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
package de.nomagic.printerController.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.gcode.GCodeDecoder;
import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.pacemaker.ClientConnectionFactory;
import de.nomagic.printerController.pacemaker.DeviceInformation;
import de.nomagic.printerController.pacemaker.Protocol;
import de.nomagic.printerController.planner.Planner;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class PrintProcess
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Cfg cfg;
    private Protocol proto = null;
    private Planner plan = null;
    private GCodeDecoder decoder = null;
    private boolean ClientisConnected = false;
    private ClientConnection cc;

    public PrintProcess()
    {
    }

    public boolean connectToPacemaker()
    {
        cc = ClientConnectionFactory.establishConnectionTo(cfg);
        if(null == cc)
        {
            log.error("Could not establish the connection !");
            return false;
        }
        else
        {
            proto = new Protocol();
            proto.setCfg(cfg);
            proto.ConnectToChannel(cc);
            plan = new Planner(proto);
            decoder = new GCodeDecoder(plan);
            ClientisConnected = true;
            return true;
        }
    }

    public void closeClientConnection()
    {
        if(null != cc)
        {
            cc.close();
            cc = null;
        }
        ClientisConnected = false;
    }

    public boolean isClientConnected()
    {
        return ClientisConnected;
    }

    public boolean executeGCode(final String line)
    {
        return decoder.sendLine(line);
    }

    public Cfg getCfg()
    {
        return cfg;
    }

    public void setCfg(final Cfg cfg)
    {
        this.cfg = cfg;
        if((null != cfg) && (null != proto))
        {
            proto.setCfg(cfg);
        }
    }

    public DeviceInformation getPrinterAbilities()
    {
        return plan.getPrinterAbilities();
    }

}
