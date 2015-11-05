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

    void close();
    boolean doShutDown();
    boolean doImmediateShutDown();

 // Stepper Control
    boolean addPauseFor(final Double seconds);
    boolean addMoveTo(final RelativeMove relMove);
    boolean letMovementStop();
    boolean startHoming(Axis_enum[] axis);
    boolean disableAllStepperMotors();
    boolean enableAllStepperMotors();
    boolean setStepsPerMilimeter(final Axis_enum axle, final Double stepsPerMillimeter);
 // FAN
    boolean setFanSpeedfor(final int fan, final int speed);
 // Temperature
    boolean setCurrentExtruderTemperatureNoWait(final Double temperature);
    boolean setCurrentExtruderTemperatureAndDoWait(final Double temperature, final GCodeResultStream resultStream);
    boolean waitForEverythingInLimits(final GCodeResultStream resultStream);
    boolean setPrintBedTemperatureNoWait(final Double temperature);
    boolean setChamberTemperatureNoWait(final Double temperature);
    boolean setPrintBedTemperatureAndDoWait(final Double temperature, final GCodeResultStream resultStream);
    double requestTemperatureOfHeater(Heater_enum pos);
    boolean istheHeaterConfigured(Heater_enum func);
    String getCurrentExtruderTemperature();
    String getHeatedBedTemperature();
 // Switches
    int getStateOfSwitch(Switch_enum theSwitch);
    boolean switchExtruderTo(int num);
    Reply sendRawOrderFrame(int ClientNumber, int order, Integer[] parameterBytes, int length);
    void waitForClientQueueEmpty();
    boolean runPIDautotune(Heater_enum Extruder,
            Double Temperature,
            int numCycles,
            GCodeResultStream resultStream);
    void registerTemperatureObserver(TemperatureObserver observer);
    TimeoutHandler getTimeoutHandler();
}
