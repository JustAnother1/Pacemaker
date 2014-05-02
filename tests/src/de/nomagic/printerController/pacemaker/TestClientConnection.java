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

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TestClientConnection implements ClientConnection
{

    public TestClientConnection()
    {

    }

    @Override
    public Reply sendRequest(byte order, byte[] parameter)
    {
        // reply is a OK Frame
        Reply res = new Reply(new byte[] {0x42, // sync
                                             2, // length
                                             0, // Control - sequence number not checked
                                          0x10, // Reply Code - OK
                                             // Parameter
                                             0 // CRC - not checked
                                             });
        return res;
    }

    @Override
    public Reply sendRequest(int order, Integer[] parameter, int offset, int length)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reply sendRequest(int order, int[] parameter, int offset, int length)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reply sendRequest(byte order, byte[] parameter, int offset, int length)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public long getTimeOfLastSuccessfulReply()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
