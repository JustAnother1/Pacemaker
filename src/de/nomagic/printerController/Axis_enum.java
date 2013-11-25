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
package de.nomagic.printerController;

/** supported Axis in G-Code.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public enum Axis_enum
{
    X, Y, Z, E, F;

    public static final int size = Axis_enum.values().length;

    public char getChar()
    {
        switch(this)
        {
        case X: return 'x';
        case Y: return 'y';
        case Z: return 'z';
        case E: return 'e';
        case F: return 'f';
        default: return '?';
        }
    }
}
