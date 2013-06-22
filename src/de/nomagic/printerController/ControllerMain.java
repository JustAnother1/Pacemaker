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

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.gui.MainWindow;
import de.nomagic.printerController.printer.Cfg;
import de.nomagic.printerController.printer.PrintProcess;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ControllerMain
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Cfg cfg = new Cfg();
    private String fileToPrint = null;

    /**
     *
     */
    public ControllerMain()
    {
    }

    private void printHelp()
    {
        System.out.println("Printer Controller for Pacemaker");
        System.out.println("Parameters:");
        System.out.println("-h                         : print this message.");
        System.out.println("-p <G-Code File>           : print the file and exit");
        System.out.println("-r <Configuration File>    : read configuration from file");
        System.out.println("-c TCP:<host or ip>:<port> : connect to client using TCP");
        System.out.println("\n" +
        		           "-c UART:<device>,<baudrate>,<bits per symbol>,<parity>,<Numbe of Stop Bits>\n" +
                           "                           : connect to client using UART");
    }

    public boolean parseCommandLineParameters(final String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].startsWith("-"))
            {
                if(true == "-h".equals(args[i]))
                {
                    printHelp();
                    return false;
                }
                else if(true == "-c".equals(args[i]))
                {
                    i++;
                    cfg.setClientDeviceString(args[i]);
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
                    }
                    catch (final FileNotFoundException e)
                    {
                        System.err.println(e.getLocalizedMessage());
                        return false;
                    }
                }
                else
                {
                    System.err.println("Invalid Parameter : " + args[i]);
                    printHelp();
                    return false;
                }
            }
            else
            {
                printHelp();
                return false;
            }
        }
        return true;
    }

    public void sendGCodeFile()
    {
        final PrintProcess pp = new PrintProcess();
        pp.setCfg(cfg);

        if(false == pp.connectToPacemaker())
        {
            System.err.println("Could not Connect to Pacemaker !");
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

    public void startGui()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    @SuppressWarnings("unused")
                    final MainWindow gui = new MainWindow(cfg);
                }
                catch(final Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(final String[] args)
    {
        final ControllerMain cm = new ControllerMain();
        if(false == cm.parseCommandLineParameters(args))
        {
            return;
        }
        if(false == cm.hasFileToPrint())
        {
            cm.startGui();
        }
        else
        {
            cm.sendGCodeFile();
        }
    }

}
