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

/** supported Functions of Fans.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public enum Fan_enum
{
    // The Number is the Value for the P Word in M106
    // with Extruder_0(1) the G-Code M106 P1 S255 will turn on the Fan on the Extruder_0 to full power.
    Printed_Part(0), Extruder_0(1), Extruder_1(2), Extruder_2(3);

    private final int index;

    Fan_enum(int idx)
    {
        this.index = idx;
    }

    public int getValue()
    {
        return index;
    }
}
