package de.nomagic.printerController.core;

public interface GCode
{
    GCode getGCodeFrom(String line);
    String getLineWithoutCommentWithoutWord(final Character wordType);
    boolean hasWord(final Character wordType);
    Double getWordValue(final Character word);
    Double getWordValue(final Character word, double defaultValue);
    boolean isEmpty();
    boolean isValid();
}
