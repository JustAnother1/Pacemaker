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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public final class MacroFactory
{
    private MacroFactory()
    {
    }

    public static Macro getMacroFromLine(String macroString)
    {
        if(null == macroString)
        {
            return null;
        }
        if(1 > macroString.length())
        {
            return null;
        }
        if(true == macroString.startsWith(GCodeMacro.TYPE_DEFINITION))
        {
            return GCodeMacro.getMacroFromDefinition(macroString);
        }
        else if(true == macroString.startsWith(ExecutorMacro.TYPE_DEFINITION))
        {
            return ExecutorMacro.getMacroFromDefinition(macroString);
        }
        else if(true == macroString.startsWith(OrderFrameMacro.TYPE_DEFINITION))
        {
            return OrderFrameMacro.getMacroFromDefinition(macroString);
        }
        // new Macro Types go in here
        else
        {
            return null;
        }
    }

}
