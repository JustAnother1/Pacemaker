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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class UdpInterface extends InteractiveInterface
{
    public static final int PORT = 2342;
    public static final int MAX_PACKET_SIZE = 1024;
    public UdpInterface()
    {
    }

    public void run()
    {
        DatagramSocket serverSocket;
        try
        {
            serverSocket = new DatagramSocket(PORT);
        }
        catch(SocketException e)
        {
            e.printStackTrace();
            return;
        }
        byte[] receiveData = new byte[MAX_PACKET_SIZE];
        byte[] sendData = new byte[MAX_PACKET_SIZE];
        while(false == isInterrupted())
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try
            {
                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData());
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = parseString(sentence);
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }

}
