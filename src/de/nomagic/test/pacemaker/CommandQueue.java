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

import java.util.LinkedList;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class CommandQueue
{
    public static final int totalSlots = 500;
    private int executedSlots = 0;
    private LinkedList<Slot> queue = new LinkedList<Slot>();
    private Thread worker;

    public CommandQueue()
    {
        worker = new Thread() {
            public void run()
            {
                try
                {
                    for(;;)
                    {
                        remove();
                        sleep(500);
                    }
                }
                catch(InterruptedException e)
                {
                }
            }
        };
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
            System.err.println("ERROR: Parameter to short !");
        }
        int usedBytes = 0;
        do
        {
            final int length = parameter[0 + usedBytes];
            final int type = parameter[1 + usedBytes];
            if(0 == length)
            {
                System.err.println("ERROR: invalid length !");
                break;
            }
            System.out.println("Found Block of Type " + type + " and length " + length);
            if(length + usedBytes < ParameterLength)
            {
                final int[] slotData = new int[length -1];
                System.arraycopy(parameter, 2 + usedBytes,
                                 slotData, 0,
                                 length -1);
                final Slot theSlot = SlotFactory.getSlot(type, slotData);
                validate(theSlot);
                System.out.println("adding : " + theSlot);
                queue.add(theSlot);
                usedBytes = usedBytes + length + 1;
            }
            else
            {
                System.err.println("ERROR: invalid Data !");
                break;
            }
        } while((usedBytes + 2) < ParameterLength);
        return sendOKReply();
    }

    private void validate(Slot theSlot)
    {
        if(null == theSlot)
        {
            System.err.println("ERROR: invalid Data !");
        }
        if(theSlot instanceof BasicLinearMoveSlot)
        {

        }
        else
        {
            theSlot.validate();
        }
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
