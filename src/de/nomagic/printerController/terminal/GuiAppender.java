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
package de.nomagic.printerController.terminal;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 * @param <E>
 *
 */
public class GuiAppender extends AppenderBase<ILoggingEvent>
{
    static private JTextArea out = null;

    public static void setTextArea(JTextArea jTextArea)
    {
        out = jTextArea;
    }

    public GuiAppender()
    {
    }

    @Override
    protected void append(ILoggingEvent e)
    {
        final String message;
        if(null == e)
        {
            return;
        }
        Level l = e.getLevel();
        long startTime = e.getLoggerContextVO().getBirthTime();
        long eventTime = e.getTimeStamp() - startTime;
        if(Level.ERROR == l)
        {
            message = "" + eventTime + ": ERROR : " + e.getFormattedMessage() + "\n";
        }
        else
        {
            message = "" + eventTime + ": " + e.getFormattedMessage() + "\n";
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                if (out != null)
                {
                    out.append(message);
                }
            }
       });
    }


}
