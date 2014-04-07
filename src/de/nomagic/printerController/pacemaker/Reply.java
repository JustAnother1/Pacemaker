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
package de.nomagic.printerController.pacemaker;

import java.nio.charset.Charset;

import de.nomagic.printerController.Tool;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Reply
{
    private static final int POS_OF_LENGTH = 1;
    private static final int POS_OF_CONTROL = 2;
    private static final int POS_OF_REPLY_CODE = 3;
    private static final int POS_OF_PARAMETER_START = 4;

    private final byte[] data;
    private final int length;
    private final boolean valid;

    public Reply(byte[] data)
    {
        if((null == data) || (POS_OF_PARAMETER_START + 1 > data.length))
        {
            valid = false;
            this.data = new byte[0];
            length = 0;
        }
        else
        {
            valid = true;
            this.data = data;
            length = (0xff & data[POS_OF_LENGTH]);
        }
    }

    public String getDump()
    {
        return "Reply " + Tool.fromByteBufferToHexString(data);
    }

    @Override
    public String toString()
    {
        if(true == isDebugFrame())
        {
            switch(getReplyCode())
            {
            case Protocol.RESPONSE_DEBUG_FRAME_DEBUG_MESSAGE:
                return "Reply : " + getParameterAsString(0);

            case Protocol.RESPONSE_DEBUG_FRAME_NEW_EVENT:
                // TODO
            default:return "Reply " + Tool.fromByteBufferToHexString(data);
            }
        }
        else
        {
            if(true == valid)
            {
                return "Reply " + Tool.fromByteBufferToHexString(data);
            }
            else
            {
                return "invalid Reply!";
            }
        }
    }

    public byte getReplyCode()
    {
        if(false == valid)
        {
            return (byte) 0xff;
        }
        else
        {
            return data[POS_OF_REPLY_CODE];
        }
    }

    public boolean isOKReply()
    {
        if(false == valid)
        {
            return false;
        }
        else
        {
            if(Protocol.RESPONSE_OK == data[POS_OF_REPLY_CODE])
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public byte getControl()
    {
        if(false == valid)
        {
            return (byte) 0xff;
        }
        else
        {
            return data[POS_OF_CONTROL];
        }
    }

    public byte[] getParameter()
    {
        if(false == valid)
        {
            return new byte[0];
        }
        else
        {
            final byte[] res = new byte[length - 2];
            for(int i = 0; i < length - 2; i ++)
            {
                res[i] = data[i + POS_OF_PARAMETER_START];
            }
            return res;
        }
    }

    public String getParameterAsString(final int offset)
    {
        if(false == valid)
        {
            return "";
        }
        else
        {
            final String res = new String(data,
                                          offset + POS_OF_PARAMETER_START,
                                          (length - 2) - offset,
                                          Charset.forName("UTF-8"));
            return res;
        }
    }

    public boolean isDebugFrame()
    {
        if(false == valid)
        {
            return false;
        }
        else
        {
            if(Protocol.DEBUG_FLAG == (Protocol.DEBUG_FLAG & data[POS_OF_CONTROL]))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public boolean isValid()
    {
        return valid;
    }

}
