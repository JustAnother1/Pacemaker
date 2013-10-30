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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Interface.InteractiveInterface;
import de.nomagic.printerController.Interface.StandardStreamInterface;
import de.nomagic.printerController.Interface.TcpInterface;
import de.nomagic.printerController.Interface.UdpInterface;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.gui.MainWindow;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ControllerMain implements CloseApplication
{
    public final static String DEFAULT_CONFIGURATION_FILE_NAME = "pacemaker.cfg";
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Cfg cfg = new Cfg();
    private String fileToPrint = null;
    private boolean hasReadConfiguration = false;
    private boolean shallStartGui = true;
    private boolean shallStartTcp = false;
    private boolean schallStartUdp = false;
    private boolean schallStartStandardStreams = false;
    private CoreStateMachine core;
    private Vector<InteractiveInterface> interfaces = new Vector<InteractiveInterface>();

    /**
     *
     */
    public ControllerMain()
    {
    }

    public void printHelp()
    {
        System.out.println("Printer Controller for Pacemaker");
        System.out.println("Parameters:");
        System.out.println("-h                         : print this message.");
        System.out.println("-p <G-Code File>           : print the file and exit(does not start other interfaces");
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
        if(false == hasReadConfiguration)
        {
            System.out.println("No Configuration File defined. Trying default (" + DEFAULT_CONFIGURATION_FILE_NAME + ") !");
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

    public void sendGCodeFile()
    {
        if(false == hasReadConfiguration)
        {
            System.out.println("No Configuration File found ! Printing not possible !");
            return;
        }

        CoreStateMachine pp = new CoreStateMachine(cfg);
        if(false == pp.isOperational())
        {
            System.err.println("Could not Connect to Pacemaker Client !");
            return;
        }

        String line;
        try
        {
            final InputStream fis = new FileInputStream(fileToPrint);
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(fis, Charset.forName("UTF-8")) );
            while ((line = br.readLine()) != null)
            {
                if(false == pp.executeGCode(line))
                {
                    log.error("Failed to send the Line : {} !", line);
                    break;
                }
            }
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
        pp.close();
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
        core = new CoreStateMachine(cfg);
        final CloseApplication Closer = this;
        // If we want the GUI then we want it even with non operational core!
        if(true == shallStartGui)
        {
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
            return;
        }
        if(true == shallStartTcp)
        {
            TcpInterface tcp = new TcpInterface();
            tcp.addPacemakerCore(core);
            tcp.addCloser(Closer);
            tcp.start();
            interfaces.add(tcp);
        }
        if(true == schallStartUdp)
        {
            UdpInterface udp = new UdpInterface();
            udp.start();
            interfaces.add(udp);
        }
        if(true == schallStartStandardStreams)
        {
            StandardStreamInterface stdStream = new StandardStreamInterface();
            stdStream.start();
            interfaces.add(stdStream);
        }
    }

    public static void main(final String[] args)
    {
        final ControllerMain cm = new ControllerMain();
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
        }
    }

    @Override
    public void close()
    {
        Iterator<InteractiveInterface> it = interfaces.iterator();
        while(it.hasNext())
        {
            InteractiveInterface cutInterface = it.next();
            cutInterface.close();
        }
        core.close();
    }

}
