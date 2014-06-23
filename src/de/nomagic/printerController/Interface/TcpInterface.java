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
package de.nomagic.printerController.Interface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** send G-Codes using a TCP connection.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TcpInterface extends InteractiveInterface
{
    public static final int PORT = 2342;

    public TcpInterface()
    {
    }

    public void run()
    {
        ServerSocket welcomeSocket;
        try
        {
            welcomeSocket = new ServerSocket(PORT);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        while(false == isInterrupted())
        {
            try
            {
                final Socket connectionSocket = welcomeSocket.accept();
                in = connectionSocket.getInputStream();
                out = connectionSocket.getOutputStream();
                out.write("start\r\n".getBytes());
                readFromStreams();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            welcomeSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
