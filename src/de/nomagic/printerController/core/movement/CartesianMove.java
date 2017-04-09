package de.nomagic.printerController.core.movement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Axis_enum;
import de.nomagic.printerController.core.devices.Stepper;
import de.nomagic.printerController.pacemaker.Protocol;

public class CartesianMove
{
	/** 16 bit for steps */
    private final int MAX_POSSIBLE_STEPPS_PER_BASICLINEARMOVE  = 65535;
    /** everything shorter than this will be assumed to be 0 */
    public static final double MIN_MOVEMENT_DISTANCE_MM_SECOND = 0.00001;

    public static final double MOVEMENT_SPEED_TOLERANCE_MM_SECOND = 0.0001;
    /** if the axis has steps the speed may not be 0. So this is the speed is will have at least */
    public static final double MIN_MOVEMENT_SPEED_MM_SECOND = 0.1;

    private static int nextId = 0; // singleton

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final int MaxPossibleClientSpeedInStepsPerSecond;

    private double feedrateMmPerSecond = MIN_MOVEMENT_SPEED_MM_SECOND;
    private boolean isHoming = false;
    private boolean hasMovement = false;
    private boolean hasEndSpeed = false;
    private double endSpeedMms = 0.0;
    private boolean hasStartSpeed = false;
    private double startSpeedMms = 0.0;
    private int primaryAxis = -1;
    private int numParts = 1;


    private HashMap<Axis_enum, Double> distancesMm = new HashMap<Axis_enum, Double>();

    private Vector<Integer> activeAxises = new Vector<Integer>();
    private HashMap<Integer, Integer> StepsOnAxis = new HashMap<Integer, Integer>();

    private HashMap<Integer, Boolean> AxisDirectionIncreasing = new HashMap<Integer, Boolean>();

    private int StepsOnPrimaryAxis = -1;


    private boolean hasCommand = false;

    private boolean Command_on = true;
    private Integer[] Command_switches;


    private int maxStepperNumber = -1;



    private int myId;


    private PrinterProperties printer;

    public CartesianMove(int MaxPossibleClientSpeedStepsPerSecond, PrinterProperties printer)
    {
        this.MaxPossibleClientSpeedInStepsPerSecond = MaxPossibleClientSpeedStepsPerSecond;
        this.printer = printer;
        myId = nextId;
        nextId++;
    }

    public void setFeedrateMmPerMinute(double feedrateMmPerMinute)
    {
        if(MIN_MOVEMENT_SPEED_MM_SECOND * 60 < feedrateMmPerMinute)
        {
            this.feedrateMmPerSecond = feedrateMmPerMinute/60;
        }
        // else ignore invalid Feedrate
    }

    public void setDistanceMm(Axis_enum axis, double distanceMm)
    {
        log.debug("ID{}: adding {} = {} mm", myId, axis, distanceMm);
        distancesMm.put(axis, distanceMm);
        if(MIN_MOVEMENT_DISTANCE_MM_SECOND < Math.abs(distanceMm))
        {
            hasMovement = true;
        }
    }

    public boolean hasMovement()
    {
    	return hasMovement;
    }

    public void setIsHoming(boolean b)
    {
        isHoming = b;
    }

    public void setStartSpeedMms(double theSpeedMms)
    {
        startSpeedMms = theSpeedMms;
        log.trace("ID{}: start speed set to {} mm/s", myId, theSpeedMms);
        hasStartSpeed = true;
    }

    public void setEndSpeedMms(double theSpeedMms)
    {
        endSpeedMms = theSpeedMms;
        log.trace("ID{}: end speed set to {} mm/s", myId, theSpeedMms);
        hasEndSpeed = true;
    }

    public boolean hasEndSpeedSet()
    {
        return hasEndSpeed;
    }

    public int getId()
    {
        return myId;
    }

    public void addEndStopOnOffCommand(boolean on, Integer[] switches)
    {
        hasCommand = true;
        Command_on = on;
        Command_switches = switches;
    }

	public boolean send(Protocol pro, CartesianMove nextMove)
	{
        if((null == pro))
        {
        	log.warn("No Protocol available to send the move!");
            return false;
        }
        if(true == hasMovement)
        {
        	// send movement
	        if(false == hasStartSpeed)
	        {
	        	log.trace("Tried to send Move without start Speed set!");
	        	startSpeedMms = 0;
	        	hasStartSpeed = true;
	        }
			// calculate speeds and accelerations
			// convert into BasicLinearMoves
			BasicLinearMove[] basicMoves = getMoveDataAsBasicLinearMove(nextMove);
			// send BasicLinearMoves
	        if(false == pro.addBasicLinearMove(basicMoves))
	        {
	        	log.error("Failed to send the Basic Linear Move !");
	            return false;
	        }
		}
        else
        {
        	log.trace("No movement to send available in move {}", myId);
        }
		// send switch commands
        if(true == hasCommand)
        {
            return pro.endStopOnOff(Command_on, Command_switches);
        }
        return true;
	}

