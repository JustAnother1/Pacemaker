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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Event
{
    private final Action_enum type;
    private final Object Parameter;
    private final Object Parameter2;
    private final EventSource src;

    public Event(Action_enum type, Object parameter, EventSource src)
    {
        this.type = type;
        this.Parameter = parameter;
        this.Parameter2 = null;
        this.src = src;
    }

    public Event(Action_enum type, Object parameter, Object parameter2, EventSource src)
    {
        this.type = type;
        this.Parameter = parameter;
        this.Parameter2 = parameter2;
        this.src = src;
    }

    public Action_enum getType()
    {
        return type;
    }

    public Object getParameter()
    {
        return Parameter;
    }

    public Object getParameter2()
    {
        return Parameter2;
    }

    public EventSource getSrc()
    {
        return src;
    }

}
