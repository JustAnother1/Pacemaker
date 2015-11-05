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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class SDCardPrinterImpl extends Thread implements SDCardPrinter
{
    private volatile String currentFileName = "";
    private volatile RandomAccessFile currenFile;
    private SDCardSimulation sdCard;
    private GCodeDecoder gCodeDecoder;
    private volatile boolean isRunning = false;
    private volatile boolean isPrinting = false;
    private volatile boolean needsToRestart = false;

    public SDCardPrinterImpl(SDCardSimulation sdCard, GCodeDecoder gCodeDecoder)
    {
        super("SD Card Printer");
        this.sdCard = sdCard;
        this.gCodeDecoder = gCodeDecoder;
    }

    public boolean startResumePrinting(String selectedSDCardFile)
    {
        if(true == isRunning)
        {
            if(currentFileName != selectedSDCardFile)
            {
                // paused -> start new File
                needsToRestart = true;
                currentFileName = selectedSDCardFile;
                isPrinting = true;
            }
            else
            {
                // we have been paused -> resume
                isPrinting = true;
            }
        }
        else
        {
            // start of new print
            currentFileName = selectedSDCardFile;
            this.start();
        }
        return true;
    }

    private void print()
    {
        boolean Done = false;
        do
        {
            String curLine;
            try
            {
                if(false == isPrinting)
                {
                    do
                    {
                        try
                        {
                            sleep(1);
                        }
                        catch(InterruptedException e)
                        {
                            Done = true;
                        }
                    }while(false == isPrinting);
                }
                curLine = currenFile.readUTF();
                if(null == curLine)
                {
                    Done = true;
                }
                else
                {
                    gCodeDecoder.sendLine(curLine, null);
                }
            }
            catch(IOException e)
            {
                Done = true;
            }
        }while(false == Done);
        isPrinting = false;
    }

    public void run()
    {
        isRunning = true;
        do
        {
            needsToRestart = false;
            currenFile = sdCard.openFileForReading(currentFileName);
            if(null != currenFile)
            {
                isPrinting = true;
                print();
            }
        } while(true == needsToRestart);
        isRunning = false;
    }

    public String getPrintStatus()
    {
        final StringBuffer res = new StringBuffer();
        if(false == isPrinting)
        {
            res.append("No SD Card Print active.");
        }
        else
        {
            res.append("printing : " + currentFileName + "\n");
            try
            {
                res.append("position in File : " + currenFile.getFilePointer() + "\n");
                res.append("File Length : " + currenFile.length() + "\n");
            }
            catch(IOException e)
            {
                res.append("IOException !\n");
            }
        }
        return res.toString();
    }

    public void pausePrinting()
    {
        isPrinting = false;
    }

    public boolean setSDCardPosition(long position)
    {
        if(null == currenFile)
        {
            return false;
        }
        try
        {
            if(position > currenFile.length())
            {
                return false;
            }
        }
        catch(IOException e)
        {
            return false;
        }
        try
        {
            currenFile.seek(position);
        }
        catch(IOException e)
        {
            return false;
        }
        return true;
    }

}
