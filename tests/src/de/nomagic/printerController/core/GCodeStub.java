package de.nomagic.printerController.core;

public class GCodeStub implements GCode
{

    public GCodeStub()
    {
    }

    @Override
    public GCode getGCodeFrom(String line)
    {
        return this;
    }

    @Override
    public String getLineWithoutCommentWithoutWord(Character wordType)
    {
        return null;
    }

    @Override
    public boolean hasWord(Character wordType)
    {
        return false;
    }

    @Override
    public Double getWordValue(Character word)
    {
        return null;
    }

    @Override
    public Double getWordValue(Character word, double defaultValue)
    {
        return null;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean isValid()
    {
        return false;
    }

}
