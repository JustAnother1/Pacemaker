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

import de.nomagic.printerController.Tool;
import de.nomagic.printerController.pacemaker.Protocol;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Slot
{
    protected String dataDescription;
    protected String typeDescription;
    private int type;
    private int[] data;

    protected Slot()
    {

    }

    public Slot(int type, int[] data)
    {
        this.type = type;
        this.data = data;
        switch(type)
        {
        case Protocol.MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER:
            typeDescription = "command";
            dataDescription = parseCommand(data);
            break;

        case Protocol.MOVEMENT_BLOCK_TYPE_DELAY:
            typeDescription = "Delay";
            dataDescription = parseDelay(data);
            break;

        case Protocol.MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD:
            typeDescription = "set active Toolhead";
            dataDescription = parseToolhead(data);
            break;

        default:
            typeDescription = "invalid(" + type + ")";
            break;
        }
    }

    private String parseToolhead(int[] data)
    {
        return "" + data[0];
    }

    private String parseDelay(int[] data)
    {
        final int time = ((data[0] * 256) + data[1]) *10;
        return "" + time + "us";
    }

    private String parseCommand(int[] data)
    {
        return Protocol.orderCodeToString((byte)(0xff & data[0]))
               + Tool.fromByteBufferToHexString(data, data.length -1, 1);
    }

    @Override
    public String toString()
    {
        return "Slot [" + typeDescription + " : " + dataDescription + "]";
    }

    public void validate()
    {
        switch(type)
        {
        case Protocol.MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER:
            System.out.println("Command : " + Tool.fromByteBufferToHexString(data));
            //TODO improve
            break;

        case Protocol.MOVEMENT_BLOCK_TYPE_DELAY:
            System.out.println("Delay : " + Tool.fromByteBufferToHexString(data));
            //TODO improve
            break;

        case Protocol.MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD:
            System.out.println("active Toolhead : " + Tool.fromByteBufferToHexString(data));
            //TODO improve
            break;

        default:
            System.err.println("ERROR: invalid(" + type + ") !");
            break;
        }
    }


}
