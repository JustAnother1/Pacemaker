package de.nomagic.test.pacemaker;

import de.nomagic.printerController.pacemaker.Protocol;

public final class SlotFactory
{
    private SlotFactory()
    {
    }

    public static Slot getSlot(int type, int[] data)
    {
        switch(type)
        {
        case Protocol.MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE:
            return new BasicLinearMoveSlot(data);

        case Protocol.MOVEMENT_BLOCK_TYPE_COMMAND_WRAPPER:
        case Protocol.MOVEMENT_BLOCK_TYPE_DELAY:
        case Protocol.MOVEMENT_BLOCK_TYPE_SET_ACTIVE_TOOLHEAD:
            return new Slot(type, data);

        default:
            return null;
        }
    }

}
