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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TimeOutTask extends Thread implements TimeoutHandler
{
    public static final int MAX_TIMEOUT = 10;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private AtomicIntegerArray countdowns = new AtomicIntegerArray(MAX_TIMEOUT);

    private ConcurrentHashMap<Integer, Event> TimeoutEvents = new ConcurrentHashMap<Integer, Event>();
    private ConcurrentHashMap<Integer, Integer> TimeoutTimes = new ConcurrentHashMap<Integer, Integer>();
    private AtomicInteger nextTimeoutID = new AtomicInteger(-1);

    public TimeOutTask()
    {
        super("TimeOutTask");
        for(int i = 0; i < MAX_TIMEOUT; i++)
        {
            countdowns.set(i, -1);
        }
    }


    public void run()
    {
        do
        {
            try
            {
                sleep(1);
            }
            catch(InterruptedException e1)
            {
                return;
            }
            for(int i = 0; i < MAX_TIMEOUT; i++)
            {
                if(countdowns.get(i) > 0)
                {
                    if(true == countdowns.compareAndSet(i, 1, -1))
                    {
                        // timeout !
                        log.trace("Timeout Event!");
                        final Event e = TimeoutEvents.get(i);
                        final EventSource src = e.getSrc();
                        if(null != src)
                        {
                            src.reportEventStatus(new ActionResponse(e.getParameter()));
                        }
                        else
                        {
                            log.trace("No Source !!");
                        }
                    }
                    else
                    {
                        countdowns.decrementAndGet(i);
                        // this timeout is still active
                    }
                }
            }
            // else nothing to do
        }while(false == this.isInterrupted());
    }

    @Override
    public int createTimeout(Event e, int ms)
    {
        final int nextId = nextTimeoutID.incrementAndGet();
        if(MAX_TIMEOUT > nextId)
        {
            TimeoutEvents.put(nextId, e);
            TimeoutTimes.put(nextId, ms);
            return nextId;
        }
        else
        {
            return ERROR_FAILED_TO_CREATE_TIMEOUT;
        }
    }

    @Override
    public void startTimeout(int timeoutId)
    {
        countdowns.set(timeoutId, TimeoutTimes.get(timeoutId));
    }

    @Override
    public void stopTimeout(int timeoutId)
    {
        countdowns.set(timeoutId, -1);
    }

}
