package de.nomagic.printerController.pacemaker;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceInformation
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private String FirmwareName = "";
    private String SerialNumber = "";
    private String BoardName = "";
    private String givenName = "";
    private Vector<Integer> majorVersionsSupported = null;
    private int minorVersionSupportedUpTo = -1;
    // Extensions
    private boolean hasExtensionStepperControl= false;
    private boolean hasExtensionBasicMove = false;
    private boolean hasExtensionEventReporting = false;

    private int FirmwareType = -1;
    private int FirmwareRevisionMajor = -1;
    private int FirmwareRevisionMinor = -1;
    private int HardwareType = -1;
    private int HardwareRevision = -1;
    private int NumberSteppers = -1;
    private int NumberHeaters = -1;
    private int NumberPwmSwitchedOutputs = -1;
    private int NumberTemperatureSensors = -1;
    private int NumberSwitches = -1;
    private int NumberOutputSignals = -1;
    private int numberBuzzer = -1;
    private int OrderQueueTotalSlots = -1;
    private int OrderQueueUsedSlots = -1;

    private Protocol uartProtocol;



    public DeviceInformation()
    {
    }


    @Override
    public String toString()
    {
        String res =
        "DeviceInformation:\n" +
        "Firmware = " + FirmwareName + "\n" +
        "Serial Number = " + SerialNumber + "\n" +
        "Board Name = " + BoardName + "\n" +
        "given Name = " + givenName + "\n";
        if(null == majorVersionsSupported)
        {
            res = res + "Supported Protocol Versions : not available !\n";
        }
        else
        {
            res = res + "Supported Protocol Versions : ";
            for(int i = 0; i < majorVersionsSupported.size(); i++)
            {
                res = res + majorVersionsSupported.get(i) + " ";
            }
            res = res + "\n";
        }

        res = res + "Supported Protocol Minor Versions: " +  minorVersionSupportedUpTo + "\n" +
        "Supported Protocol Extensions = ";
        if(true == hasExtensionStepperControl)
        {
            res = res + "Stepper_control ";
        }
        if(true == hasExtensionBasicMove)
        {
            res = res + "Basic_move ";
        }
        if(true == hasExtensionEventReporting)
        {
            res = res + "Event Reporting ";
        }
        res = res + "\n" +
        "Firmware Type = " + FirmwareType + "\n" +
        "Firmware Revision = " + FirmwareRevisionMajor + "." + FirmwareRevisionMinor + "\n" +
        "Hardware Type = " + HardwareType + "\n" +
        "Hardware Revision = " + HardwareRevision + "\n" +
        "Number of Steppers = " + NumberSteppers + "\n" +
        "Number of Heaters = " + NumberHeaters + "\n" +
        "Number of PWM switched Outputs = " + NumberPwmSwitchedOutputs + "\n" +
        "Number of Temperature Sensors = " + NumberTemperatureSensors + "\n" +
        "Number of Switches = " + NumberSwitches + "\n" +
        "Number of Output Signals = " + NumberOutputSignals + "\n" +
        "Number of Buzzers = " + numberBuzzer + "\n" +
        "Number of queue slots = " + OrderQueueTotalSlots + "\n" +
        "Number of used queue slots = " + OrderQueueUsedSlots + "\n";
        return res;
    }

    public boolean readDeviceInformationFrom(final Protocol uartProtocol) throws IOException
    {
        this.uartProtocol = uartProtocol;

        FirmwareName = requestString(Protocol.INFO_FIRMWARE_NAME_STRING);
        SerialNumber = requestString(Protocol.INFO_SERIAL_NUMBER_STRING);
        BoardName = requestString(Protocol.INFO_BOARD_NAME_STRING);
        givenName = requestString(Protocol.INFO_GIVEN_NAME_STRING);
        majorVersionsSupported = requestList(Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MAJOR);
        minorVersionSupportedUpTo = requestInteger(Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MINOR);

        final Vector<Integer> extensions = requestList(Protocol.INFO_LIST_OF_SUPPORTED_PROTOCOL_EXTENSIONS);
        if(null == extensions)
        {
            log.error("Could not read the supported protocol extensions !");
            return false;
        }
        for(int i = 0; i < extensions.size(); i++)
        {
            switch(extensions.get(i))
            {
            case Protocol.INFO_PROTOCOL_EXTENSION_STEPPER_CONTROL:
                hasExtensionStepperControl = true;
                break;
            case Protocol.INFO_PROTOCOL_EXTENSION_BASIC_MOVE:
                hasExtensionBasicMove = true;
                break;
            case Protocol.INFO_PROTOCOL_EXTENSION_EVENT_REPORTING:
                hasExtensionEventReporting = true;
                break;
            }
        }

        FirmwareType = requestInteger(Protocol.INFO_FIRMWARE_TYPE);
        if(-1 == FirmwareType)
        {
            log.error("Could not read Firmware Type !");
            return false;
        }
        FirmwareRevisionMajor = requestInteger(Protocol.INFO_FIRMWARE_REVISION_MAJOR);
        if(-1 == FirmwareRevisionMajor)
        {
            log.error("Could not read Firmware Revision Major !");
            return false;
        }
        FirmwareRevisionMinor = requestInteger(Protocol.INFO_FIRMWARE_REVISION_MINOR);
        if(-1 == FirmwareRevisionMinor)
        {
            log.error("Could not read Firmware Revision Minor !");
            return false;
        }
        HardwareType = requestInteger(Protocol.INFO_FIRMWARE_TYPE);
        if(-1 == HardwareType)
        {
            log.error("Could not read Hardware Type !");
            return false;
        }
        HardwareRevision =requestInteger(Protocol.INFO_HARDWARE_REVISION);
        if(-1 == HardwareRevision)
        {
            log.error("Could not read the Hardware Revision !");
            return false;
        }
        NumberSteppers = requestInteger(Protocol.INFO_NUMBER_STEPPERS);
        if(-1 == NumberSteppers)
        {
            log.error("Could not read number of stepper motors !");
            return false;
        }
        NumberHeaters = requestInteger(Protocol.INFO_NUMBER_HEATERS);
        if(-1 == NumberHeaters)
        {
            log.error("Could not read number of Heaters !");
            return false;
        }
        NumberPwmSwitchedOutputs = requestInteger(Protocol.INFO_NUMBER_PWM);
        if(-1 == NumberPwmSwitchedOutputs)
        {
            log.error("Could not read number of PWM switched Outputs !");
            return false;
        }
        NumberTemperatureSensors = requestInteger(Protocol.INFO_NUMBER_TEMP_SENSOR);
        if(-1 == NumberTemperatureSensors)
        {
            log.error("Could not read number of Temperature Sensors !");
            return false;
        }
        NumberSwitches = requestInteger(Protocol.INFO_NUMBER_INPUT);
        if(-1 == NumberSwitches)
        {
            log.error("Could not read number of Switches !");
            return false;
        }
        NumberOutputSignals = requestInteger(Protocol.INFO_NUMBER_OUTPUT);
        if(-1 == NumberOutputSignals)
        {
            log.error("Could not read number of Output Signals !");
            return false;
        }
        numberBuzzer = requestInteger(Protocol.INFO_NUMBER_BUZZER);
        if(-1 == numberBuzzer)
        {
            log.error("Could not read number of buzzers !");
            return false;
        }

        OrderQueueTotalSlots = requestInteger(Protocol.INFO_QUEUE_TOTAL_SLOTS);
        if(-1 == OrderQueueTotalSlots)
        {
            log.error("Could not read number of total queue slots !");
            return false;
        }

        OrderQueueUsedSlots = requestInteger(Protocol.INFO_QUEUE_USED_SLOTS);
        if(-1 == OrderQueueUsedSlots)
        {
            log.error("Could not read number of used queue slots !");
            return false;
        }
        return true;
    }

    private int requestInteger(final int which) throws IOException
    {
        final Reply r = uartProtocol.sendInformationRequest(which);
        if(null == r)
        {
            return -1;
        }
        final byte[] p = r.getParameter();
        if(null == p)
        {
            return -2;
        }
        int res = -3;
        switch(p.length)
        {
        case 1: res = p[0]; break;
        case 2: res = (p[0]*256) + p[1]; break;
        }
        return res;
    }

    private String requestString(final int which) throws IOException
    {
        final Reply r = uartProtocol.sendInformationRequest(which);
        if(null == r)
        {
            return "";
        }
        return r.getParameterAsString(0);
    }

    private Vector<Integer> requestList(final int which) throws IOException
    {
        final Reply r = uartProtocol.sendInformationRequest(which);
        if(null == r)
        {
            return null;
        }
        final byte[] p = r.getParameter();
        if(null == p)
        {
            return null;
        }
        final Vector<Integer> res = new Vector<Integer>();
        for(int i = 0; i < p.length; i++)
        {
            res.add((int) p[i]);
        }
        return res;
    }


    public boolean isProtocolVersionSupported(final int which)
    {
        return majorVersionsSupported.contains(which);
    }

    /**
     * @return the firmwareName
     */
    public String getFirmwareName()
    {
        return FirmwareName;
    }

    /**
     * @return the hasExtensionStepperControl
     */
    public boolean hasExtensionStepperControl()
    {
        return hasExtensionStepperControl;
    }

    /**
     * @return the hasExtensionBasicMove
     */
    public boolean hasExtensionBasicMove()
    {
        return hasExtensionBasicMove;
    }

    /**
     * @return the firmwareType
     */
    public int getFirmwareType()
    {
        return FirmwareType;
    }

    /**
     * @return the hardwareType
     */
    public int getHardwareType()
    {
        return HardwareType;
    }

    /**
     * @return the hardwareRevision
     */
    public int getHardwareRevision()
    {
        return HardwareRevision;
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber()
    {
        return SerialNumber;
    }

    /**
     * @return the numberSteppers
     */
    public int getNumberSteppers()
    {
        return NumberSteppers;
    }

    /**
     * @return the numberHeaters
     */
    public int getNumberHeaters()
    {
        return NumberHeaters;
    }

    /**
     * @return the numberTemperatureSensors
     */
    public int getNumberTemperatureSensors()
    {
        return NumberTemperatureSensors;
    }

    /**
     * @return the numberSwitches
     */
    public int getNumberSwitches()
    {
        return NumberSwitches;
    }

    /**
     * @return the numberOutputSignals
     */
    public int getNumberOutputSignals()
    {
        return NumberOutputSignals;
    }


    public String getBoardName()
    {
        return BoardName;
    }


    public String getGivenName()
    {
        return givenName;
    }


    public int getMinorVersionSupportedUpTo()
    {
        return minorVersionSupportedUpTo;
    }


    public boolean hasExtensionEventReporting()
    {
        return hasExtensionEventReporting;
    }


    public int getFirmwareRevisionMajor()
    {
        return FirmwareRevisionMajor;
    }


    public int getFirmwareRevisionMinor()
    {
        return FirmwareRevisionMinor;
    }


    public int getNumberPwmSwitchedOutputs()
    {
        return NumberPwmSwitchedOutputs;
    }


    public int getNumberBuzzer()
    {
        return numberBuzzer;
    }


    public int getOrderQueueTotalSlots()
    {
        return OrderQueueTotalSlots;
    }


    public int getOrderQueueUsedSlots()
    {
        return OrderQueueUsedSlots;
    }

}