    private BasicLinearMove[] getMoveDataAsBasicLinearMove(CartesianMove nextMove)
    {
    	// convert distances in mm to distances in steps
    	convertToSteps();
    	// check if this move has Steps
    	if(1 > StepsOnPrimaryAxis)
    	{
    		return null;
    	}
        // check if we need to break the move into many small moves
    	numParts = (StepsOnPrimaryAxis/MAX_POSSIBLE_STEPPS_PER_BASICLINEARMOVE) + 1; // 0..65534 = 1; 65535.. 131069 =2; ...
    	// create all the moves
    	BasicLinearMove[] moves = createMovesWithSteps();
    	moves = calculateSpeedsFor(moves, nextMove);
    	return moves;
    }

    private BasicLinearMove[] calculateSpeedsFor(BasicLinearMove[] moves, CartesianMove nextMove)
    {
    	SpeedCalculation calc = new SpeedCalculation(this, nextMove);
    	if(false == calc.isValid())
    	{
    		return null;
    	}
    	int endSpeedFraction = calc.getEndSpeedFraction();
    	log.trace("endSpeedFraction = {}", endSpeedFraction);
    	int travelSpeedFraction = calc.getTravelSpeedFraction();
    	log.trace("travelSpeedFraction = {}", travelSpeedFraction);
    	int accelerationSteps = calc.getAccelerationSteps();
    	log.trace("accelearionSteps = {}", accelerationSteps);
    	int decelerationSteps = calc.getDecelerationSteps();
    	log.trace("decelerationSteps = {}", decelerationSteps);
		int StepsOnPrimaryAxis = moves[0].getStepsOnStepper(primaryAxis);
    	log.trace("StepsOnPrimaryAxis = {}", StepsOnPrimaryAxis);
		int travelSteps = calc.getTravelSteps();
    	log.trace("travelSteps = {}", travelSteps);
    	log.trace("numParts = {}", numParts);
		for(int i = 0; i < numParts; i++)
		{
			moves[i].setHoming(isHoming);
		}

		int primaryStepsPerMove = StepsOnPrimaryAxis/numParts;
		// acceleration steps
		int AllaccelearionSteps = accelerationSteps;
		int AlldecelerationSteps = decelerationSteps;
        int startSpeedFraction =  (int)((startSpeedMms * printer.getStepsPerMm(primaryAxis) * 255)
                / MaxPossibleClientSpeedInStepsPerSecond);
		double fracDiffAccel = travelSpeedFraction - startSpeedFraction;
		double fracDiffDeccel = travelSpeedFraction - endSpeedFraction;
		int curMove = 0;
		do
		{
			if(accelerationSteps < primaryStepsPerMove)
			{
				moves[curMove].setAccelerationSteps(accelerationSteps);
				moves[curMove].setTravelSpeedFraction(travelSpeedFraction);
				int remainingSteps = primaryStepsPerMove - accelerationSteps;
				if(travelSteps > remainingSteps)
				{
					travelSteps = travelSteps - remainingSteps;
				}
				else
				{
					remainingSteps = remainingSteps - travelSteps;
					travelSteps = 0;
				}
				if(0 < remainingSteps)
				{
					moves[curMove].setDecellerationSteps(remainingSteps);
					decelerationSteps = decelerationSteps - remainingSteps;
					if(0 < decelerationSteps)
					{
						int remainingSpeedDown = (int)Math.round((decelerationSteps/AlldecelerationSteps) * fracDiffDeccel);
						moves[curMove].setEndSpeedFraction(endSpeedFraction + remainingSpeedDown);
					}
					else
					{
						moves[curMove].setEndSpeedFraction(endSpeedFraction);
					}
				}
				accelerationSteps = 0;
			}
			else
			{
				moves[curMove].setAccelerationSteps(primaryStepsPerMove);
				accelerationSteps = accelerationSteps - primaryStepsPerMove;
				if(0 < accelerationSteps)
				{
					int remainingSpeedUp = (int)Math.round((accelerationSteps/AllaccelearionSteps) * fracDiffAccel);
					moves[curMove].setTravelSpeedFraction(travelSpeedFraction - remainingSpeedUp);
				}
				else
				{
					moves[curMove].setTravelSpeedFraction(travelSpeedFraction);
				}
			}
			curMove++;
		} while((0 < accelerationSteps) && (curMove < numParts));

		// travel Steps
		while((0 < travelSteps) && (curMove < numParts))
		{
			if(travelSteps > primaryStepsPerMove)
			{
				travelSteps = travelSteps - primaryStepsPerMove;
			}
			else
			{
				int remainingSteps = primaryStepsPerMove - travelSteps;
				travelSteps = 0;
				moves[curMove].setDecellerationSteps(remainingSteps);
				decelerationSteps = decelerationSteps - remainingSteps;
				if(0 < decelerationSteps)
				{
					int remainingSpeedDown = (int)Math.round((decelerationSteps/AlldecelerationSteps) * fracDiffDeccel);
					moves[curMove].setEndSpeedFraction(endSpeedFraction + remainingSpeedDown);
				}
				else
				{
					moves[curMove].setEndSpeedFraction(endSpeedFraction);
				}
			}
			curMove++;
		}

		// decel steps
		while((0 < decelerationSteps) && (curMove < numParts))
		{
			moves[curMove].setDecellerationSteps(primaryStepsPerMove);
			decelerationSteps = decelerationSteps - primaryStepsPerMove;
			if(0 < decelerationSteps)
			{
				int remainingSpeedDown = (int)Math.round((decelerationSteps/AlldecelerationSteps) * fracDiffDeccel);
				moves[curMove].setEndSpeedFraction(endSpeedFraction + remainingSpeedDown);
			}
			else
			{
				moves[curMove].setEndSpeedFraction(endSpeedFraction);
			}
			curMove++;
		}

		return moves;
	}

