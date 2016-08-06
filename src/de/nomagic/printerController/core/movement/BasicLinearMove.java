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
package de.nomagic.printerController.core.movement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.pacemaker.Protocol;

public class BasicLinearMove
{

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	private int maxSteps = 0;
	private boolean isHomingMove = false;
	private int TravelSpeedFraction = 0;
	private int EndSpeedFraction = 0;
	private int activeSteppersMap = 0;
	private int directionsMap = 0;
	private int primaryAxis = 0;
	private int accelerationSteps = 0;
	private int decellerationsteps = 0;
	private HashMap<Integer, Integer> StepsOnAxis = new HashMap<Integer, Integer>();
	
	
	private final int id;
    private byte[] movementCommand = null;
    
    public BasicLinearMove(int id)
    {
    	this.id = id;
    }
    
    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
        Iterator<Entry<Integer, Integer>> it = StepsOnAxis.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
            Integer axis = pair.getKey();
            Integer steps = pair.getValue();
            sb.append("(" + steps + " steps on Stepper " + axis + ")");
        }
    	
		return "BasicLinearMove " + sb.toString() + " [maxSteps=" + maxSteps + ", isHomingMove=" + isHomingMove
				+ ", TravelSpeedFraction=" + TravelSpeedFraction + ", EndSpeedFraction=" + EndSpeedFraction
				+ ", activeSteppersMap=" + activeSteppersMap + ", directionsMap=" + directionsMap + ", primaryAxis="
				+ primaryAxis + ", accelerationSteps=" + accelerationSteps + ", decellerationsteps="
				+ decellerationsteps + ", id=" + id + ", movementCommand="
				+ Arrays.toString(movementCommand) + "]";
	}



	public int getId()
    {
    	return id;
    }

    public void addAxis(int stepper, int steps)
    {
    	if(0 > steps)
    	{
    		// inverted
    		directionsMap = directionsMap | (1<<stepper);
    		steps = Math.abs(steps);
    	}
    	if(0 == steps)
    	{
    		// We don't need an Axis with no steps on it.
    		return;
    	}
    	if(maxSteps < steps)
    	{
    		primaryAxis = stepper;
    		maxSteps = steps;
    	}
    	StepsOnAxis.put(stepper, steps);
    	activeSteppersMap = activeSteppersMap | (1<<stepper);
    }
    
    public int getStepsOnStepper(int stepper)
    {
    	Integer Steps = StepsOnAxis.get(stepper);
    	if(null == Steps)
    	{
    		return 0;
    	}
    	else
    	{
    		return Steps.intValue();
    	}
    }
    
    public void setHoming(boolean isHoming)
    {
    	isHomingMove = isHoming;
    }
    
    public void setTravelSpeedFraction(int TravelSpeedFraction)
	{
	this.TravelSpeedFraction = TravelSpeedFraction;
	}
    
    public void setEndSpeedFraction(int EndSpeedFraction)
	{
    	this.EndSpeedFraction = EndSpeedFraction;
	}
    
    public void setAccelerationSteps(int accelerationSteps)
    {
    	this.accelerationSteps = accelerationSteps;
	}
    
    public void setDecellerationSteps(int decellerationsteps)
    {
    	this.decellerationsteps = decellerationsteps;
    }
    
    public  byte[] getMoveData()
    {
        // Prepare data
        int steppsStart;
        if(StepsOnAxis.size() < 7)
        {
            // 1 byte Axis selection mode
            if(255 > maxSteps)
            {
                fillTopPartForUpTo8Axis(1);
            }
            else
            {
                fillTopPartForUpTo8Axis(2);
            }
            steppsStart = 7;
        }
        else if(StepsOnAxis.size() < 15)
        {
            // 2 byte Axis selection mode
            if(255 > maxSteps)
            {
                fillTopPartForUpTo16Axis(1);
            }
            else
            {
                fillTopPartForUpTo16Axis(2);
            }
            steppsStart = 9;
        }
        else
        {
            log.error("Too Many Steppers - Can only handle 15 !");
            return null;
        }
        // Add Steps
        if(255 > maxSteps)
        {
            fillBottomPartUsingOneByteForSteps(steppsStart);
        }
        else
        {
            fillBottomPartUsingTwoByteForSteps(steppsStart);
        }
    	return movementCommand;
    }
    
    private void fillTopPartForUpTo8Axis(int BytesPerStep)
    {
        movementCommand = new byte[7 + (BytesPerStep * (2 + StepsOnAxis.size()))];
        movementCommand[0] = (byte)(0xff & movementCommand.length - 1); // Length
        movementCommand[1] = Protocol.MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE; // Type
        // active Steppers
        movementCommand[2] = (byte)activeSteppersMap;
        // Byte per steps
        if(1 == BytesPerStep)
        {
            movementCommand[3] = 0;
        }
        else
        {
            movementCommand[3] = (byte) 0x80;
        }
        // directions
        movementCommand[3] =  (byte)(movementCommand[3] | (0x7f & directionsMap));
        // Homing
        if(true == isHomingMove)
        {
            movementCommand[4] = 0x10;
        }
        // Primary Axis
        movementCommand[4] =(byte)(movementCommand[4] | (0x0f & primaryAxis));
        // Nominal Speed
        movementCommand[5] = (byte)(0xff & TravelSpeedFraction);
        // end Speed
        movementCommand[6] = (byte)(0xff & EndSpeedFraction);
    }

    private void fillTopPartForUpTo16Axis(int BytesPerStep)
    {
        movementCommand = new byte[9 + (BytesPerStep * (2 + StepsOnAxis.size()))];
        movementCommand[0] = (byte)(0xff & movementCommand.length - 1); // Length
        movementCommand[1] = Protocol.MOVEMENT_BLOCK_TYPE_BASIC_LINEAR_MOVE; // Type
        // Active Steppers
        final int ActiveSteppersMap =  activeSteppersMap;
        movementCommand[3] = (byte)(0xff & ActiveSteppersMap);
        movementCommand[2] = (byte)(0x80 | (0x7f & (ActiveSteppersMap >> 8)));
        // Byte per steps
        if(1 == BytesPerStep)
        {
            movementCommand[4] = 0;
        }
        else
        {
            movementCommand[4] = (byte) 0x80;
        }
        // directions
        final int DirectionMap = directionsMap;
        movementCommand[5] =  (byte)(0xff & DirectionMap);
        movementCommand[4] =  (byte)(movementCommand[4] | (0x7f & (DirectionMap>>8)));
        // Homing
        if(true == isHomingMove)
        {
            movementCommand[6] = 0x10;
        }
        // Primary Axis
        movementCommand[6] =(byte)(movementCommand[6] | (0x0f & (primaryAxis + 1))); // TODO
        // Nominal Speed
        movementCommand[7] = (byte)(0xff & TravelSpeedFraction);
        // end Speed
        movementCommand[8] = (byte)(0xff & EndSpeedFraction);
    }

    private void fillBottomPartUsingOneByteForSteps(int offset)
    {
        movementCommand[offset    ] = (byte)(0xff & accelerationSteps);
        movementCommand[offset + 1] = (byte)(0xff & decellerationsteps);
        final int numStepperToGo = StepsOnAxis.size();
        int stepperfound = 0;
        for(int i = 0; i < 16; i++)
        {
        	Integer steps = StepsOnAxis.get(i);
        	if(null == steps)
        	{
        		continue;
        	}
            if(0 != steps)
            {
                movementCommand[offset + 2 + stepperfound] = (byte)(0xff & steps);
                stepperfound ++;
                if(stepperfound == numStepperToGo)
                {
                    break;
                }
            }
        }
    }

    private void fillBottomPartUsingTwoByteForSteps(int offset)
    {
        movementCommand[offset    ] = (byte)(0xff & (accelerationSteps>>8));
        movementCommand[offset + 1] = (byte)(0xff & accelerationSteps);
        movementCommand[offset + 2] = (byte)(0xff & (decellerationsteps>>8));
        movementCommand[offset + 3] = (byte)(0xff & decellerationsteps);
        final int numStepperToGo = StepsOnAxis.size();
        int stepperfound = 0;
        // highest Stepper Number can be 0 -> then we still have one stepper
        for(int i = 0; i < 16; i++)
        {
        	Integer steps = StepsOnAxis.get(i);
        	if(null == steps)
        	{
        		continue;
        	}
            if(0 != steps)
            {
                movementCommand[offset + 4 + stepperfound*2] = (byte)(0xff & (steps>>8));
                movementCommand[offset + 5 + stepperfound*2] = (byte)(0xff & steps);
                stepperfound ++;
                if(stepperfound == numStepperToGo)
                {
                    break;
                }
            }
        }
    }
    
    
}

