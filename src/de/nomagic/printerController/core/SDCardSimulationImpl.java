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
package de.nomagic.printerController.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class SDCardSimulationImpl implements SDCardSimulation
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private File rootFolder;
    private FileWriter fw;
    private String LastErrorReason = "";


    public SDCardSimulationImpl(File Folder)
    {
        this.rootFolder = Folder;
    }

    public String[] getListOfFiles()
    {
        return getNamesListOfFilesRecursively(rootFolder, "");
    }

    private String[] getNamesListOfFilesRecursively(File root, String prefix)
    {
        if(null == root)
        {
            log.error("No Folder !");
            return new String[0];
        }
        if(false == root.isDirectory())
        {
            log.error("Not a Folder !");
            return new String[0];
        }
        final File[] files = root.listFiles();
        final Vector<String> allNames = new Vector<String>();
        for(int i = 0; i < files.length; i++)
        {
            final File curFile = files[i];
            if(true == curFile.isDirectory())
            {
                final String[] res = getNamesListOfFilesRecursively(curFile, prefix + curFile.getName());
                for(int k = 0; k < res.length; k++)
                {
                    allNames.add(res[k]);
                }
            }
            else
            {
                allNames.add(prefix + curFile.getName());
            }
        }
        return allNames.toArray(new String[0]);
    }

    public void closeFile()
    {
        if(null != fw)
        {
            try
            {
                fw.flush();
                fw.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            fw = null;
        }
    }

    public boolean appendToFile(String line)
    {
        if(null == fw)
        {
            return false;
        }
        else
        {
            try
            {
                fw.write(line + "\r\n");
                return true;
            }
            catch(IOException e)
            {
                LastErrorReason = e.getMessage();
                return false;
            }
        }
    }

    public String getLastErrorReason()
    {
        return LastErrorReason;
    }

    public boolean createAndOpenNewFile(String fileName)
    {
        try
        {
            fw = new FileWriter(fileName, false);
        }
        catch(IOException e)
        {
            LastErrorReason = e.getMessage();
            return false;
        }
        return true;
    }

    public boolean deleteFile(String fileName)
    {
        final File f = new File(fileName);
        return f.delete();
    }

    public RandomAccessFile openFileForReading(String fileName)
    {
        try
        {
            return new RandomAccessFile(fileName, "r");
        }
        catch(FileNotFoundException e)
        {
            LastErrorReason = "File not Found";
            return null;
        }
    }


}
