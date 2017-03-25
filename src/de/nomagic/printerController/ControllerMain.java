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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import de.nomagic.printerController.Interface.InteractiveInterface;
import de.nomagic.printerController.Interface.StandardStreamInterface;
import de.nomagic.printerController.Interface.TcpInterface;
import de.nomagic.printerController.Interface.UdpInterface;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.Reference;
import de.nomagic.printerController.gui.MainWindow;

/** Main Class to start Pacemaker Host.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ControllerMain implements CloseApplication, GCodeResultStream
{
    public static final String DEFAULT_CONFIGURATION_FILE_NAME = "pacemaker.cfg";
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private final Cfg cfg = new Cfg();
    private String fileToPrint = null;
    private boolean hasReadConfiguration = false;
    private boolean shallStartGui = true;
    private boolean shallStartTcp = false;
    private boolean schallStartUdp = false;
    private boolean schallStartStandardStreams = false;
    private CoreStateMachine core;
    private Vector<InteractiveInterface> interfaces = new Vector<InteractiveInterface>();

    public ControllerMain(final String[] args)
    {
    	 Thread.setDefaultUncaughtExceptionHandler(
                 new Thread.UncaughtExceptionHandler() {
                     @Override public void uncaughtException(Thread t, Throwable e) {
                         System.out.println(t.getName()+": "+e);
                         e.printStackTrace();
                         System.exit(-1);
                     }
                 });
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
            System.out.println("Build from " + getCommitID());
            break;
        }
    }

    public void printHelp()
    {
        System.out.println("Printer Controller for Pacemaker");
        System.out.println("Parameters:");
        System.out.println("-h                         : print this message.");
        System.out.println("-p <G-Code File>           : print the file and exit(does not start other interfaces)");
        System.out.println("-r <Configuration File>    : read configuration from file\n"
                         + "                           : defaults to " + DEFAULT_CONFIGURATION_FILE_NAME);
        System.out.println("-c TCP:<host or ip>:<port> : connect to client using TCP");
        System.out.println("\n"
                         + "-c UART:<device>,<baudrate>,<bits per symbol>,<parity>,<Numbe of Stop Bits>\n"
                         + "                           : connect to client using UART");
        System.out.println("-t                         : enable the TCP Interface");
        System.out.println("-u                         : enable the UDP Interface");
        System.out.println("-s                         : enable the Standard Input Output Stream Interface");
        System.out.println("--no-gui                   : dont start the graphic Interface");
        System.out.println("-v                         : verbose output for even more messages use -v -v");
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

    private void configureGuiLogging()
    {
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Appender<ILoggingEvent> appender = new GuiAppender();
        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender(appender);
    }

    public boolean parseCommandLineParameters(final String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].startsWith("-"))
            {
                if(true == "-h".equals(args[i]))
                {
                    return false;
                }
                else if(true == "-c".equals(args[i]))
                {
                    i++;
                    Integer num = cfg.getNumberOfClients();
                    num ++;
                    cfg.setClientDeviceString(num, args[i]);
                }
                else if(true == "-p".equals(args[i]))
                {
                    i++;
                    fileToPrint = args[i];

                }
                else if(true == "-r".equals(args[i]))
                {
                    i++;
                    try
                    {
                        final FileInputStream cfgIn = new FileInputStream(new File(args[i]));
                        cfg.readFrom(cfgIn);
                        hasReadConfiguration = true;
                    }
                    catch (final FileNotFoundException e)
                    {
                        hasReadConfiguration = false;
                        System.err.println(e.getLocalizedMessage());
                        return false;
                    }
                }
                else if(true == "-t".equals(args[i]))
                {
                    shallStartTcp = true;
                }
                else if(true == "-u".equals(args[i]))
                {
                    schallStartUdp = true;
                }
                else if(true == "-s".equals(args[i]))
                {
                    schallStartStandardStreams = true;
                }
                else if(true == "-v".equals(args[i]))
                {
                    // already handled -> ignore
                }
                else if(true == "--no-gui".equals(args[i]))
                {
                    shallStartGui = false;
                }
                else
                {
                    System.err.println("Invalid Parameter : " + args[i]);
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        // read default configuration
        if(false == hasReadConfiguration)
        {
            System.out.println("No Configuration File defined. Trying default ("
                               + DEFAULT_CONFIGURATION_FILE_NAME + ") !");
            try
            {
                final FileInputStream cfgIn = new FileInputStream(new File(DEFAULT_CONFIGURATION_FILE_NAME));
                cfg.readFrom(cfgIn);
                hasReadConfiguration = true;
            }
            catch (final FileNotFoundException e)
            {
                hasReadConfiguration = false;
                System.err.println(e.getLocalizedMessage());
                // this is OK if we go for the GUI !
            }
        }
        return true;
    }

    public static String getCommitID()
    {
        try
        {
            final InputStream s = ControllerMain.class.getResourceAsStream("/commit-id");
            final BufferedReader in = new BufferedReader(new InputStreamReader(s));
            final String commitId = in.readLine();
            final String changes = in.readLine();
            if(null != changes)
            {
                if(0 < changes.length())
                {
                    return commitId + "-(" + changes + ")";
                }
                else
                {
                    return commitId;
                }
            }
            else
            {
                return commitId;
            }
        }
        catch( Exception e )
        {
            return e.toString();
        }
    }

    public void sendGCodeFile()
    {
        if(false == hasReadConfiguration)
        {
            System.out.println("No Configuration File found ! Printing not possible !");
            return;
        }

        final CoreStateMachine pp = new CoreStateMachine(cfg);
        if(false == pp.isOperational())
        {
            System.err.println("Could not Connect to Pacemaker Client !");
            return;
        }

        String line;
        try
        {
            int linecount = 0;
            final InputStream fis = new FileInputStream(fileToPrint);
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(fis, Charset.forName("UTF-8")) );
            while ((line = br.readLine()) != null)
            {
                System.out.print("\rNow sending Line " + linecount + "  ");
                linecount ++;
                final String lineResult = pp.executeGCode(line, this);
                writeLine(lineResult);
                if(true  == lineResult.startsWith("!!"))
                {
                    log.error("Failed to send the Line : {} !", line);
                    log.error("The Reply was : {} !", lineResult);
                    log.error("The Problem was : {} !", pp.getLastErrorReason());
                    break;
                }
            }
            // flush movement queue
            Executor exe = pp.getExecutor();
            exe.letMovementStop(new Reference("G-Code File"));
            log.trace("Closing G-Code File,..");
            br.close();
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        log.trace("Closing Executor,...");
        final Executor exe = pp.getExecutor();
        Reference ref = new Reference(this.getSource());
        exe.waitForClientQueueEmpty(ref);
        log.trace("Closing Core,...");
        pp.close(ref);
        log.trace("Finished Sending the G-Code File.");
    }

    public boolean hasFileToPrint()
    {
        if(null == fileToPrint)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void startInterfaces()
    {
        // set up the printer
        if(false == hasReadConfiguration)
        {
            System.out.println("No Configuration File found ! Printing not possible !");
            return;
        }

        core = new CoreStateMachine(cfg);

        final CloseApplication Closer = this;
        // If we want the GUI then we want it even with non operational core!
        if(true == shallStartGui)
        {
            configureGuiLogging();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        @SuppressWarnings("unused")
                        final MainWindow gui = new MainWindow(cfg, core, Closer);
                    }
                    catch(final Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        if(false == core.isOperational())
        {
            System.err.println("Could not Connect to Pacemaker Client !");
            if(false == shallStartGui)
            {
            	close();
            }
            return;
        }
        if(true == shallStartTcp)
        {
            final TcpInterface tcp = new TcpInterface();
            tcp.addPacemakerCore(core);
            tcp.start();
            interfaces.add(tcp);
        }
        if(true == schallStartUdp)
        {
            final UdpInterface udp = new UdpInterface();
            udp.addPacemakerCore(core);
            udp.start();
            interfaces.add(udp);
        }
        if(true == schallStartStandardStreams)
        {
            final StandardStreamInterface stdStream = new StandardStreamInterface();
            stdStream.addPacemakerCore(core);
            stdStream.start();
            interfaces.add(stdStream);
        }
    }

    public static void main(final String[] args)
    {
        final ControllerMain cm = new ControllerMain(args);
        if(false == cm.parseCommandLineParameters(args))
        {
            cm.printHelp();
            return;
        }
        if(false == cm.hasFileToPrint())
        {
            cm.startInterfaces();
        }
        else
        {
            cm.sendGCodeFile();
            System.exit(0);
        }
    }

    @Override
    public void close()
    {
        final Iterator<InteractiveInterface> it = interfaces.iterator();
        while(it.hasNext())
        {
            final InteractiveInterface cutInterface = it.next();
            cutInterface.close();
        }
        if(null != core)
        {
            core.close(new Reference(this.getSource()));
        }
    }

    @Override
    public void write(String msg)
    {
        // We can not do a log without an end of line :-(
        log.debug(msg);
    }

    @Override
    public void writeLine(String msg)
    {
        log.debug(msg);
    }

	@Override
	public String getSource()
	{
		if(true == hasFileToPrint())
		{
			return fileToPrint;
		}
		return "cmdline";
	}

}
