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

/** time has run out on something.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TimeoutException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public TimeoutException()
    {
    }

    public TimeoutException(String message)
    {
        super(message);
    }

    public TimeoutException(Throwable cause)
    {
        super(cause);
    }

    public TimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
