package de.nomagic.printerController.gui;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestGCodeMacro
{

    @Test
    public void testLoadSave()
    {
        GCodeMacro m = new GCodeMacro("G92");
        String def = m.getDefinition();
        System.out.println("Definition: " + def);
        Macro m2 = GCodeMacro.getMacroFromDefinition(def);
        String def2 = m2.getDefinition();
        System.out.println("Definition2: " + def);
        assertEquals(def, def2);
    }

}
