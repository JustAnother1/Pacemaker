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
package de.nomagic.printerController;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public final class Tool
{
    private Tool()
    {
        // Not used !
    }

    public static String fromExceptionToString(final Throwable e)
    {
        if(null == e)
        {
            return "Exception [null]";
        }
        String res = e.getLocalizedMessage() + "\n" ;
        final StringWriter s = new StringWriter();
        final PrintWriter p = new PrintWriter(s);
        e.printStackTrace(p);
        p.flush();
        res = res + s.toString();
        return "Exception [" +res + "]";
    }

    public static String fromByteBufferToHexString(final byte[] buf)
    {
        return fromByteBufferToHexString(buf, buf.length, 0);
    }

    public static String fromByteBufferToHexString(final byte[] buf, int length)
    {
        return fromByteBufferToHexString(buf, length, 0);
    }

    public static String fromByteBufferToHexString(final byte[] buf, int length, int offset)
    {
        if(null == buf)
        {
            return "[]";
        }
        final StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++)
        {
            sb.append(String.format("%02X", buf[i + offset]));
            sb.append(" ");
        }
        return "[" + (sb.toString()).trim() + "]";
    }

    public static String fromByteBufferToUtf8String(final byte[] buf)
    {
        if(null == buf)
        {
            return "[]";
        }
        final StringBuffer sb = new StringBuffer();
        for(int i = 0; i < buf.length; i++)
        {
            sb.append((char)buf[i]);
        }
        return "[" + (sb.toString()).trim() + "]";
    }

    public static boolean isValidChar(final char c)
    {
        final char[] validChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                                   'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                                   'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                                   'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                                   '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for(int i = 0; i < validChars.length; i++)
        {
            if(c == validChars[i])
            {
                return true;
            }
        }
        return false;
    }

    public static String onlyAllowedChars(final String src)
    {
        final StringBuffer dst = new StringBuffer();
        for(int i = 0; i < src.length(); i++)
        {
            final char cur = src.charAt(i);
            if(true == isValidChar(cur))
            {
                dst.append(cur);
            }
        }
        return dst.toString();
    }

    public static String getStacTrace()
    {
        // Thread.dumpStack();
        final StackTraceElement[] trace = (Thread.currentThread()).getStackTrace();
        if(0 == trace.length)
        {
            return "This task has not been started yet !";
        }
        else
        {
            final StringBuffer res = new StringBuffer();
            for(int i = 0; i < trace.length; i++)
            {
                res.append(trace[i].toString());
                res.append("\n");
            }
            return res.toString();
        }
    }
}
