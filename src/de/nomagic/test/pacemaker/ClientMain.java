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

import javax.swing.SwingUtilities;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ClientMain
{
    private StatusWindow sw;
    private boolean shouldRun = true;

    public ClientMain()
    {
    }

    public void startGui()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    sw = new StatusWindow();
                }
                catch(final Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startCommunicating(int port)
    {
        // create TCP Port and Listen to connections
        System.out.println("Starting to listen on Port " + port);
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(port);
        }
        catch(final IOException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        while(true== shouldRun)
        {
            try
            {
                final Socket s = server.accept();
                System.out.println("Received a Connection !");
                s.setTcpNoDelay(true);
                final InputStream in = s.getInputStream();
                final OutputStream out = s.getOutputStream();
                final ProtocolClient pc = new ProtocolClient(in, out, sw);
                if(null != sw)
                {
                    sw.setProtocolClient(pc);
                }
                try
                {
                    pc.communicate();
                }
                catch(final IOException e)
                {
                    System.out.println(e.getMessage());
                }
                s.close();
            }
            catch(final IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
        try
        {
            server.close();
        }
        catch(final IOException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(final String[] args) throws IOException
    {

            final ClientMain cm = new ClientMain();
            cm.startGui();
            cm.startCommunicating(12345);

    }

}
