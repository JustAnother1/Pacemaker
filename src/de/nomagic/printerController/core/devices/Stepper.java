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
package de.nomagic.printerController.core.devices;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Stepper
{
    public final static int MAX_STEPS_PER_PART = 65535;

    private boolean hasDistance = false;
    private double distanceInMillimeters = 0.0;

    private Vector<Integer> StepperNumberVect = new Vector<Integer>();
    private Vector<Integer> ProtocolIndexVect = new Vector<Integer>();
    private Vector<Integer> MaxAccellerationVect = new Vector<Integer>();
    private Vector<Boolean> DirectionInvertedVect = new Vector<Boolean>();
    private Vector<Double> StepsPerMillimeterVect = new Vector<Double>();
    private HashMap<Integer, Double> RoundingErrorVect = new HashMap<Integer, Double>();

    private double LastSpeedInMillimeterperSecond = 0.0;

    public Stepper()
    {
    }

    public void addStepper(int StepperNumber ,
                           int ProtocolIdx,
                           int maxAccelleration,
                           boolean DirectionInverted,
                           double StepsPerMillimeter)
    {
        StepperNumberVect.add(StepperNumber);
        ProtocolIndexVect.add(ProtocolIdx);
        MaxAccellerationVect.add(maxAccelleration);
        DirectionInvertedVect.add(DirectionInverted);
        StepsPerMillimeterVect.add(StepsPerMillimeter);
    }

    public void setStepsPerMillimeter(Double steps)
    {

    }

    public void clearMove()
    {
        hasDistance = false;
        distanceInMillimeters = 0.0;
    }

    public void addMove(double distanceInMillimeters)
    {
        this.distanceInMillimeters = distanceInMillimeters;
        hasDistance = true;
    }


    private int getSteps(int StepperIndex)
    {
        if(true == hasDistance)
        {
            double dSteps = (distanceInMillimeters * StepsPerMillimeterVect.get(StepperIndex));
            Double RoundingError = RoundingErrorVect.get(StepperIndex);
            if(null != RoundingError)
            {
                dSteps = dSteps + RoundingError;
            }
            int iSteps = (int)dSteps;
            double SteppRoundingError = (dSteps - iSteps);
            RoundingErrorVect.put(StepperIndex, SteppRoundingError);
            return iSteps;
        }
        else
        {
            return 0;
        }
    }


    public Vector<Integer> addActiveProtocolIndexes(Vector<Integer> prots)
    {
        if(true == hasDistance)
        {
            for(int i = 0; i < ProtocolIndexVect.size(); i++)
            {
                int pro = ProtocolIndexVect.get(i);
                if(false == prots.contains(pro))
                {
                    prots.add(pro);
                }
            }
        }
        return prots;
    }

    public double getLastSpeedInMillimeterperSecond()
    {
        // TODO Auto-generated method stub
        return LastSpeedInMillimeterperSecond;
    }

    public int getMinimumPossiblePartialMoves(int protocol)
    {
        if(false == hasDistance)
        {
            // no movement on axis -> no parts needed
            return 0;
        }
        else
        {
            if(false == ProtocolIndexVect.contains(protocol))
            {
                // Movement not on the requested Protocol -> no parts needed
                return 0;
            }
            else
            {
                // Find max number of needed parts
                int parts = 0;
                for(int i = 0; i < ProtocolIndexVect.size(); i++)
                {
                    int proIdx = ProtocolIndexVect.get(i);
                    if(proIdx == protocol)
                    {
                        int neededParts = getSteps(i) / MAX_STEPS_PER_PART + 1; // +1 is for Integer division rounding (10/50 = 0)
                        if (parts < neededParts)
                        {
                            parts = neededParts;
                        }
                    }
                }
                return parts;
            }
        }
    }

}
