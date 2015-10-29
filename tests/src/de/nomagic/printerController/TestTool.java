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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TestTool
{

    /**
     * Test method for {@link de.nomagic.printerController.Tool#fromExceptionToString(java.lang.Throwable)}.
     */
    @Test
    public void testFromExceptionToString()
    {
        Exception e = new IOException();
        String res = Tool.fromExceptionToString(e);
        assertTrue(res.contains("java.io.IOException"));
        assertEquals("Exception [null]", Tool.fromExceptionToString(null));
    }

    @Test
    public void testFromByteBufferToHexStringByteArray()
    {
        assertEquals("[]", Tool.fromByteBufferToHexString((byte[])null));
        assertEquals("[]", Tool.fromByteBufferToHexString((int[])null));
        assertEquals("[01 02 03]", Tool.fromByteBufferToHexString(new byte[] {1, 2, 3}));
        assertEquals("[01 02 03]", Tool.fromByteBufferToHexString(new int[] {1, 2, 3}));
        assertEquals("[01 02]", Tool.fromByteBufferToHexString(new byte[] {1, 2, 3}, 2));
        assertEquals("[02 03]", Tool.fromByteBufferToHexString(new byte[] {1, 2, 3}, 2, 1));
        assertEquals("[02 03]", Tool.fromByteBufferToHexString(new int[] {1, 2, 3}, 2, 1));
        assertEquals("[]", Tool.fromByteBufferToHexString((byte[])null, 2, 1));
        assertEquals("[]", Tool.fromByteBufferToHexString((int[])null, 2, 1));
    }

    /**
     * Test method for {@link de.nomagic.printerController.Tool#fromByteBufferToUtf8String(byte[])}.
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testFromByteBufferToUtf8String()
    {
        try
        {
            assertEquals("[]", Tool.fromByteBufferToUtf8String(null));
            assertEquals("[The Test String]", Tool.fromByteBufferToUtf8String("The Test String".getBytes("UTF8")));
        }
        catch(UnsupportedEncodingException e)
        {
            fail("UTF8 char set not supported !");
        }
    }

    /**
     * Test method for {@link de.nomagic.printerController.Tool#isValidChar(char)}.
     */
    @Test
    public void testIsValidChar()
    {
        assertTrue(Tool.isValidChar('a'));
        assertFalse(Tool.isValidChar(','));
    }

    /**
     * Test method for {@link de.nomagic.printerController.Tool#onlyAllowedChars(java.lang.String)}.
     */
    @Test
    public void testOnlyAllowedChars()
    {
        assertEquals("TestString", Tool.onlyAllowedChars("TestString"));
        assertEquals("anotherString", Tool.onlyAllowedChars("another,. String"));
    }

    /**
     * Test method for {@link de.nomagic.printerController.Tool#getStacTrace()}.
     */
    @Test
    public void testGetStacTrace()
    {
        assertNotNull(Tool.getStacTrace());
    }

}
