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

import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class OrderFrameMacro extends Macro
{
    public static final String TYPE_DEFINITION = "OrderFrame";

    private static final long serialVersionUID = 1L;
    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private int order;
    private int parameterLength;
    private Integer[] parameterBytes;
    private transient Executor exe = null;

    public OrderFrameMacro()
    {
    }

    @Override
    public void updateCore(CoreStateMachine core)
    {
        exe = core.getExecutor();
    }

    @Override
    public void execute()
    {
        if(null != exe)
        {
            final Reply r = exe.sendRawOrderFrame(0, order, parameterBytes, parameterLength);
            log.info(r.toString());
        }
    }

    public void setOrder(int orderI)
    {
        this.order = orderI;
    }

    public void setParameterLength(int paraLength)
    {
        parameterLength = paraLength;
    }

    public void setParameter(Integer[] data)
    {
        parameterBytes = data;
    }

    @Override
    public String getDefinition()
    {
        final StringBuffer sb = new StringBuffer();
        for(int i = 0; i < parameterBytes.length; i++)
        {
            sb.append(parameterBytes[i]);
            sb.append( SEPERATOR);
        }
        final String help = sb.toString();
        return TYPE_DEFINITION + SEPERATOR +
               getPrefix() + SEPERATOR +
               order + SEPERATOR +
               parameterLength + SEPERATOR +
               help;
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
        final String OrderStr =  help.substring(0, help.indexOf(SEPERATOR));
        final int order = Integer.parseInt(OrderStr);
        help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        final String LengthStr = help.substring(0, help.indexOf(SEPERATOR));
        final int length = Integer.parseInt(LengthStr);
        help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        final Vector<Integer> vec = new  Vector<Integer>();
        while(true == help.contains(SEPERATOR))
        {
            final String aLine = help.substring(0, help.indexOf(SEPERATOR));
            vec.add(Integer.parseInt(aLine));
            help = help.substring(help.indexOf(SEPERATOR) + SEPERATOR.length());
        }
        if(length <= vec.size())
        {
            final OrderFrameMacro res = new OrderFrameMacro();
            res.setValuesFromPrefix(prefix);
            res.setOrder(order);
            res.setParameterLength(length);
            res.setParameter(vec.toArray(new Integer[0]));
            return res;
        }
        else
        {
            return null;
        }
    }

}
