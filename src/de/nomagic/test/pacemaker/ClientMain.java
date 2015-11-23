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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientMain
{
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private StatusWindow sw;
    private boolean shouldRun = true;

    public ClientMain(final String[] args)
    {
    	startLogging(args);
    }

    private void startLogging(final String[] args)
    {
        int numOfV = 0;
        for(int i = 0; i < args.length; i++)
        {
            if(true == "-v".equals(args[i]))
            {
                numOfV ++;
            }
        }

        // configure Logging
        switch(numOfV)
        {
        case 0: setLogLevel("warn"); break;
        case 1: setLogLevel("debug");break;
        case 2:
        default:
            setLogLevel("trace");
            break;
        }
    }

    private void setLogLevel(String LogLevel)
    {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try
        {
            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            final String logCfg =
            "<configuration>" +
              "<appender name='STDOUT' class='ch.qos.logback.core.ConsoleAppender'>" +
                "<encoder>" +
                  "<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>" +
                "</encoder>" +
              "</appender>" +
              "<root level='" + LogLevel + "'>" +
                "<appender-ref ref='STDOUT' />" +
              "</root>" +
            "</configuration>";
            ByteArrayInputStream bin;
            try
            {
                bin = new ByteArrayInputStream(logCfg.getBytes("UTF-8"));
                configurator.doConfigure(bin);
            }
            catch(UnsupportedEncodingException e)
            {
                // A system without UTF-8 ? - No chance to do anything !
                e.printStackTrace();
                System.exit(1);
            }
        }
        catch (JoranException je)
        {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public void startGui()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    sw = new StatusWindow();
                }
                catch(final Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startCommunicating(int port)
    {
        // create TCP Port and Listen to connections
        log.info("Starting to listen on Port {}", port);
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(port);
        }
        catch(final IOException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        while(true== shouldRun)
        {
            try
            {
                final Socket s = server.accept();
                log.info("Received a Connection !");
                s.setTcpNoDelay(true);
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                final ProtocolClient pc = new ProtocolClient(in, out, sw);
                if(null != sw)
                {
                    sw.setProtocolClient(pc);
                }
                try
                {
                    pc.communicate();
                }
                catch(final IOException e)
                {
                    log.error(e.getMessage());
                }
                s.close();
            }
            catch(final IOException e)
            {
                log.error(e.getMessage());
            }
        }
        try
        {
            server.close();
        }
        catch(final IOException e)
        {
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(final String[] args) throws IOException
    {
            final ClientMain cm = new ClientMain(args);
            cm.startGui();
            cm.startCommunicating(12345);
    }

}
