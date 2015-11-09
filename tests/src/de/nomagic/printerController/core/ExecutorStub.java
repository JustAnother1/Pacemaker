
package de.nomagic.printerController.core;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.pacemaker.Reply;

public class ExecutorStub implements Executor
{

    private String LastError = null;

    public ExecutorStub()
    {
    }


    @Override
    public boolean isOperational()
    {
        return false;
    }

    public void setLastError(String theError)
    {
        LastError = theError;
    }

    @Override
    public String getLastErrorReason()
    {
        return LastError;
    }

    @Override
    public void close()
    {
    }

    boolean doShutDownReturn;

    public void set_doShutDownReturn(boolean value)
    {
    	doShutDownReturn = value;
    }

    @Override
    public boolean doShutDown()
    {
        return doShutDownReturn;
    }

    boolean doImmediateShutDownReturn;

    public void set_doImmediateShutDownReturn(boolean value)
    {
    	doImmediateShutDownReturn = value;
    }

    @Override
    public boolean doImmediateShutDown()
    {
        return doImmediateShutDownReturn;
    }

    @Override
    public boolean addPauseFor(Double seconds)
    {
        return false;
    }

    @Override
    public boolean addMoveTo(RelativeMove relMove)
    {
        return false;
    }

    @Override
    public boolean letMovementStop()
    {
        return false;
    }

    private boolean startHomingReturn;

    public void setReturnFor_startHoming(boolean value)
    {
    	startHomingReturn = value;
    }

    @Override
    public boolean startHoming(Axis_enum[] axis)
    {
    	return startHomingReturn;
    }

    private boolean disableAllStepperMotorsReturn;

    public void setReturnFor_disableAllStepperMotors(boolean value)
    {
    	disableAllStepperMotorsReturn = value;
    }

    @Override
    public boolean disableAllStepperMotors()
    {
        return disableAllStepperMotorsReturn;
    }

    private boolean enableAllStepperMotorsReturn;

    public void setReturnFor_enableAllStepperMotors(boolean value)
    {
    	enableAllStepperMotorsReturn = value;
    }

    @Override
    public boolean enableAllStepperMotors()
    {
        return enableAllStepperMotorsReturn;
    }

    private boolean setStepsPerMilimeterReturn;
    private Double StepsPerMilimeterSet;

    public void setReturnFor_setStepsPerMilimeter(boolean value)
    {
    	setStepsPerMilimeterReturn = value;
    }

    public Double getStepsPerMilimeterSet()
    {
    	return StepsPerMilimeterSet;
    }

    @Override
    public boolean setStepsPerMilimeter(Axis_enum axle, Double stepsPerMillimeter)
    {
    	StepsPerMilimeterSet = stepsPerMillimeter;
        return setStepsPerMilimeterReturn;
    }

    private boolean setFanSpeedforReturn;
    private int fan_set;
    private int speed_set;

    public void setReturnFor_setFanSpeedfor(boolean value)
    {
    	setFanSpeedforReturn = value;
    }

    public int get_fan_set()
    {
    	return fan_set;
    }

    public int get_speed_set()
    {
    	return speed_set;
    }

    @Override
    public boolean setFanSpeedfor(int fan, int speed)
    {
    	fan_set = fan;
    	speed_set = speed;
        return setFanSpeedforReturn;
    }

    private boolean setCurrentExtruderTemperatureNoWaitReturn;
    private Double ExtruderTemperatureSet;

    public void setReturnFor_setCurrentExtruderTemperatureNoWait(boolean value)
    {
    	setCurrentExtruderTemperatureNoWaitReturn = value;
    }

    public Double get_ExtruderTemperatureSet()
    {
    	return ExtruderTemperatureSet;
    }

    @Override
    public boolean setCurrentExtruderTemperatureNoWait(Double temperature)
    {
    	ExtruderTemperatureSet = temperature;
        return setCurrentExtruderTemperatureNoWaitReturn;
    }

    private boolean setCurrentExtruderTemperatureAndDoWaitReturn;

    public void setReturnfor_setCurrentExtruderTemperatureAndDoWait(boolean value)
    {
    	setCurrentExtruderTemperatureAndDoWaitReturn = value;
    }

    private Double ExtruderDoWait_setTemperature;

    public Double getExtruderDoWait_setTemperature()
    {
    	return ExtruderDoWait_setTemperature;
    }

    private GCodeResultStream ExtruderDoWait_setResultStream;

    public GCodeResultStream getExtruderDoWait_setResultStream()
    {
    	return ExtruderDoWait_setResultStream;
    }

