
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

    @Override
    public boolean doImmediateShutDown()
    {
        return false;
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

    @Override
    public boolean setCurrentExtruderTemperatureAndDoWait(Double temperature,
            GCodeResultStream resultStream)
    {
        return false;
    }

    @Override
    public boolean waitForEverythingInLimits(GCodeResultStream resultStream)
    {
        return false;
    }

    @Override
    public boolean setPrintBedTemperatureNoWait(Double temperature)
    {
        return false;
    }

    @Override
    public boolean setChamberTemperatureNoWait(Double temperature)
    {
        return false;
    }

    @Override
    public boolean setPrintBedTemperatureAndDoWait(Double temperature,
            GCodeResultStream resultStream)
    {
        return false;
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

    @Override
    public String getCurrentExtruderTemperature()
    {
        return null;
    }

    @Override
    public String getHeatedBedTemperature()
    {
        return null;
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
