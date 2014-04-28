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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.junit.Test;

import de.nomagic.printerController.gui.GCodeMacro;
import de.nomagic.printerController.gui.Macro;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TestCfg
{

    private final double ALLOWED_DELTA = 0.0001;

    private Cfg storeAndLoad(Cfg dut)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        assertTrue(dut.saveTo(bout));
        byte[] data = bout.toByteArray();
        System.out.println(Tool.fromByteBufferToUtf8String(data));
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Cfg readCfg = new Cfg();
        readCfg.readFrom(bin);
        return readCfg;
    }

    @Test
    public void testGetGeneralSetting()
    {
        Cfg dut = new Cfg();
        assertTrue(dut.getGeneralSetting("test", true)); //creates setting in configuration and returns the default value
        assertTrue(dut.getGeneralSetting("test", false)); // setting is read, so default is not used !
        dut.setValueOfSetting("test", "foo");
        assertFalse(dut.getGeneralSetting("test", true));
        assertEquals("aValue", dut.getGeneralSetting("testString", "aValue"));
        assertEquals("aValue", dut.getGeneralSetting("testString", "not found"));
        assertEquals(7, dut.getGeneralSetting("testInt", 7));
        assertEquals(7, dut.getGeneralSetting("testInt", 18));
        assertEquals(3.14, dut.getGeneralSetting("testDouble", 3.14), ALLOWED_DELTA);
        assertEquals(3.14, dut.getGeneralSetting("testDouble", 47.11), ALLOWED_DELTA);
        Cfg readCfg = storeAndLoad(dut);
        assertFalse(readCfg.getGeneralSetting("test", true));
        assertEquals("aValue", readCfg.getGeneralSetting("testString", "not found"));
        assertEquals(7, readCfg.getGeneralSetting("testInt", 18));
        assertEquals(3.14, readCfg.getGeneralSetting("testDouble", 47.11), ALLOWED_DELTA);
    }

    @Test
    public void testSaveTo()
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Cfg dut = new Cfg();
        assertTrue(dut.saveTo(bout));
    }

    @Test
    public void testReadFrom()
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Cfg dut = new Cfg();
        assertTrue(dut.getGeneralSetting("test", true));
        assertTrue(dut.saveTo(bout));
        byte[] data = bout.toByteArray();
        System.out.println(Tool.fromByteBufferToUtf8String(data));
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Cfg readCfg = new Cfg();
        readCfg.readFrom(bin);
        assertTrue(dut.getGeneralSetting("test", false));
    }

    @Test
    public void testConnectionDefinition()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.setClientDeviceString(1, "two");
        assertEquals(2, dut.getNumberOfClients());
        assertEquals("one", dut.getConnectionDefinitionOfClient(0));
        assertEquals("two", dut.getConnectionDefinitionOfClient(1));
        assertEquals("", dut.getConnectionDefinitionOfClient(5));
        Cfg readCfg = storeAndLoad(dut);
        assertEquals(2, readCfg.getNumberOfClients());
        assertEquals("one", readCfg.getConnectionDefinitionOfClient(0));
        assertEquals("two", readCfg.getConnectionDefinitionOfClient(1));
        assertEquals("", readCfg.getConnectionDefinitionOfClient(5));
    }

    @Test
    public void testTemperatureSensor()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addTemperatureSensor(0, 5, Heater_enum.Print_Bed);
        dut.addTemperatureSensor(0, 1, Heater_enum.Extruder_0);
        assertNull(dut.getFunctionOfTemperatureSensor(0, 4));
        assertNull(dut.getFunctionOfTemperatureSensor(1, 5));
        assertEquals(Heater_enum.Print_Bed, dut.getFunctionOfTemperatureSensor(0, 5));
        assertEquals(Heater_enum.Extruder_0, dut.getFunctionOfTemperatureSensor(0, 1));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfTemperatureSensor(0, 4));
        assertNull(readCfg.getFunctionOfTemperatureSensor(1, 5));
        assertEquals(Heater_enum.Print_Bed, readCfg.getFunctionOfTemperatureSensor(0, 5));
        assertEquals(Heater_enum.Extruder_0, readCfg.getFunctionOfTemperatureSensor(0, 1));
    }

    @Test
    public void testHeater()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addHeater(0, 4, Heater_enum.Extruder_1);
        dut.addHeater(0, 1, Heater_enum.Print_Bed);
        assertNull(dut.getFunctionOfHeater(0, 5));
        assertNull(dut.getFunctionOfHeater(2, 5));
        assertEquals(Heater_enum.Extruder_1, dut.getFunctionOfHeater(0, 4));
        assertEquals(Heater_enum.Print_Bed, dut.getFunctionOfHeater(0, 1));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfHeater(0, 5));
        assertNull(readCfg.getFunctionOfHeater(2, 5));
        assertEquals(Heater_enum.Extruder_1, readCfg.getFunctionOfHeater(0, 4));
        assertEquals(Heater_enum.Print_Bed, readCfg.getFunctionOfHeater(0, 1));

    }

    @Test
    public void testFan()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addFan(0, 4, Fan_enum.Printed_Part);
        dut.addFan(0, 8, Fan_enum.Extruder_0);
        assertNull(dut.getFunctionOfFan(0, 1));
        assertNull(dut.getFunctionOfFan(2, 1));
        assertEquals(Fan_enum.Printed_Part, dut.getFunctionOfFan(0, 4));
        assertEquals(Fan_enum.Extruder_0, dut.getFunctionOfFan(0, 8));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfFan(0, 1));
        assertNull(readCfg.getFunctionOfFan(2, 1));
        assertEquals(Fan_enum.Printed_Part, readCfg.getFunctionOfFan(0, 4));
        assertEquals(Fan_enum.Extruder_0, readCfg.getFunctionOfFan(0, 8));
    }

    @Test
    public void testOutput()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addOutput(0, 2, Output_enum.Fan_Hot_End_0);
        dut.addOutput(0, 8, Output_enum.Fan_Hot_End_1);
        assertNull(dut.getFunctionOfOutput(0, 1));
        assertNull(dut.getFunctionOfOutput(2, 1));
        assertEquals(Output_enum.Fan_Hot_End_0, dut.getFunctionOfOutput(0, 2));
        assertEquals(Output_enum.Fan_Hot_End_1, dut.getFunctionOfOutput(0, 8));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfOutput(0, 1));
        assertNull(readCfg.getFunctionOfOutput(2, 1));
        assertEquals(Output_enum.Fan_Hot_End_0, readCfg.getFunctionOfOutput(0, 2));
        assertEquals(Output_enum.Fan_Hot_End_1, readCfg.getFunctionOfOutput(0, 8));
    }

    @Test
    public void testSwitches()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addSwitch(0, 3, Switch_enum.Xmax);
        dut.addSwitch(0, 6, Switch_enum.Ymin);
        assertNull(dut.getFunctionOfSwitch(0, 1));
        assertNull(dut.getFunctionOfSwitch(2, 1));
        assertEquals(Switch_enum.Xmax, dut.getFunctionOfSwitch(0, 3));
        assertEquals(Switch_enum.Ymin, dut.getFunctionOfSwitch(0, 6));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfSwitch(0, 1));
        assertNull(readCfg.getFunctionOfSwitch(2, 1));
        assertEquals(Switch_enum.Xmax, readCfg.getFunctionOfSwitch(0, 3));
        assertEquals(Switch_enum.Ymin, readCfg.getFunctionOfSwitch(0, 6));
    }

    @Test
    public void testUseSteppers()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        assertFalse(dut.shouldUseSteppers(0));
        assertFalse(dut.shouldUseSteppers(1));
        assertFalse(dut.shouldUseSteppers(5));
        dut.setUseSteppers(0, true);
        assertTrue(dut.shouldUseSteppers(0));
        Cfg readCfg = storeAndLoad(dut);
        assertTrue(readCfg.shouldUseSteppers(0));
        assertFalse(readCfg.shouldUseSteppers(1));
        assertFalse(readCfg.shouldUseSteppers(5));
    }

    @Test
    public void testStepper()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        assertNull(dut.getFunctionOfAxis(0, 3));
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertEquals(Axis_enum.X, dut.getFunctionOfAxis(0, 3));
        Cfg readCfg = storeAndLoad(dut);
        assertNull(readCfg.getFunctionOfAxis(0, 2));
        assertEquals(Axis_enum.X, readCfg.getFunctionOfAxis(0, 3));
    }

    @Test
    public void testStepperInvertedMovementDirection()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertFalse(dut.isMovementDirectionInverted(0, 5));
        assertFalse(dut.isMovementDirectionInverted(1, 2));
        dut.setMovementDirectionInverted(0, 3, true);
        dut.setMovementDirectionInverted(0, 4, false);
        assertTrue(dut.isMovementDirectionInverted(0, 3));
        Cfg readCfg = storeAndLoad(dut);
        assertFalse(readCfg.isMovementDirectionInverted(0, 5));
        assertFalse(readCfg.isMovementDirectionInverted(1, 2));
        assertTrue(readCfg.isMovementDirectionInverted(0, 3));
        assertFalse(readCfg.isMovementDirectionInverted(0, 4));
    }

    @Test
    public void testStepperMaxSpeed()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertEquals(0, dut.getMaxSpeedFor(0, 5));
        assertEquals(0, dut.getMaxSpeedFor(0, 4));
        assertEquals(0, dut.getMaxSpeedFor(1, 4));
        dut.setMaxSpeedFor(0, 3, 100);
        dut.setMaxSpeedFor(0, 4, 400);
        assertEquals(100, dut.getMaxSpeedFor(0, 3));
        assertEquals(400, dut.getMaxSpeedFor(0, 4));
        Cfg readCfg = storeAndLoad(dut);
        assertEquals(100, readCfg.getMaxSpeedFor(0, 3));
        assertEquals(400, readCfg.getMaxSpeedFor(0, 4));
        assertEquals(0, readCfg.getMaxSpeedFor(0, 5));
        assertEquals(0, readCfg.getMaxSpeedFor(1, 4));
    }

    @Test
    public void testStepperMaxAcceleration()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertEquals(0, dut.getMaxAccelerationFor(0, 5), ALLOWED_DELTA);
        assertEquals(0, dut.getMaxAccelerationFor(0, 4), ALLOWED_DELTA);
        assertEquals(0, dut.getMaxAccelerationFor(1, 4), ALLOWED_DELTA);
        dut.setMaxAccelerationFor(0, 3, 100.5);
        dut.setMaxAccelerationFor(0, 4, 400.5);
        assertEquals(100.5, dut.getMaxAccelerationFor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, dut.getMaxAccelerationFor(0, 4), ALLOWED_DELTA);
        Cfg readCfg = storeAndLoad(dut);
        assertEquals(100.5, readCfg.getMaxAccelerationFor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, readCfg.getMaxAccelerationFor(0, 4), ALLOWED_DELTA);
        assertEquals(0, readCfg.getMaxAccelerationFor(0, 5), ALLOWED_DELTA);
        assertEquals(0, readCfg.getMaxAccelerationFor(1, 4), ALLOWED_DELTA);
    }

    @Test
    public void testStepperStepsPerMillimeter()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertEquals(0, dut.getStepsPerMillimeterFor(0, 5), ALLOWED_DELTA);
        assertEquals(0, dut.getStepsPerMillimeterFor(0, 4), ALLOWED_DELTA);
        assertEquals(0, dut.getStepsPerMillimeterFor(1, 4), ALLOWED_DELTA);
        dut.setSteppsPerMillimeterFor(0, 3, 100.5);
        dut.setSteppsPerMillimeterFor(0, 4, 400.5);
        assertEquals(100.5, dut.getStepsPerMillimeterFor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, dut.getStepsPerMillimeterFor(0, 4), ALLOWED_DELTA);
        Cfg readCfg = storeAndLoad(dut);
        assertEquals(100.5, readCfg.getStepsPerMillimeterFor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, readCfg.getStepsPerMillimeterFor(0, 4), ALLOWED_DELTA);
        assertEquals(0, readCfg.getStepsPerMillimeterFor(0, 5), ALLOWED_DELTA);
        assertEquals(0, readCfg.getStepsPerMillimeterFor(1, 4), ALLOWED_DELTA);
    }

    @Test
    public void testStepperMaxJerk()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        dut.addStepper(0, 3, Axis_enum.X);
        dut.addStepper(0, 4, Axis_enum.Y);
        assertEquals(0, dut.getMaxJerkMmSfor(0, 5), ALLOWED_DELTA);
        assertEquals(0, dut.getMaxJerkMmSfor(0, 4), ALLOWED_DELTA);
        assertEquals(0, dut.getMaxJerkMmSfor(1, 4), ALLOWED_DELTA);
        dut.setMaxJerkMmSFor(0, 3, 100.5);
        dut.setMaxJerkMmSFor(0, 4, 400.5);
        assertEquals(100.5, dut.getMaxJerkMmSfor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, dut.getMaxJerkMmSfor(0, 4), ALLOWED_DELTA);
        Cfg readCfg = storeAndLoad(dut);
        assertEquals(100.5, readCfg.getMaxJerkMmSfor(0, 3), ALLOWED_DELTA);
        assertEquals(400.5, readCfg.getMaxJerkMmSfor(0, 4), ALLOWED_DELTA);
        assertEquals(0, readCfg.getMaxJerkMmSfor(0, 5), ALLOWED_DELTA);
        assertEquals(0, readCfg.getMaxJerkMmSfor(1, 4), ALLOWED_DELTA);
    }

    @Test
    public void testFirmwareSettings()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        Vector<Setting> res = dut.getAllFirmwareSettingsFor(0);
        assertNotNull(res);
        assertEquals(0, res.size());
        Vector<Setting> res1 = dut.getAllFirmwareSettingsFor(1);
        assertNotNull(res1);
        assertEquals(0, res1.size());
        dut.addFirmwareConfiguration(0, "foo", "five");
        dut.addFirmwareConfiguration(0, "bar", "three");
        Vector<Setting> res2 = dut.getAllFirmwareSettingsFor(0);
        assertNotNull(res2);
        assertEquals(2, res2.size());
        Setting s1 = res2.get(0);
        assertEquals("foo", s1.getName());
        assertEquals("five", s1.getValue());
        Setting s2 = res2.get(1);
        assertEquals("bar", s2.getName());
        assertEquals("three", s2.getValue());
        Cfg readCfg = storeAndLoad(dut);
        Vector<Setting> res3 = readCfg.getAllFirmwareSettingsFor(0);
        assertNotNull(res3);
        assertEquals(2, res3.size());
        Setting s3 = res3.get(0);
        assertEquals("foo", s3.getName());
        assertEquals("five", s3.getValue());
        Setting s4 = res3.get(1);
        assertEquals("bar", s4.getName());
        assertEquals("three", s4.getValue());
        Vector<Setting> res4 = dut.getAllFirmwareSettingsFor(1);
        assertNotNull(res4);
        assertEquals(0, res4.size());
    }

    @Test
    public void testMacros()
    {
        Cfg dut = new Cfg();
        dut.setClientDeviceString(0, "one");
        Vector<Macro> res = dut.getMacros();
        assertNotNull(res);
        assertEquals(0, res.size());
        Vector<Macro> vec = new Vector<Macro>();
        vec.add(null);
        GCodeMacro gm = new GCodeMacro("G92");
        vec.add(gm);
        dut.setMacros(vec);
        Vector<Macro> res2 = dut.getMacros();
        Vector<Macro> exp = new Vector<Macro>();
        exp.add(gm);
        for(int i = 0; i < exp.size(); i++)
        {
            assertNotNull(res2.get(i));
            String expDef = exp.get(i).getDefinition();
            String resDef = res2.get(i).getDefinition();
            assertEquals(expDef, resDef);
        }
        Cfg readCfg = storeAndLoad(dut);
        Vector<Macro> res3 = readCfg.getMacros();
        for(int i = 0; i < exp.size(); i++)
        {
            assertNotNull(res3.get(i));
            String expDef = exp.get(i).getDefinition();
            String resDef = res3.get(i).getDefinition();
            assertEquals(expDef, resDef);
        }
    }
}
