package de.nomagic.printerController.core;

public class SDCardPrinterStub implements SDCardPrinter
{

    public SDCardPrinterStub()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean startResumePrinting(String selectedSDCardFile)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getPrintStatus()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pausePrinting()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean setSDCardPosition(long position)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
