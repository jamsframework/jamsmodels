/*
 * HydroNETControl.java
 * Created on 2. Juni 2006, 18:00
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */


/**
 * Description:  This Class manages a set of 2D points and provides a method to compute
 *				 a polynom as result of nonlinear regression via solution of a least
 *				 squares minimization problem. Most of the code, especially the classes
 *				 Matrix, Numeric and specialFunctions, comes from Brian Lewis,
 *				 url: http://www.mcs.kent.edu/~blewis/
 * Copyright:    Copyright (c) 2000
 * Company:      FSU Jena
 * @author:      Christian Fischer
 */

package org.unijena.hydronet;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.JAMS;
import org.unijena.j2k.*;

@JAMSComponentDescription(
        title="HydroNETControl",
        author="Christian Fischer",
        description="Context Component which controls optimization"
        )
	
public class HydroNETControl extends JAMSContext {
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Nitrongen Output Neuron"
            )
            public JAMSEntity NitrogenOutEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Cost Output Neuron"
            )
            public JAMSEntity CostOutEntity;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "smallest improvement which is accepted"
            )
            public JAMSDouble delta_min;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "learning rate"
            )
            public JAMSDouble learningrate;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "momentum"
            )
            public JAMSDouble momentum = new JAMSDouble(0.9);  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "largest accepted nitrogen value"
            )
            public JAMSDouble nitrogen_goal;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "largest accepted nitrogen value"
            )
            public JAMSDouble current_output;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "largest accepted nitrogen value"
            )
            public JAMSDouble current_iteration;
    
    private double errorNO,errorCost;   
    private double avgperformance=1000,performance;
    private double minError = 1000000000000000.0;
    private double lasterror;    
    private boolean firstiteration;
    private int iteration;
    //private double delta_min = 0.5;    
    //private double learningrate = 0.000005;
    private int breakcount = 550;
    
    public boolean hasNext() throws JAMSEntity.NoSuchAttributeException {
	DistNeuron NitrogenOutNeuron = (DistNeuron)NitrogenOutEntity.getObject("NEURON");
	DistNeuron CostOutNeuron = (DistNeuron)CostOutEntity.getObject("NEURON");
			
	errorNO = NitrogenOutNeuron.getActivation() - nitrogen_goal.getValue();
	double outbefore  = NitrogenOutNeuron.getActivation();
        errorCost = CostOutNeuron.getActivation();
			
	if (firstiteration) {
	    firstiteration = false;
	    lasterror = Math.abs(errorNO) + Math.abs(errorCost);
	    return true;
	}
	    
	
	if (Math.abs(errorNO)+Math.abs(errorCost) < minError)
	    minError = Math.abs(errorNO)+Math.abs(errorCost);
	
	performance = lasterror - (Math.abs(errorNO) + Math.abs(errorCost));
	
	avgperformance = 0.9*avgperformance + 0.1 * performance;
	
	if (Math.abs(errorNO + errorCost ) > Math.abs(minError)) {
              //learningrate /= 1.05;
              breakcount--;
            }
            else
            {
                minError = errorNO + errorCost;
                if (performance < delta_min.getValue()) {
                    learningrate.setValue(learningrate.getValue()*1.035);
                }
                else {
                    breakcount = 20;
                }
            }
	lasterror = Math.abs(errorNO)+Math.abs(errorCost);
	
	DistNeuron.eta = learningrate.getValue();
			
	System.out.println("Output before learning : " + outbefore + " NO - Output : " + new Double(NitrogenOutNeuron.getActivation()).toString() + "\t" + 
                         "  Cost - Output : " + new Double(CostOutNeuron.getActivation()).toString() + 
		         "  AvgPerf : " + new Double(avgperformance).toString());
	
	current_output.setValue(NitrogenOutNeuron.getActivation());
	current_iteration.setValue(iteration);
	
	return (breakcount >= 0 /*&& learningrate > 0.000000000001*/ && avgperformance >= delta_min.getValue() );
	
	
    }
    
    public void init () {
	
	firstiteration = true;
	
	iteration = 0;
	
	DistNeuron.alpha = momentum.getValue();
	
	if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
	
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
    }
    
    public void singleRun() {  		
	if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
			
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }	
    }
    
    public void run () {
	try {
	    while (hasNext()) {
		singleRun();
		iteration++;
		}
	    
	    for (int i=hrus.getEntities().size()-1;i>=0;i--) {
		JAMSEntity e = hrus.getEntities().get(i);
	    
		DistNeuron d = (DistNeuron)e.getObject("DIST_NEURON");
	
		if (d.getInitalExternInput() != 0)
		    e.setDouble("reduction",d.getInput() / d.getInitalExternInput());
		else
		    e.setDouble("reduction",0.0);
		
		e.setDouble("new_input",((DistNeuron)e.getObject("DIST_NEURON")).getInput());
	    }	    	    
	}
	catch (JAMSEntity.NoSuchAttributeException e) {
	    getModel().getRuntime().sendInfoMsg("No such attribute Exception: " + e.getMessage());		    
	}		
    }
    
    public void cleanup() {
	if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
}
