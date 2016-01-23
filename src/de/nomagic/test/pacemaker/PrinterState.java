package de.nomagic.test.pacemaker;

public class PrinterState
{
	private int[] speedOn = {0, 0, 0, 0};
	private boolean[] directionOn = {false, false, false, false};
	private boolean[] AxisIsHomed = {false, false, false, false};
	private double[] AxisPosition = {0.0,   0.0,   0.0,   0.0};
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

	public boolean getDirectionisForward(int i)
	{
		if((i > -1) && (i < 4))
		{
			return directionOn[i];
		}
		else
		{
			return false;
		}
	}

	public void setDirectionIsForward(int i, boolean b)
	{
		if((i > -1) && (i < 4))
		{
			directionOn[i] = b;
		}
		else
		{
		}
	}

	public String getPosition()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("X : ");
		if(true == AxisIsHomed[0])
		{
			sb.append("" + AxisPosition[0]);
		}
		else
		{
			sb.append("not homed");
		}
		sb.append("\n");

		sb.append("Y : ");
		if(true == AxisIsHomed[1])
		{
			sb.append("" + AxisPosition[1]);
		}
		else
		{
			sb.append("not homed");
		}
		sb.append("\n");

		sb.append("Z : ");
		if(true == AxisIsHomed[2])
		{
			sb.append("" + AxisPosition[2]);
		}
		else
		{
			sb.append("not homed");
		}
		sb.append("\n");

		sb.append("E : ");
		if(true == AxisIsHomed[3])
		{
			sb.append("" + AxisPosition[3]);
		}
		else
		{
			sb.append("not homed");
		}
		sb.append("\n");
		return sb.toString();
	}

	public void homeAxis(int i)
	{
		AxisIsHomed[i] = true;
		AxisPosition[i] = 0.0;
	}

	public void moveSteps(int i, int steps)
	{
		double distance = steps/getStepsPerMmFor(i);
		AxisPosition[i] += distance;
	}


}
