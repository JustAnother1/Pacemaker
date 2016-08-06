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
package de.nomagic.test.pacemaker;

import java.io.IOException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class CommandQueue
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public static final int totalSlots = 500;
    private int executedSlots = 0;
    private LinkedList<Slot> queue = new LinkedList<Slot>();
    private Thread worker;
    private PrinterState curState = new PrinterState();

    public CommandQueue()
    {
        worker = new Thread() {
            public void run()
            {
                try
                {
                    for(;;)
                    {
                    	Slot theSlot;
                        theSlot = remove();
                        if(null != theSlot)
                        {
                        	/*
                        	if(theSlot instanceof BasicLinearMoveSlot)
                        	{
                        		log.info(theSlot.toString());
                        		BasicLinearMoveSlot move = (BasicLinearMoveSlot) theSlot;
                        		log.info(move.getCartesianMove());
                        	}
                        	*/
                        }
                        sleep(500);
                    }
                }
                catch(InterruptedException e)
                {
                }
            }
        };
        worker.start();
    }

    public byte[] clear()
    {
        queue.clear();
        return sendOKReply();
    }

    public byte[] add(int[] parameter, int ParameterLength)
    {
        if(2 > ParameterLength)
        {
            log.error("ERROR: Parameter to short !");
        }
        if(2 == ParameterLength)
        {
        	// Host is polling to know free slots -> no data in this Request
        	return senRawOKReply();
        }
        int usedBytes = 0;
        do
        {
            final int length = parameter[0 + usedBytes];
            final int type = parameter[1 + usedBytes];
            if(0 == length)
            {
                log.error("ERROR: length = 0 !");
                break;
            }            
            if(1 == length)
            {
            	// empty packet -> Host needs to know how many free slots,..
            	return senRawOKReply();
            }
            log.trace("Found Block of Type " + type + " and length " + length);
            if(length + usedBytes < ParameterLength)
            {
                final int[] slotData = new int[length -1];
                System.arraycopy(parameter, 2 + usedBytes,
                                 slotData, 0,
                                 length -1);
                final Slot theSlot = SlotFactory.getSlot(type, slotData, curState);
                validate(theSlot);
                log.trace("adding : " + theSlot);
                queue.add(theSlot);
            	if(theSlot instanceof BasicLinearMoveSlot)
            	{
            		log.info(theSlot.toString());
            		BasicLinearMoveSlot move = (BasicLinearMoveSlot) theSlot;
            		log.info(move.getCartesianMove(curState));
            	}
                usedBytes = usedBytes + length + 1;
            }
            else
            {
                log.error("ERROR: length inconsistent !");
                return sendRawErrorReply(Protocol.MOVEMENT_BLOCK_MALFORMED_BLOCK);                
            }
        } while((usedBytes + 2) < ParameterLength);
        return senRawOKReply();
    }

	private void validate(Slot theSlot)
    {
        if(null == theSlot)
        {
            log.error("ERROR: invalid Slot !");
            return;
        }
        theSlot.validate();
    }

    public Slot remove()
    {
        if(false == queue.isEmpty())
        {
            return queue.removeFirst();
        }
        else
        {
            return null;
        }
    }
    
    private byte[] sendRawErrorReply(byte cause) 
    {
        final byte[] res = new byte[9 + Protocol.REPLY_POS_OF_START_OF_PARAMETER];
        res[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_ORDER_SPECIFIC_ERROR;
        res[Protocol.REPLY_POS_OF_LENGTH] = (byte) (9 + 2);
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 0] = cause;
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 1] = 0;
        final int availableSlots = totalSlots - queue.size();
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 2] = (byte)(0xff & (availableSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 3] = (byte)(0xff & (availableSlots));
        final int usedSlots =  0;//queue.size();
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 4] = (byte)(0xff & (usedSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 5] = (byte)(0xff & (usedSlots));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 6] = (byte)(0xff & (executedSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 7] = (byte)(0xff & (executedSlots));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 8] = (byte)0xff;
        return res;
	}
    
    private byte[] senRawOKReply()
    {
        final byte[] res = new byte[6 + Protocol.REPLY_POS_OF_START_OF_PARAMETER];
        res[Protocol.REPLY_POS_OF_REPLY_CODE] = Protocol.RESPONSE_OK;
        res[Protocol.REPLY_POS_OF_LENGTH] = (byte) (6 + 2);
        final int availableSlots = totalSlots - queue.size();
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 0] = (byte)(0xff & (availableSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 1] = (byte)(0xff & (availableSlots));
        final int usedSlots =  0;//queue.size();
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 2] = (byte)(0xff & (usedSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 3] = (byte)(0xff & (usedSlots));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 4] = (byte)(0xff & (executedSlots /256));
        res[Protocol.REPLY_POS_OF_START_OF_PARAMETER + 5] = (byte)(0xff & (executedSlots));
        return res;
    }

    private byte[] sendOKReply()
    {
        final byte[] res = new byte[6];
        final int availableSlots = totalSlots - queue.size();
        res[0] = (byte)(0xff & (availableSlots /256));
        res[1] = (byte)(0xff & (availableSlots));
        final int usedSlots =  0;//queue.size();
        res[2] = (byte)(0xff & (usedSlots /256));
        res[3] = (byte)(0xff & (usedSlots));
        res[4] = (byte)(0xff & (executedSlots /256));
        res[5] = (byte)(0xff & (executedSlots));
        return res;
    }

}
