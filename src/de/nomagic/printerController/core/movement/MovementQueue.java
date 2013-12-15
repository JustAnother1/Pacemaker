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
package de.nomagic.printerController.core.movement;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class MovementQueue
{
    public static final int MAX_QUEUE_SIZE = 1000;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private LinkedList<StepperMove> entries = new LinkedList<StepperMove>();
    private final String Name;

    public MovementQueue(String Name)
    {
        this.Name = Name;
    }

    public void add(StepperMove sm)
    {
        entries.add(sm);
    }

    public boolean isEmpty()
    {
        if(1 > entries.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isFull()
    {
        if(MAX_QUEUE_SIZE <= entries.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int size()
    {
        return entries.size();
    }

    public StepperMove getMove(int idx)
    {
        if(idx < entries.size())
        {
            return entries.get(idx);
        }
        else
        {
            return null;
        }
    }

    public void finishedOneMove()
    {
        log.trace("{} : Removing a move", Name);
        entries.removeFirst();
    }

}
