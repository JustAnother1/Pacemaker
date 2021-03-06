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

/** supported Functions of Heaters.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public enum Heater_enum
{
    Extruder_0(0), Extruder_1(1), Extruder_2(2), Print_Bed(3), Chamber(4);

    public static final int size = Heater_enum.values().length;

    private final int index;

    Heater_enum(int idx)
    {
        this.index = idx;
    }

    public int getValue()
    {
        return index;
    }
}
