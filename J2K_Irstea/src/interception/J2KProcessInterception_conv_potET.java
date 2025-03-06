/*
 * J2KProcessInterception_conv_potET
 * Created on 29-03-2013 after J2KProcessInterception.java by P. Krause
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * 26-05-2021 : Modified by Louise Mimeau
 * conversion of etpot from mm to Liters removed (conversion is now in CropCoefficient.java)
 */
package interception;


import jams.data.*;
import jams.model.*;

/**
*
* @author Francois Tilmant
*/
@JAMSComponentDescription(
        title="J2KProcessInterception_conv_potET",
        author="Francois Tilmant",
        description="Calculates daily interception based on DICKINSON 1984",
        version="1.0_0",
        date="2013-03-19"
        )
public class J2KProcessInterception_conv_potET extends JAMSComponent {

    /*
     *  Component variables
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area",
            unit="m^2"
            )
            public JAMSDouble par_area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable mean tempeature",
            unit="degC"
            )
            public JAMSDouble st_tmean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable rain",
            unit="L"
            )
            public JAMSDouble st_rain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable snow",
            unit="L"
            )
            public JAMSDouble st_snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable potET",
            unit="L"
            )
            public JAMSDouble st_pot_et;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable actET",
            unit="L"
            )
            public JAMSDouble st_act_et;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable LAI"
            )
            public JAMSDouble st_act_lai;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Snow parameter TRS",
            lowerBound = -10.0,
            upperBound = 10.0,
            defaultValue = "0.0",
            unit = "degC"
            )
            public JAMSDouble par_snow_trs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Snow parameter TRANS",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "2.0",
            unit = "K"
            )
            public JAMSDouble par_snow_trans;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Interception parameter a_rain",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "0.2",
            unit = "mm"
            )
            public JAMSDouble par_a_rain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Interception parameter a_snow",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "0.5",
            unit = "mm"
            )
            public JAMSDouble par_a_snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net-rain",
            unit="L"
            )
            public JAMSDouble st_net_rain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net-snow",
            unit="L"
            )
            public JAMSDouble st_net_snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable throughfall",
            unit="L"
            )
            public JAMSDouble st_throughfall;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
           description = "state variable dy-interception",
            unit="L"
            )
            public JAMSDouble st_interception;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable interception storage",
            unit="L"
            )
            public JAMSDouble st_interc_storage;

        
     /* @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "id de la HRU pour Debug",
            unit="-"
            )

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "id de la HRU pour debug"
           )
        public JAMSDouble HRU_id;
      
      @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time step",
            unit = "d")
        public Attribute.Calendar time;

            
    /*
     *  Component run stages
     */

    public void init() throws JAMSEntity.NoSuchAttributeException{
        this.st_interc_storage.setValue(0);
    }

    public void run() throws JAMSEntity.NoSuchAttributeException{

        double run_alpha = 0;
        double run_out_throughfall = 0;
        double run_out_interception = 0;
        
        /*double ID_HRU = HRU_id.getDouble();/*/

        double run_in_rain = st_rain.getValue();
        double run_in_snow = st_snow.getValue();
        double run_in_temp = st_tmean.getValue();

        // double in_potETP = potET.getValue()*area.getValue();
        double run_in_pot_etp = st_pot_et.getValue();
        double run_in_act_etp = 0;

        double run_in_lai = this.st_act_lai.getValue();
        double run_in_area = par_area.getValue();

        double run_out_interception_storage = st_interc_storage.getValue();
        double run_out_act_etp = run_in_act_etp;

        double run_sum_precip = run_in_rain + run_in_snow;
        
      // if (HRU_id.getValue() == 1.0){
           //System.out.println(time.toString());
       // }
        
        double run_delta_etp = run_in_pot_etp - run_in_act_etp;

        double run_rel_rain, run_rel_snow;
        if(run_sum_precip > 0){
            run_rel_rain = run_in_rain / run_sum_precip;
            run_rel_snow = run_in_snow / run_sum_precip;
        } else{
            run_rel_rain = 1.0; //throughfall without precip is in general considered to be liquid
            run_rel_snow = 0;
        }

        //determining if precip falls as rain or snow
        if(run_in_temp < (par_snow_trs.getValue() - par_snow_trans.getValue())){
            //alpha = alpha_snow;
            run_alpha = par_a_snow.getValue();
        } else{
            //alpha = alpha_rain;
            run_alpha = par_a_rain.getValue();
        }

        //determinining maximal interception capacity of actual day
        double run_max_int_ccap = (run_in_lai * run_alpha) * run_in_area;

        //if interception storage has changed from snow to rain then throughfall
        //occur because interception storage of antecedend day might be larger
        //then the maximum storage capacity of the actual time step.
        if(run_out_interception_storage > run_max_int_ccap){
            run_out_throughfall = run_out_interception_storage - run_max_int_ccap;
            run_out_interception_storage = run_max_int_ccap;
        }

        //determining the potential storage volume for daily Interception
        double run_delta_intc = run_max_int_ccap - run_out_interception_storage;

        //reducing rain and filling of Interception storage
        if(run_delta_intc > 0){
            //double save_rain = sum_precip;
            if(run_sum_precip > run_delta_intc){
                run_out_interception_storage = run_max_int_ccap;
                run_sum_precip = run_sum_precip - run_delta_intc;
                run_out_throughfall = run_out_throughfall + run_sum_precip;
                run_out_interception = run_delta_intc;
                run_delta_intc = 0;
            } else{
                run_out_interception_storage = (run_out_interception_storage + run_sum_precip);
                run_out_interception = run_sum_precip;
                run_sum_precip = 0;
            }
        } else{
            run_out_throughfall = run_out_throughfall + run_sum_precip;
        }
        
        
    //    if (HRU_id.getValue() == 1.0){
    //       System.out.println(time.toString());
    //    }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration

        if(run_delta_etp > 0){
            if(run_out_interception_storage > run_delta_etp){
                run_out_interception_storage = run_out_interception_storage - run_delta_etp;
                run_out_act_etp = run_in_act_etp + run_delta_etp;
                run_delta_etp = 0;

            } else{
                run_out_act_etp = run_in_act_etp + run_out_interception_storage;
                run_out_interception_storage = 0;
                run_delta_etp = 0;
            }
        } 
        
     //   if (HRU_id.getValue() == 1.0){
     //      System.out.println(time.toString());
     //   }
        
        this.st_net_rain.setValue(run_out_throughfall * run_rel_rain);
        this.st_net_snow.setValue(run_out_throughfall * run_rel_snow);
        this.st_act_et.setValue(run_out_act_etp);
        // this.potET.setValue(in_potETP);
        this.st_interc_storage.setValue(run_out_interception_storage);
        this.st_interception.setValue(run_out_interception);
        this.st_throughfall.setValue(run_out_throughfall);
        
    }

    public void cleanup() {
        this.st_interc_storage.setValue(0);
    }

}
