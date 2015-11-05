package de.nomagic.printerController.core;

import java.io.RandomAccessFile;

public interface SDCardSimulation
{
    String[] getListOfFiles();
    void closeFile();
    boolean appendToFile(String line);
    boolean createAndOpenNewFile(String fileName);
    boolean deleteFile(String fileName);
    RandomAccessFile openFileForReading(String fileName);
    String getLastErrorReason();
}
