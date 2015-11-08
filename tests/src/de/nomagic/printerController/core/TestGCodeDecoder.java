/**
 *
 */
package de.nomagic.printerController.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lars
 *
 */
public class TestGCodeDecoder
{
    private GCodeDecoder dut;
    private ExecutorStub exe;
    private SDCardSimulation sdCard;
    private SDCardPrinter sdPrinter;

    @Before
    public void setUp()
    {
        exe = new ExecutorStub();
        sdCard = new SDCardSimulationStub();
        sdPrinter = new SDCardPrinterStub();
        dut = new GCodeDecoder(exe, sdCard);
        dut.addSDCardPrinter(sdPrinter);
    }

    @After
    public void tearDown()
    {
        dut.close();
    }

    @Test
    public void testSendLine_nullNull()
    {
        assertEquals("", dut.sendLine(null, null));
    }

    @Test
    public void testSendLine_emptyString()
    {
        assertEquals("", dut.sendLine("", null));
    }

    @Test
    public void testSendLine_emptyCode()
    {
        assertEquals("", dut.sendLine("    ", null));
    }

    @Test
    public void testSendLine_validCodeNoCommand()
    {
        assertEquals("!! Line has no G, M or T Code !", dut.sendLine("F1500", null));
    }

    // start of G-Codes

