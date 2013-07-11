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
package de.nomagic.test.pacemaker;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public interface Hardware
{
    String getFirmwareNameString();
    String getSerialNumberString();
    String getBoardNameString();
    String getGivenNameString();
    byte getProtocolVersionMajor();
    byte getProtocolVersionMinor();
    int[] getListOfSupportedProtocolExtensions();
    int getFirmwareType();
    int getFirmwareRevisionMajor();
    int getFirmwareRevisionMinor();
    int getHardwareType();
    int getHardwareRevision();
    int getNumberSteppers();
    int getNumberHeaters();
    int getNumberPwm();
    int getNumberTempSensor();
    int getNumberInput();
    int getNumberOutput();
    int getNumberBuzzer();
    String getNameOfInput(int idx);
    String getNameOfOutput(int idx);
    String getNameOfPwm(int idx);
    String getNameOfStepper(int idx);
    String getNameOfHeater(int idx);
    String getNameOfTemperatureSensor(int idx);
    void reset();
    int getTemperatureFromSensor(int devIdx);
    byte[] getConfigurationOfHeater(int heaterIdx);
    void setConfigurationOfHeater(int heaterIdx, int tempSensor);
    void setTargetTemperatureOfHeater(int heaterIdx, int targetTemp);
    String getNameOfBuzzer(int idx);
    int getInputValue(int devIdx);
    void setOutputTo(int devIdx, int state);
    void setPwmTo(int devIdx, int pwm);
}
