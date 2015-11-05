package de.nomagic.printerController.core;

public interface SDCardPrinter
{
    boolean startResumePrinting(String selectedSDCardFile);
    String getPrintStatus();
    void pausePrinting();
    boolean setSDCardPosition(long position);
}
