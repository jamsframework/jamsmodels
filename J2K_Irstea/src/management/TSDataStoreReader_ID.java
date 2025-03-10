/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package management;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.ArrayList;

/**
 *
 * @author Francois Tilmant
 */
@JAMSComponentDescription(title = "TSDataStoreReader_ID",
        author = "Francois Tilmant",
        description = "TSDataStoreReader with name of the stations in an Array",
        version = "1.0_0",
        date = "2014-06-03")
public class TSDataStoreReader_ID extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String in_id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The time interval within which the component shall read "
            + "data from the datastore")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Aggregate multiple datastore entries to averages or sums?",
            defaultValue = "true")
    public Attribute.Boolean par_calc_avg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The current model time - needed in case of aggregation over irregular time steps (e.g. months). "
                    + "Aggregation is disabled if this value is not set.")
    public Attribute.Calendar par_time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Descriptive name of the dataset (equals datastore ID)")
    public Attribute.String par_data_set_name;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of double values received from the datastore. Order "
            + "according to datastore")
    public Attribute.DoubleArray in_data_array;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station elevations")
    public Attribute.DoubleArray in_elevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station names")
    public Attribute.DoubleArray in_names;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's x coordinate")
    public Attribute.DoubleArray in_x_coord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's y coordinate")
    public Attribute.DoubleArray in_y_coord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Regression coefficients")
    public Attribute.DoubleArray par_reg_coeff;

    private TSDataStore run_store;
    private double[] run_doubles;
    private double[] run_elevation_array;
    private double[] run_names_array;
    boolean run_shifted = false;
    int run_ts_ratio = 1;
    Attribute.Calendar run_store_date;
    int run_store_unit, run_store_unit_count, run_target_unit, run_target_unit_count;

    @Override
    public void init() {
        run_shifted = false;
        InputDataStore run_is = null;
        if (in_id != null) {
            run_is = getModel().getWorkspace().getInputDataStore(in_id.getValue());
        }

        // check if store exists
        if (run_is == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + in_id + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        // check if this is a TSDataStore
        if (!(run_is instanceof TSDataStore)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + in_id + "\" from " + getInstanceName() + ": Datastore is not a time series datastore!");
            return;
        }

        run_store = (TSDataStore) run_is;

        // check if the store's time interval matches the provided time interval
        if (run_store.getStartDate().after(timeInterval.getStart()) && (run_store.getStartDate().compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + in_id + "\" from " + getInstanceName() + ": Start date of datastore ("
                    + run_store.getStartDate() + ") does not match given time interval ("
                    + timeInterval.getStart() + ")!");
            return;
        }

        if (run_store.getEndDate().before(timeInterval.getEnd()) && (run_store.getEndDate().compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + in_id + "\" from " + getInstanceName() + ": End date of datastore ("
                    + run_store.getEndDate() + ") does not match given time interval ("
                    + timeInterval.getEnd() + ")!");
            return;
        }

        // extract some meta information
        DataSetDefinition run_ds_def = run_store.getDataSetDefinition();
        if (run_ds_def.getAttributeValues("X") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + in_id + "\" from " + getInstanceName() + ": x coordinate not specified");
        }
        if (run_ds_def.getAttributeValues("Y") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + in_id + "\" from " + getInstanceName() + ": y coordinate not specified");
        }
        if (run_ds_def.getAttributeValues("ELEVATION") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + in_id + "\" from " + getInstanceName() + ": elevation not specified");
        }
        if (run_ds_def.getAttributeValues("ID") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + in_id + "\" from " + getInstanceName() + ": name not specified");
        }
        in_x_coord.setValue(listToDoubleArray(run_ds_def.getAttributeValues("X")));
        in_y_coord.setValue(listToDoubleArray(run_ds_def.getAttributeValues("Y")));
        in_elevation.setValue(listToDoubleArray(run_ds_def.getAttributeValues("ELEVATION")));
        run_elevation_array = in_elevation.getValue();
        in_names.setValue(listToDoubleArray(run_ds_def.getAttributeValues("X")));
        run_names_array = in_names.getValue();
        par_data_set_name.setValue(in_id.getValue());

        getModel().getRuntime().println("Datastore " + in_id + " initialized!", JAMS.VVERBOSE);
        run_doubles = new double[run_store.getDataSetDefinition().getColumnCount()];
        in_data_array.setValue(run_doubles);
    }

    private double[] listToDoubleArray(ArrayList<Object> run_list) {
        double[] run_result = new double[run_list.size()];
        int i = 0;
        for (Object o : run_list) {
            run_result[i] = ((Double) o).doubleValue();
            i++;
        }
        return run_result;
    }

    private void checkConsistency() {

        // check if we need to shift forward
        Attribute.Calendar run_target_date = timeInterval.getStart().clone();
        run_target_unit = timeInterval.getTimeUnit();
        run_target_unit_count = timeInterval.getTimeUnitCount();
        run_store_date = run_store.getStartDate().clone();
        run_store_unit = run_store.getTimeUnit();
        run_store_unit_count = run_store.getTimeUnitCount();

        run_store_date.removeUnsignificantComponents(run_store_unit);
        run_target_date.removeUnsignificantComponents(run_target_unit);

        int run_offset = run_store_date.compareTo(run_target_date, run_target_unit);

        if (run_offset > 0) {

            getModel().getRuntime().sendHalt("Time series data read by " + this.getInstanceName() + " start after model start time!"
                    + "\n(" + run_store.getStartDate() + " vs " + timeInterval.getStart() + ")");

        } else if (run_offset < 0) {

            // check if we can calculate offset directly
            // this can be done if the step size can be calculated directly from
            // milliseconds representation, i.e. for weekly time steps and below
            // else we calculate offset by iterating in time (less efficient)
            long run_diff = (run_target_date.getTimeInMillis() - run_store_date.getTimeInMillis()) / 1000;
            int run_steps;
            switch (run_store_unit) {
                case Attribute.Calendar.DAY_OF_YEAR:
                    run_steps = (int) (run_diff / 3600 / 24 / run_store_unit_count);
                    run_store_date.add(run_store_unit, run_store_unit_count * run_steps);
                    break;
                case Attribute.Calendar.HOUR_OF_DAY:
                    run_steps = (int) (run_diff / 3600 / run_store_unit_count);
                    run_store_date.add(run_store_unit, run_store_unit_count * run_steps);
                    break;
                case Attribute.Calendar.WEEK_OF_YEAR:
                    run_steps = (int) (run_diff / 3600 / 24 / 7 / run_store_unit_count);
                    run_store_date.add(run_store_unit, run_store_unit_count * run_steps);
                    break;
                case Attribute.Calendar.MINUTE:
                    run_steps = (int) (run_diff / 60 / run_store_unit_count);
                    run_store_date.add(run_store_unit, run_store_unit_count * run_steps);
                    break;
                case Attribute.Calendar.SECOND:
                    run_steps = (int) (run_diff / run_store_unit_count);
                    run_store_date.add(run_store_unit, run_store_unit_count * run_steps);
                    break;
                default:
                    run_steps = iterateStoreDate(run_target_date);
            }

            // skip forward datastore to required start time
            run_store.skip(run_steps);

        }

        // check if we have different step size in store and model
        if (run_store_unit != run_target_unit || run_store_unit_count != run_target_unit_count) {

            // if both units have a constant duration, calculate this duration and the related ratio
            if (run_store_unit > Attribute.Calendar.MONTH && run_target_unit > Attribute.Calendar.MONTH) {
                int run_store_ms = getMilliseconds(run_store_unit);
                int run_target_ms = getMilliseconds(run_target_unit);
                double run_d_ratio = (double) (run_target_ms * run_target_unit_count) / (run_store_ms * run_store_unit_count);
                int run_ratio = (int) Math.floor(run_d_ratio);
                if (run_ratio != run_d_ratio) {
                    getModel().getRuntime().sendHalt("Time steps in datastore " + run_store.getID() + " and model are incompatible. "
                            + "Please adapt your datastore first!");
                }

                run_ts_ratio = run_ratio;
            } else {
                run_ts_ratio = -1;
            }

        }
    }

    private int getMilliseconds(int run_unit) {
        int run_ms = 0;
        switch (run_unit) {
            case Attribute.Calendar.DAY_OF_YEAR:
                run_ms = 1000 * 3600 * 24;
                break;
            case Attribute.Calendar.HOUR_OF_DAY:
                run_ms = 1000 * 3600;
                break;
            case Attribute.Calendar.WEEK_OF_YEAR:
                run_ms = 1000 * 3600 * 24 * 7;
                break;
            case Attribute.Calendar.MINUTE:
                run_ms = 1000 * 60;
                break;
            case Attribute.Calendar.SECOND:
                run_ms = 1000;
                break;
            case Attribute.Calendar.MILLISECOND:
                run_ms = 1;
                break;
            default:
                getModel().getRuntime().sendHalt("Cannot calculate constant time unit duration!");
        }
        return run_ms;
    }

    private int iterateStoreDate(Attribute.Calendar run_date) {
        int run_steps = 0;
        while (run_store_date.compareTo(run_date, run_store_unit) < 0) {
            run_store_date.add(run_store_unit, run_store_unit_count);
            run_steps++;
        }
        return run_steps;
    }

    @Override
    public void initAll() {
        checkConsistency();
    }

    @Override
    public void run() {

        if (run_ts_ratio == 1 || par_time == null) {

            DefaultDataSet run_ds = run_store.getNext();
            DataValue[] run_data = run_ds.getData();
            for (int i = 1; i < run_data.length; i++) {
                run_doubles[i - 1] = run_data[i].getDouble();
            }

            in_data_array.setValue(run_doubles);


        } else {

            int n;

            // get the ratio (fixed or dynamic)
            if (run_ts_ratio < 0) {
                Attribute.Calendar run_next_time = par_time.clone();
                run_next_time.add(run_target_unit, run_target_unit_count);
                n = iterateStoreDate(run_next_time);
            } else {
                n = run_ts_ratio;
            }

            // calc the aggregated values based on the ratio
            for (int i = 0; i < run_doubles.length; i++) {
                run_doubles[i] = 0;
            }

            for (int j = 0; j < n; j++) {
                DefaultDataSet run_ds = run_store.getNext();
                DataValue[] run_data = run_ds.getData();
                for (int i = 1; i < run_data.length; i++) {
                    run_doubles[i - 1] += run_data[i].getDouble();
                }
            }

            if (par_calc_avg.getValue()) {
                for (int i = 0; i < run_doubles.length; i++) {
                    run_doubles[i] /= n;
                }
            }

            in_data_array.setValue(run_doubles);


            // create some output
//            String s = store.getID() + " ";
//            if (time != null) {
//                s += time + " ";
//            }
//            for (int i = 0; i < doubles.length; i++) {
//                s += doubles[i] + " ";
//            }
//            getModel().getRuntime().println(s, JAMS.VVERBOSE);

        }

    }

    @Override
    public void cleanup() {
        run_store.close();
    }
}
