package de.nomagic.printerController.pacemaker;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.core.Reference;

/** Information about the Client.
*
* @author Lars P&ouml;tter
* (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
*/
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
    private boolean hasExtensionQueuedCommand= false;
    private boolean hasExtensionBasicMove = false;
    private boolean hasExtensionEventReporting = false;

    private int FirmwareType = -1;
    private int FirmwareRevisionMajor = -1;
    private int FirmwareRevisionMinor = -1;
    private int HardwareType = -1;
    private int HardwareRevision = -1;
    private int maxSteppsPerSecond = -1;
    private int hostTimeoutSeconds = -1;
    private int NumberSteppers = -1;
    private int NumberHeaters = -1;
    private int NumberPwmSwitchedOutputs = -1;
    private int NumberTemperatureSensors = -1;
    private int NumberSwitches = -1;
    private int NumberOutputSignals = -1;
    private int NumberBuzzer = -1;

    private Protocol pro;

    private String[] stepperNames;
    private String[] heaterNames;
    private String[] pwmOutputNames;
    private String[] tempertureSensorNames;
    private String[] switchesNames;
    private String[] outputSignalNames;
    private String[] buzzerNames;

    private boolean hasBeenRead = false;

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
        "Given Name = " + givenName + "\n";
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
        if(true == hasExtensionQueuedCommand)
        {
            res = res + "Queued_Command ";
        }
        if(true == hasExtensionBasicMove)
        {
            res = res + "Basic_move ";
        }
        if(true == hasExtensionEventReporting)
        {
            res = res + "Event_Reporting ";
        }
        res = res + "\n" +
        "Firmware Type = " + FirmwareType + "\n" +
        "Firmware Revision = " + FirmwareRevisionMajor + "." + FirmwareRevisionMinor + "\n" +
        "Hardware Type = " + HardwareType + "\n" +
        "Hardware Revision = " + HardwareRevision + "\n" +
        "max. steps per second = " + maxSteppsPerSecond + "\n" +
        "Host Timeout = " + hostTimeoutSeconds + " seconds\n" +
        "Number of Steppers = " + NumberSteppers + "\n" +
        "Number of Heaters = " + NumberHeaters + "\n" +
        "Number of PWM switched Outputs = " + NumberPwmSwitchedOutputs + "\n" +
        "Number of Temperature Sensors = " + NumberTemperatureSensors + "\n" +
        "Number of Switches = " + NumberSwitches + "\n" +
        "Number of Output Signals = " + NumberOutputSignals + "\n" +
        "Number of Buzzers = " + NumberBuzzer + "\n";
        return res;
    }

    private int readValueOf(int which, String Name, Reference ref) throws IOException
    {
        final int res = requestInteger(which, ref);
        if(0 > res)
        {
            log.debug("Could not read the {} !", Name);
        }
        return res;
    }

    private int readDeviceCount(int device, String Name, Reference ref) throws IOException
    {

        final Reply r = pro.sendDeviceCountRequest(device, ref);
        if(null == r)
        {
            hasBeenRead = false;
            return -1;
        }
        if(false == r.isOKReply())
        {
            hasBeenRead = false;
            return -4;
        }
        final byte[] p = r.getParameter();
        if(null == p)
        {
            hasBeenRead = false;
            return -2;
        }
        int res = -3;
        switch(p.length)
        {
        case 1: res = 0xff & p[0]; break;
        case 2: res = ((0xff & p[0])*256) + (0xff & p[1]); break;
        default: res = -5;
        }

        if(0 > res)
        {
            log.error("Could not read the {} !", Name);
        }
        return res;
    }

    public boolean readDeviceInformationFrom(final Protocol pro, Reference ref) throws IOException
    {
        this.pro = pro;
        hasBeenRead = true;
        FirmwareName = requestString(Protocol.INFO_FIRMWARE_NAME_STRING, ref);
        if(false == hasBeenRead) { return false;}
        SerialNumber = requestString(Protocol.INFO_SERIAL_NUMBER_STRING, ref);
        if(false == hasBeenRead) { return false;}
        BoardName = requestString(Protocol.INFO_BOARD_NAME_STRING, ref);
        if(false == hasBeenRead) { return false;}
        givenName = requestString(Protocol.INFO_GIVEN_NAME_STRING, ref);
        if(false == hasBeenRead) { return false;}
        majorVersionsSupported = requestList(Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MAJOR, ref);
        if(false == hasBeenRead) { return false;}
        if(null == majorVersionsSupported)
        {
            log.error("Could not read the supported protocol major versions !");
            hasBeenRead = false;
            return false;
        }
        minorVersionSupportedUpTo = requestInteger(Protocol.INFO_SUPPORTED_PROTOCOL_VERSION_MINOR, ref);
        if(false == hasBeenRead) { return false;}
        final Vector<Integer> extensions = requestList(Protocol.INFO_LIST_OF_SUPPORTED_PROTOCOL_EXTENSIONS, ref);
        if(false == hasBeenRead) { return false;}
        if(null == extensions)
        {
            log.error("Could not read the supported protocol extensions !");
            hasBeenRead = false;
            return false;
        }
        for(int i = 0; i < extensions.size(); i++)
        {
            final int ExtensionType = extensions.get(i);
            switch(ExtensionType)
            {
            case Protocol.INFO_PROTOCOL_EXTENSION_STEPPER_CONTROL:
                hasExtensionStepperControl = true;
                break;

            case Protocol.INFO_PROTOCOL_EXTENSION_QUEUED_COMMAND:
                hasExtensionQueuedCommand = true;
                break;

            case Protocol.INFO_PROTOCOL_EXTENSION_BASIC_MOVE:
                hasExtensionBasicMove = true;
                break;

            case Protocol.INFO_PROTOCOL_EXTENSION_EVENT_REPORTING:
                hasExtensionEventReporting = true;
                break;

            default:
                log.error("Client supports unknown Extension of Type {} !", ExtensionType);
                break;
            }
        }

        FirmwareType = readValueOf(Protocol.INFO_FIRMWARE_TYPE, "Firmware Type", ref);
        if(false == hasBeenRead) { return false;}
        FirmwareRevisionMajor = readValueOf(Protocol.INFO_FIRMWARE_REVISION_MAJOR, "Firmware Revision Major", ref);
        if(false == hasBeenRead) { return false;}
        FirmwareRevisionMinor = readValueOf(Protocol.INFO_FIRMWARE_REVISION_MINOR, "Firmware Revision Minor", ref);
        if(false == hasBeenRead) { return false;}
        HardwareType = readValueOf(Protocol.INFO_HARDWARE_TYPE, "Hardware Type", ref);
        if(false == hasBeenRead) { return false;}
        HardwareRevision = readValueOf(Protocol.INFO_HARDWARE_REVISION, "Hardware Revision", ref);
        if(false == hasBeenRead) { return false;}
        maxSteppsPerSecond = readValueOf(Protocol.INFO_MAX_STEP_RATE, "maximum supported step rate", ref);
        if(false == hasBeenRead) { return false;}
        hostTimeoutSeconds = readValueOf(Protocol.INFO_HOST_TIMEOUT, "host timeout", ref);
        if(false == hasBeenRead) { return false;}
        NumberSteppers = readDeviceCount(Protocol.DEVICE_TYPE_STEPPER, "number of stepper motors", ref);
        if(false == hasBeenRead) { return false;}
        NumberHeaters = readDeviceCount(Protocol.DEVICE_TYPE_HEATER, "number of Heaters", ref);
        if(false == hasBeenRead) { return false;}
        NumberPwmSwitchedOutputs = readDeviceCount(Protocol.DEVICE_TYPE_PWM_OUTPUT, "number of PWM switched Outputs", ref);
        if(false == hasBeenRead) { return false;}
        NumberTemperatureSensors = readDeviceCount(Protocol.DEVICE_TYPE_TEMPERATURE_SENSOR, "number of Temperature Sensors", ref);
        if(false == hasBeenRead) { return false;}
        NumberSwitches = readDeviceCount(Protocol.DEVICE_TYPE_INPUT, "number of Switches", ref);
        if(false == hasBeenRead) { return false;}
        NumberOutputSignals = readDeviceCount(Protocol.DEVICE_TYPE_OUTPUT, "number of Output Signals", ref);
        if(false == hasBeenRead) { return false;}
        NumberBuzzer = readDeviceCount(Protocol.DEVICE_TYPE_BUZZER, "number of buzzers", ref);
        if(false == hasBeenRead) { return false;}
        return hasBeenRead;
    }

    public boolean readConnectorNames(Reference ref) throws IOException
    {
        if(null == pro)
        {
            return false;
        }
        else
        {
            if(-1 != NumberSteppers)
            {
                stepperNames = getAllNames(NumberSteppers, Protocol.DEVICE_TYPE_STEPPER, ref);
            }
            if(-1 != NumberHeaters)
            {
                heaterNames = getAllNames(NumberHeaters, Protocol.DEVICE_TYPE_HEATER, ref);
            }
            if(-1 != NumberPwmSwitchedOutputs)
            {
                pwmOutputNames = getAllNames(NumberPwmSwitchedOutputs, Protocol.DEVICE_TYPE_PWM_OUTPUT, ref);
            }
            if(-1 != NumberTemperatureSensors)
            {
                tempertureSensorNames = getAllNames(NumberTemperatureSensors, Protocol.DEVICE_TYPE_TEMPERATURE_SENSOR, ref);
            }
            if(-1 != NumberSwitches)
            {
                switchesNames = getAllNames(NumberSwitches, Protocol.DEVICE_TYPE_INPUT, ref);
            }
            if(-1 != NumberOutputSignals)
            {
                outputSignalNames = getAllNames(NumberOutputSignals, Protocol.DEVICE_TYPE_OUTPUT, ref);
            }
            if(-1 != NumberBuzzer)
            {
                buzzerNames = getAllNames(NumberBuzzer, Protocol.DEVICE_TYPE_BUZZER, ref);
            }
            return true;
        }
    }

    private String[] getAllNames(int Number, byte deviceType, Reference ref) throws IOException
    {
        final String[] allNames = new String[Number];
        for(int i = 0; i < Number; i++)
        {
            allNames[i] = requestDeviceNameString(deviceType, i, ref);
        }
        return allNames;
    }

    private String requestDeviceNameString(final byte type, final int index, Reference ref) throws IOException
    {
        final Reply r = pro.sendDeviceNameRequest(type, index, ref);
        if(null == r)
        {
            log.error("Device Name Request Failed !");
            return "";
        }
        return r.getParameterAsString(0);
    }

    private int requestInteger(final int which, Reference ref) throws IOException
    {
        final Reply r = pro.sendInformationRequest(which, ref);
        if(null == r)
        {
            hasBeenRead = false;
            return -1;
        }
        if(false == r.isOKReply())
        {
            hasBeenRead = false;
            return -4;
        }
        final byte[] p = r.getParameter();
        if(null == p)
        {
            hasBeenRead = false;
            return -2;
        }
        int res = -3;
        switch(p.length)
        {
        case 1:
            res = 0xff & p[0];
            break;

        case 2:
            res = (  (0xff & p[0])*256)
                   + (0xff & p[1]);
            break;

        case 3:
            res = (   (0xff & p[0])*256*256)
                   + ((0xff & p[1])*256)
                   +  (0xff & p[2]);
            break;

        case 4:
            res = (   (0xff & p[0])*256*256*256)
                   + ((0xff & p[1])*256*256)
                   + ((0xff & p[2])*256)
                   +  (0xff & p[3]);
            break;

        default:
            res = -5;
            break;
        }
        return res;
    }

    private String requestString(final int which, Reference ref) throws IOException
    {
        final Reply r = pro.sendInformationRequest(which, ref);
        if(null == r)
        {
            hasBeenRead = false;
            return "";
        }
        if(true == r.isOKReply())
        {
            return r.getParameterAsString(0);
        }
        else
        {
            hasBeenRead = false;
            return "";
        }
    }

    private Vector<Integer> requestList(final int which, Reference ref) throws IOException
    {
        final Reply r = pro.sendInformationRequest(which, ref);
        if(null == r)
        {
            log.error("Received no Reply !");
            return null;
        }
        if(false == r.isOKReply())
        {
            log.error("Received Error Reply !");
            return null;
        }
        final byte[] p = r.getParameter();
        if(null == p)
        {
            log.error("Received no Reply !");
            return null;
        }
        final Vector<Integer> res = new Vector<Integer>();
        for(int i = 0; i < p.length; i++)
        {
            res.add((int) p[i]);
        }
        return res;
    }

    public String getStepperConnectorName(int index)
    {
        if(   (null != stepperNames)
           && (-1 < index)
           && (index < NumberSteppers) )
        {
            return stepperNames[index];
        }
        else
        {
            return "";
        }
    }

    public String getHeaterConnectorName(int index)
    {
        if(   (null != heaterNames)
           && (-1 < index)
           && (index < NumberHeaters) )
        {
            return heaterNames[index];
        }
        else
        {
            log.error("Requested invalid Heater connector Name with index {} !", index);
            return "";
        }
    }

    public String getPwmOutputConnectorName(int index)
    {
        if(   (null != pwmOutputNames)
           && (-1 < index)
           && (index < NumberOutputSignals) )
        {
            return pwmOutputNames[index];
        }
        else
        {
            return "";
        }
    }

    public String getTemperatureSensorConnectorName(int index)
    {
        if(   (null != tempertureSensorNames)
           && (-1 < index)
           && (index < NumberTemperatureSensors) )
        {
            return tempertureSensorNames[index];
        }
        else
        {
            return "";
        }
    }

    public String getSwitchConnectorName(int index)
    {
        if(   (null != switchesNames)
           && (-1 < index)
           && (index < NumberSwitches) )
        {
            return switchesNames[index];
        }
        else
        {
            return "";
        }
    }

    public String getOutputConnectorName(int index)
    {
        if(   (null != outputSignalNames)
           && (-1 < index)
           &&(index < NumberOutputSignals) )
        {
            return outputSignalNames[index];
        }
        else
        {
            return "";
        }
    }

    public String getBuzzerConnectorName(int index)
    {
        if(   (null != buzzerNames)
           && (-1 < index)
           && (index < NumberBuzzer) )
        {
            return buzzerNames[index];
        }
        else
        {
            return "";
        }
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

    public boolean hasExtensionQueuedCommand()
    {
        return hasExtensionQueuedCommand;
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
        return NumberBuzzer;
    }

    public int getMaxSteppsPerSecond()
    {
        return maxSteppsPerSecond;
    }

    public int getHostTimeoutSeconds()
    {
        return hostTimeoutSeconds;
    }

}
