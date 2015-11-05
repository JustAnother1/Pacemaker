/**
 *
 */
package de.nomagic.printerController.core;

import java.io.RandomAccessFile;

/**
 * @author lars
 *
 */
public class SDCardSimulationStub implements SDCardSimulation
{

    /**
     *
     */
    public SDCardSimulationStub()
    {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#getListOfFiles()
     */
    @Override
    public String[] getListOfFiles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#closeFile()
     */
    @Override
    public void closeFile()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#appendToFile(java.lang.String)
     */
    @Override
    public boolean appendToFile(String line)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#createAndOpenNewFile(java.lang.String)
     */
    @Override
    public boolean createAndOpenNewFile(String fileName)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#deleteFile(java.lang.String)
     */
    @Override
    public boolean deleteFile(String fileName)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#openFileForReading(java.lang.String)
     */
    @Override
    public RandomAccessFile openFileForReading(String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see de.nomagic.printerController.core.SDCardSimulation#getLastErrorReason()
     */
    @Override
    public String getLastErrorReason()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