    @Test
    public void testSendLine_Code_G20_G21()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("G20", null));
    	assertEquals("ok", dut.sendLine("M92 X100", null));
    	assertEquals( 100.0/25.4, exe.getStepsPerMilimeterSet(), 0.0001);
    	assertEquals("ok", dut.sendLine("G21", null));
    	assertEquals("ok", dut.sendLine("M92 X100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_G28()
    {
    	exe.setReturnFor_startHoming(true);
        assertEquals("ok", dut.sendLine("G28", null));
    }

    @Test
    public void testSendLine_Code_G28_exe_fail()
    {
    	exe.setLastError("G28 failed");
    	exe.setReturnFor_startHoming(false);
        assertEquals("!! G28 failed", dut.sendLine("G28", null));
    }

    @Test
    public void testSendLine_Code_G28_X()
    {
    	exe.setReturnFor_startHoming(true);
        assertEquals("ok", dut.sendLine("G28 X127", null));
    }

    @Test
    public void testSendLine_Code_M0()
    {
    	exe.set_doShutDownReturn(true);
        assertEquals("ok", dut.sendLine("M0", null));
    }

    @Test
    public void testSendLine_Code_M0_executor_fail()
    {
    	exe.set_doShutDownReturn(false);
    	exe.setLastError("M0 failed");
        assertEquals("!! M0 failed", dut.sendLine("M0", null));
    }

    @Test
    public void testSendLine_Code_M17()
    {
    	exe.setReturnFor_enableAllStepperMotors(true);
        assertEquals("ok", dut.sendLine("M17", null));
    }

    @Test
    public void testSendLine_Code_M17_executor_fail()
    {
    	exe.setReturnFor_enableAllStepperMotors(false);
    	exe.setLastError("M17 failed");
        assertEquals("!! M17 failed", dut.sendLine("M17", null));
    }

    @Test
    public void testSendLine_Code_M18()
    {
    	exe.setReturnFor_disableAllStepperMotors(true);
        assertEquals("ok", dut.sendLine("M18", null));
    }

    @Test
    public void testSendLine_Code_M18_executor_fail()
    {
    	exe.setReturnFor_disableAllStepperMotors(false);
    	exe.setLastError("M18 failed");
        assertEquals("!! M18 failed", dut.sendLine("M18", null));
    }

    @Test
    public void testSendLine_Code_M84()
    {
    	exe.setReturnFor_disableAllStepperMotors(true);
        assertEquals("ok", dut.sendLine("M18", null));
    }

    @Test
    public void testSendLine_Code_M84_executor_fail()
    {
    	exe.setReturnFor_disableAllStepperMotors(false);
    	exe.setLastError("M18 failed");
        assertEquals("!! M18 failed", dut.sendLine("M18", null));
    }

    @Test
    public void testSendLine_Code_M92_noParameter()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	String res = dut.sendLine("M92", null);
    	assertTrue(res != null);
    	assertTrue(res.startsWith("!!"));
    }

    @Test
    public void testSendLine_Code_M92_X()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("M92 X100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_Y()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("M92 Y100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_Z()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("M92 Z100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_E()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("M92 E100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_StupidUnits()
    {
    	exe.setReturnFor_setStepsPerMilimeter(true);
    	assertEquals("ok", dut.sendLine("G20", null));
    	assertEquals("ok", dut.sendLine("M92 X100", null));
    	assertEquals( 100.0/25.4, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_StupidUnitsExecFails()
    {
    	exe.setReturnFor_setStepsPerMilimeter(false);
    	exe.setLastError("M92 failed");
    	assertEquals("ok", dut.sendLine("G20", null));
    	assertEquals("!! M92 failed", dut.sendLine("M92 X100", null));
    	assertEquals( 100.0/25.4, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M92_E_execFails()
    {
    	exe.setReturnFor_setStepsPerMilimeter(false);
    	exe.setLastError("M92 failed");
    	assertEquals("!! M92 failed", dut.sendLine("M92 E100", null));
    	assertEquals( 100.0, exe.getStepsPerMilimeterSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M104()
    {
    	exe.setReturnFor_setCurrentExtruderTemperatureNoWait(true);
    	assertEquals("ok", dut.sendLine("M104 S220", null));
    }

    @Test
    public void testSendLine_Code_M104_checkValue()
    {
    	exe.setReturnFor_setCurrentExtruderTemperatureNoWait(true);
    	assertEquals("ok", dut.sendLine("M104 S220", null));
    	assertEquals(220.0, exe.get_ExtruderTemperatureSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M104_checkValueNonInteger()
    {
    	exe.setReturnFor_setCurrentExtruderTemperatureNoWait(true);
    	assertEquals("ok", dut.sendLine("M104 S220.4", null));
    	assertEquals(220.4, exe.get_ExtruderTemperatureSet(), 0.0001);
    }

    @Test
    public void testSendLine_Code_M104_execFails()
    {
    	exe.setReturnFor_setCurrentExtruderTemperatureNoWait(false);
    	exe.setLastError("M104 failed");
    	assertEquals("!! M104 failed", dut.sendLine("M104 S220", null));
    }

    @Test
    public void testSendLine_Code_M104_noParam()
    {
    	exe.setReturnFor_setCurrentExtruderTemperatureNoWait(true);
    	String res =  dut.sendLine("M104", null);
    	assertTrue(null != res);
    	assertTrue(res.startsWith("!!"));
    }

    @Test
    public void testSendLine_Code_M106()
    {
    	exe.setReturnFor_setFanSpeedfor(true);
    	assertEquals("ok", dut.sendLine("M106 S127", null));
    }

    @Test
    public void testSendLine_Code_M106_noParam()
    {
    	exe.setReturnFor_setFanSpeedfor(true);
    	String res = dut.sendLine("M106", null);
    	assertTrue(res != null);
    	assertTrue(res.startsWith("!!"));
    }

    @Test
    public void testSendLine_Code_M106_P()
    {
    	exe.setReturnFor_setFanSpeedfor(true);
    	assertEquals("ok", dut.sendLine("M106 P2 S100", null));
    	assertEquals(2, exe.get_fan_set());
    	assertEquals((100 * 256) + 100, exe.get_speed_set());
    }

    @Test
    public void testSendLine_Code_M106_P_execFail()
    {
    	exe.setReturnFor_setFanSpeedfor(false);
    	exe.setLastError("M106 failed");
    	assertEquals("!! M106 failed", dut.sendLine("M106 P2 S100", null));
    	assertEquals(2, exe.get_fan_set());
    	assertEquals((100 * 256) + 100, exe.get_speed_set());
    }

    @Test
    public void testSendLine_Code_M106_execFail()
    {
    	exe.setReturnFor_setFanSpeedfor(false);
    	exe.setLastError("M106 failed");
    	assertEquals("!! M106 failed", dut.sendLine("M106 S127", null));
    }

    @Test
    public void testSendLine_Code_M106_check_Value()
    {
    	exe.setReturnFor_setFanSpeedfor(true);
    	assertEquals("ok", dut.sendLine("M106 S127", null));
    	assertEquals(0, exe.get_fan_set());
    	assertEquals((127*256) + 127, exe.get_speed_set());

    	assertEquals("ok", dut.sendLine("M106 S0", null));
    	assertEquals(0, exe.get_fan_set());
    	assertEquals(0, exe.get_speed_set());

    	assertEquals("ok", dut.sendLine("M106 S255", null));
    	assertEquals(0, exe.get_fan_set());
    	assertEquals(0xffff, exe.get_speed_set());
    }


    // End of G-Codes

    @Test
    public void testGetLastErrorReason_null()
    {
        assertNull(dut.getLastErrorReason());
    }

    @Test
    public void testGetLastErrorReason_fromExecutor()
    {
        exe.setLastError("no beer available!");
        assertEquals("no beer available!", dut.getLastErrorReason());
    }

    @Test
    public void testGetLastErrorReason_notNull()
    {
        GCodeStub gcs = new GCodeStub();
        dut.setGCodeImplementation(gcs);
        dut.sendLine("bla", null);
        assertEquals("G-Code is invalid !", dut.getLastErrorReason());
    }

}
