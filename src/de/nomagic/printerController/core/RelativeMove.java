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
package de.nomagic.printerController.core;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class RelativeMove
{
    private final double x;
    private final double y;
    private final double z;
    private final double e;
    private final double f;

    public RelativeMove(final double x,
                        final double y,
                        final double z,
                        final double e,
                        final double f)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.e = e;
        this.f = f;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public double getE()
    {
        return e;
    }

    public double getF()
    {
        return f;
    }
}
