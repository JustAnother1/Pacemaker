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
package de.nomagic.printerController.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.SwingWorker;

import org.jfree.util.Log;

import de.nomagic.printerController.GCodeResultStream;
import de.nomagic.printerController.core.CoreStateMachine;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class PrintWorker extends SwingWorker<Void, Void>
{
    private final File GCodeFile;
    private final CoreStateMachine pp;
    private final GCodeResultStream resultStream;

    public PrintWorker(File f, CoreStateMachine pp, GCodeResultStream res)
    {
        this.GCodeFile = f;
        this.pp = pp;
        this.resultStream = res;
    }

    protected Void doInBackground()
    {
        BufferedReader br = null;
        FileInputStream fin = null;
        try
        {
            fin = new FileInputStream(GCodeFile);
            br = new BufferedReader(new InputStreamReader(fin, Charset.forName("UTF-8")));
            String curLine = br.readLine();
            while(null != curLine)
            {
                final String res = pp.executeGCode(curLine, resultStream);
                if(true == res.startsWith("!!"))
                {
                    Log.error(res);
                    return null;
                }
                curLine = br.readLine();
            }
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        finally
        {
            if(null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            if(null != fin)
            {
                try
                {
                    fin.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

}
