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
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.pacemaker.Reply;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class OrderFrameMacro extends Macro
{
    private static final long serialVersionUID = 1L;
    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private int order;
    private int parameterLength;
    private int[] parameterBytes;
    private Executor exe = null;

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

    public void setParameter(int[] data)
    {
        parameterBytes = data;
    }

}
