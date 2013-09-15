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

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purejavacomm.CommPort;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class UartClientConnection extends ClientConnection
{
    private final static Logger log = LoggerFactory.getLogger("UartClientConnection");

    public final static int TIMEOUT_PORT_OPEN_MS = 1000;

    // databits: 5,6,7,8
    public final static String[] bits = {"5", "6", "7", "8"};
    // parity: None, even, odd mark, space
    public final static String[] parityOptions = {"None", "Even", "Odd", "Mark", "Space"};
    // stop bits: 1, 1.5, 2
    public final static String[] stop = {"1", "1 1/2", "2"};

    private SerialPort port;
    private boolean connected = false;

    public static String getDescriptorFor(String DeviceName,
                                          int baudrate,
                                          int dataBitsIdx,
                                          int parityIdx,
                                          int stopBitsIdx,
                                          boolean use_rts_cts_in,
                                          boolean use_rts_cts_out,
                                          boolean use_xon_xoff_in,
                                          boolean use_xon_xoff_out)
    {
        String res = DeviceName
                + ":" + baudrate
                + ":" + bits[dataBitsIdx]
                + ":" + parityOptions[parityIdx]
                + ":" + stop[stopBitsIdx]
                + ":" + use_rts_cts_in
                + ":" + use_rts_cts_out
                + ":" + use_xon_xoff_in
                + ":" + use_xon_xoff_out ;
        return res;
    }

    public static String getPortNameFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        String res = sc.next();
        sc.close();
        return res;
    }

    public static int getBaudrateFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        int res = Integer.parseInt(sc.next());
        sc.close();
        return res;
    }

    public static int getDataBitIdxFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        String help = sc.next();
        sc.close();
        int i;
        for(i = 0; i < bits.length; i++)
        {
            if(true == bits[i].equals(help))
            {
                break;
            }
        }
        return i; // Default -> 8 bits
    }

    public static int getParityIdxFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        String help = sc.next();
        sc.close();
        for(int i = 0; i < parityOptions.length; i++)
        {
            if(true == parityOptions[i].equals(help))
            {
                return i;
            }
        }
        return 0; // default -> No Parity
    }

    public static int getStopBitIdxFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        sc.next(); // skip parity
        String help = sc.next();
        sc.close();
        for(int i = 0; i < stop.length; i++)
        {
            if(true == stop[i].equals(help))
            {
                return i;
            }
        }
        return 0; // default -> 1 Stop Bit
    }

    public static boolean getRtsCtsInFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        sc.next(); // skip parity
        sc.next(); // skip stop bits
        String help = sc.next();
        boolean res = Boolean.getBoolean(help);
        sc.close();
        return res;
    }

    public static boolean getRtsCtsOutFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        sc.next(); // skip parity
        sc.next(); // skip stop bits
        sc.next(); // skip RTS / CTS In
        String help = sc.next();
        boolean res = Boolean.getBoolean(help);
        sc.close();
        return res;
    }

    public static boolean getXonXoffInFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        sc.next(); // skip parity
        sc.next(); // skip stop bits
        sc.next(); // skip RTS / CTS In
        sc.next(); // skip RTS / CTS Out
        String help = sc.next();
        boolean res = Boolean.getBoolean(help);
        sc.close();
        return res;
    }

    public static boolean getXonXoffOutFromDescriptor(String data)
    {
        Scanner sc = new Scanner(data);
        sc.useDelimiter(":");
        sc.next(); // skip Port Name
        sc.next(); // skip Baudrate
        sc.next(); // skip data bits
        sc.next(); // skip parity
        sc.next(); // skip stop bits
        sc.next(); // skip RTS / CTS In
        sc.next(); // skip RTS / CTS Out
        sc.next(); // skip Xon / Xoff In
        String help = sc.next();
        boolean res = Boolean.getBoolean(help);
        sc.close();
        return res;
    }

    public static ClientConnection establishConnectionTo(String data)
    {
        UartClientConnection res = new UartClientConnection(data);
        if(true == res.isConnected())
        {
            return res;
        }
        else
        {
            return null;
        }
    }

    private boolean isConnected()
    {
        return connected;
    }

    public UartClientConnection(String data)
    {
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("jna.nosys", "true");
        String PortName = getPortNameFromDescriptor(data);
        try
        {
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(PortName);
            if(CommPortIdentifier.PORT_SERIAL != portId.getPortType())
            {
                log.error("Specified Port {} is not a Serial Port ({})!", PortName, portId.getPortType());
                return;
            }
            CommPort basePort = null;
            try
            {
                basePort = portId.open("Pacemaker Host", TIMEOUT_PORT_OPEN_MS);
            }
            catch(PortInUseException e)
            {
                log.error("Specified Port {} is in use by another Application !", PortName);
                return;
            }
            if(false ==(basePort instanceof SerialPort))
            {
                log.error("Specified Port {} is not a Serial Port Object!", PortName);
                return;
            }
            port = (SerialPort)basePort;
            int flowControl = SerialPort.FLOWCONTROL_NONE;
            if(true == getRtsCtsInFromDescriptor(data))
            {
                flowControl = flowControl | SerialPort.FLOWCONTROL_RTSCTS_IN;
            }
            if(true == getRtsCtsOutFromDescriptor(data))
            {
                flowControl = flowControl | SerialPort.FLOWCONTROL_RTSCTS_OUT;
            }
            if(true == getXonXoffInFromDescriptor(data))
            {
                flowControl = flowControl | SerialPort.FLOWCONTROL_XONXOFF_IN;
            }
            if(true == getXonXoffOutFromDescriptor(data))
            {
                flowControl = flowControl | SerialPort.FLOWCONTROL_XONXOFF_OUT;
            }
            port.setFlowControlMode(flowControl);

            int spDataBits;
            int dataBitIdx = getDataBitIdxFromDescriptor(data);
            switch(dataBitIdx)
            {
            case 0: spDataBits = SerialPort.DATABITS_5;break;
            case 1: spDataBits = SerialPort.DATABITS_6;break;
            case 2: spDataBits = SerialPort.DATABITS_7;break;
            case 3: spDataBits = SerialPort.DATABITS_8;break;
            default: spDataBits = SerialPort.DATABITS_8;break;
            }
            int spStopBits;
            int stopBitIdx = getStopBitIdxFromDescriptor(data);
            switch(stopBitIdx)
            {
            case 0:spStopBits = SerialPort.STOPBITS_1;break;
            case 1:spStopBits = SerialPort.STOPBITS_1_5;break;
            case 2:spStopBits = SerialPort.STOPBITS_2;break;
            default: spStopBits = SerialPort.STOPBITS_1;break;
            }
            int spParity;
            int parityIdx = getParityIdxFromDescriptor(data);
            switch(parityIdx)
            {
            case 0: spParity = SerialPort.PARITY_NONE; break;
            case 1: spParity = SerialPort.PARITY_EVEN; break;
            case 2: spParity = SerialPort.PARITY_ODD; break;
            case 3: spParity = SerialPort.PARITY_MARK; break;
            case 4: spParity = SerialPort.PARITY_SPACE; break;
            default: spParity = SerialPort.PARITY_NONE; break;
            }
            port.setSerialPortParams(getBaudrateFromDescriptor(data), spDataBits, spStopBits, spParity);

            in = port.getInputStream();
            out = port.getOutputStream();
            connected = true;
            this.start();
            log.info("Serial Port is open");
            return;
        }
        catch(NoSuchPortException e)
        {
            log.error("There is no port named {} !", PortName);
            e.printStackTrace();
        }
        catch(UnsupportedCommOperationException e)
        {
            log.error("The Interface {} does not support the requested parameters !", PortName);
            e.printStackTrace();
        }
        catch(IOException e)
        {
            log.error("The Interface {} caused an IO Exception !", PortName);
            e.printStackTrace();
        }
        close(); // In case that we had a problem after the open
    }

    @Override
    public void close()
    {
        if(null != port)
        {
            port.close();
        }
    }
}
