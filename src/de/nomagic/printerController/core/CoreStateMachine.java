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

import de.nomagic.printerController.Cfg;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class CoreStateMachine
{

    private boolean isOperational = false;
    private Executor exe;
    private GCodeDecoder decoder;
    private final ActionHandler handler;

    public CoreStateMachine(final Cfg cfg)
    {
        handler = new ActionHandler(cfg);
        if(false == handler.isOperational())
        {
            return;
        }
        exe = new Executor(handler);
        decoder = new GCodeDecoder(exe);
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

    public boolean executeGCode(final String line)
    {
        if(false == isOperational)
        {
            return false;
        }
        else
        {
            return decoder.sendLine(line);
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
        decoder.close();
        exe.close();
        handler.close();
    }

}
