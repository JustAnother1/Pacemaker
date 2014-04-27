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

import org.junit.Test;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TestCfg
{

    @Test
    public void testGetGeneralSetting()
    {
        Cfg dut = new Cfg();
        assertTrue(dut.getGeneralSetting("test", true)); //creates setting in configuration and returns the default value
        assertTrue(dut.getGeneralSetting("test", false)); // setting is read, so default is not used !
        dut.setValueOfSetting("test", "false");
        assertFalse(dut.getGeneralSetting("test", true));
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
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        Cfg readCfg = new Cfg();
        readCfg.readFrom(bin);
        assertTrue(dut.getGeneralSetting("test", false));
    }

}
