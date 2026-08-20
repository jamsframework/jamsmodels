package Isotopes;

import jams.data.*;
import jams.model.*;

/**
 * Aggregates per-HRU precipitation isotope values (e.g. "rain2h") up to a
 * single catchment-scale, precip-weighted daily value, for use as the
 * TimeLoop-scope precipIso/precipAmount inputs of YoungWaterFraction /
 * TimeVariableFYW.
 *
 * On days without rainfall at a given HRU, its isotope reading is a
 * placeholder (e.g. 0), not a real measurement -- including it in the
 * weighted mean would bias the catchment isotope signal toward 0 on days
 * with only partial-catchment rainfall. This component excludes any HRU
 * whose precip amount for the day does not exceed rainThreshold from the
 * isotope weighting, while catchmentPrecipAmount itself is still an
 * area-weighted mean over ALL HRUs, since zero precip is a legitimate
 * contribution to the catchment total.
 *
 * Runs once per day; wire it as a plain (non-context) component placed
 * after HRULoop has finished updating HRU attributes for the day, with an
 * EntityCollection port bound to the model's HRUs.
 */
@JAMSComponentDescription(
        title = "CatchmentPrecipIso",
        author = "A. Watson",
        description = "Precip-weighted aggregation of per-HRU precipitation isotope values to catchment scale, excluding non-rain HRUs",
        date = "2026-08-05"
)
public class CatchmentPrecipIso extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU entity collection to aggregate over"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the per-HRU precipitation amount attribute [mm]",
            defaultValue = "rain"
    )
    public Attribute.String precipAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the per-HRU precipitation isotope attribute [permil]",
            defaultValue = "rain2h"
    )
    public Attribute.String isoAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of the per-HRU area attribute",
            defaultValue = "area"
    )
    public Attribute.String areaAttributeName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "An HRU's precip amount must exceed this for the day to count as a rainfall " +
                    "event there and contribute to the isotope weighting; below it, the isotope reading " +
                    "is treated as a placeholder and excluded",
            defaultValue = "0.0"
    )
    public Attribute.Double rainThreshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Weight HRU contributions by area as well as precip amount",
            defaultValue = "True"
    )
    public Attribute.Boolean areaWeighting;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment-scale precipitation isotope value for the day [permil], weighted by " +
                    "precip amount (and area) over HRUs with a rainfall event only. NaN if no HRU rained."
    )
    public Attribute.Double catchmentPrecipIso;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment-scale precipitation amount for the day [mm], area-weighted mean over all HRUs"
    )
    public Attribute.Double catchmentPrecipAmount;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Number of HRUs counted as having a rainfall event on the current day"
    )
    public Attribute.Integer nRainHRUs;

    @Override
    public void run() {

        boolean weightByArea = areaWeighting.getValue();
        double threshold = rainThreshold.getValue();

        double isoWeightedSum = 0, isoWeightSum = 0;
        double amountWeightedSum = 0, areaSum = 0;
        int nRain = 0;

        for (Attribute.Entity hru : hrus.getEntities()) {

            double amount = hru.getDouble(precipAttributeName.getValue());
            double area = weightByArea ? hru.getDouble(areaAttributeName.getValue()) : 1.0;

            amountWeightedSum += amount * area;
            areaSum += area;

            if (amount > threshold) {
                double iso = hru.getDouble(isoAttributeName.getValue());
                double w = amount * area;
                isoWeightedSum += iso * w;
                isoWeightSum += w;
                nRain++;
            }
        }

        catchmentPrecipAmount.setValue(areaSum > 0 ? amountWeightedSum / areaSum : Double.NaN);
        catchmentPrecipIso.setValue(isoWeightSum > 0 ? isoWeightedSum / isoWeightSum : Double.NaN);
        nRainHRUs.setValue(nRain);
    }
}