    @Override
    public boolean setCurrentExtruderTemperatureAndDoWait(Double temperature,
            GCodeResultStream resultStream)
    {
    	ExtruderDoWait_setTemperature = temperature;
    	ExtruderDoWait_setResultStream = resultStream;
        return setCurrentExtruderTemperatureAndDoWaitReturn;
    }

    private boolean waitForEverythingInLimitsReturn;

    public void set_waitForEverythingInLimitsReturn(boolean value)
    {
    	waitForEverythingInLimitsReturn = value;
    }

    @Override
    public boolean waitForEverythingInLimits(GCodeResultStream resultStream)
    {
        return waitForEverythingInLimitsReturn;
    }

    private boolean setPrintBedTemperatureNoWaitReturn;

    public void set_setPrintBedTemperatureNoWaitReturn(boolean value)
    {
    	setPrintBedTemperatureNoWaitReturn = value;
    }

    private Double PrintBedTemperatureNoWait_setTemperature;

    public Double get_PrintBedTemperatureNoWait_setTemperature()
    {
    	return PrintBedTemperatureNoWait_setTemperature;
    }

    @Override
    public boolean setPrintBedTemperatureNoWait(Double temperature)
    {
    	PrintBedTemperatureNoWait_setTemperature = temperature;
        return setPrintBedTemperatureNoWaitReturn;
    }

    private boolean setChamberTemperatureNoWaitReturn;

    public void set_setChamberTemperatureNoWaitReturn(boolean value)
    {
    	setChamberTemperatureNoWaitReturn = value;
    }

    private Double setChamberTemperatureNoWait_setTemperature;

    public Double get_setChamberTemperatureNoWait_setTemperature()
    {
    	return setChamberTemperatureNoWait_setTemperature;
    }

    @Override
    public boolean setChamberTemperatureNoWait(Double temperature)
    {
    	setChamberTemperatureNoWait_setTemperature = temperature;
        return setChamberTemperatureNoWaitReturn;
    }

    private boolean setPrintBedTemperatureAndDoWaitReturn;

    public void set_setPrintBedTemperatureAndDoWaitReturn(boolean value)
    {
    	setPrintBedTemperatureAndDoWaitReturn = value;
    }

    private Double setPrintBedTemperatureAndDoWait_setTemperature;

    public Double get_setPrintBedTemperatureAndDoWait_setTemperature()
    {
    	return setPrintBedTemperatureAndDoWait_setTemperature;
    }

    private GCodeResultStream setPrintBedTemperatureAndDoWait_setResultStream;

    public GCodeResultStream get_setPrintBedTemperatureAndDoWait_setResultStream()
    {
    	return setPrintBedTemperatureAndDoWait_setResultStream;
    }
    @Override
    public boolean setPrintBedTemperatureAndDoWait(Double temperature,
            GCodeResultStream resultStream)
    {
    	setPrintBedTemperatureAndDoWait_setTemperature = temperature;
    	setPrintBedTemperatureAndDoWait_setResultStream = resultStream;
        return setPrintBedTemperatureAndDoWaitReturn;
    }

    @Override
    public double requestTemperatureOfHeater(Heater_enum pos)
    {
        return 0;
    }

    @Override
    public boolean istheHeaterConfigured(Heater_enum func)
    {

        return false;
    }

    private String getCurrentExtruderTemperatureReturn;

    public void set_getCurrentExtruderTemperatureReturn(String value)
    {
    	getCurrentExtruderTemperatureReturn = value;
    }

    @Override
    public String getCurrentExtruderTemperature()
    {
        return getCurrentExtruderTemperatureReturn;
    }

    private String getHeatedBedTemperatureReturn;

    public void set_getHeatedBedTemperatureReturn(String value)
    {
    	getHeatedBedTemperatureReturn = value;
    }

    @Override
    public String getHeatedBedTemperature()
    {
        return getHeatedBedTemperatureReturn;
    }

    @Override
    public int getStateOfSwitch(Switch_enum theSwitch)
    {
        return 0;
    }

    @Override
    public boolean switchExtruderTo(int num)
    {
        return false;
    }

    @Override
    public Reply sendRawOrderFrame(int ClientNumber, int order,
            Integer[] parameterBytes, int length)
    {
        return null;
    }

    @Override
    public void waitForClientQueueEmpty()
    {
    }

    @Override
    public boolean runPIDautotune(Heater_enum Extruder, Double Temperature,
            int numCycles, GCodeResultStream resultStream)
    {
        return false;
    }

    @Override
    public void registerTemperatureObserver(TemperatureObserver observer)
    {
    }

    @Override
    public TimeoutHandler getTimeoutHandler()
    {
        return null;
    }

}
