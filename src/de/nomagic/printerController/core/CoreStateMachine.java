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
package de.nomagic.printerController.core;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.GCodeResultStream;

/** Public API of Pacemaker Core.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public final class CoreStateMachine
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private boolean isOperational = false;
    private Executor exe;
    private GCodeDecoder decoder;

    public CoreStateMachine(final Cfg cfg)
    {
        log.info("starting Executor,...");
        exe = new ExecutorImpl(cfg);
        if(false == exe.isOperational())
        {
            return;
        }
        log.info("starting G-Code Decoder,...");
        SDCardSimulation sdCard = new SDCardSimulationImpl(new File(cfg.getGeneralSetting("sdcardfolder", "sdcard")));
        decoder = new GCodeDecoder(exe, sdCard);
        SDCardPrinter sdPrinterWorker =  new SDCardPrinterImpl(sdCard, decoder);
        decoder.addSDCardPrinter(sdPrinterWorker);
        // everything is now up and running
        isOperational = true;
    }

    /**
     *
     * @return true if everything is ready to start.
     */
    public boolean isOperational()
    {
        return isOperational;
    }

    public Executor getExecutor()
    {
        return exe;
    }

    public String executeGCode(final String line, final GCodeResultStream resultStream)
    {
        if(false == isOperational)
        {
            return "!! Pacemaker Core is not operational !";
        }
        else
        {
            return decoder.sendLine(line, resultStream);
        }
    }

    public String getLastErrorReason()
    {
        if(false == isOperational)
        {
            return "Pacemaker Core is not operational !";
        }
        else
        {
            return decoder.getLastErrorReason();
        }
    }

    public void close()
    {
        if(null != decoder)
        {
            decoder.close();
        }
        if(null != exe)
        {
            exe.close();
        }
    }

}
