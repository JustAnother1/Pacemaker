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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.CloseApplication;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public abstract class InteractiveInterface extends Thread
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected InputStream in;
    protected OutputStream out;

    private CoreStateMachine core;
    protected CloseApplication closer;

    public void addPacemakerCore(CoreStateMachine core)
    {
        this.core = core;
    }

    public void addCloser(CloseApplication closer)
    {
        this.closer = closer;
    }

    protected String parseString(String line)
    {
        return core.executeGCode(line);
    }

    protected void readFromStreams()
    {
        InputStreamReader readIn;
        StringBuffer curLineBuffer = new StringBuffer();
        try
        {
            readIn = new  InputStreamReader(in, "UTF-8");
        }
        catch(UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
            return;
        }
        try
        {
            while(false == isInterrupted())
            {
                int ci = readIn.read();
                if(-1 == ci)
                {
                    log.error("Read -1");
                    return;
                }
                char c = (char)ci;
                if((c != '\r') && (c != '\n'))
                {
                    curLineBuffer.append(c);
                }
                else
                {
                    if(0 < curLineBuffer.length())
                    {
                        String line = curLineBuffer.toString();
                        curLineBuffer = new StringBuffer();
                        out.write(parseString(line).getBytes("UTF-8"));
                        out.write("\r\n".getBytes());
                    }
                    // else empty line or \r\n -> ignore
                }
            }
        }
        catch(IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run()
    {
        readFromStreams();
    }

    public void close()
    {
        this.interrupt();
    }

}
