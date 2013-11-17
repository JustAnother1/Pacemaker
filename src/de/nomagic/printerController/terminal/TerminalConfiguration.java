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
package de.nomagic.printerController.terminal;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TerminalConfiguration
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private static final int VERSION_NUMBER = 1;


    private static final String CONFIG_FILE_NAME = "PacemakerTerminal.cfg";
    private String ConnectionString = "";
    private Vector<Macro> Macros = new Vector<Macro>();
    private boolean changed = true;

    public TerminalConfiguration()
    {
        readConfig();
    }

    public void close()
    {
        log.trace("Close is called");
        if(true == changed)
        {
            writeConfig();
        }
    }

    private void readConfig()
    {
        log.info("reading configuration");
        boolean failed = false;
        File f = new File(CONFIG_FILE_NAME);
        if(true == f.canRead())
        {
            ObjectInputStream oin = null;
            try
            {
                oin = new ObjectInputStream(new FileInputStream(f));
                Macro curM = null;
                int ver = (Integer)oin.readObject();
                if(ver != VERSION_NUMBER)
                {
                    log.error("Configuration file had Version Number {} but needs {}", ver, VERSION_NUMBER);
                    failed = true;
                }
                // else ok
                ConnectionString = (String)oin.readObject();
                do
                {
                    curM = (Macro)oin.readObject();
                    if(null != curM)
                    {
                        Macros.add(curM);
                    }
                } while(curM != null);
                if(false == failed)
                {
                    changed = false;
                }
            }
            catch(ClassCastException e)
            {
                failed = true;
            }
            catch(EOFException e)
            {
                // ok
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(null != oin)
                {
                    try
                    {
                        oin.close();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(true == failed)
        {
            // the configuration file is invalid -> delete it
            f.delete();
        }
    }

    private void writeConfig()
    {
        if(true == changed)
        {
            log.info("Writing configuration");
            File f = new File(CONFIG_FILE_NAME);
            try
            {
                f.createNewFile();
                if(true == f.canWrite())
                {
                    ObjectOutputStream oOut = null;
                    try
                    {
                        oOut = new ObjectOutputStream(new FileOutputStream(f));
                        oOut.writeObject(VERSION_NUMBER);
                        oOut.writeObject(ConnectionString);
                        for(int i = 0; i < Macros.size(); i++)
                        {
                            oOut.writeObject(Macros.get(i));
                        }
                    }
                    catch(FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        if(null != oOut)
                        {
                            try
                            {
                                oOut.close();
                            }
                            catch(IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                {
                    log.error("Could not save the Macros !");
                }
            }
            catch(IOException e1)
            {
                e1.printStackTrace();
                log.error("IO Exception! Could not save the Macros !");
            }
        }
    }

    public Vector<Macro> getMacros()
    {
        return Macros;
    }

    public void updateMacros(Vector<Macro> macros)
    {
        Macros = macros;
        changed = true;
    }

    public void setConnectionString(String ClientDefinition)
    {
        ConnectionString = ClientDefinition;
        changed = true;
    }

    public String getConnectionString()
    {
        return ConnectionString;
    }

}
