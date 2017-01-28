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
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.core.ActionResponse;
import de.nomagic.printerController.core.Action_enum;
import de.nomagic.printerController.core.Event;
import de.nomagic.printerController.core.EventSource;
import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class PlannedMoves implements EventSource
{
    public static final int QUEUE_FLUSH_TIMEOUT_MS = 50;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private LinkedList<CartesianMove> entries = new LinkedList<CartesianMove>();
    private volatile boolean needsFlushing = false;
    private Protocol pro;
    private int MaxClientStepsPerSecond;
    private final int timeoutID;
    private final TimeoutHandler to;
    private boolean firstMove = true;

    public PlannedMoves(int MaxClientStepsPerSecond, TimeoutHandler to)
    {
        this.MaxClientStepsPerSecond = MaxClientStepsPerSecond;
        this.to= to;
        timeoutID = to.createTimeout(new Event(Action_enum.timeOut, null, this), QUEUE_FLUSH_TIMEOUT_MS);
        if(TimeoutHandler.ERROR_FAILED_TO_CREATE_TIMEOUT == timeoutID)
        {
            log.error("Failed to create Queue Flush Timeout !");
        }
        else
        {
            to.startTimeout(timeoutID);
        }
    }

    @Override
    public String toString()
    {
        return "pro=" + pro + ", Max Steps =" + MaxClientStepsPerSecond + "/second\n";
    }

    public void addProtocol(Protocol pro)
    {
        this.pro = pro;
    }

    /** only called by timeout.
     *  Timeout thing to make sure single moves get send out.
     */
    @Override
    public void reportEventStatus(ActionResponse response, Reference ref)
    {
        to.startTimeout(timeoutID);
        final int size= entriesSize();
        if(1 == size)
        {
            if(false == needsFlushing)
            {
                needsFlushing = true;
            }
            else
            {
                // OK the queue has been with a single move for two times -> flush the Queue
                if(true == flushQueueToClient(ref))
                {
                    needsFlushing = false;
                }
                else
                {
                    System.exit(99);
                }
            }
        }
        else
        {
            needsFlushing = false;
        }
    }

    public boolean flushQueueToClient(Reference ref)
    {
    	boolean res = true;
        final int size = entriesSize();
        if(0 < size)
        {
            res = sendLastMoves();
        }
        // else no moves to send
        // wait for client to execute all the queued moves
        while(0 < pro.getNumberOfCommandsInClientQueue(ref))
        {
        	try
        	{
				Thread.sleep(1);
			}
        	catch (InterruptedException e)
        	{
        		// is OK
			}
        }
        return res;
    }

    public boolean addMove(CartesianMove aMove)
    {
    	if(true == firstMove)
    	{
    		aMove.setStartSpeedMms(0);
    		firstMove = false;
    	}
        synchronized(entries)
        {
            entries.addLast(aMove);
        }
        sendAllPossibleMoves();
        return true;
    }

    public void addEndStopOnOffCommand(boolean on, Integer[] switches, PrinterProperties printerProps)
    {
        // Add the command to the last issued move
    	CartesianMove aMove = null;
        try
        {
            synchronized(entries)
            {
                aMove = entries.removeLast();
            }
        }
        catch(NoSuchElementException e)
        {
            aMove = null;
        }
        if(null == aMove)
        {
            aMove = new CartesianMove(MaxClientStepsPerSecond, printerProps);
    		aMove.setStartSpeedMms(0);
            log.trace("created Move({}) to hold end stop command.", aMove.getId());
        }
        aMove.addEndStopOnOffCommand(on, switches);
        addMove(aMove);
    }

    public boolean hasAllMovementFinished()
    {
        synchronized(entries)
        {
            return entries.isEmpty();
        }
    }

    private int entriesSize()
    {
        synchronized(entries)
        {
            return entries.size();
        }
    }

    private CartesianMove getFirstMove()
    {
        CartesianMove firstMove;
        synchronized(entries)
        {
        	try
        	{
        		firstMove = entries.removeFirst();
        	}
        	catch(NoSuchElementException e)
        	{
        		firstMove = null;
        	}
        }
        return firstMove;
    }

    private void sendAllPossibleMoves()
    {
        final int size = entriesSize();
        if(1 > size)
        {
            // no moves available to send
        	log.trace("No Move to send");
            return;
        }
        CartesianMove firstMove;
        for(;;)
        {
	        firstMove = getFirstMove();
	        if(null == firstMove)
	        {
	        	log.trace("Could not get move!(size = {})", size);
	        	return;
	        }
	        CartesianMove secondMove;
	        if(false == firstMove.hasEndSpeedSet())
	        {
	        	secondMove = getFirstMove();
	        	boolean res = firstMove.send(pro, secondMove);
	            synchronized(entries)
	            {
	            	entries.addFirst(secondMove);
	            }
	            if(false == res)
	            {
	            	log.error("Failed to send move");
	            	return;
	            }
	        }
	        else
	        {
	        	if(false == firstMove.send(pro, null))
	        	{
	        		log.error("Failed to get Queue size");
	        		return;
	        	}
	        }
        }
    }

    private boolean sendLastMoves()
    {
        int size = entriesSize();
        if(1 > size)
        {
            // no moves available to send
        	log.trace("No last move to send.");
            return true;
        }
        synchronized(entries)
        {
            final CartesianMove aMove = entries.removeLast();
            if(null != aMove)
            {
	            aMove.setEndSpeedMms(0.0);
	            entries.addLast(aMove);
            }
        }
        sendAllPossibleMoves();
        // now the Queue _must_ be empty!
        size = entriesSize();
        if(0 != size)
        {
            log.error("{} Moves in Queue after flushing !", size);
            return false;
        }
        return true;
    }



}
