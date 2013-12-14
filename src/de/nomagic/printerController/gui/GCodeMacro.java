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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class GCodeMacro extends Macro
{
    private static final long serialVersionUID = 1L;

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private transient CoreStateMachine core;
    private String[] lines;

    public GCodeMacro(String[] lines)
    {
        this.lines = lines;
    }

    public GCodeMacro(String line)
    {
        lines = new String[1];
        lines[0] = line;
    }

    @Override
    public void updateCore(CoreStateMachine core)
    {
        this.core = core;
    }

    @Override
    public void execute()
    {
        if(null != core)
        {
            for(int i = 0; i < lines.length; i++)
            {
                log.info(core.executeGCode(lines[i]));
            }
        }
    }

}
