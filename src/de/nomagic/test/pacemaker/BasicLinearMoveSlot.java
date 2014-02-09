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
package de.nomagic.test.pacemaker;


/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class BasicLinearMoveSlot extends Slot
{
    private int decelerationSteps;
    private int nominalSpeed;
    private int endSpeed;
    private int accelerationSteps;
    private int primaryAxis;
    private int StepsOnPrimaryAxis;

    public BasicLinearMoveSlot(int[] data)
    {
        typeDescription = "basic linear move";
        dataDescription = parseBasicLinearMove(data);
    }

    private String parseBasicLinearMove(int[] data)
    {
        final StringBuffer res = new StringBuffer();
        boolean twoByteAxisFormat;
        if(0 == (0x80 & data[0]))
        {
            twoByteAxisFormat = false;
        }
        else
        {
            twoByteAxisFormat = true;
        }
        int AxisSelection;
        int nextByte;
        if(false == twoByteAxisFormat)
        {
            AxisSelection = (0x7f & data[0]);
            nextByte = 1;
        }
        else
        {
            AxisSelection = (0x7f & data[0])<<8 + (0xff & data[1]);
            nextByte = 2;
        }
        boolean twoByteStepCount;
        if(0 == (0x80 & data[nextByte]))
        {
            twoByteStepCount = false;
        }
        else
        {
            twoByteStepCount = true;
        }
        int AxisDirection;
        if(false == twoByteAxisFormat)
        {
            AxisDirection = (0x7f & data[nextByte]);
            nextByte = nextByte + 1;
        }
        else
        {
            AxisDirection = (0x7f & data[nextByte])<<8 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        primaryAxis = (0x0f & data[nextByte]);
        res.append(" primaryAxis=" + primaryAxis);
        if(0 == (0x10 & data[nextByte]))
        {
            // normal move
        }
        else
        {
            // homing move
            res.append(" homing");
        }
        nextByte++;
        nominalSpeed = (0xff & data[nextByte]);
        res.append(" nominalSpeed=" + nominalSpeed);
        nextByte++;
        endSpeed = (0xff & data[nextByte]);
        res.append(" endSpeed=" + endSpeed);
        nextByte++;
        if(true == twoByteStepCount)
        {
            accelerationSteps = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        else
        {
            accelerationSteps = (0xff & data[nextByte]);
            nextByte ++;
        }
        res.append(" accelSteps=" + accelerationSteps);

        if(true == twoByteStepCount)
        {
            decelerationSteps = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
            nextByte = nextByte + 2;
        }
        else
        {
            decelerationSteps = (0xff & data[nextByte]);
            nextByte ++;
        }
        res.append(" decelSteps=" + decelerationSteps);
        for(int i = 0; i < 16; i++)
        {
            final int pattern = 0x1<<i;
            if(pattern == (AxisSelection & pattern))
            {
                int StepsOnAxis;
                if(true == twoByteStepCount)
                {
                    StepsOnAxis = (0xff & data[nextByte])*256 + (0xff & data[nextByte + 1]);
                    nextByte = nextByte + 2;
                }
                else
                {
                    StepsOnAxis = (0xff & data[nextByte]);
                    nextByte ++;
                }
                if(primaryAxis == i)
                {
                    StepsOnPrimaryAxis = StepsOnAxis;
                }
                res.append("(" + StepsOnAxis + " Steps on Axis " + i);
                if(pattern == (AxisDirection & pattern))
                {
                    res.append(" direction increasing)");
                }
                else
                {
                    res.append(" direction decreasing)");
                }
            }
            // else this axis is not selected
        }
        return res.toString();
    }

    public void validate()
    {
        if(nominalSpeed > endSpeed)
        {
            System.err.println("ERROR: nominal Speed lower than end Speed !"
                    + " (nominal = " + nominalSpeed + ", end speed = " + endSpeed + ")");
        }
        if(StepsOnPrimaryAxis < (accelerationSteps + decelerationSteps))
        {
            System.err.println("ERROR: Step Numbers wrong !"
                    + " (acceleration = " + accelerationSteps
                    + ", deceleration = " + decelerationSteps
                    + ", steps on primary Axis = " + StepsOnPrimaryAxis + ")");
        }
        // TOD add further checks
    }

}
