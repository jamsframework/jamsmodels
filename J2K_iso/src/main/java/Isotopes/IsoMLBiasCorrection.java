package Isotopes;

/*
 * IsoMLBiasCorrection.java
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
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import jams.components.machineLearning.kernels.FixedMeanModell;
import jams.components.machineLearning.kernels.Kernel;
import jams.components.machineLearning.kernels.LinearMeanModell;
import jams.components.machineLearning.kernels.MaternClass;
import jams.components.machineLearning.kernels.MeanModell;

import Jama.Matrix;
import Jama.CholeskyDecomposition;
import Jama.LUDecomposition;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import jams.JAMS;

/**
 * Applies an offline-trained precipitation-isotope bias corrector (trained by
 * jams.components.machineLearning.GaussianLearner, see IsoBiasTrainingCollector
 * for the training-set assembly) inside the live per-HRU, per-timestep loop.
 *
 * This does NOT reuse GaussianLearner's own instance methods: its internal
 * fields (kernel, alpha, Solver, fastSolver, Observations, ...) are
 * package-private in jams.components.machineLearning, not visible from this
 * package, and GaussianLearner's own "apply" mode is batch-oriented rather than
 * suited to being called once per HRU per timestep. Instead, this component
 * independently reads the exact same file format GaussianLearner.serializeModel()
 * writes, and replicates its getMean()/getVariance() math using the same public
 * Jama/Kernel/MeanModell APIs. If GaussianLearner's serialization format ever
 * changes, this class needs to change with it.
 *
 * The feature vector is NOT hard-coded: it is read from the featureListFile
 * IsoBiasTrainingCollector wrote during training, and assembled by name. That
 * handshake is what keeps training and application in step - a hard-coded list
 * here would silently query the GP with the wrong features whenever the
 * training feature set changed.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IsoMLBiasCorrection",
        author = "Andrew Watson",
        description = "Applies an offline-trained Gaussian process precipitation-"
        + "isotope bias corrector, once per HRU per timestep, computing "
        + "correctedValue = rawValue + correctionWeight * gpMean(x). The "
        + "covariate vector is built by name from the feature list "
        + "IsoBiasTrainingCollector wrote at training time (featureListFile), so "
        + "the two cannot drift out of step. correctionWeight is the single "
        + "bounded parameter meant to be exposed to the outer catchment "
        + "calibration, so the optimizer decides how much to trust this "
        + "correction against the river/groundwater tracer efficiency metrics, "
        + "without the GP's own fitted shape being disturbed by that indirect "
        + "downstream signal. Also writes the GP's predictive variance, for "
        + "diagnosing where the correction is extrapolating.",
        date = "2026-09-23",
        version = "2.4_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1_0", comment = "Added outsideBasinAvg covariate"),
    @VersionComments.Entry(version = "1.2_0", comment = "Added iemi covariate"),
    @VersionComments.Entry(version = "2.0_0", comment = "Feature vector is now read "
            + "from featureListFile and assembled by name instead of hard-coded, "
            + "matching IsoBiasTrainingCollector 2.0_0; added per-HRU antecedent "
            + "precipitation, rhum and tempRange covariates"),
    @VersionComments.Entry(version = "2.1_0", comment = "Computes doySin/doyCos "
            + "to match IsoBiasTrainingCollector's cyclic day-of-year encoding. "
            + "The period constant must stay identical in both components."),
    @VersionComments.Entry(version = "2.2_0", comment = "A relative iemiFile is "
            + "now resolved against the model workspace, as modelDataFile and "
            + "featureListFile already were. It used to be opened with a raw "
            + "FileReader, which resolves against the JVM working directory - not "
            + "the per-run exec directory on the JAMS cloud server. Absolute paths "
            + "are passed through unchanged."),
    @VersionComments.Entry(version = "2.4_0", comment = "SIMPLE PIPELINE. Set "
            + "trainingDataFile and logTheta and this component builds the GP itself at model "
            + "start, so the three-stage arrangement (collector run, separate builder run "
            + "writing a serialized .dat, correction reading it) collapses to one model that "
            + "SHOWS the hyperparameters it runs with. Covariate names come from the training "
            + "file header, and the centre and scale are refitted from the same rows, so a "
            + "feature list cannot fall out of step. Every arithmetic step is "
            + "IsoGpModelBuilder's, in the same order, so corrections are identical to the "
            + "serialized path, which still works when trainingDataFile is left empty."),
    @VersionComments.Entry(version = "2.3_0", comment = "(1) clipToTrainingRange "
            + "(default true): each standardised covariate is clipped to the range "
            + "the GP was trained on before it is queried. The linear mean model "
            + "otherwise extrapolates without bound - the isoGSM arm, trained on 2023 "
            + "only (iEMI 0.18..1.50), was queried at iEMI down to -1.08 and pushed "
            + "rain d2H to the +50 clamp. (2) tmean, tempRange, rhum, amount and "
            + "antecedent7/30 are now computed exactly as IsoBiasTrainingCollector "
            + "computes them: inverse-distance weighting over ALL stations of the "
            + "raw network arrays (power idwPower), at the HRU coordinates. The HRU "
            + "attributes tmean/tmax/tmin/rhum/rain were built differently (nidw 3, "
            + "corrected and elevation-adjusted rain), so the GP was being queried "
            + "with covariates it was never trained on; those inputs are now ignored. "
            + "(3) per-run state (antecedent history, clip counters) is reset in "
            + "init(), so repeated runs in one JVM start identically. (4) normalised "
            + "training rows and the covariate-independent variance terms are "
            + "precomputed, and the variance is only computed when it is wired - "
            + "identical results, far less work when covariates vary by HRU; the "
            + "antecedent history is a primitive ring buffer instead of a list of "
            + "boxed values.")
})
public class IsoMLBiasCorrection extends JAMSComponent {

    private static final int MAX_ANTECEDENT_WINDOW = 30;

    /**
     * Period for the cyclic day-of-year encoding. Must equal the collector's
     * DAYS_IN_YEAR, or training and prediction sit in different spaces.
     */
    private static final double DAYS_IN_YEAR = 365.25;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Path to the serialized GP model written by GaussianLearner"
    )
    public Attribute.String modelDataFile;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "SIMPLE PIPELINE. Training rows written by IsoBiasTrainingCollector "
            + "(tab separated, one column per covariate and the bias last). Set this together "
            + "with logTheta and the component builds the GP itself at model start, so no "
            + "separate training run, serialized model file or feature list is needed. Leave "
            + "empty to load a serialized model from modelDataFile instead."
    )
    public Attribute.String trainingDataFile = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "SIMPLE PIPELINE. GP hyperparameters as log values, comma separated, "
            + "one length scale per covariate in the order of the training file plus sigma last. "
            + "Fitted once in Python from trainingDataFile and pasted here, so the values the "
            + "model runs with are visible in the model. They are NOT calibrated - the optimiser "
            + "only scales the correction through correctionWeight."
    )
    public Attribute.String logTheta = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "SIMPLE PIPELINE. GP mean function, \"constant\" (falls back to the mean "
            + "training bias away from the data) or \"linear\".",
            defaultValue = "constant"
    )
    public Attribute.String meanModel = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Feature list written by IsoBiasTrainingCollector - defines "
            + "the covariate order this component must reproduce",
            defaultValue = "output/iso_bias_gp_features.txt"
    )
    public Attribute.String featureListFile;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU ID - used to key each HRU's own antecedent-precipitation history"
    )
    public Attribute.Double id = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU elevation [m]"
    )
    public Attribute.Double elevation = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU x coordinate"
    )
    public Attribute.Double xCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU y coordinate"
    )
    public Attribute.Double yCoord = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "DEPRECATED since 2.3_0, ignored. The HRU temperature is "
            + "regionalised differently from the covariate the GP was trained on; "
            + "wire tmeanNetworkArray and its coordinates instead."
    )
    public Attribute.Double tmean = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "DEPRECATED since 2.3_0, ignored - wire tmaxNetworkArray"
    )
    public Attribute.Double tmax = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "DEPRECATED since 2.3_0, ignored - wire tminNetworkArray"
    )
    public Attribute.Double tmin = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "DEPRECATED since 2.3_0, ignored - wire rhumNetworkArray"
    )
    public Attribute.Double rhum = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "DEPRECATED since 2.3_0, ignored - wire amountArray"
    )
    public Attribute.Double rain = null;

    // Station networks, wired to the SAME arrays as IsoBiasTrainingCollector's
    // inputs of the same name. The covariates are interpolated from them to the
    // HRU coordinates with the collector's own IDW rule, so a covariate means the
    // same thing when the GP is queried as when it was trained.
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Station tmean values - same array as the collector's tmeanNetworkArray")
    public Attribute.DoubleArray tmeanNetworkArray = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "x coordinates of tmeanNetworkArray")
    public Attribute.DoubleArray tmeanNetworkXCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "y coordinates of tmeanNetworkArray")
    public Attribute.DoubleArray tmeanNetworkYCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Station tmax values - same array as the collector's tmaxNetworkArray")
    public Attribute.DoubleArray tmaxNetworkArray = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "x coordinates of tmaxNetworkArray")
    public Attribute.DoubleArray tmaxNetworkXCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "y coordinates of tmaxNetworkArray")
    public Attribute.DoubleArray tmaxNetworkYCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Station tmin values - same array as the collector's tminNetworkArray")
    public Attribute.DoubleArray tminNetworkArray = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "x coordinates of tminNetworkArray")
    public Attribute.DoubleArray tminNetworkXCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "y coordinates of tminNetworkArray")
    public Attribute.DoubleArray tminNetworkYCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Station rhum values - same array as the collector's rhumNetworkArray")
    public Attribute.DoubleArray rhumNetworkArray = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "x coordinates of rhumNetworkArray")
    public Attribute.DoubleArray rhumNetworkXCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "y coordinates of rhumNetworkArray")
    public Attribute.DoubleArray rhumNetworkYCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Rain-gauge values - same array as the collector's amountArray "
            + "(the uncorrected gauge data, not the corrected/elevation-adjusted HRU rain)")
    public Attribute.DoubleArray amountArray = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "x coordinates of amountArray")
    public Attribute.DoubleArray amountNetworkXCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "y coordinates of amountArray")
    public Attribute.DoubleArray amountNetworkYCoord = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "IDW power - must equal the collector's tmeanIdwPower",
            defaultValue = "2.0")
    public Attribute.Double idwPower = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Clip each standardised covariate to the range the GP was "
            + "trained on before querying it. The mean model is linear, so outside "
            + "that range the correction otherwise grows without bound.",
            defaultValue = "true")
    public Attribute.Boolean clipToTrainingRange = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current model time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Raw regionalized isotope value to correct [permil]"
    )
    public Attribute.Double rawValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Running average of the outside-basin reference stations, "
            + "written by IsoBiasTrainingCollector each timestep"
    )
    public Attribute.Double outsideBasinAvg = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Path to the monthly regional index CSV - must be the same "
            + "file/station/column used at training time"
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
            description = "Global scalar controlling how much of the GP's mean "
            + "correction gets applied. Intended for the outer catchment optimizer.",
            defaultValue = "1.0"
    )
    public Attribute.Double correctionWeight;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Bias-corrected isotope value [permil]"
    )
    public Attribute.Double correctedValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "GP predictive variance at this HRU/timestep's covariate point"
    )
    public Attribute.Double correctedValueVariance = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Copy of the value BEFORE this correction [permil]. Wire "
            + "this when the component runs ahead of another corrector, because "
            + "that one's own \"uncorrected\" output would already carry this "
            + "correction and would not be the raw input."
    )
    public Attribute.Double uncorrectedValue = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lower bound on the corrected value [permil]. The GP is "
            + "unbounded and will extrapolate at an HRU/timestep unlike anything it "
            + "trained on; an implausible tracer value propagates into the mixing "
            + "chain and destabilises it, so the output is clamped exactly as "
            + "IsoPrecipCorrection clamps its own.",
            defaultValue = "-200.0"
    )
    public Attribute.Double minValue = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Upper bound on the corrected value [permil]",
            defaultValue = "50.0"
    )
    public Attribute.Double maxValue = null;

    // Deserialized model state - written once in init(), read-only afterwards,
    // so concurrent per-HRU invocation under HRULoop's ConcurrentContextProcessor
    // is safe.
    private double[] min, max, base;
    private double[][] trainX;
    private Matrix alpha;
    private Matrix observations;
    private int trainLength;
    private Kernel kernel;
    private CholeskyDecomposition fastSolver;
    private LUDecomposition solver;

    private String[] selectedFeatures;

    /** Centre and scale read from featureListFile; identity for a legacy list. */
    private double[] featureCentre, featureScale;
    /** Target transform, inverted on the GP output to get back to permil. */
    private double targetCentre = 0.0, targetScale = 1.0;
    private Map<String, Double> iemiByMonth;

    /**
     * Per-HRU antecedent-precipitation history. Each HRU only ever reads and
     * writes its own entry, so a ConcurrentHashMap is sufficient under
     * HRULoop's concurrent execution - no two threads touch the same key.
     */
    private final ConcurrentHashMap<Long, History> precipHistory
            = new ConcurrentHashMap<Long, History>();

    /**
     * The last MAX_ANTECEDENT_WINDOW daily amounts of one HRU, newest first, in a
     * primitive ring buffer. It replaces a LinkedList of boxed Doubles that cost
     * ~7 % of a whole model run when covariates vary by HRU; values are read in
     * the same newest-to-oldest order, so every sum is bit-identical.
     */
    private static final class History {
        final double[] v = new double[MAX_ANTECEDENT_WINDOW];
        int head = 0, size = 0;

        void addFirst(double x) {
            head = (head + MAX_ANTECEDENT_WINDOW - 1) % MAX_ANTECEDENT_WINDOW;
            v[head] = x;
            if (size < MAX_ANTECEDENT_WINDOW) {
                size++;
            }
        }
    }

    /** Training rows after the Learner min/max normalisation - fixed once loaded. */
    private double[][] trainXNorm;
    /** Per-covariate range of the (standardised) training rows. */
    private double[] trainLo, trainHi;
    /** Covariate-independent parts of predictVariance(), computed once. */
    private double varTOne, varMySigma;
    private boolean clip;

    /** Queries made, and queries clipped per covariate, in the current run. */
    private final AtomicLong nQueries = new AtomicLong();
    private AtomicLongArray nClipped;

    @Override
    public void init() {
        // Per-run state first, BEFORE the early return: an optimizer re-runs the
        // same component instance, and a history carried over from the previous
        // run would give the first 30 days of this run a different antecedent
        // covariate from a fresh single run.
        precipHistory.clear();
        nQueries.set(0);
        if (nClipped != null) {
            nClipped = new AtomicLongArray(nClipped.length());
        }
        nanWarned = false;
        if (kernel != null) {
            return; // model already loaded
        }

        boolean inline = trainingDataFile != null
                && trainingDataFile.getValue() != null
                && !trainingDataFile.getValue().trim().isEmpty();

        if (inline) {
            // SIMPLE PIPELINE: covariate names come from the training file's header, so no
            // separate feature list can fall out of step with the model.
            selectedFeatures = readTrainingHeader();
            if (selectedFeatures == null) {
                return; // already halted
            }
        } else {
            selectedFeatures = readFeatureList();
        }
        validateRequiredInputs();
        loadIemiIfNeeded();
        if (inline) {
            buildFromTrainingData();
        } else {
            deserializeModel();
        }
        if (trainX == null) {
            return; // the load or the build has already halted
        }
        precompute();
        clip = (clipToTrainingRange == null) || clipToTrainingRange.getValue();
        nClipped = new AtomicLongArray(selectedFeatures.length);

        if (trainX != null && trainX.length > 0 && trainX[0].length != selectedFeatures.length) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: the serialized model was trained on "
                    + trainX[0].length + " covariates but featureListFile lists "
                    + selectedFeatures.length + " - the model and the feature list are "
                    + "from different training runs. Re-run the training pass (with this "
                    + "component disabled) so both are regenerated together."
            );
            return;
        }

        getModel().getRuntime().println(
                "IsoMLBiasCorrection: applying GP trained on "
                + java.util.Arrays.toString(selectedFeatures)
                + (clip ? ", covariates clipped to the training range" : ", NO clipping")
        );
    }

    private void precompute() {
        int d = trainX[0].length;
        trainXNorm = new double[trainLength][];
        trainLo = new double[d];
        trainHi = new double[d];
        java.util.Arrays.fill(trainLo, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(trainHi, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < trainLength; i++) {
            trainXNorm[i] = normalize(trainX[i]);
            for (int k = 0; k < d; k++) {
                trainLo[k] = Math.min(trainLo[k], trainX[i][k]);
                trainHi[k] = Math.max(trainHi[k], trainX[i][k]);
            }
        }
        if (correctedValueVariance == null) {
            return;
        }
        // Everything in GaussianLearner.getVariance() that does not involve the
        // query point, evaluated with the same operations in the same order.
        Matrix one = new Matrix(1, trainLength);
        Matrix oneT = new Matrix(trainLength, 1);
        for (int i = 0; i < trainLength; i++) {
            one.set(0, i, 1.0);
            oneT.set(i, 0, 1.0);
        }
        Matrix rMinus1Eins = (fastSolver != null) ? fastSolver.solve(oneT) : solver.solve(oneT);
        varTOne = one.times(rMinus1Eins).get(0, 0);
        double myHat = one.times(alpha).get(0, 0) / varTOne;
        Matrix tmp = new Matrix(1, trainLength);
        for (int i = 0; i < trainLength; i++) {
            tmp.set(0, i, observations.get(i, 0) - myHat);
        }
        Matrix solveTmp = (fastSolver != null)
                ? fastSolver.solve(tmp.transpose()) : solver.solve(tmp.transpose());
        varMySigma = tmp.times(solveTmp).get(0, 0) / observations.getRowDimension();
    }

    @Override
    public void cleanup() {
        long n = nQueries.get();
        if (!clip || n == 0 || nClipped == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(
                "IsoMLBiasCorrection: share of GP queries clipped to the training range -");
        for (int k = 0; k < selectedFeatures.length; k++) {
            sb.append(String.format(java.util.Locale.ROOT, " %s %.1f%%",
                    selectedFeatures[k], 100.0 * nClipped.get(k) / n));
        }
        getModel().getRuntime().println(sb.toString());
    }

    private String[] readFeatureList() {
        List<String> out = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            String path = FileTools.createAbsoluteFileName(
                    getModel().getWorkspaceDirectory().getAbsolutePath(),
                    featureListFile.getValue()
            );
            reader = new BufferedReader(new FileReader(path));
            String line;
            List<Double> centres = new ArrayList<Double>();
            List<Double> scales = new ArrayList<Double>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // "name" (legacy) or "name<TAB>centre<TAB>scale". The training run
                // standardises its covariates; querying the GP in raw units when it
                // was trained in standardised ones would corrupt every correction
                // silently, so the transform travels with the feature list.
                String[] parts = line.split("\t");
                String name = parts[0].trim();
                if (name.isEmpty()) {
                    continue;
                }
                if (name.startsWith("#")) {
                    // "#target<TAB>centre<TAB>scale". The GP is trained on a
                    // standardised target because MaternClass has no amplitude
                    // parameter, so its output must be scaled back into permil.
                    if (name.equals("#target") && parts.length >= 3) {
                        targetCentre = Double.parseDouble(parts[1].trim());
                        double ts = Double.parseDouble(parts[2].trim());
                        targetScale = Math.abs(ts) > 1e-12 ? ts : 1.0;
                    }
                    continue;
                }
                out.add(name);
                if (parts.length >= 3) {
                    centres.add(Double.parseDouble(parts[1].trim()));
                    double sc = Double.parseDouble(parts[2].trim());
                    scales.add(Math.abs(sc) > 1e-12 ? sc : 1.0);
                } else {
                    centres.add(0.0);
                    scales.add(1.0);
                }
            }
            featureCentre = new double[centres.size()];
            featureScale = new double[scales.size()];
            for (int k = 0; k < centres.size(); k++) {
                featureCentre[k] = centres.get(k);
                featureScale[k] = scales.get(k);
            }
        } catch (NumberFormatException e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: featureListFile has an unparseable centre/scale "
                    + "column: " + e.toString() + " - expected \"name<TAB>centre<TAB>scale\"."
            );
        } catch (IOException e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: could not read featureListFile: " + e.toString()
                    + " - it is written by IsoBiasTrainingCollector during the training run."
            );
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        return out.toArray(new String[0]);
    }

    /**
     * Fail loudly at startup if the trained feature list needs an input that
     * isn't wired here, rather than silently emitting uncorrected values for
     * the whole run.
     */
    private void validateRequiredInputs() {
        for (String f : selectedFeatures) {
            String missing = null;
            if (f.equals("elevation") && elevation == null) {
                missing = "elevation";
            } else if (f.equals("xCoord") && xCoord == null) {
                missing = "xCoord";
            } else if (f.equals("yCoord") && yCoord == null) {
                missing = "yCoord";
            } else if (f.equals("tmean") && !wired(tmeanNetworkArray, tmeanNetworkXCoord, tmeanNetworkYCoord)) {
                missing = "tmeanNetworkArray/tmeanNetworkXCoord/tmeanNetworkYCoord";
            } else if (f.equals("rhum") && !wired(rhumNetworkArray, rhumNetworkXCoord, rhumNetworkYCoord)) {
                missing = "rhumNetworkArray/rhumNetworkXCoord/rhumNetworkYCoord";
            } else if (f.equals("tempRange")
                    && !(wired(tmaxNetworkArray, tmaxNetworkXCoord, tmaxNetworkYCoord)
                    && wired(tminNetworkArray, tminNetworkXCoord, tminNetworkYCoord))) {
                missing = "tmax/tmin network arrays and coordinates";
            } else if ((f.equals("amount") || f.startsWith("antecedent"))
                    && !wired(amountArray, amountNetworkXCoord, amountNetworkYCoord)) {
                missing = "amountArray/amountNetworkXCoord/amountNetworkYCoord";
            } else if (usesNetwork(f) && (xCoord == null || yCoord == null)) {
                missing = "xCoord/yCoord (the HRU location the station networks are interpolated to)";
            } else if (f.startsWith("antecedent") && id == null) {
                missing = "id (needed to key per-HRU antecedent history)";
            } else if (f.equals("outsideBasinAvg") && outsideBasinAvg == null) {
                missing = "outsideBasinAvg";
            } else if (f.equals("iemi") && iemiFile == null) {
                missing = "iemiFile";
            }
            if (missing != null) {
                getModel().getRuntime().sendHalt(
                        "IsoMLBiasCorrection: the trained model uses feature \"" + f
                        + "\" but its input (" + missing + ") is not wired on this component."
                );
                return;
            }
        }
    }

    private static boolean wired(Attribute.DoubleArray v, Attribute.DoubleArray x, Attribute.DoubleArray y) {
        return v != null && x != null && y != null;
    }

    private static boolean usesNetwork(String f) {
        return f.equals("tmean") || f.equals("rhum") || f.equals("tempRange")
                || f.equals("amount") || f.startsWith("antecedent");
    }

    private void loadIemiIfNeeded() {
        boolean needed = false;
        for (String f : selectedFeatures) {
            if (f.equals("iemi")) {
                needed = true;
                break;
            }
        }
        if (!needed || iemiFile == null) {
            return;
        }
        try {
            // Resolve against the model workspace, exactly as modelDataFile and
            // featureListFile are. Those two already loaded on the cloud server;
            // this one did not, because a raw path resolves against the JVM's
            // working directory rather than the per-run exec directory. Absolute
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
        } catch (Exception e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: could not load iemiFile: " + e.toString()
            );
        }
    }

    /** Covariate names of the training file - every column except the last, which is the bias. */
    private String[] readTrainingHeader() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(trainingPath()));
            String head = r.readLine();
            if (head == null) {
                getModel().getRuntime().sendHalt(
                        "IsoMLBiasCorrection: trainingDataFile " + trainingPath() + " is empty.");
                return null;
            }
            String[] cols = head.trim().split("\t");
            if (cols.length < 2) {
                getModel().getRuntime().sendHalt(
                        "IsoMLBiasCorrection: trainingDataFile needs at least one covariate column "
                        + "and the bias column, found \"" + head.trim() + "\".");
                return null;
            }
            String[] names = new String[cols.length - 1];
            for (int k = 0; k < names.length; k++) {
                names[k] = cols[k].trim();
            }
            return names;
        } catch (Exception e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: could not read trainingDataFile "
                    + trainingPath() + ": " + e.toString());
            return null;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
    }

    private String trainingPath() {
        return FileTools.createAbsoluteFileName(
                getModel().getWorkspaceDirectory().getAbsolutePath(), trainingDataFile.getValue());
    }

    /**
     * SIMPLE PIPELINE. Build the GP here, at model start, from the collector's training rows
     * and the hyperparameters written into this component.
     *
     * Every step is the one IsoGpModelBuilder performs, in the same order and with the same
     * guards, so a model built here and a model deserialized from a .dat give identical
     * corrections. Standardise the covariates and the target on the training rows (divisor n,
     * a constant column keeps scale 1), min-max to [-1, 1] as Learner does, fit the mean
     * function by least squares, build the Matern covariance, add sigma squared to the
     * diagonal, and solve for alpha.
     */
    private void buildFromTrainingData() {
        String raw = (logTheta == null) ? null : logTheta.getValue();
        if (raw == null || raw.trim().isEmpty()) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: trainingDataFile is set but logTheta is empty. Fit the "
                    + "hyperparameters in Python and write them into this component.");
            return;
        }
        int d = selectedFeatures.length;
        double[] theta = new double[d + 1];
        String[] parts = raw.trim().split("[,;\\s]+");
        if (parts.length != d + 1) {
            getModel().getRuntime().sendHalt("IsoMLBiasCorrection: logTheta has " + parts.length
                    + " values but " + d + " covariates need " + (d + 1)
                    + " (one length scale each, plus sigma).");
            return;
        }
        try {
            for (int i = 0; i < parts.length; i++) {
                theta[i] = Math.exp(Double.parseDouble(parts[i]));
            }
        } catch (NumberFormatException e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: could not parse logTheta \"" + raw + "\".");
            return;
        }

        List<double[]> rows = new ArrayList<double[]>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(trainingPath()));
            r.readLine();                                   // header, already parsed
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] f = line.trim().split("\t");
                if (f.length < d + 1) {
                    continue;
                }
                double[] row = new double[d + 1];
                boolean ok = true;
                for (int k = 0; k <= d; k++) {
                    row[k] = Double.parseDouble(f[k].trim());
                    if (Double.isNaN(row[k]) || Double.isInfinite(row[k])) {
                        ok = false;
                    }
                }
                if (ok) {
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            getModel().getRuntime().sendHalt("IsoMLBiasCorrection: could not read trainingDataFile "
                    + trainingPath() + ": " + e.toString());
            return;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        int n = rows.size();
        if (n < 2) {
            getModel().getRuntime().sendHalt("IsoMLBiasCorrection: trainingDataFile holds "
                    + n + " usable rows.");
            return;
        }

        // --- standardise, exactly as IsoBiasTrainingCollector fits centre and scale
        featureCentre = new double[d];
        featureScale = new double[d];
        for (int k = 0; k < d; k++) {
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += rows.get(i)[k];
            }
            double mean = sum / n;
            double ss = 0.0;
            for (int i = 0; i < n; i++) {
                double dev = rows.get(i)[k] - mean;
                ss += dev * dev;
            }
            double sd = Math.sqrt(ss / n);
            featureCentre[k] = mean;
            featureScale[k] = (sd > 1e-12) ? sd : 1.0;
        }
        double tsum = 0.0;
        for (int i = 0; i < n; i++) {
            tsum += rows.get(i)[d];
        }
        targetCentre = tsum / n;
        double tss = 0.0;
        for (int i = 0; i < n; i++) {
            double dev = rows.get(i)[d] - targetCentre;
            tss += dev * dev;
        }
        double tsd = Math.sqrt(tss / n);
        targetScale = (tsd > 1e-12) ? tsd : 1.0;

        double[][] X = new double[n][d];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                X[i][k] = (rows.get(i)[k] - featureCentre[k]) / featureScale[k];
            }
            y[i] = (rows.get(i)[d] - targetCentre) / targetScale;
        }

        // --- min/max/base exactly as jams.components.machineLearning.Learner computes them
        min = new double[d];
        max = new double[d];
        base = new double[d];
        java.util.Arrays.fill(min, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                min[k] = Math.min(min[k], X[i][k]);
                max[k] = Math.max(max[k], X[i][k]);
            }
        }
        for (int k = 0; k < d; k++) {
            base[k] = (min[k] + max[k]) / 2.0;
            if (max[k] - min[k] == 0.0) {
                max[k] = min[k] + 1.0;                      // Learner's zero-divisor guard
            }
        }

        String kind = (meanModel == null || meanModel.getValue() == null
                || meanModel.getValue().trim().isEmpty())
                ? "constant" : meanModel.getValue().trim().toLowerCase();
        MeanModell mm;
        if (kind.equals("constant")) {
            mm = new FixedMeanModell(d);
        } else if (kind.equals("linear")) {
            mm = new LinearMeanModell(d);
        } else {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: meanModel must be \"constant\" or \"linear\", not \""
                    + kind + "\".");
            return;
        }
        MaternClass mat = new MaternClass(d);
        mat.SetMeanModell(mm);
        if (!mat.SetParameter(theta)) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: the kernel rejected logTheta - expected "
                    + (d + 1) + " values for " + d + " covariates.");
            return;
        }
        mm.create(X, y);
        observations = mm.Transform(X, y);

        double[][] Kd = new double[n][n];
        for (int i = 0; i < n; i++) {
            double[] xi = normalizeWith(X[i]);
            for (int j = 0; j < n; j++) {
                Kd[i][j] = mat.kernel(xi, normalizeWith(X[j]), i, j);
            }
        }
        double noise = theta[theta.length - 1] * theta[theta.length - 1];
        for (int i = 0; i < n; i++) {
            Kd[i][i] += noise;
        }
        // Jama tests symmetry with exact equality; the matrix is symmetric in exact arithmetic
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double avg = 0.5 * (Kd[i][j] + Kd[j][i]);
                Kd[i][j] = avg;
                Kd[j][i] = avg;
            }
        }
        Matrix K = new Matrix(Kd);
        CholeskyDecomposition chol = new CholeskyDecomposition(K);
        if (chol.isSPD()) {
            fastSolver = chol;
            alpha = chol.solve(observations);
        } else {
            solver = new LUDecomposition(K);
            alpha = solver.solve(observations);
        }
        kernel = mat;
        trainX = X;
        trainLength = n;
        getModel().getRuntime().println(String.format(
                "IsoMLBiasCorrection: built the GP here from %s - %d rows, %d covariates %s, "
                + "%s mean, logTheta [%s], solved by %s",
                trainingDataFile.getValue(), n, d, java.util.Arrays.toString(selectedFeatures),
                kind, raw.trim(), (fastSolver != null) ? "Cholesky" : "LU"));
    }

    /** Learner's min-max normalisation, for rows already standardised. */
    private double[] normalizeWith(double[] x) {
        double[] out = new double[x.length];
        for (int k = 0; k < x.length; k++) {
            out[k] = 2.0 * (x[k] - base[k]) / (max[k] - min[k]);
        }
        return out;
    }

    private void deserializeModel() {
        String file = getModel().getWorkspacePath() + "/" + modelDataFile.getValue();
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            min = (double[]) in.readObject();
            max = (double[]) in.readObject();
            base = (double[]) in.readObject();
            trainX = (double[][]) in.readObject();
            alpha = (Matrix) in.readObject();
            observations = (Matrix) in.readObject();
            trainLength = trainX.length;
            kernel = (Kernel) in.readObject();
            kernel.MM = (MeanModell) in.readObject();

            Object solverObj = in.readObject();
            if (solverObj instanceof CholeskyDecomposition) {
                fastSolver = (CholeskyDecomposition) solverObj;
            } else {
                solver = (LUDecomposition) solverObj;
            }
        } catch (Exception e) {
            getModel().getRuntime().sendHalt(
                    "IsoMLBiasCorrection: could not load GP model from " + file + ": " + e.toString()
            );
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
    }

    @Override
    public void run() {
        if (uncorrectedValue != null) {
            uncorrectedValue.setValue(rawValue.getValue());
        }
        double nodata = JAMS.getMissingDataValue();
        double hruAmount = networkAmount(nodata);
        Map<String, Double> candidate = computeFeatures(hruAmount, nodata);

        double[] x = new double[selectedFeatures.length];
        boolean usable = true;
        for (int k = 0; k < selectedFeatures.length; k++) {
            Double v = candidate.get(selectedFeatures[k]);
            if (v == null || v == nodata || Double.isNaN(v)) {
                usable = false;
                break;
            }
            // Same centre/scale the training run fitted, so the GP is queried in
            // the units it was trained in.
            x[k] = (featureScale == null) ? v
                    : (v - featureCentre[k]) / featureScale[k];
        }
        if (usable) {
            nQueries.incrementAndGet();
            if (clip) {
                // In standardised units, which is where trainX lives. Beyond the
                // training range the kernel term has decayed and only the linear
                // mean is left, so the correction would keep growing with the
                // covariate; holding it at the edge keeps the prediction at the
                // most extreme conditions the GP has actually seen.
                for (int k = 0; k < x.length; k++) {
                    if (x[k] < trainLo[k]) {
                        x[k] = trainLo[k];
                        nClipped.incrementAndGet(k);
                    } else if (x[k] > trainHi[k]) {
                        x[k] = trainHi[k];
                        nClipped.incrementAndGet(k);
                    }
                }
            }
        }

        if (!usable) {
            // A covariate is unavailable for this HRU/timestep (e.g. the month
            // falls outside the index CSV's coverage). Leave the value
            // uncorrected rather than querying the GP with a fabricated input.
            correctedValue.setValue(clamp(rawValue.getValue()));
            if (correctedValueVariance != null) {
                correctedValueVariance.setValue(0.0);
            }
        } else {
            // Back out of the standardised target space the GP was trained in.
            double meanCorrection = cachedMean(x) * targetScale + targetCentre;
            correctedValue.setValue(clamp(
                    rawValue.getValue() + correctionWeight.getValue() * meanCorrection));
            if (correctedValueVariance != null) {
                // Variance scales with the square of a linear rescaling.
                correctedValueVariance.setValue(cachedVariance(x) * targetScale * targetScale);
            }
        }

        recordPrecipitation(hruAmount);
    }

    /**
     * One-entry memo over the covariate vector.
     *
     * This component runs once per HRU per timestep - 1647 x ~3300 = 5.4 million
     * evaluations over a full run - and each evaluation is a dot product over
     * every training point. But whether the answer actually differs between HRUs
     * depends entirely on the covariates: the current set (dayOfYear, iemi) is
     * HRU-independent, so 1646 of every 1647 predictions recompute an identical
     * number. Caching the last (covariates -> prediction) pair collapses that
     * back to one evaluation per timestep.
     *
     * It is keyed on the covariate vector rather than on time, so it stays
     * correct if the feature set later includes something that does vary by HRU
     * (elevation, tmean, rain): the key then changes per HRU and every lookup
     * simply misses, giving the old behaviour rather than a wrong answer.
     *
     * The holder is immutable and published through a volatile field, so a
     * concurrent HRULoop can only ever read a self-consistent pair - a race
     * costs a redundant computation, never a mismatched one.
     */
    private static final class Memo {
        final double[] key;
        final double mean, variance;

        Memo(double[] key, double mean, double variance) {
            this.key = key;
            this.mean = mean;
            this.variance = variance;
        }

        boolean matches(double[] x) {
            if (key.length != x.length) {
                return false;
            }
            for (int i = 0; i < x.length; i++) {
                if (key[i] != x[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private volatile Memo memo = null;

    private Memo memoFor(double[] x) {
        Memo m = memo;
        if (m != null && m.matches(x)) {
            return m;
        }
        m = new Memo(x.clone(), predictMean(x),
                correctedValueVariance != null ? predictVariance(x) : Double.NaN);
        memo = m;
        return m;
    }

    private double cachedMean(double[] x) {
        return memoFor(x).mean;
    }

    private double cachedVariance(double[] x) {
        return memoFor(x).variance;
    }

    /**
     * Bound the corrected value to a physically possible range.
     *
     * Unset bounds mean no clamping, so a model that does not wire them keeps
     * the previous behaviour.
     */
    private double clamp(double v) {
        // NaN first: every comparison against NaN is false, so a NaN would pass
        // both bounds untouched and propagate into the mixing chain silently.
        // Falling back to the uncorrected value keeps the run physical and is
        // the same choice made when a covariate is unavailable.
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            if (!nanWarned) {
                nanWarned = true;
                getModel().getRuntime().println(
                        "IsoMLBiasCorrection: the GP returned a non-finite correction; "
                        + "falling back to the uncorrected value. This usually means the "
                        + "serialized model is degenerate - check that its covariance "
                        + "matrix is not effectively the identity."
                );
            }
            return rawValue.getValue();
        }
        if (minValue != null && v < minValue.getValue()) {
            return minValue.getValue();
        }
        if (maxValue != null && v > maxValue.getValue()) {
            return maxValue.getValue();
        }
        return v;
    }

    /** One warning per run is enough; this fires per HRU per timestep otherwise. */
    private volatile boolean nanWarned = false;

    private Map<String, Double> computeFeatures(double hruAmount, double nodata) {
        Map<String, Double> f = new LinkedHashMap<String, Double>();

        double doy = (double) time.get(Attribute.Calendar.DAY_OF_YEAR);
        f.put("dayOfYear", doy);
        // Must match IsoBiasTrainingCollector's encoding EXACTLY, including the
        // 365.25 period - the GP is queried in the space it was trained in, and
        // a different period here would rotate every query off its training
        // point silently. See the collector for why the cyclic form is needed.
        f.put("doySin", Math.sin(2.0 * Math.PI * doy / DAYS_IN_YEAR));
        f.put("doyCos", Math.cos(2.0 * Math.PI * doy / DAYS_IN_YEAR));
        if (elevation != null) {
            f.put("elevation", elevation.getValue());
        }
        if (xCoord != null) {
            f.put("xCoord", xCoord.getValue());
        }
        if (yCoord != null) {
            f.put("yCoord", yCoord.getValue());
        }
        if (outsideBasinAvg != null) {
            f.put("outsideBasinAvg", outsideBasinAvg.getValue());
        }
        if (iemiByMonth != null) {
            // java.util.Calendar.MONTH is 0-based (JANUARY=0)
            f.put("iemi", iemiByMonth.get(MonthlyIndexLookup.yearMonthKey(
                    time.get(Attribute.Calendar.YEAR),
                    time.get(Attribute.Calendar.MONTH) + 1)));
        }
        if (xCoord == null || yCoord == null) {
            return f;
        }
        // The station-network covariates, built exactly as the collector builds
        // them at an isotope station - here at the HRU instead.
        double hx = xCoord.getValue(), hy = yCoord.getValue();
        double power = (idwPower == null) ? 2.0 : idwPower.getValue();
        if (wired(tmeanNetworkArray, tmeanNetworkXCoord, tmeanNetworkYCoord)) {
            f.put("tmean", idw(hx, hy, tmeanNetworkXCoord, tmeanNetworkYCoord,
                    tmeanNetworkArray, nodata, power));
        }
        if (wired(rhumNetworkArray, rhumNetworkXCoord, rhumNetworkYCoord)) {
            f.put("rhum", idw(hx, hy, rhumNetworkXCoord, rhumNetworkYCoord,
                    rhumNetworkArray, nodata, power));
        }
        if (wired(tmaxNetworkArray, tmaxNetworkXCoord, tmaxNetworkYCoord)
                && wired(tminNetworkArray, tminNetworkXCoord, tminNetworkYCoord)) {
            double tx = idw(hx, hy, tmaxNetworkXCoord, tmaxNetworkYCoord, tmaxNetworkArray, nodata, power);
            double tn = idw(hx, hy, tminNetworkXCoord, tminNetworkYCoord, tminNetworkArray, nodata, power);
            f.put("tempRange", (tx == nodata || tn == nodata) ? null : tx - tn);
        }
        if (hruAmount != nodata && id != null) {
            // As in the collector: amount and the antecedent sums are only
            // defined on a day the gauge network has a value at this location.
            History hist = historyFor((long) id.getValue());
            f.put("amount", hruAmount);
            f.put("antecedent7", antecedentSum(hist, 7, nodata));
            f.put("antecedent30", antecedentSum(hist, 30, nodata));
        }
        return f;
    }

    /** Today's gauge rain interpolated to this HRU, or nodata when unwired or empty. */
    private double networkAmount(double nodata) {
        if (!wired(amountArray, amountNetworkXCoord, amountNetworkYCoord)
                || xCoord == null || yCoord == null) {
            return nodata;
        }
        return idw(xCoord.getValue(), yCoord.getValue(), amountNetworkXCoord,
                amountNetworkYCoord, amountArray,
                nodata, (idwPower == null) ? 2.0 : idwPower.getValue());
    }

    /**
     * IsoBiasTrainingCollector.idwInterpolate, verbatim: every station with a
     * value, weight 1/d^power, a coincident station returned as-is, nodata when
     * no station has a value. Any change there must be made here too.
     */
    private static double idw(double targetX, double targetY,
            Attribute.DoubleArray srcXa, Attribute.DoubleArray srcYa,
            Attribute.DoubleArray srcValuea, double nodata, double power) {
        double[] srcX = srcXa.getValue(), srcY = srcYa.getValue(), srcValue = srcValuea.getValue();
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

    private History historyFor(long hruId) {
        History hist = precipHistory.get(hruId);
        if (hist == null) {
            hist = new History();
            History existing = precipHistory.putIfAbsent(hruId, hist);
            if (existing != null) {
                hist = existing;
            }
        }
        return hist;
    }

    /**
     * Cumulative precipitation over the previous {@code days} days for this HRU,
     * skipping days with no gauge value - IsoBiasTrainingCollector.antecedentSum.
     */
    private double antecedentSum(History hist, int days, double nodata) {
        double sum = 0.0;
        int n = Math.min(days, hist.size);
        for (int k = 0; k < n; k++) {
            double v = hist.v[(hist.head + k) % MAX_ANTECEDENT_WINDOW];
            if (v != nodata) {
                sum += v;
            }
        }
        return sum;
    }

    /**
     * Record today's rainfall AFTER the correction is computed, so the
     * antecedent windows cover strictly prior days - matching how
     * IsoBiasTrainingCollector builds the same covariates at training time.
     * The collector records every day, nodata included, so this does too.
     */
    private void recordPrecipitation(double hruAmount) {
        if (id == null || !wired(amountArray, amountNetworkXCoord, amountNetworkYCoord)
                || xCoord == null || yCoord == null) {
            return;
        }
        historyFor((long) id.getValue()).addFirst(hruAmount);
    }

    private double[] normalize(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = 2.0 * (x[i] - base[i]) / (max[i] - min[i]);
        }
        return result;
    }

    /** Mirrors GaussianLearner.getMean(double[]). */
    private double predictMean(double[] x) {
        double[] xNorm = normalize(x);
        Matrix kstar = new Matrix(1, trainLength);

        for (int i = 0; i < trainLength; i++) {
            double k = kernel.kernel(trainXNorm[i], xNorm, i, -1);
            kstar.set(0, i, k);
        }

        Matrix prediction = kstar.times(alpha);
        double[][] xWrap = new double[][]{x};
        return kernel.MM.ReTransform(xWrap, prediction)[0];
    }

    /**
     * Mirrors GaussianLearner.getVariance(double[]), minus the
     * invCovarianzMatrix branch - deserializeModel() always leaves that field
     * null, so a freshly-loaded model never takes that path either. The terms
     * that do not involve x (tOne, mySigma) come from precompute().
     */
    private double predictVariance(double[] x) {
        double[] xNorm = normalize(x);
        Matrix kstar = new Matrix(1, trainLength);
        Matrix kstarT = new Matrix(trainLength, 1);

        for (int i = 0; i < trainLength; i++) {
            double k = kernel.kernel(trainXNorm[i], xNorm, i, -1);
            kstar.set(0, i, k);
            kstarT.set(i, 0, k);
        }

        Matrix rMinus1r = (fastSolver != null) ? fastSolver.solve(kstarT) : solver.solve(kstarT);
        double sigma2 = 1.0 - kstar.times(rMinus1r).get(0, 0);

        return Math.abs(varMySigma) * Math.sqrt(sigma2 + sigma2 * sigma2 / varTOne);
    }
}
