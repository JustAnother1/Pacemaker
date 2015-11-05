
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

    @Override
    public boolean doShutDown()
    {
        return false;
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

    @Override
    public boolean startHoming(Axis_enum[] axis)
    {
        return false;
    }

    @Override
    public boolean disableAllStepperMotors()
    {
        return false;
    }

    @Override
    public boolean enableAllStepperMotors()
    {
        return false;
    }

    @Override
    public boolean setStepsPerMilimeter(Axis_enum axle,
            Double stepsPerMillimeter)
    {
        return false;
    }

    @Override
    public boolean setFanSpeedfor(int fan, int speed)
    {
        return false;
    }

    @Override
    public boolean setCurrentExtruderTemperatureNoWait(Double temperature)
    {
        return false;
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
