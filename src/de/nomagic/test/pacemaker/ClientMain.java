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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientMain
{
    private final StatusWindow sw = new StatusWindow();


    /**
     * @throws IOException
     *
     */
    public ClientMain(final int port) throws IOException
    {
        // create TCP Port and Listen to connections
        System.out.println("Starting to listen on Port " + port);
        final ServerSocket server = new ServerSocket(port);
        final Socket s = server.accept();
        System.out.println("Received a Connection !");
        s.setTcpNoDelay(true);
        final InputStream in = s.getInputStream();
        final OutputStream out = s.getOutputStream();
        final ProtocolClient pc = new ProtocolClient(in, out, sw);
        pc.communicate();
        server.close();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException
    {
        try
        {
            @SuppressWarnings("unused")
            final ClientMain cm = new ClientMain(12345);
        }
        catch(final IOException e)
        {
            System.out.println(e.getMessage());
            // e.printStackTrace();
            System.exit(1);
        }
    }

}
