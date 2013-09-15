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
import java.util.Arrays;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Reply
{
    private final static int POS_OF_REPLY_CODE = 1;
    private final static int POS_OF_LENGTH = 3;
    private final static int POS_OF_CONTROL = 2;
    private final static int POS_OF_PARAMETER_START = 4;

    private final byte[] data;
    private final int length;

    public Reply(byte[] data)
    {
        this.data = data;
        length = (0xff & data[POS_OF_LENGTH]);
    }

    @Override
    public String toString()
    {
        return "Reply [data=" + Arrays.toString(data) + ", length=" + length + "]";
    }

    public byte getReplyCode()
    {
        return data[POS_OF_REPLY_CODE];
    }

    public boolean isOKReply()
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

    public byte getControl()
    {
        return data[POS_OF_CONTROL];
    }

    public byte[] getParameter()
    {
        byte[] res = new byte[length];
        for(int i = 0; i < length; i ++)
        {
            res[i] = data[i + POS_OF_PARAMETER_START];
        }
        return res;
    }

    public String getParameterAsString(final int offset)
    {
        final String res = new String(data,
                                      offset + POS_OF_PARAMETER_START,
                                      length - offset,
                                      Charset.forName("UTF-8"));
        return res;
    }

    public boolean isDebugFrame()
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
