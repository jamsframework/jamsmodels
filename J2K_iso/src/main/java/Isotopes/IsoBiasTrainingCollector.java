package Isotopes;

/*
 * IsoBiasTrainingCollector.java
 * Created on 27.08.2026
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IsoBiasTrainingCollector",
        author = "Andrew Watson",
        description = "Assembles the offline training set for a machine-learning "
        + "precipitation-isotope bias corrector. Runs once per timestep at "
        + "STATION level (wire it in a plain Context inside TimeLoop, not inside "
        + "HRULoop - it reads the same per-station arrays IsoRegionaliser does, "
        + "not per-HRU attributes). Each timestep, for every station with a valid "
        + "observed/simulated pair, it records a covariate row and a target value "
        + "(observed - simulated).\n\n"
        + "WHICH covariates go into the row is controlled by featureColumns, a "
        + "semicolon-separated list chosen from: elevation, xCoord, yCoord, "
        + "dayOfYear, tmean, amount, outsideBasinAvg, iemi, antecedent7, "
        + "antecedent30, rhum, tempRange. This is deliberately configurable "
        + "because the useful feature set is an empirical question and the "
        + "sample count is small - adding dimensions to a sparse training set "
        + "makes the fit worse, not better, so subsets need to be testable "
        + "without a recompile. The selected list is written to featureListFile "
        + "so Isotopes.IsoMLBiasCorrection builds the identical vector at "
        + "application time; a mismatch there would silently query the GP with "
        + "features it was never trained on.\n\n"
        + "antecedent7/antecedent30 are cumulative precipitation over the 7/30 "
        + "days BEFORE the current one (today excluded, since today's amount is "
        + "already its own covariate) - a proxy for air-mass rainout history, "
        + "which is a first-order control on event-scale isotope composition "
        + "that purely geographic/seasonal covariates cannot represent. "
        + "tmean/rhum/tempRange are inverse-distance-weighted from their own "
        + "station networks onto each isotope station's coordinates, since those "
        + "networks generally differ in station count and ordering from the "
        + "isotope network - they are NOT index-matched.\n\n"
        + "Stations with elevation == the missing-data sentinel are treated as "
        + "OUTSIDE the basin (a real convention in this network, not just "
        + "missing data) and are never used as their own training row; their "
        + "observations instead feed a forward-filled running average exposed as "
        + "the outsideBasinAvg covariate. cleanup() reports whether such an "
        + "observation was ever actually seen, plus a full breakdown of how many "
        + "candidate station-days were dropped at each filter, so a small "
        + "training set can be traced to its actual cause rather than guessed at.",
        date = "2026-09-11",
        version = "2.3_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Added tmean as a covariate, "
            + "IDW-interpolated from the general temperature network onto each "
            + "isotope station's coordinates"),
    @VersionComments.Entry(version = "1.2_0", comment = "Added precipitation amount "
            + "as a covariate and exact-duplicate-row filtering, to address a "
            + "singular covariance matrix caused by near-identical rows across "
            + "years for the same station"),
    @VersionComments.Entry(version = "1.3_0", comment = "Stations with elevation == "
            + "missingDataValue are now treated as outside-basin reference "
            + "stations, folded in as a forward-filled running average covariate"),
    @VersionComments.Entry(version = "1.4_0", comment = "Added iemi, a monthly "
            + "regional moisture-recycling index looked up from a CSV by "
            + "calendar month"),
    @VersionComments.Entry(version = "1.5_0", comment = "Removed the "
            + "haveOutsideAvg gate that was zeroing out the entire training set "
            + "when the outside-basin network did not overlap the run period"),
    @VersionComments.Entry(version = "2.0_0", comment = "Configurable feature set "
            + "via featureColumns + featureListFile handshake with "
            + "IsoMLBiasCorrection; new antecedent7/antecedent30/rhum/tempRange "
            + "daily covariates; per-filter drop counters reported at cleanup; "
            + "outputFile now resolved against the workspace directory"),
    @VersionComments.Entry(version = "2.1_0", comment = "The amountThreshold gate "
            + "no longer discards station-days whose rainfall amount is missing "
            + "when no positive threshold is set - that was dropping 350 of the "
            + "450 available isotope observations. amount also left out of the "
            + "default feature set for the same reason; antecedent7/30 are "
            + "retained since they are summed over whatever prior days are "
            + "available and so tolerate gauge gaps."),
    @VersionComments.Entry(version = "2.2_0", comment = "Added doySin/doyCos, a "
            + "cyclic day-of-year encoding. dayOfYear as a plain number puts 31 "
            + "December 364 units from 1 January, so a GP with a linear mean "
            + "fits a ramp across the season and extrapolates it into the "
            + "unsampled months: the isoGSM correction ran from +43.5 permil in "
            + "January to -105.9 in December and hit the -200 clamp, with 39% "
            + "of days lying outside the dayOfYear 19..218 training range. "
            + "Projecting onto the unit circle removes the discontinuity and "
            + "bounds the covariates to [-1,1] so the mean cannot run away."),
    @VersionComments.Entry(version = "2.3_0", comment = "A relative iemiFile is "
            + "now resolved against the model workspace, as outputFile already "
            + "was. It used to be opened with a raw FileReader, which resolves "
            + "against the JVM working directory - not the per-run exec directory "
            + "on the JAMS cloud server - so the covariate file could never be "
            + "found there. Absolute paths are passed through unchanged.")
})
public class IsoBiasTrainingCollector extends JAMSComponent {

    /** Every covariate this component knows how to compute. */
    public static final String[] AVAILABLE_FEATURES = {
        "elevation", "xCoord", "yCoord", "dayOfYear", "doySin", "doyCos",
        "tmean", "amount", "outsideBasinAvg", "iemi", "antecedent7",
        "antecedent30", "rhum", "tempRange"
    };

    private static final int MAX_ANTECEDENT_WINDOW = 30;

    /** Period for the cyclic day-of-year encoding (doySin / doyCos). */
    private static final double DAYS_IN_YEAR = 365.25;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Observed precipitation isotope value per station [permil]"
    )
    public Attribute.DoubleArray observedArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Simulated/reference isotope value per station [permil]"
    )
    public Attribute.DoubleArray simulatedArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Station elevation [m]. The missing-data sentinel marks "
            + "an outside-basin reference station."
    )
    public Attribute.DoubleArray elevationArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Station x coordinate"
    )
    public Attribute.DoubleArray xCoordArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Station y coordinate"
    )
    public Attribute.DoubleArray yCoordArray;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Co-located precipitation amount per station [mm]. Gates "
            + "collection via amountThreshold, and supplies the amount / "
            + "antecedent7 / antecedent30 covariates."
    )
    public Attribute.DoubleArray amountArray = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum amount for a station-day to count as a rainfall event",
            defaultValue = "0.0"
    )
    public Attribute.Double amountThreshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Column IDs for observedArray (the datastore's columnID). "
            + "Wire this together with simulatedIDs/amountIDs to match stations BY ID "
            + "instead of by array position. The observed, simulated and rainfall "
            + "series generally come from different datastores with different station "
            + "sets and orderings, in which case positional matching silently pairs "
            + "unrelated stations and the resulting bias is meaningless."
    )
    public Attribute.StringArray observedIDs = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Column IDs for simulatedArray. Only used when the simulated "
            + "network's coordinates are NOT wired. Beware: if both datastores number "
            + "their columns sequentially from different starting points, ID matching "
            + "produces coincidental integer overlaps that look like real matches."
    )
    public Attribute.StringArray simulatedIDs = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Simulated network station/grid x coordinates. When wired "
            + "(together with the y coordinates) the simulated value is taken from the "
            + "nearest simulated point to each isotope station instead of by column ID. "
            + "This is the correct treatment when the simulated field is a model GRID "
            + "rather than a set of real stations, and it matches how IsoRegionaliser "
            + "itself samples this field (nidw=1, i.e. nearest point)."
    )
    public Attribute.DoubleArray simulatedNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Simulated network station/grid y coordinates"
    )
    public Attribute.DoubleArray simulatedNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Column IDs for amountArray. Retained for diagnostics only - "
            + "rainfall is interpolated onto the isotope stations rather than "
            + "column-matched, because the two networks are not co-located."
    )
    public Attribute.StringArray amountIDs = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Explicit per-station elevation [m] as a semicolon-separated "
            + "list, in the SAME column order as observedArray, e.g. "
            + "\"-9999;-9999;1360;1288;...\". Takes priority over both the HRU lookup "
            + "and the datastore's own elevation field. Prefer this when you have "
            + "surveyed station elevations: an HRU's mean elevation is not the "
            + "station's point elevation, and in steep terrain the two differ enough "
            + "to blur exactly the altitude gradient this covariate exists to test. "
            + "The missing-data sentinel (-9999) marks an outside-basin station. The "
            + "count is checked against the observed array length at startup, since "
            + "this list is positional."
    )
    public Attribute.String stationElevations = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU entity collection. When wired together with observedIDs "
            + "(whose column IDs are the HRU each station falls in), station elevation "
            + "is taken from that HRU's own elevation instead of the datastore's "
            + "elevation field. Use this when the datastore's elevation metadata is "
            + "unusable - and note it also gives a reliable outside-basin test, since "
            + "a station whose HRU is not in the catchment simply will not resolve."
    )
    public Attribute.EntityCollection hrus = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Rain-gauge network station x coordinates"
    )
    public Attribute.DoubleArray amountNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Rain-gauge network station y coordinates"
    )
    public Attribute.DoubleArray amountNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Mean temperature array from the general temperature network [degC]"
    )
    public Attribute.DoubleArray tmeanNetworkArray = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Temperature network station x coordinates"
    )
    public Attribute.DoubleArray tmeanNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Temperature network station y coordinates"
    )
    public Attribute.DoubleArray tmeanNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Relative humidity array from the humidity network [%]. "
            + "Required only if \"rhum\" is in featureColumns."
    )
    public Attribute.DoubleArray rhumNetworkArray = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Humidity network station x coordinates"
    )
    public Attribute.DoubleArray rhumNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Humidity network station y coordinates"
    )
    public Attribute.DoubleArray rhumNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max temperature array [degC]. Required only if "
            + "\"tempRange\" is in featureColumns."
    )
    public Attribute.DoubleArray tmaxNetworkArray = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max temperature network station x coordinates"
    )
    public Attribute.DoubleArray tmaxNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max temperature network station y coordinates"
    )
    public Attribute.DoubleArray tmaxNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Min temperature array [degC]. Required only if "
            + "\"tempRange\" is in featureColumns."
    )
    public Attribute.DoubleArray tminNetworkArray = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Min temperature network station x coordinates"
    )
    public Attribute.DoubleArray tminNetworkXCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Min temperature network station y coordinates"
    )
    public Attribute.DoubleArray tminNetworkYCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Power for inverse-distance weighting of the auxiliary "
            + "networks onto each isotope station's location",
            defaultValue = "2.0"
    )
    public Attribute.Double tmeanIdwPower;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current model time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Path to the monthly regional index CSV"
    )
    public Attribute.String iemiFile = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Value in the CSV's station column to filter to",
            defaultValue = "Cat"
    )
    public Attribute.String iemiStation = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Header name of the station column",
            defaultValue = "Station"
    )
    public Attribute.String iemiStationColumn = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Header name of the date column",
            defaultValue = "Date"
    )
    public Attribute.String iemiDateColumn = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Header name of the value column to use",
            defaultValue = "iEMI6"
    )
    public Attribute.String iemiValueColumn = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Semicolon-separated covariate list, in the order they "
            + "enter the feature vector. Chosen from: elevation, xCoord, yCoord, "
            + "dayOfYear, doySin, doyCos, tmean, amount, outsideBasinAvg, iemi, antecedent7, "
            + "antecedent30, rhum, tempRange.",
            defaultValue = "elevation;dayOfYear;tmean;antecedent7;antecedent30;iemi"
    )
    public Attribute.String featureColumns;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "File the selected feature list is written to, for "
            + "IsoMLBiasCorrection to read back",
            defaultValue = "output/iso_bias_gp_features.txt"
    )
    public Attribute.String featureListFile;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Assembled training data (\"data\" / \"predict\" objects)"
    )
    public Attribute.Entity trainData;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Held-out data in the same (\"data\"/\"predict\") shape. Wire "
            + "GaussianLearner's validationData here rather than at trainData, so its "
            + "resultFile becomes a genuine out-of-sample assessment. With both pointed "
            + "at the same entity a GP simply interpolates its training points and "
            + "reports a perfect fit, which says nothing about skill."
    )
    public Attribute.Entity validationData = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of STATIONS (not samples) to hold out, 0.0-0.9. "
            + "Whole stations are withheld rather than random rows because rows from "
            + "one station are strongly autocorrelated - a random split leaves near "
            + "neighbours of every test row in the training set and flatters the score. "
            + "Holding out stations measures what actually matters here: predicting the "
            + "bias at a location the model was not trained on. 0 disables the split.",
            defaultValue = "0.0"
    )
    public Attribute.Double holdoutFraction;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Running, forward-filled average of the outside-basin "
            + "stations' observed isotope value [permil]"
    )
    public Attribute.Double outsideBasinAvg;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optional tab-separated dump of the assembled samples"
    )
    public Attribute.String outputFile = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Centre and scale the covariates AND the target to zero "
            + "mean and unit variance before handing them to the learner. Both halves "
            + "are needed, and the target matters more. "
            + "MaternClass (kernelMethod=3) has no amplitude parameter - k(x,x) is "
            + "fixed at 1 - so a GP trained on a residual whose variance is ~400 "
            + "cannot express its own prior scale and explains the lot as noise; "
            + "scaling the target to unit variance is what that kernel is built to "
            + "expect. The same kernel is ARD, carrying one length scale per "
            + "covariate, and on raw inputs spanning 1.9 (iemi) to 580 (antecedent30) "
            + "its hyperparameter search settles on length scales of 0.09-0.8 for "
            + "covariates spanning hundreds, so every off-diagonal covariance "
            + "underflows to zero, the covariance matrix degenerates to the identity, "
            + "and every query returns the prior mean with a constant variance. "
            + "Measured on this catchment 214 samples with this exact kernel, "
            + "station-held-out: raw R2 0.07; covariates scaled alone R2 0.07 (no "
            + "change at all); covariates and target both scaled R2 0.30. The scaling "
            + "is fitted on the TRAINING rows only and written to featureListFile so "
            + "IsoMLBiasCorrection applies the identical transform and inverts it on "
            + "the prediction. Set false only to reproduce a pre-2026-09 run.",
            defaultValue = "true"
    )
    public Attribute.Boolean standardiseData = null;

    private final List<double[]> features = new ArrayList<double[]>();
    private final List<Double> targets = new ArrayList<Double>();
    private final java.util.Set<String> seenRows = new java.util.HashSet<String>();
    private final LinkedList<double[]> precipHistory = new LinkedList<double[]>();

    private double runningOutsideAvg = 0.0;
    private boolean haveOutsideAvg = false;
    private Map<String, Double> iemiByMonth;
    private String[] selectedFeatures;

    /** simIdx[i] / amtIdx[i] = index into the simulated / amount array for observed station i, or -1. */
    private int[] simIdx, amtIdx;
    private boolean[] isHoldoutStation;
    private final List<double[]> vFeatures = new ArrayList<double[]>();
    private final List<Double> vTargets = new ArrayList<Double>();
    private double[] stationElevation;
    private boolean mappingBuilt = false;
    private int dropNoStationMatch;

    /** Per-covariate centre and scale, fitted on the training rows. Identity when disabled. */
    private double[] featureMean, featureSd;
    /** Centre and scale of the target, fitted on the training rows. */
    private double targetMean = 0.0, targetSd = 1.0;

    // Per-filter drop counters, so a small training set can be traced to its
    // actual cause instead of guessed at.
    private int seenCandidates, dropOutsideStation, dropObserved, dropSimulated,
            dropAmount, dropFeatureUnavailable, dropDuplicate;

    @Override
    public void init() {
        features.clear();
        targets.clear();
        vFeatures.clear();
        vTargets.clear();
        seenRows.clear();
        precipHistory.clear();
        runningOutsideAvg = 0.0;
        haveOutsideAvg = false;
        seenCandidates = dropOutsideStation = dropObserved = dropSimulated
                = dropAmount = dropFeatureUnavailable = dropDuplicate = dropNoStationMatch = 0;

        if (selectedFeatures != null) {
            return; // already initialised on a previous init() call
        }

        selectedFeatures = parseFeatureColumns();
        validateSelectedFeatures();
        loadIemi();
        writeFeatureList();

        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: feature vector = "
                + java.util.Arrays.toString(selectedFeatures)
        );
    }

    private String[] parseFeatureColumns() {
        String raw = featureColumns.getValue();
        List<String> out = new ArrayList<String>();
        for (String part : raw.split(";")) {
            String name = part.trim();
            if (!name.isEmpty()) {
                out.add(name);
            }
        }
        return out.toArray(new String[0]);
    }

    /**
     * Fail loudly at startup if a requested covariate is unknown or its input
     * network isn't wired - otherwise every row would be silently dropped later
     * and the run would end with an empty training set and no explanation.
     */
    private void validateSelectedFeatures() {
        List<String> known = java.util.Arrays.asList(AVAILABLE_FEATURES);
        for (String f : selectedFeatures) {
            if (!known.contains(f)) {
                getModel().getRuntime().sendHalt(
                        "IsoBiasTrainingCollector: unknown feature \"" + f + "\" in featureColumns. "
                        + "Available: " + java.util.Arrays.toString(AVAILABLE_FEATURES)
                );
                return;
            }
            String missing = null;
            if (f.equals("tmean") && tmeanNetworkArray == null) {
                missing = "tmeanNetworkArray";
            } else if (f.equals("rhum") && rhumNetworkArray == null) {
                missing = "rhumNetworkArray";
            } else if (f.equals("tempRange") && (tmaxNetworkArray == null || tminNetworkArray == null)) {
                missing = "tmaxNetworkArray/tminNetworkArray";
            } else if ((f.equals("amount") || f.startsWith("antecedent"))
                    && (amountArray == null || amountNetworkXCoord == null || amountNetworkYCoord == null)) {
                missing = "amountArray/amountNetworkXCoord/amountNetworkYCoord";
            } else if (f.equals("iemi") && iemiFile == null) {
                missing = "iemiFile";
            }
            if (missing != null) {
                getModel().getRuntime().sendHalt(
                        "IsoBiasTrainingCollector: feature \"" + f + "\" is selected but its "
                        + "input (" + missing + ") is not wired."
                );
                return;
            }
        }
    }

    private void loadIemi() {
        if (iemiFile == null) {
            return;
        }
        try {
            // Resolve against the model workspace, exactly as outputFile is. A raw
            // relative path would resolve against the JVM's working directory,
            // which on the JAMS cloud server is not the per-run exec directory -
            // and an absolute Windows path does not exist there at all. Absolute
            // paths are passed through unchanged so older models keep working.
            String iemiPath = iemiFile.getValue();
            if (!new java.io.File(iemiPath).isAbsolute()) {
                iemiPath = FileTools.createAbsoluteFileName(
                        getModel().getWorkspaceDirectory().getAbsolutePath(), iemiPath);
            }
            iemiByMonth = MonthlyIndexLookup.load(
                    iemiPath, iemiStation.getValue(),
                    iemiStationColumn.getValue(), iemiDateColumn.getValue(),
                    iemiValueColumn.getValue()
            );
            getModel().getRuntime().println(
                    "IsoBiasTrainingCollector: loaded " + iemiByMonth.size()
                    + " monthly " + iemiValueColumn.getValue() + " values for station "
                    + iemiStation.getValue()
            );
        } catch (Exception e) {
            getModel().getRuntime().sendHalt(
                    "IsoBiasTrainingCollector: could not load iemiFile: " + e.toString()
            );
        }
    }

    private void writeFeatureList() {
        BufferedWriter writer = null;
        try {
            String path = FileTools.createAbsoluteFileName(
                    getModel().getWorkspaceDirectory().getAbsolutePath(),
                    featureListFile.getValue()
            );
            writer = new BufferedWriter(new FileWriter(path));
            // The target transform, so IsoMLBiasCorrection can invert the GP's
            // prediction back into permil. Marked with a leading # so a reader
            // that predates standardisation skips it as a comment.
            writer.write("#target" + "\t" + targetMean + "\t" + targetSd);
            writer.newLine();
            for (int k = 0; k < selectedFeatures.length; k++) {
                // name<TAB>centre<TAB>scale. IsoMLBiasCorrection must apply the
                // identical transform or it queries the GP in different units from
                // the ones it was trained in - a silent, total corruption of the
                // correction. A bare name (no columns) is read as centre 0 scale 1,
                // so model files from before standardisation still load.
                writer.write(selectedFeatures[k]);
                writer.write("\t");
                writer.write(Double.toString(featureMean == null ? 0.0 : featureMean[k]));
                writer.write("\t");
                writer.write(Double.toString(featureSd == null ? 1.0 : featureSd[k]));
                writer.newLine();
            }
        } catch (IOException e) {
            getModel().getRuntime().sendHalt(
                    "IsoBiasTrainingCollector: could not write featureListFile: " + e.toString()
                    + " - IsoMLBiasCorrection needs this file to build a matching feature vector."
            );
        } finally {
            closeQuietly(writer);
        }
    }

    /**
     * Work out, once, which simulated/amount array position corresponds to each
     * observed station - by column ID where the IDs are wired, otherwise falling
     * back to array position with a loud warning when the lengths disagree.
     */
    private void buildStationMapping(double[] observed, double[] simulated, double[] amount) {
        mappingBuilt = true;
        int n = observed.length;
        simIdx = new int[n];
        amtIdx = new int[n];

        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: array lengths - observed=" + observed.length
                + ", simulated=" + simulated.length
                + ", elevation=" + elevationArray.getValue().length
                + ", amount=" + (amount == null ? "n/a" : String.valueOf(amount.length))
        );

        Map<String, Integer> simById = indexById(simulatedIDs);
        Map<String, Integer> amtById = indexById(amountIDs);
        String[] obsIds = (observedIDs == null) ? null : observedIDs.getValue();

        int simMatched = 0, amtMatched = 0;
        for (int i = 0; i < n; i++) {
            simIdx[i] = -1;
            amtIdx[i] = -1;

            if (obsIds != null && i < obsIds.length && simById != null) {
                Integer j = simById.get(obsIds[i].trim());
                simIdx[i] = (j == null) ? -1 : j;
            } else if (i < simulated.length) {
                simIdx[i] = i; // positional fallback
            }
            if (simIdx[i] >= 0) {
                simMatched++;
            }

            if (amount != null) {
                if (obsIds != null && i < obsIds.length && amtById != null) {
                    Integer j = amtById.get(obsIds[i].trim());
                    amtIdx[i] = (j == null) ? -1 : j;
                } else if (i < amount.length) {
                    amtIdx[i] = i;
                }
                if (amtIdx[i] >= 0) {
                    amtMatched++;
                }
            }
        }

        if (simulatedNetworkXCoord != null && simulatedNetworkYCoord != null) {
            getModel().getRuntime().println(
                    "IsoBiasTrainingCollector: sampling the simulated field at each "
                    + "station's coordinates (nearest of " + simulatedNetworkXCoord.getValue().length
                    + " simulated points) - column-ID matching not used");
        }
        chooseHoldoutStations(n);
        resolveStationElevations(n, obsIds);
        applyExplicitElevations(n);

        if (simById != null) {
            getModel().getRuntime().println(
                    "IsoBiasTrainingCollector: matched " + simMatched + "/" + n
                    + " observed stations to a simulated column BY ID"
                    + (amount == null ? "" : ", " + amtMatched + "/" + n + " to a rainfall column")
            );
        } else {
            getModel().getRuntime().println(
                    "IsoBiasTrainingCollector: WARNING - matching observed to simulated BY ARRAY "
                    + "POSITION (simulatedIDs not wired). This is only valid if both datastores "
                    + "hold the same stations in the same order; if they do not, every bias value "
                    + "pairs unrelated stations and the training target is meaningless. Wire "
                    + "observedIDs/simulatedIDs (the readers' columnID) to match by ID instead."
            );
            if (simulated.length != observed.length) {
                getModel().getRuntime().println(
                        "IsoBiasTrainingCollector: WARNING - simulated array has "
                        + simulated.length + " columns but observed has " + observed.length
                        + "; positional matching cannot be correct here."
                );
            }
        }
    }

    /**
     * Take each station's elevation from the HRU it sits in, rather than from
     * the datastore's own elevation field. Needed here because that field is
     * not a usable elevation for this network, and because the simulated
     * isotope field is regionalised with elevationCorrection switched off - so
     * an uncorrected altitude effect is expected to sit in the residual and
     * cannot be seen through a constant covariate.
     */
    private void resolveStationElevations(int n, String[] obsIds) {
        if (hrus == null || obsIds == null) {
            return;
        }
        double nodata = JAMS.getMissingDataValue();
        Map<Long, Double> hruElev = new HashMap<Long, Double>();
        for (Attribute.Entity e : hrus.getEntities()) {
            try {
                hruElev.put((long) e.getDouble("ID"), e.getDouble("elevation"));
            } catch (Exception ex) {
                // entity without ID/elevation - skip
            }
        }

        stationElevation = new double[n];
        int resolved = 0;
        double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            Double v = null;
            if (i < obsIds.length) {
                try {
                    v = hruElev.get((long) Double.parseDouble(obsIds[i].trim()));
                } catch (NumberFormatException ex) {
                    v = null;
                }
            }
            stationElevation[i] = (v == null) ? nodata : v;
            if (v != null) {
                resolved++;
                lo = Math.min(lo, v);
                hi = Math.max(hi, v);
            }
        }
        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: resolved elevation from HRUs for "
                + resolved + "/" + n + " stations"
                + (resolved > 0 ? String.format(" (range %.0f - %.0f m)", lo, hi) : "")
        );
    }

    /**
     * Explicit surveyed station elevations, overriding anything resolved from
     * the HRUs or the datastore. Positional, so the count is validated against
     * the observed array rather than trusted.
     */
    private void applyExplicitElevations(int n) {
        if (stationElevations == null || stationElevations.getValue() == null
                || stationElevations.getValue().trim().isEmpty()) {
            return;
        }
        String[] parts = stationElevations.getValue().split(";");
        List<Double> vals = new ArrayList<Double>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                try {
                    vals.add(Double.valueOf(t));
                } catch (NumberFormatException e) {
                    getModel().getRuntime().sendHalt(
                            "IsoBiasTrainingCollector: stationElevations contains a "
                            + "non-numeric entry: \"" + t + "\"");
                    return;
                }
            }
        }
        if (vals.size() != n) {
            getModel().getRuntime().sendHalt(
                    "IsoBiasTrainingCollector: stationElevations has " + vals.size()
                    + " entries but observedArray has " + n + " columns. This list is "
                    + "positional, so a count mismatch means the values would be "
                    + "assigned to the wrong stations.");
            return;
        }

        double nodata = JAMS.getMissingDataValue();
        stationElevation = new double[n];
        int usable = 0;
        double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double v = vals.get(i);
            // -9999 is the convention this network uses for an outside-basin
            // station; map it onto whatever sentinel JAMS is configured with.
            stationElevation[i] = (v == -9999.0) ? nodata : v;
            if (stationElevation[i] != nodata) {
                usable++;
                lo = Math.min(lo, v);
                hi = Math.max(hi, v);
            }
        }
        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: using explicit stationElevations for "
                + usable + "/" + n + " stations"
                + (usable > 0 ? String.format(" (range %.0f - %.0f m)", lo, hi) : "")
                + ", " + (n - usable) + " flagged outside-basin");
    }

    /** Value at the nearest source point to (targetX, targetY); nodata if none valid. */
    private double nearestValue(double targetX, double targetY,
            double[] srcX, double[] srcY, double[] srcValue, double nodata) {
        double best = Double.MAX_VALUE;
        double value = nodata;
        int n = Math.min(srcValue.length, Math.min(srcX.length, srcY.length));
        for (int j = 0; j < n; j++) {
            if (srcValue[j] == nodata) {
                continue;
            }
            double dx = targetX - srcX[j];
            double dy = targetY - srcY[j];
            double d2 = dx * dx + dy * dy;
            if (d2 < best) {
                best = d2;
                value = srcValue[j];
            }
        }
        return value;
    }

    /**
     * Pick whole stations to withhold, spread evenly across the column order and
     * chosen deterministically so a rerun is reproducible.
     */
    private void chooseHoldoutStations(int n) {
        isHoldoutStation = new boolean[n];
        double frac = (holdoutFraction == null) ? 0.0 : holdoutFraction.getValue();
        if (frac <= 0.0) {
            return;
        }
        if (frac > 0.9) {
            frac = 0.9;
        }
        int step = (int) Math.max(2, Math.round(1.0 / frac));
        int held = 0;
        for (int i = 0; i < n; i++) {
            if (i % step == 0) {
                isHoldoutStation[i] = true;
                held++;
            }
        }
        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: holding out " + held + "/" + n
                + " stations (every " + step + "th) for out-of-sample validation");
    }

    private Map<String, Integer> indexById(Attribute.StringArray ids) {
        if (ids == null || ids.getValue() == null) {
            return null;
        }
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        String[] v = ids.getValue();
        for (int i = 0; i < v.length; i++) {
            map.put(v[i].trim(), i);
        }
        return map;
    }

    @Override
    public void run() {
        double[] observed = observedArray.getValue();
        double[] simulated = simulatedArray.getValue();
        double[] elevation = elevationArray.getValue();
        double[] xCoord = xCoordArray.getValue();
        double[] yCoord = yCoordArray.getValue();
        double[] amount = (amountArray != null) ? amountArray.getValue() : null;

        if (!mappingBuilt) {
            buildStationMapping(observed, simulated, amount);
        }
        if (stationElevation != null) {
            elevation = stationElevation;
        }


        double nodata = JAMS.getMissingDataValue();
        double minAmount = amountThreshold.getValue();
        double idwPower = tmeanIdwPower.getValue();
        int dayOfYear = time.get(Attribute.Calendar.DAY_OF_YEAR);

        // The rain-gauge network is its own station set (41 columns here) that
        // is NOT co-located with the isotope collectors - ID matching found 0 of
        // 24 in common - so neither positional nor ID matching is meaningful for
        // it. Interpolate it onto each isotope station's coordinates instead,
        // exactly as tmean/rhum/tmax/tmin are handled.
        // The simulated field is an isoRSM GRID (sequential ids, synthetic names,
        // regular x/y spacing), not a set of real stations - so there is no
        // meaningful column correspondence with the isotope network. Sample it at
        // each station's location instead, using the nearest grid point, which is
        // what IsoRegionaliser does with this same field (nidw=1).
        double[] simAtStation = null;
        if (simulatedNetworkXCoord != null && simulatedNetworkYCoord != null) {
            simAtStation = new double[observed.length];
            for (int i = 0; i < observed.length; i++) {
                simAtStation[i] = nearestValue(xCoord[i], yCoord[i],
                        simulatedNetworkXCoord.getValue(), simulatedNetworkYCoord.getValue(),
                        simulated, nodata);
            }
        }

        double[] precipAtStation = null;
        if (amount != null && amountNetworkXCoord != null && amountNetworkYCoord != null) {
            precipAtStation = new double[observed.length];
            for (int i = 0; i < observed.length; i++) {
                precipAtStation[i] = idwInterpolate(xCoord[i], yCoord[i],
                        amountNetworkXCoord.getValue(), amountNetworkYCoord.getValue(),
                        amount, nodata, idwPower);
            }
        }

        Double iemiValue = null;
        if (iemiByMonth != null) {
            // java.util.Calendar.MONTH is 0-based (JANUARY=0)
            iemiValue = iemiByMonth.get(MonthlyIndexLookup.yearMonthKey(
                    time.get(Attribute.Calendar.YEAR),
                    time.get(Attribute.Calendar.MONTH) + 1));
        }

        // Outside-basin stations feed the running average and nothing else.
        double outsideSum = 0.0;
        int outsideCount = 0;
        for (int i = 0; i < observed.length; i++) {
            if (elevation[i] == nodata && observed[i] != nodata) {
                outsideSum += observed[i];
                outsideCount++;
            }
        }
        if (outsideCount > 0) {
            runningOutsideAvg = outsideSum / outsideCount;
            haveOutsideAvg = true;
        }
        outsideBasinAvg.setValue(runningOutsideAvg);

        for (int i = 0; i < observed.length; i++) {
            seenCandidates++;

            if (elevation[i] == nodata) {
                dropOutsideStation++;
                continue;
            }
            if (observed[i] == nodata) {
                dropObserved++;
                continue;
            }
            double simValue;
            if (simAtStation != null) {
                simValue = simAtStation[i];
            } else if (simIdx[i] < 0) {
                dropNoStationMatch++;
                continue;
            } else {
                simValue = simulated[simIdx[i]];
            }
            if (simValue == nodata) {
                dropSimulated++;
                continue;
            }
            // Only gate on rainfall amount when an explicit positive threshold is
            // set. Under the default threshold of 0 a MISSING amount must not
            // discard an otherwise-valid isotope observation: the isotope network
            // and the rain-gauge network have different gaps, and dropping on
            // nodata here was discarding ~78% of all available observations
            // (350 of 450) for no benefit. If "amount" is itself a selected
            // feature, a missing value is still caught by the feature loop below
            // and counted as featureUnavailable, which is the honest place for it.
            if (minAmount > 0.0) {
                double a = (precipAtStation == null) ? nodata : precipAtStation[i];
                if (a == nodata || a < minAmount) {
                    dropAmount++;
                    continue;
                }
            }

            Map<String, Double> candidate = computeFeatures(
                    i, precipAtStation, elevation, xCoord, yCoord, dayOfYear,
                    iemiValue, nodata, idwPower
            );

            double[] x = new double[selectedFeatures.length];
            boolean usable = true;
            for (int k = 0; k < selectedFeatures.length; k++) {
                Double v = candidate.get(selectedFeatures[k]);
                if (v == null || v == nodata || Double.isNaN(v)) {
                    usable = false;
                    break;
                }
                x[k] = v;
            }
            if (!usable) {
                dropFeatureUnavailable++;
                continue;
            }

            double bias = observed[i] - simValue;

            // Exact-duplicate rows make the GP's covariance matrix singular.
            if (!seenRows.add(java.util.Arrays.toString(x) + "|" + bias)) {
                dropDuplicate++;
                continue;
            }

            if (isHoldoutStation != null && isHoldoutStation[i]) {
                vFeatures.add(x);
                vTargets.add(bias);
            } else {
                features.add(x);
                targets.add(bias);
            }
        }

        // Record today's precipitation AFTER collecting, so antecedent windows
        // cover strictly prior days.
        if (precipAtStation != null) {
            precipHistory.addFirst(precipAtStation.clone());
            while (precipHistory.size() > MAX_ANTECEDENT_WINDOW) {
                precipHistory.removeLast();
            }
        }

        // Publish every timestep: JAMS runs every top-level sibling's run()
        // phase before cascading cleanup(), so a post-TimeLoop consumer
        // (GaussianLearner) needs trainData populated before this component's
        // own cleanup() fires.
        publishTrainData();
    }

    private Map<String, Double> computeFeatures(int i, double[] precipAtStation,
            double[] elevation, double[] xCoord, double[] yCoord, int dayOfYear,
            Double iemiValue, double nodata, double idwPower) {

        Map<String, Double> f = new LinkedHashMap<String, Double>();
        f.put("elevation", elevation[i]);
        f.put("xCoord", xCoord[i]);
        f.put("yCoord", yCoord[i]);
        f.put("dayOfYear", (double) dayOfYear);
        // Day-of-year is CYCLIC: 31 December and 1 January are one day apart,
        // but as a plain number they sit 364 units apart at opposite ends of a
        // straight line. A GP with a linear mean model therefore fits a ramp
        // across the season and keeps extrapolating it - the isoGSM arm's
        // correction slid from +43.5 permil in January to -105.9 in December,
        // hitting the -200 clamp, because the training samples only span
        // dayOfYear 19..218 and 39% of days were outside that range.
        // Projecting onto the unit circle removes the discontinuity, and
        // because sin/cos are bounded in [-1,1] the linear mean cannot run away.
        f.put("doySin", Math.sin(2.0 * Math.PI * dayOfYear / DAYS_IN_YEAR));
        f.put("doyCos", Math.cos(2.0 * Math.PI * dayOfYear / DAYS_IN_YEAR));
        f.put("outsideBasinAvg", runningOutsideAvg);
        f.put("iemi", iemiValue);

        if (precipAtStation != null && precipAtStation[i] != nodata) {
            f.put("amount", precipAtStation[i]);
            f.put("antecedent7", antecedentSum(i, 7, nodata));
            f.put("antecedent30", antecedentSum(i, 30, nodata));
        }
        if (tmeanNetworkArray != null) {
            f.put("tmean", idwOrNull(xCoord[i], yCoord[i], tmeanNetworkXCoord,
                    tmeanNetworkYCoord, tmeanNetworkArray, nodata, idwPower));
        }
        if (rhumNetworkArray != null) {
            f.put("rhum", idwOrNull(xCoord[i], yCoord[i], rhumNetworkXCoord,
                    rhumNetworkYCoord, rhumNetworkArray, nodata, idwPower));
        }
        if (tmaxNetworkArray != null && tminNetworkArray != null) {
            Double tmax = idwOrNull(xCoord[i], yCoord[i], tmaxNetworkXCoord,
                    tmaxNetworkYCoord, tmaxNetworkArray, nodata, idwPower);
            Double tmin = idwOrNull(xCoord[i], yCoord[i], tminNetworkXCoord,
                    tminNetworkYCoord, tminNetworkArray, nodata, idwPower);
            f.put("tempRange", (tmax == null || tmin == null) ? null : tmax - tmin);
        }
        return f;
    }

    /** Cumulative precipitation at station i over the previous {@code days} days. */
    private double antecedentSum(int i, int days, double nodata) {
        double sum = 0.0;
        int n = 0;
        for (double[] past : precipHistory) {
            if (n++ >= days) {
                break;
            }
            if (i < past.length && past[i] != nodata) {
                sum += past[i];
            }
        }
        return sum;
    }

    private Double idwOrNull(double targetX, double targetY,
            Attribute.DoubleArray srcX, Attribute.DoubleArray srcY,
            Attribute.DoubleArray srcValue, double nodata, double power) {

        if (srcX == null || srcY == null || srcValue == null) {
            return null;
        }
        double v = idwInterpolate(targetX, targetY, srcX.getValue(),
                srcY.getValue(), srcValue.getValue(), nodata, power);
        return (v == nodata) ? null : v;
    }

    private void publishTrainData() {
        fitScaling();
        fill(trainData, features, targets);
        if (validationData != null) {
            // With no holdout configured, mirror the training set so a wired
            // GaussianLearner still has something to score against - but note
            // that score is in-sample and not a measure of skill.
            if (vFeatures.isEmpty()) {
                fill(validationData, features, targets);
            } else {
                fill(validationData, vFeatures, vTargets);
            }
        }
    }

    /**
     * Fit the centre and scale from the TRAINING rows only.
     *
     * Held-out rows are deliberately excluded: standardising with statistics that
     * saw the holdout would leak it into the fit and flatter the validation score
     * that resultFile reports. Recomputed on each publish because rows accumulate
     * as the run proceeds and the learner reads whatever is current; at a few
     * hundred rows the cost is nil.
     */
    private void fitScaling() {
        int d = selectedFeatures.length;
        featureMean = new double[d];
        featureSd = new double[d];
        java.util.Arrays.fill(featureSd, 1.0);

        targetMean = 0.0;
        targetSd = 1.0;

        boolean on = (standardiseData == null) || standardiseData.getValue();
        if (!on || features.isEmpty()) {
            return;
        }
        int n = features.size();
        for (int k = 0; k < d; k++) {
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += features.get(i)[k];
            }
            double mean = sum / n;
            double ss = 0.0;
            for (int i = 0; i < n; i++) {
                double dev = features.get(i)[k] - mean;
                ss += dev * dev;
            }
            double sd = Math.sqrt(ss / n);
            featureMean[k] = mean;
            // A constant covariate carries no information; leaving its scale at 1
            // passes it through as a column of zeros rather than dividing by zero.
            featureSd[k] = (sd > 1e-12) ? sd : 1.0;
        }

        // The target too. Without this the covariate scaling buys nothing: the
        // kernel's prior variance is fixed at 1 and cannot stretch to a residual
        // whose variance is in the hundreds.
        double tsum = 0.0;
        for (int i = 0; i < n; i++) {
            tsum += targets.get(i);
        }
        targetMean = tsum / n;
        double tss = 0.0;
        for (int i = 0; i < n; i++) {
            double dev = targets.get(i) - targetMean;
            tss += dev * dev;
        }
        double tsd = Math.sqrt(tss / n);
        targetSd = (tsd > 1e-12) ? tsd : 1.0;
    }

    private void fill(Attribute.Entity target, List<double[]> f, List<Double> t) {
        int n = f.size();
        double[][] data = new double[n][];
        double[] predict = new double[n];
        for (int i = 0; i < n; i++) {
            // Copy rather than hand over the stored row: the stored rows stay raw
            // so writeSamples() keeps dumping physical units, and scaling in place
            // would re-scale the same arrays on every timestep's publish.
            double[] raw = f.get(i);
            double[] row = new double[raw.length];
            for (int k = 0; k < raw.length; k++) {
                row[k] = (raw[k] - featureMean[k]) / featureSd[k];
            }
            data[i] = row;
            predict[i] = (t.get(i) - targetMean) / targetSd;
        }
        target.setObject("data", data);
        target.setObject("predict", predict);
    }

    /**
     * Plain inverse-distance-weighted interpolation of one station network's
     * array onto a single target coordinate. Mirrors the weighting form used by
     * org.unijena.j2k.regionalisation.Regionalisation elsewhere in this model,
     * but computed on the fly here rather than from precomputed entity weights,
     * since the target is a station coordinate, not an HRU. No elevation
     * lapse-rate correction is applied.
     */
    private double idwInterpolate(double targetX, double targetY,
            double[] srcX, double[] srcY, double[] srcValue,
            double nodata, double power) {

        double weightedSum = 0.0;
        double weightTotal = 0.0;

        for (int j = 0; j < srcValue.length; j++) {
            if (srcValue[j] == nodata) {
                continue;
            }
            double dx = targetX - srcX[j];
            double dy = targetY - srcY[j];
            double dist2 = dx * dx + dy * dy;

            if (dist2 < 1e-9) {
                return srcValue[j]; // coincident station
            }
            double weight = 1.0 / Math.pow(dist2, power / 2.0);
            weightedSum += weight * srcValue[j];
            weightTotal += weight;
        }
        return (weightTotal <= 0.0) ? nodata : weightedSum / weightTotal;
    }

    @Override
    public void cleanup() {
        publishTrainData();
        int n = features.size();

        // Rewrite the handshake file now the scaling is actually fitted - the
        // copy written at init() could only carry the names. GaussianLearner
        // serializes its model at cleanup too, so the pair stays consistent.
        writeFeatureList();
        if (featureMean != null && (standardiseData == null || standardiseData.getValue())) {
            StringBuilder sb = new StringBuilder(
                    "IsoBiasTrainingCollector: standardised covariates (centre / scale) -");
            for (int k = 0; k < selectedFeatures.length; k++) {
                sb.append(" ").append(selectedFeatures[k]).append("=")
                        .append(String.format("%.4g", featureMean[k])).append("/")
                        .append(String.format("%.4g", featureSd[k]));
            }
            sb.append("  |  target=").append(String.format("%.4g", targetMean))
                    .append("/").append(String.format("%.4g", targetSd));
            getModel().getRuntime().println(sb.toString());
        }

        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: collected " + n + " training samples ("
                + vFeatures.size() + " held out) from "
                + seenCandidates + " candidate station-days"
        );
        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: dropped - outsideBasinStation=" + dropOutsideStation
                + ", noObserved=" + dropObserved
                + ", noStationMatch=" + dropNoStationMatch
                + ", noSimulated=" + dropSimulated
                + ", amountFilter=" + dropAmount
                + ", featureUnavailable=" + dropFeatureUnavailable
                + ", duplicateRow=" + dropDuplicate
        );
        getModel().getRuntime().println(
                "IsoBiasTrainingCollector: outside-basin observation ever seen this run: "
                + haveOutsideAvg + (haveOutsideAvg ? "" : " - outsideBasinAvg stayed at its "
                + "neutral default (0.0) for every sample, i.e. it carried no information "
                + "this run")
        );

        if (outputFile != null && outputFile.getValue() != null && !outputFile.getValue().isEmpty()) {
            writeSamples();
        }
    }

    private void writeSamples() {
        BufferedWriter writer = null;
        try {
            // Resolve against the workspace directory, the same way
            // GaussianLearner resolves its own resultFile/modelDataFile - a bare
            // relative path would otherwise resolve against the JVM working
            // directory, which is not the workspace.
            String path = FileTools.createAbsoluteFileName(
                    getModel().getWorkspaceDirectory().getAbsolutePath(),
                    outputFile.getValue()
            );
            writer = new BufferedWriter(new FileWriter(path));

            StringBuilder header = new StringBuilder();
            for (String f : selectedFeatures) {
                header.append(f).append("\t");
            }
            header.append("bias");
            writer.write(header.toString());
            writer.newLine();

            for (int i = 0; i < features.size(); i++) {
                StringBuilder line = new StringBuilder();
                for (double v : features.get(i)) {
                    line.append(v).append("\t");
                }
                line.append(targets.get(i));
                writer.write(line.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            getModel().getRuntime().sendInfoMsg(
                    "IsoBiasTrainingCollector: could not write output file: " + e.toString()
            );
        } finally {
            closeQuietly(writer);
        }
    }

    private void closeQuietly(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                // nothing to do
            }
        }
    }
}
