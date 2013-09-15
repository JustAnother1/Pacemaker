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

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import de.nomagic.printerController.CloseApplication;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TerminalMain implements CloseApplication
{
    private TerminalWindow tw;

    public TerminalMain()
    {
    }

    private void configureLogging()
    {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> appender = new GuiAppender();
        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender(appender);
    }

    private void startGui()
    {
        final CloseApplication Closer = this;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                tw = new TerminalWindow(Closer);
                GuiAppender.setTextArea(tw.getTextArea());
            }
        });
    }

    public static void main(String[] args)
    {
        TerminalMain tm = new TerminalMain();
        tm.configureLogging();
        tm.startGui();
    }

    @Override
    public void close()
    {
        if(null != tw)
        {
            tw.close();
        }
        System.exit(0);
    }
}
