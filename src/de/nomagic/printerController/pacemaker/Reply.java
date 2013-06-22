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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class Reply
{
    private final byte replyCode;
    private final byte control;
    private final byte[] parameter;
    public Reply(final byte replyCode, final byte control, final byte[] parameter)
    {
        this.replyCode = replyCode;
        this.control = control;
        this.parameter = parameter;
    }

    public byte getReplyCode()
    {
        return replyCode;
    }

    public boolean isOKReply()
    {
        if(Protocol.RESPONSE_OK == replyCode)
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
        return control;
    }

    public byte[] getParameter()
    {
        return parameter;
    }

    public String getParameterAsString(final int offset)
    {
        final String res = new String(parameter,
                                      offset,
                                      parameter.length -offset,
                                      Charset.forName("UTF-8"));
        return res;
    }

}
