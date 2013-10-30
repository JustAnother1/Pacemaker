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

import de.nomagic.printerController.Axis_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class RelativeMove
{
    private double x;
    private boolean hasX = false;
    private double y;
    private boolean hasY = false;
    private double z;
    private boolean hasZ = false;
    private double e;
    private boolean hasE = false;
    private double f;
    private boolean hasF = false;

    public RelativeMove()
    {
    }

    public boolean has(Axis_enum axis)
    {
        switch(axis)
        {
        case X: return hasX;
        case Y: return hasY;
        case Z: return hasZ;
        case E: return hasE;
        case F: return hasF;
        default: return false;
        }
    }

    public double get(Axis_enum axis)
    {
        switch(axis)
        {
        case X: return x;
        case Y: return y;
        case Z: return z;
        case E: return e;
        case F: return f;
        default: return 0.0;
        }
    }

    public void setX(double x)
    {
        this.x = x;
        hasX = true;
    }

    public void setY(double y)
    {
        this.y = y;
        hasY = true;
    }

    public void setZ(double z)
    {
        this.z = z;
        hasZ = true;
    }

    public void setE(double e)
    {
        this.e = e;
        hasE = true;
    }

    public void setF(double f)
    {
        this.f = f;
        hasF = true;
    }

}
