package Isotopes;

import jams.data.*;
import jams.model.*;

@JAMSComponentDescription(
    title="Check Isotope Inputs",
    author="Andrew Watson",
    description="Checks for rainfall with missing isotope values",
    date="2026-07-08",
    version="1.0"
)
public class check_isotopes extends JAMSComponent {

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Rainfall",
        unit = "L"
    )
    public Attribute.Double rain;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Precipitation d2H"
    )
    public Attribute.Double rain_d2H;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "HRU ID"
    )
    public Attribute.Double ID;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "Current model time"
    )
    public Attribute.Calendar time;

    private int errorCount = 0;

    @Override
    public void run() {

        if (rain.getValue() > 0 && Math.abs(rain_d2H.getValue()) < 1e-9) {

            errorCount++;

            getModel().getRuntime().println(
                "WARNING: Rainfall > 0 but δ2H = 0"
                + " | HRU=" + (int)ID.getValue()
                + " | Date=" + time.toString()
                + " | Rain=" + rain.getValue()
                + " | d2H=" + rain_d2H.getValue()
            );
        }
    }

    @Override
    public void cleanup() {

        getModel().getRuntime().println(
            "Total rainfall/isotope mismatches = " + errorCount
        );
    }
}