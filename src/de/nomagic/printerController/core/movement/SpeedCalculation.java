package de.nomagic.printerController.core.movement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedCalculation 
{
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private boolean valid;
	private int endSpeedFraction;
	private int travelSpeedFraction;
	private int acceleartionSteps;
	private int decceleartionSteps;
	private int travelSteps;
	
	public SpeedCalculation(CartesianMove firstMove, CartesianMove nextMove)
	{
		// simplest version (start Speed = end Speed = 0; No speed limit);
		firstMove.setEndSpeedMms(0);
		endSpeedFraction = 0;
		travelSpeedFraction = 1;
		travelSteps = 0;
		int steps = firstMove.getStepsOnStepper(firstMove.getPrimaryStepper());
		if(0 == steps)
		{
			log.error("No Steps on Primary Axis !");
		}
		acceleartionSteps = steps/2;
		decceleartionSteps = acceleartionSteps;
		travelSteps = steps - acceleartionSteps - decceleartionSteps;
		// set startSpeed of second move to end speed of first move.
		if(null != nextMove)
		{
			nextMove.setStartSpeedMms(0);
		}
		valid = true;
	}
	
	public boolean isValid()
	{
		return valid;
	}

	public int getEndSpeedFraction() 
	{
		return endSpeedFraction;
	}

	public int getTravelSpeedFraction() 
	{
		return travelSpeedFraction;
	}

	public int getAccelerationSteps() 
	{
		return acceleartionSteps;
	}

	public int getDecelerationSteps() 
	{
		return decceleartionSteps;
	}

	public int getTravelSteps() 
	{
		return travelSteps;
	}
	
	
}
