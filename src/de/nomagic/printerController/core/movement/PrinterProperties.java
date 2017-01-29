package de.nomagic.printerController.core.movement;

import java.util.HashMap;

import de.nomagic.printerController.Axis_enum;

public class PrinterProperties 
{
	private double[]  roundingErrors = new double[Axis_enum.size];
    private HashMap<Axis_enum, Integer> StepperNumberOfAxis = new HashMap<Axis_enum, Integer>();
    private HashMap<Integer, Double> StepsPerMmOnStepper = new HashMap<Integer, Double>();
    private HashMap<Integer, Boolean> isDirectionInvertedOnStepper = new HashMap<Integer, Boolean>();
    
	public PrinterProperties()
	{
		for(int i = 0; i < Axis_enum.size; i++)
		{
			roundingErrors[i] = 0.0;
		}
	}

	public void addStepperForAxis(Axis_enum axis, int Number)
	{
		StepperNumberOfAxis.put(axis, Number);
	}
	
	public int getStepperNumberFor(Axis_enum axis) 
	{
		Integer Number = StepperNumberOfAxis.get(axis);
		if(null == Number)
		{
			return -1;
		}
		else
		{
			return Number.intValue();
		}
	}

	public void setGathereddRoundingErrorOn(Axis_enum axis, double difference) 
	{
		roundingErrors[axis.ordinal()] = roundingErrors[axis.ordinal()] + difference;
	}

	public double getGathereddRoundingErrorOn(Axis_enum axis) 
	{
		return roundingErrors[axis.ordinal()];
	}

	public void setSteppsPerMmOn(int stepper, Double stepsPerMm)
	{
		StepsPerMmOnStepper.put(stepper, stepsPerMm);
	}
	
	public Double getStepsPerMm(int stepperNumber) 
	{
		return StepsPerMmOnStepper.get(stepperNumber);
	}

	public void setDirectionOnStepper(int stepper, boolean isInverted)
	{
		isDirectionInvertedOnStepper.put(stepper, isInverted);
	}
	
	public boolean isDirectionInverted(int stepperNumber) 
	{
		Boolean res = isDirectionInvertedOnStepper.get(stepperNumber);
		if(null == res)
		{
			return false;
		}
		else
		{
			return res.booleanValue();
		}
	}
	
}
