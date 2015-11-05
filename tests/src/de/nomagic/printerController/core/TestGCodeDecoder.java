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
    public void testSendLine()
    {
        assertEquals("", dut.sendLine(null, null));
    }

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
