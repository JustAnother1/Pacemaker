package de.nomagic.printerController.core;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.Heater_enum;
import de.nomagic.printerController.Switch_enum;
import de.nomagic.printerController.pacemaker.Reply;

public interface Executor
{
    public static final int SWITCH_STATE_OPEN = 0;
    public static final int SWITCH_STATE_CLOSED = 1;
    public static final int SWITCH_STATE_NOT_AVAILABLE = 2;
    public static final double HOT_END_FAN_ON_TEMPERATURE = 50.0;

    boolean isOperational();

    String getLastErrorReason();

    void close(Reference ref);
    boolean doShutDown(Reference ref);
    boolean doImmediateShutDown(Reference ref);

 // Stepper Control
    boolean addPauseFor(final Double seconds, Reference ref);
    boolean addMoveTo(final RelativeMove relMove, Reference ref);
    boolean letMovementStop(Reference ref);
    boolean startHoming(Axis_enum[] axis, Reference ref);
    boolean disableAllStepperMotors(Reference ref);
    boolean enableAllStepperMotors(Reference ref);
    boolean setStepsPerMilimeter(final Axis_enum axle, final Double stepsPerMillimeter);
 // FAN
    boolean setFanSpeedfor(final int fan, final int speed, Reference ref);
 // Temperature
    boolean setCurrentExtruderTemperatureNoWait(final Double temperature, Reference ref);
    boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature, final GCodeResultStream resultStream, Reference ref);
    boolean waitForEverythingInLimits(final GCodeResultStream resultStream, Reference ref);
    boolean setPrintBedTemperatureNoWait(final Double temperature, Reference ref);
    boolean setChamberTemperatureNoWait(final Double temperature, Reference ref);
    boolean setPrintBedTemperatureAndDoWait(final Double temperature, final GCodeResultStream resultStream, Reference ref);
    double requestTemperatureOfHeater(Heater_enum pos, Reference ref);
    boolean istheHeaterConfigured(Heater_enum func);
    String getCurrentExtruderTemperature(Reference ref);
    String getHeatedBedTemperature(Reference ref);
 // Switches
    int getStateOfSwitch(Switch_enum theSwitch, Reference ref);
    boolean switchExtruderTo(int num, Reference ref);
    Reply sendRawOrderFrame(int ClientNumber, int order, Integer[] parameterBytes, int length);
    void waitForClientQueueEmpty(Reference ref);
    boolean runPIDautotune(Heater_enum Extruder,
            Double Temperature,
            int numCycles,
            GCodeResultStream resultStream, 
            Reference ref);
    void registerTemperatureObserver(TemperatureObserver observer);
    TimeoutHandler getTimeoutHandler();
}
