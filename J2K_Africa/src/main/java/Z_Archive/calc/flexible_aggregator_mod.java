package calc;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author watso
 */
@JAMSComponentDescription(
    title="Flexible Aggregator (Fixed Window)",
    author="watso",
    description="Aggregates a variable over a user-defined number of timesteps "
                + "(e.g. 8-day MODIS aggregation). Outputs sum or average.",
    date = "2026-04-21",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class flexible_aggregator_mod extends JAMSComponent {

    /*
     *  INPUTS
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Input value to aggregate"
    )
    public Attribute.Double value;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of timesteps to aggregate (e.g. 8 for MODIS)",
            defaultValue = "8"
    )
    public Attribute.Integer windowSize;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calculate average (true) or sum (false)",
            defaultValue = "true"
    )
    public Attribute.Boolean average;

    /*
     *  OUTPUT
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Aggregated output"
    )
    public Attribute.Double aggregate;

    /*
     *  INTERNAL VARIABLES
     */

    private double sum;
    private int counter;

    /*
     *  COMPONENT RUN STAGES
     */

    @Override
    public void init() {
        sum = 0;
        counter = 0;
    }

    @Override
    public void run() {

        double val = value.getValue();

        // ignore missing values
        if (val == jams.JAMS.getMissingDataValue()) {
            aggregate.setValue(jams.JAMS.getMissingDataValue());
            return;
        }

        sum += val;
        counter++;

        // check if window complete
        if (counter == windowSize.getValue()) {

            if (average.getValue()) {
                aggregate.setValue(sum / counter);
            } else {
                aggregate.setValue(sum);
            }

            // reset for next window
            sum = 0;
            counter = 0;

        } else {
            // no output until window complete
            aggregate.setValue(jams.JAMS.getMissingDataValue());
        }
    }

    @Override
    public void cleanup() {
        // optional: output remaining values at end
        if (counter > 0) {
            if (average.getValue()) {
                aggregate.setValue(sum / counter);
            } else {
                aggregate.setValue(sum);
            }
        }
    }
}