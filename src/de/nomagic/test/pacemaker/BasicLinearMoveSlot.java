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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Tool;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class BasicLinearMoveSlot extends Slot
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final int NUM_MAX_AXIS = 16;

    private int decelerationSteps;
    private int nominalSpeed;
    private int endSpeed;
    private int accelerationSteps;
    private int primaryAxis;
    private int[] StepsOn = new int[NUM_MAX_AXIS];

    private double Xmm;
    private double Ymm;
    private double Zmm;
    private double Emm;

    private int[] rawData = new int[300];
    private int rawDataLength;

    private boolean isHomingMove = true;
    private PrinterState curState;

    public BasicLinearMoveSlot(int[] data, PrinterState curState)
    {
    	this.curState = curState;
        typeDescription = "basic linear move";
        if(null != data)
        {
        	System.arraycopy( data, 0, rawData, 0, data.length );
        	rawDataLength = data.length;
            dataDescription = parseBasicLinearMove(data);
        }
    }
    
    private String getRawDataAsHexString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	for(int i = 0; i < rawDataLength; i++)
    	{
    		sb.append(String.format("%02x ", 0xff & rawData[i]));
    	}
    	sb.append(" ]");
    	return sb.toString();
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
        	isHomingMove = false;
        }
        else
        {
            // homing move
        	isHomingMove = true;
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
                res.append("(" + StepsOnAxis + " Steps on Axis " + i);
                if(pattern == (AxisDirection & pattern))
                {
                    res.append(" direction increasing)");
                    StepsOn[i] = StepsOnAxis;
                }
                else
                {
                    res.append(" direction decreasing)");
                    StepsOn[i] = 0 - StepsOnAxis;
                }
                if(true == isHomingMove)
                {
                	curState.homeAxis(i);
                }
                else
                {
                	curState.moveSteps(i, StepsOn[i]);
                }
                log.info(curState.getPosition());
            }
            // else this axis is not selected
        }
        return res.toString();
    }

    @Override
    public void validate()
    {
        if(nominalSpeed < endSpeed)
        {
            log.error("ERROR: nominal Speed lower than end Speed !"
                    + " (nominal = " + nominalSpeed + ", end speed = " + endSpeed + ")");
        	log.error("That happened on this move: " + getRawDataAsHexString());
        }
        if(Math.abs(StepsOn[primaryAxis]) < (Math.abs(accelerationSteps) + Math.abs(decelerationSteps)))
        {
            log.error("ERROR: Step Numbers wrong !"
                    + " (acceleration = " + accelerationSteps
                    + ", deceleration = " + decelerationSteps
                    + ", steps on primary Axis = " + StepsOn[primaryAxis] + ")");
        	log.error("That happened on this move: " + getRawDataAsHexString());
        }
        boolean hasMovement = false;
        for(int i = 0; i < NUM_MAX_AXIS; i++)
        {
        	int speed = curState.getSpeedOn(i);
        	if(0 != StepsOn[i])
        	{
        		hasMovement = true;
            	if(speed != nominalSpeed)
            	{
            		log.info("{} : changing speed from {} to {} in {} Steps !",
            				   i,                     speed, nominalSpeed, accelerationSteps);
            		if(speed < nominalSpeed)
            		{
            			// we accelerate
            			if(0 == accelerationSteps)
            			{
            	        	log.error("Acceleration in 0 Steps !");
            	        	log.error("That happened on this move: " + getRawDataAsHexString());
            			}
            		}
            		else if(speed > nominalSpeed)
            		{
            			// we decellerate
            			if(0 == decelerationSteps)
            			{
            	        	log.error("Decelleration in 0 Steps !");
            	        	log.error("That happened on this move: " + getRawDataAsHexString());
            			}
            		}
            	}
        	}
        	if(speed != nominalSpeed)
        	{
        		boolean lastDirectionwasForward = curState.getDirectionisForward(i);
        		if(true == lastDirectionwasForward)
        		{
        			if(0 < StepsOn[i])
        			{
        				// no direction change
        			}
        			else
        			{
        				log.info("{} : changing direction to backwards!", i);
        				curState.setDirectionIsForward(i, false);
        				if(speed != 0)
        				{
        					log.info("{} : jerk = {}", i, speed);
        				}
        			}
        		}
        		else
        		{
        			if(0 < StepsOn[i])
        			{
        				log.info("{} : changing direction to backwards!", i);
        				curState.setDirectionIsForward(i, true);
        				if(speed != 0)
        				{
        					log.info("{} : jerk = {}", i, speed);
        				}
        			}
        			else
        			{
        				// no direction change
        			}
        		}
        	}
        	if(endSpeed != nominalSpeed)
        	{
        		log.info("{} : changing speed from {} to {} in {} Steps !",
        				  i,                     nominalSpeed, endSpeed, decelerationSteps);
        		if(endSpeed > nominalSpeed)
        		{
        			// we accelerate
        			if(0 == accelerationSteps)
        			{
        	        	log.error("Acceleration in 0 Steps !");
        	        	log.error("That happened on this move: " + getRawDataAsHexString());
        			}
        		}
        		else if(endSpeed < nominalSpeed)
        		{
        			// we decellerate
        			if(0 == decelerationSteps)
        			{
        	        	log.error("Decelleration in 0 Steps !");
        	        	log.error("That happened on this move: " + getRawDataAsHexString());
        			}
        		}
        	}
        	curState.setSpeed(i, endSpeed);
        }
        if(false == hasMovement)
        {
        	log.error("Move order has no move data !");
        	log.error("That happened on this move: " + getRawDataAsHexString());
        }
        // check that travel speed is not 0 !
        if(0 == nominalSpeed)
        {
        	log.error("Nominal Speed is 0 !");
        	log.error("That happened on this move: " + getRawDataAsHexString());
        }
        // TODO add further checks
    }

	public String getCartesianMove(PrinterState curState)
	{
		Xmm = StepsOn[0] / curState.getStepsPerMmFor(0);
		Ymm = StepsOn[1] / curState.getStepsPerMmFor(1);
		Zmm = StepsOn[2] / curState.getStepsPerMmFor(2);
		Emm = StepsOn[3] / curState.getStepsPerMmFor(3);

		log.info("Move X = " + Xmm + "mm, Y = " + Ymm + "mm, Z = " + Zmm + "mm, E = " + Emm + "mm");


		StringBuilder sb = new StringBuilder();
		if(true == isHomingMove)
		{
			sb.append("Homing: ");
		}
		sb.append("Move X = ");
		sb.append(Xmm);
		sb.append("mm, Y = ");
		sb.append(Ymm);
		sb.append("mm, Z = ");
		sb.append(Zmm);
		sb.append("mm, E = ");
		sb.append(Emm);
		sb.append("mm\n");

		sb.append("decelerationSteps : " + decelerationSteps + "\n");
		sb.append("nominalSpeed : " + nominalSpeed + "\n");
		sb.append("endSpeed : " + endSpeed + "\n");
		sb.append("accelerationSteps : " + accelerationSteps + "\n");
		sb.append("primaryAxis : " + primaryAxis + "\n");
		sb.append("StepsOn[0] (X) : " + StepsOn[0] + "\n");
		sb.append("StepsOn[1] (Y) : " + StepsOn[1] + "\n");
		sb.append("StepsOn[2] (Z) : " + StepsOn[2] + "\n");
		sb.append("StepsOn[3] (E) : " + StepsOn[3] + "\n");
		sb.append("Length of raw data : " + rawDataLength + "\n");
		sb.append(Tool.fromByteBufferToHexString(rawData, rawDataLength, 0));
		return sb.toString();
	}

}
