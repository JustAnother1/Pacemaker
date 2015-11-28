package de.nomagic.test.pacemaker;

public class PrinterState 
{
	private int[] speedOn = {0, 0, 0, 0};
	
    // TODO cfg is hardwired
	
	public PrinterState()
	{
		
	}

	public double getStepsPerMmFor(int i) 
	{
		switch(i)
		{
		case 0: return 80;
		case 1: return 80;
		case 2: return 4000;
		case 3: return 157.5; 
		default: return 1;
		}
	}

	public int getSpeedOn(int i) 
	{
		if((i > -1) && (i < 4))
		{
			return speedOn[i];
		}
		else
		{
			return 0;
		}
	}

	public void setSpeed(int i, int speed) 
	{
		if((i > -1) && (i < 4))
		{
			speedOn[i] = speed;
		}
		else
		{
		}
	}
	
	
}
