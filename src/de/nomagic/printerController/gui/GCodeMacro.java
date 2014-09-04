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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class GCodeMacro extends Macro implements GCodeResultStream
{
    public static final String TYPE_DEFINITION = "G-Code";

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
                log.info(core.executeGCode(lines[i], this));
            }
        }
    }

    @Override
    public String getDefinition()
    {
        final StringBuffer sb = new StringBuffer();
        for(int i = 0; i < lines.length; i++)
        {
            sb.append(lines[i]);
            sb.append( SEPERATOR);
        }
        final String help = sb.toString();
        return TYPE_DEFINITION + SEPERATOR + getPrefix() + SEPERATOR + help;
    }

    public static Macro getMacroFromDefinition(String macroString)
    {
        if(null == macroString)
        {
            return null;
        }
        if(1 > macroString.length())
        {
            return null;
        }
        if(false == macroString.startsWith(TYPE_DEFINITION))
        {
            return null;
        }
        String help = macroString.substring(macroString.indexOf(SEPERATOR) + SEPERATOR.length());
        final String prefix = help.substring(0, help.indexOf(SEPERATOR));
        help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        final Vector<String> vec = new  Vector<String>();
        while(true == help.contains(SEPERATOR))
        {
            final String aLine = help.substring(0, help.indexOf(SEPERATOR));
            vec.add(aLine);
            help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        }
        final GCodeMacro res = new GCodeMacro(vec.toArray(new String[0]));
        res.setValuesFromPrefix(prefix);
        return res;
    }

    @Override
    public void write(String msg)
    {
        // We can not do a log without an end of line :-(
        log.debug(msg);
    }

    @Override
    public void writeLine(String msg)
    {
        log.debug(msg);
    }

}
