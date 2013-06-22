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
package de.nomagic.printerController.gcode;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class GCode
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final HashMap<Character,Double> words = new HashMap<Character,Double>();
    private boolean valid = true;

    public GCode(String line)
    {
        line = line.toUpperCase(); // case insensitive
        line = line.trim(); // whitespace is ignored
        char curWordType = ' ';
        StringBuffer curNumber = new StringBuffer();
        for(int i = 0; i < line.length(); i++)
        {
            final char c = line.charAt(i);
            switch(c)
            {
            // Numbers
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '.':
            case '+':
            case '-':
            case '#': // variable number 1 to 5399
                curNumber.append(c);
                break;

            case 'A': // A -axis
            case 'B': // B -axis
            case 'C': // C -axis
            case 'D': // tool radius compensation number
            case 'F': // feedrate
            case 'G': // General function
            case 'H': // Tool length offset index
            case 'I': // X axis offset for arcs or in G87
            case 'J': // Y axis offset for arcs or in G87
            case 'K': // Z axis offset for arcs or in G87
            case 'L': // number of repetitions (G10)
            case 'M': // Miscellaneous function
            case 'N': // Line number
            case 'P': // dwell time(G4) + G10
            case 'Q': // feed increment (G83)
            case 'R': // arc radius
            case 'S': // Spindle speed
            case 'T': // tool selection
            case 'X': // X -axis
            case 'Y': // Y -axis
            case 'Z': // Z -axis
                if(' ' == curWordType)
                {
                    // first Word -> nothing to do
                }
                else
                {
                    // start of next word detected
                    final String help = curNumber.toString();
                    Double d = 0.0;
                    try
                    {
                        d =  Double.parseDouble(help);
                    }
                    catch(final NullPointerException e)
                    {
                        valid = false;
                        log.error("No value given for Word {} !", curWordType);
                    }
                    catch(final NumberFormatException e)
                    {
                        valid = false;
                        log.error("Invalid value({}) given for Word {} !", help, curWordType);
                    }
                    words.put(curWordType,d);
                    curNumber = new StringBuffer();
                }
                curWordType = c;
                break;

            case '(': // comment start
                char commentedChar;
                do
                {
                    i++;
                    commentedChar = line.charAt(i);
                }
                while((commentedChar != ')') && (i < line.length()));
                break;

            // non standard:
            case ';':
                // comment until end of line -> we are done here
                break;

            // whitespace
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                // ignore whitespace
                break;

            case ')': // comment end -> may never happen on it's own
            default:
                valid = false;
                log.error("Invalid character({}) in G-Code Line: {} !", c, line);
                break;
            }
        }
    }

    public boolean hasWord(final Character wordType)
    {
        return words.containsKey(wordType);
    }

    public Double getWordValue(final Character word)
    {
        return words.get(word);
    }

    public boolean isEmpty()
    {
        return  words.isEmpty();
    }

    public boolean isValid()
    {
        return valid;
    }

}
