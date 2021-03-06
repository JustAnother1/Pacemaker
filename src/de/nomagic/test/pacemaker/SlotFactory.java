package de.nomagic.test.pacemaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.pacemaker.Protocol;

public final class SlotFactory
{
	private static final Logger Log = LoggerFactory.getLogger("SlotFactory");
	
	
    private SlotFactory()
    {
    }

    public static Slot getSlot(int type, int[] data, PrinterState curState)
    {
        switch(type)
        {
        case Protocol.MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE:
            return new BasicLinearMoveSlot(data, curState);

        case Protocol.MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER:
        case Protocol.MOVEMENT_BLOCK_TYPE_DELAY:
        case Protocol.MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD:
            return new Slot(type, data);

        default:
        	Log.error("ERROR: invalid type ({})!", type);
            return null;
        }
    }

}
