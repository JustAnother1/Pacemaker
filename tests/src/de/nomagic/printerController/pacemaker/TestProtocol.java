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
package de.nomagic.printerController.pacemaker;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TestProtocol
{

    @Test
    public void testOperational()
    {
        Protocol pro = new Protocol(null, null);
        assertFalse(pro.isOperational());
        pro.closeConnection();
        assertNull(pro.getLastErrorReason());
        assertNotNull(pro.toString());
        System.out.println(pro.toString());

        ClientConnectionMock connection = new ClientConnectionMock();
        Protocol pro2 = new Protocol(connection, null);
        assertTrue(pro2.isOperational());
        pro2.closeConnection();
        assertNull(pro2.getLastErrorReason());
        assertNotNull(pro2.toString());
        System.out.println(pro.toString());
    }

    @Test
    public void testParse()
    {
        assertNotNull(Protocol.parse(null));
    }

    @Test
    public void testOrderCodes()
    {
        String order;
        System.out.println("Code : Order");
        for(byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++)
        {
            order = Protocol.orderCodeToString(b);
            assertNotNull(order);
            System.out.println("" + b + " : " + order);
        }
        order = Protocol.orderCodeToString(Byte.MAX_VALUE);
        assertNotNull(order);
        System.out.println("" + Byte.MAX_VALUE + " : " + order);
    }

}
