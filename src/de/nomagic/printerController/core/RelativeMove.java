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

import java.io.Serializable;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.core.movement.XyzTable;

/** represents a movement relative to the last position.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class RelativeMove implements Serializable
{
    private static final long serialVersionUID = 1L;
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

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("RelativeMove [");
        if(true == hasX)
        {
            sb.append(" x=" + x);
        }
        if(true == hasY)
        {
            sb.append(" y=" + y);
        }
        if(true == hasZ)
        {
            sb.append(" z=" + z);
        }
        if(true == hasE)
        {
            sb.append(" e=" + e);
        }
        if(true == hasF)
        {
            sb.append(" f=" + f);
        }
        sb.append(" ]");
        return sb.toString();
    }

    public boolean has(Axis_enum axis)
    {
        switch(axis)
        {
        case X: return hasX;
        case Y: return hasY;
        case Z: return hasZ;
        case E: return hasE;
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
        default: return 0.0;
        }
    }

    public boolean hasFeedrate()
    {
        return hasF;
    }

    public double getFeedrate()
    {
        return f;
    }

    public void setX(double x)
    {
        this.x = x;
        if((x < -XyzTable.MIN_MOVEMENT_DISTANCE) || (x > XyzTable.MIN_MOVEMENT_DISTANCE))
        {
            hasX = true;
        }
        // else x was set to 0
    }

    public void setY(double y)
    {
        this.y = y;
        if((y < -XyzTable.MIN_MOVEMENT_DISTANCE) || (y > XyzTable.MIN_MOVEMENT_DISTANCE))
        {
            hasY = true;
        }
        // else y was set to 0
    }

    public void setZ(double z)
    {
        this.z = z;
        if((z < -XyzTable.MIN_MOVEMENT_DISTANCE) || (z > XyzTable.MIN_MOVEMENT_DISTANCE))
        {
            hasZ = true;
        }
        // else z was set to 0
    }

    public void setE(double e)
    {
        this.e = e;
        if((e < -XyzTable.MIN_MOVEMENT_DISTANCE) || (e > XyzTable.MIN_MOVEMENT_DISTANCE))
        {
            hasE = true;
        }
        // else e was set to 0
    }

    public void setF(double f)
    {
        this.f = f;
        if((f < -XyzTable.MIN_MOVEMENT_DISTANCE) || (f > XyzTable.MIN_MOVEMENT_DISTANCE))
        {
            hasF = true;
        }
        // else f was set to 0
    }

}