	private BasicLinearMove[] createMovesWithSteps()
    {
    	BasicLinearMove[] res = new BasicLinearMove[numParts];
    	for(int i = 0; i < numParts; i++)
    	{
    		res[i] = new BasicLinearMove(myId);
    	}
        Iterator<Entry<Integer, Integer>> it = StepsOnAxis.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
            Integer stepper = pair.getKey();
            Integer steps = pair.getValue();
            if(numParts > 1)
            {
	            Integer StepsPerPart = steps/numParts;
	            for(int i = 0; i < numParts -1; i++)
	            {
	            	res[i].addAxis(stepper, StepsPerPart);
	            }
	            // last move
	            res[numParts -1].addAxis(stepper, steps - (StepsPerPart * (numParts - 1)));
            }
            else
            {
            	res[0].addAxis(stepper, steps);
            }
        }
        return res;
    }

    private void convertToSteps()
    {
    	maxStepperNumber = -1;

        Iterator<Entry<Axis_enum, Double>> it = distancesMm.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Axis_enum, Double> pair = (Map.Entry<Axis_enum, Double>)it.next();
            Axis_enum axis = pair.getKey();
	    	// rounding error
            final double exactSteps = printer.getGathereddRoundingErrorOn(axis)
            		                  + (distancesMm.get(pair.getKey()) * printer.getStepsPerMm(printer.getStepperNumberFor(axis)));
            int steps = (int) Math.round(exactSteps);
	        log.debug("ID{}: exact Steps = {}, got rounded to {}", myId, exactSteps, steps);
	        final Double difference = exactSteps - steps;
	        printer.setGathereddRoundingErrorOn(axis, difference);
	        // stepper number
	        int stepperNumber = printer.getStepperNumberFor(axis);
	        log.debug("ID{}: adding Stepper {} for Axis {}", myId, stepperNumber, axis);
	        activeAxises.add(stepperNumber);
	        // max Stepper Number
	        if(maxStepperNumber < stepperNumber)
	        {
	        	maxStepperNumber = stepperNumber;
	        }
	        // steps
	        addSteppersSteps(stepperNumber, steps);
        }
    }

    private void addSteppersSteps(int stepperNumber, int steps)
    {
        if(false == printer.isDirectionInverted(stepperNumber))
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(stepperNumber, true);
            }
            else
            {
                AxisDirectionIncreasing.put(stepperNumber, false);
            }
        }
        else
        {
            if(0 < steps)
            {
                AxisDirectionIncreasing.put(stepperNumber, false);
            }
            else
            {
                AxisDirectionIncreasing.put(stepperNumber, true);
            }
        }
        log.trace("adding {} steps to Stepper {}", steps, stepperNumber);
        StepsOnAxis.put(stepperNumber, steps);
        if(StepsOnPrimaryAxis < Math.abs(steps))
        {
        	StepsOnPrimaryAxis = Math.abs(steps);
        	primaryAxis = stepperNumber;
        }
    }

    public int getPrimaryStepper()
    {
    	return primaryAxis;
    }

    public int getStepsOnStepper(int stepper)
    {
    	Integer res = StepsOnAxis.get(stepper);
    	if(null == res)
    	{
    		return 0;
    	}
    	else
    	{
    		log.trace("getting {} steps on Stepper {}", res, stepper);
    		return res.intValue();
    	}
    }

}

