/*
 * ManageLanduse.java
 *
 * Created on 16. März 2006, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jams.j2k.s_n.management;

import java.util.ArrayList;
import org.jams.j2k.s_n.crop.*;
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;


/**
 *
 * @author c5ulbe
 */
public class ManageLanduse_szeno extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current hru object"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current NH4 fertilizer amount"
            )
            public JAMSDouble fertNH4N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current NO3 fertilizer amount"
            )
            public JAMSDouble fertNO3N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current organic fertilizer amount"
            )
            public JAMSDouble fertorgNactive;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current organic fertilizer amount added to residue pool"
            )
            public JAMSDouble fertorgNfresh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reduction Factor for Fertilisation 0 - 10 [-]"
            )
            public JAMSDouble ReductionFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current organic fertilizer amount"
            )
            public JAMSInteger RotPos;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current organic fertilizer amount"
            )
            public JAMSInteger ManagementPos;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Plant exisiting or not"
            )
            public JAMSBoolean plantExisting;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Indicator for harvesting"
            )
            public JAMSBoolean doHarvest;
 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Date to start reduction"
            )
            public JAMSCalendar start;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Date to end reduction"
            )
            public JAMSCalendar end;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Encapsulating time interval"
            )
            public JAMSTimeInterval timeInterval;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Indicates fertilazation optimization with plant demand"
            )
            public JAMSInteger opti;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Mineral nitrogen content in the soil profile down to 60 cm depth"
            )
            public JAMSDouble nmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimal nitrogen content in Biomass in (kgN/ha)"
            )
            public JAMSDouble optibioN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actual nitrogen content in Biomass in (kgN/ha)"
            )
            public JAMSDouble actbioN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Fraction of actual potential heat units sum [-]"
            )
            public JAMSDouble FPHUact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Fertilisation reduction due to the plant demand routine [kgN/ha]"
            )
            public JAMSDouble Nredu;
  
    
    private JAMSTimeInterval ti;
    double endbioN;
    double runNredu;
    public void init() {
        ti = new JAMSTimeInterval(start, end, timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
        
    }
     
    public void run() throws JAMSEntity.NoSuchAttributeException {
        
        JAMSEntity entity = entities.getCurrent();
        this.fertNO3N.setValue(0);
        this.fertNH4N.setValue(0);
        this.fertorgNactive.setValue(0);
        this.fertorgNfresh.setValue(0);
        boolean runplantex = false;
        this.runNredu  = 0;
        runplantex = plantExisting.getValue();
        ArrayList<J2KSNCrop> rotation = (ArrayList<J2KSNCrop>) entity.getObject("landuseRotation");
        int rotPos = RotPos.getValue();
        J2KSNCrop currentCrop = rotation.get(rotPos);
        int idc = currentCrop.idc;
        this.endbioN = currentCrop.endbioN;
        if (idc != 1 && idc != 2 && idc != 4 && idc != 5){
          runplantex = true;  
        } 
            
        ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int managementPos = ManagementPos.getValue();
        J2KSNLMArable currentManagement = managementList.get(managementPos);
        
        int nextDay = currentManagement.jDay;
        doHarvest.setValue(false);
        
//            System.out.println("da" + nextDay + time.get(JAMSCalendar.DAY_OF_YEAR));
        
        if ((nextDay-1) == time.get(time.DAY_OF_YEAR)) {
            if (currentManagement.harvest != -1) {
                //do harvesting here!!
                //System.out.println(" Julianischer Tag  "+ time.get(time.DAY_OF_YEAR));
                doHarvest.setValue(true);
            }
        }
        
        if (nextDay == time.get(time.DAY_OF_YEAR)) {
            
            if ((managementPos+1) ==  managementList.size()) {
                ManagementPos.setValue(0);
                int rotCount = rotation.size();
                rotPos = (rotPos+1) % rotCount;
                RotPos.setValue(rotPos);
            } else {
                ManagementPos.setValue(managementPos+1);
            }
            
            if (currentManagement.till != null) {
                //do tillage processing here!!
            } else if (currentManagement.fert != null) {
                //do fetilization processing here!!
                processFertilization(currentManagement);
            } else if (currentManagement.plant == true) {
                //do planting here!!
                //PHUact.setValue(0);
                runplantex = true;
            } else if (currentManagement.harvest != -1 && (idc == 1 || idc == 2 || idc == 4 || idc == 5 ) ) {
                //do harvesting here!!
                runplantex = false;
                
            }
        }
        
        plantExisting.setValue(runplantex);
        this.Nredu.setValue(runNredu);
        
    }
    
    private void processFertilization(J2KSNLMArable currentManagement) {
        double fertN_total = 0;
        
        J2KSNFertilizer fert = currentManagement.fert;
        
        double redu = ReductionFactor.getValue();
        
        if (time.after(ti.getStart()) && time.before(ti.getEnd())) {
        
         redu = Math.max(0,redu);           
        } else {
          redu = 1;  
        }
        
        double famount = currentManagement.famount * redu;
        
   
        
        fertN_total = famount * fert.fminn;
        
        //fertilasation in dependence of the demand and N_min in Soil
        
        if (opti.getValue() == 1){ 
        
        double demand_factor = Math.min(Math.sqrt(FPHUact.getValue()+ 0.3), 1);
        double future_demand = (demand_factor * endbioN) - optibioN.getValue();
        double actual_demand = optibioN.getValue() - actbioN.getValue();
        double total_demand = (future_demand + actual_demand) - nmin.getValue();
        
        if (fertN_total > total_demand){
            
            redu =  total_demand / fertN_total;
            
        }else{
            
            redu = 1;
            
        } 
        
        
        runNredu = (1 - redu) * (fert.forgn + fert.fminn) * famount; 
        famount = redu * famount;
        
        
        fertN_total = famount * fert.fminn;
        
        }
        
        double fertNH4N = fertN_total * fert.fnh4n;
        double fertNO3N = fertN_total - fertNH4N;
        double fertorgNfresh = 0.5 * fert.forgn * famount; // amount of nitrogen in the fresh organic pool added to the soil
        double fertorgNactive = 0.5 * famount * fert.forgn; //orgNact is the amount of nitrogen in the active organic pool added to the soil
        
        
       
        
        
       /* if (fertorgN > 0 || fertNO3N > 0 || fertNH4N > 0) {
        System.out.println("Gebe die Düngemengen aus :"  + fertNO3N + fertorgN + fertNH4N );
        } */
        /*double fertN03 = fertNO3_current + fertNO3_old;
        double fertNH4 = fertNH4_current + fertNH4_old;
        double fertorg = fertorg_current + fertorg_old;*/
        
        
        this.fertNO3N.setValue(fertNO3N);
        this.fertNH4N.setValue(fertNH4N);
        this.fertorgNfresh.setValue(fertorgNfresh);
        this.fertorgNactive.setValue(fertorgNactive);
    }
    
}
