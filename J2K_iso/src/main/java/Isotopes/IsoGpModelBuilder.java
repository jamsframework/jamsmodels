package Isotopes;

import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;

import jams.components.machineLearning.kernels.FixedMeanModell;
import jams.components.machineLearning.kernels.LinearMeanModell;
import jams.components.machineLearning.kernels.MaternClass;
import jams.components.machineLearning.kernels.MeanModell;

import Jama.CholeskyDecomposition;
import Jama.LUDecomposition;
import Jama.Matrix;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "IsoGpModelBuilder",
        author = "Andrew Watson",
        description = "Trains the precipitation-isotope Gaussian process on SUPPLIED "
        + "hyperparameters and writes the model file Isotopes.IsoMLBiasCorrection "
        + "loads. Drop-in replacement for jams.components.machineLearning."
        + "GaussianLearner in the same context, consuming the same trainData entity "
        + "and writing the same serialized format - but deterministic, and finishing "
        + "in seconds rather than not at all.\n\n"
        + "WHY THIS EXISTS. GaussianLearner's mode=2 hyperparameter search could not "
        + "be made to converge on this problem: two attempts, 62 minutes and 6 hours "
        + "8 minutes at full CPU from different random seeds, neither producing a "
        + "model. Its descent has no iteration cap - it exits only on tolerance - and "
        + "it seeds from random.nextDouble()*10, i.e. random LOG length scales up to "
        + "10 (length scales ~22000) when the optimum here is near log 0.1. The same "
        + "objective on the same data is solved by a standard L-BFGS-B fit in 52 "
        + "evaluations. A training step that cannot be repeated on demand is not "
        + "usable in a workflow, so this component fixes the hyperparameters and does "
        + "only the part that is deterministic: build the covariance, solve for alpha, "
        + "serialize.\n\n"
        + "THE NOISE TERM. MaternClass.kernel computes theta[last]^2 when i == j and "
        + "then discards it - the decompiled method stores it into a local and never "
        + "loads that local again - so k(x,x) = 1 exactly, with no jitter on the "
        + "diagonal. That is fatal for a catchment-wide covariate set: stations "
        + "measured on the same day have identical covariates and different bias "
        + "(154 of 214 rows here), so the covariance matrix contains exactly repeated "
        + "rows and is exactly singular. This component restores sigma^2 on the "
        + "training diagonal - the standard treatment of repeated observations at one "
        + "input - and correctly omits it from k*, which IsoMLBiasCorrection builds "
        + "with i != j regardless.\n\n"
        + "It REFUSES to write a degenerate model: if the covariance has collapsed "
        + "toward the identity, or alpha is non-finite, it halts rather than shipping "
        + "a file whose every prediction would be the prior mean or NaN. Both have "
        + "happened in this workspace.",
        date = "2026-09-17",
        version = "1.1_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version - in-model "
            + "equivalent of the Isotopes.tools.BuildGpModel command-line tool, so "
            + "retraining is a model run rather than a remembered command"),
    @VersionComments.Entry(version = "1.1_0", comment = "meanModel: \"linear\" "
            + "(LinearMeanModell, the previous behaviour and the default) or \"constant\" "
            + "(FixedMeanModell). With a linear mean the GP extrapolates along a plane in "
            + "the covariates wherever the kernel has no training data - the isoGSM "
            + "correction ran to the +50 permil clamp for months outside the Jan-Aug 2023 "
            + "training window. A constant mean falls back to the average bias there. "
            + "IsoMLBiasCorrection reads whichever mean model the file carries.")
})
public class IsoGpModelBuilder extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Training data entity written by IsoBiasTrainingCollector - "
            + "the same attribute GaussianLearner takes. Its \"data\" is the covariate "
            + "matrix and \"predict\" the target, ALREADY standardised by the collector "
            + "when standardiseData is on, which is what this component expects: it "
            + "does not standardise again."
    )
    public Attribute.Entity trainData;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Hyperparameters as LOG values, comma or semicolon separated: "
            + "one length scale per covariate, then log sigma. Same convention as "
            + "GaussianLearner's parameterFile, which is written from its logtheta "
            + "field. Fit these offline (marginal likelihood) on the pipeline the "
            + "model actually applies - standardise, THEN Learner's min-max - because "
            + "hyperparameters fitted on the wrong geometry cost a factor of three in "
            + "skill here.",
            defaultValue = "-2.293746,2.392670,-0.608701"
    )
    public Attribute.String logTheta;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Where to write the serialized model - point "
            + "IsoMLBiasCorrection's modelDataFile at the same path",
            defaultValue = "output/iso_bias_gp_model.dat"
    )
    public Attribute.String modelDataFile;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optional record of the hyperparameters actually used, in "
            + "GaussianLearner's parameterFile format (log values, one per line)",
            defaultValue = "output/iso_bias_gp_theta.txt"
    )
    public Attribute.String parameterFile = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum fraction of off-diagonal covariance entries that "
            + "must exceed 0.01 for the model to be considered usable. Below this the "
            + "covariance has collapsed toward the identity and every prediction would "
            + "be the prior mean - the exact failure this whole exercise began with.",
            defaultValue = "0.01"
    )
    public Attribute.Double minCovarianceFraction = null;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "GP mean function: \"linear\" (LinearMeanModell) or \"constant\" "
            + "(FixedMeanModell - the mean of the standardised target, so away from the "
            + "training data the correction reverts to the average bias instead of "
            + "extrapolating linearly)",
            defaultValue = "linear"
    )
    public Attribute.String meanModel = null;

    @Override
    public void run() {
        double[][] X = (double[][]) trainData.getObject("data");
        double[] y = (double[]) trainData.getObject("predict");

        if (X == null || y == null || X.length == 0) {
            getModel().getRuntime().sendHalt(
                    "IsoGpModelBuilder: trainData is empty. It must run AFTER the "
                    + "collector has published - put this component in a context that "
                    + "is a sibling of TimeLoop, exactly where GaussianLearner sits."
            );
            return;
        }

        int n = X.length;
        int d = X[0].length;
        double[] theta = parseTheta(d);
        if (theta == null) {
            return;
        }

        getModel().getRuntime().println("IsoGpModelBuilder: " + n + " samples, "
                + d + " covariates, theta " + Arrays.toString(round(theta)));

        // min/max/base exactly as jams.components.machineLearning.Learner computes
        // them, because IsoMLBiasCorrection applies the identical normalisation when
        // it reads the model back.
        double[] min = new double[d];
        double[] max = new double[d];
        double[] base = new double[d];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                min[k] = Math.min(min[k], X[i][k]);
                max[k] = Math.max(max[k], X[i][k]);
            }
        }
        for (int k = 0; k < d; k++) {
            base[k] = (min[k] + max[k]) / 2.0;
            if (max[k] - min[k] == 0.0) {
                max[k] = min[k] + 1.0;      // Learner's guard against a zero divisor
            }
        }

        MaternClass kernel = new MaternClass(d);
        String meanKind = (meanModel == null) ? "linear" : meanModel.getValue().trim().toLowerCase();
        MeanModell mm;
        if (meanKind.equals("linear")) {
            mm = new LinearMeanModell(d);
        } else if (meanKind.equals("constant")) {
            mm = new FixedMeanModell(d);
        } else {
            getModel().getRuntime().sendHalt(
                    "IsoGpModelBuilder: meanModel must be \"linear\" or \"constant\", not \""
                    + meanKind + "\"");
            return;
        }
        getModel().getRuntime().println("IsoGpModelBuilder: " + meanKind + " mean ("
                + mm.getClass().getSimpleName() + ")");
        kernel.SetMeanModell(mm);
        if (!kernel.SetParameter(theta)) {
            getModel().getRuntime().sendHalt(
                    "IsoGpModelBuilder: the kernel rejected the parameter vector - "
                    + "expected " + (d + 1) + " values for " + d + " covariates."
            );
            return;
        }
        mm.create(X, y);                    // least squares, as GaussianLearner has it do
        Matrix observations = mm.Transform(X, y);

        double[][] Kd = new double[n][n];
        for (int i = 0; i < n; i++) {
            double[] xi = normalize(X[i], base, min, max);
            for (int j = 0; j < n; j++) {
                Kd[i][j] = kernel.kernel(xi, normalize(X[j], base, min, max), i, j);
            }
        }
        // the noise MaternClass computes and then throws away
        double noise = theta[theta.length - 1] * theta[theta.length - 1];
        for (int i = 0; i < n; i++) {
            Kd[i][i] += noise;
        }
        // Jama's Cholesky tests symmetry with exact floating-point equality, so mirror
        // the upper triangle - the matrix is symmetric in exact arithmetic anyway.
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double avg = 0.5 * (Kd[i][j] + Kd[j][i]);
                Kd[i][j] = avg;
                Kd[j][i] = avg;
            }
        }

        long above = 0;
        long offCount = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    offCount++;
                    if (Kd[i][j] > 0.01) {
                        above++;
                    }
                    if (Double.isNaN(Kd[i][j]) || Double.isInfinite(Kd[i][j])) {
                        getModel().getRuntime().sendHalt(
                                "IsoGpModelBuilder: covariance contains non-finite "
                                + "entries - the length scales are pathological."
                        );
                        return;
                    }
                }
            }
        }
        double frac = (double) above / offCount;
        double need = (minCovarianceFraction == null) ? 0.01 : minCovarianceFraction.getValue();
        getModel().getRuntime().println(String.format(
                "IsoGpModelBuilder: covariance - %.3f%% of off-diagonal entries above "
                + "0.01, diagonal %.4f", 100.0 * frac, Kd[0][0]));
        if (frac < need) {
            getModel().getRuntime().sendHalt(String.format(
                    "IsoGpModelBuilder: the covariance matrix has collapsed toward the "
                    + "identity (only %.3f%% of off-diagonal entries exceed 0.01, "
                    + "needed %.3f%%). The resulting GP would return its prior mean for "
                    + "every query - which is the failure this component exists to "
                    + "prevent. Refit the hyperparameters.", 100.0 * frac, 100.0 * need));
            return;
        }

        Matrix K = new Matrix(Kd);
        Matrix alpha;
        Object solver;
        CholeskyDecomposition chol = K.chol();
        if (chol.isSPD()) {
            alpha = chol.solve(observations);
            solver = chol;
        } else {
            LUDecomposition lu = K.lu();
            alpha = lu.solve(observations);
            solver = lu;
        }
        for (double[] row : alpha.getArray()) {
            for (double v : row) {
                if (Double.isNaN(v) || Double.isInfinite(v)) {
                    getModel().getRuntime().sendHalt(
                            "IsoGpModelBuilder: alpha is non-finite - the covariance "
                            + "matrix is singular. This usually means many training "
                            + "rows share identical covariates and the noise term is "
                            + "too small to separate them."
                    );
                    return;
                }
            }
        }
        getModel().getRuntime().println("IsoGpModelBuilder: solved via "
                + (chol.isSPD() ? "Cholesky" : "LU"));

        // same object sequence GaussianLearner.serializeModel() writes, same order
        ObjectOutputStream out = null;
        try {
            String path = FileTools.createAbsoluteFileName(
                    getModel().getWorkspaceDirectory().getAbsolutePath(),
                    modelDataFile.getValue());
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            out.writeObject(min);
            out.writeObject(max);
            out.writeObject(base);
            out.writeObject(X);
            out.writeObject(alpha);
            out.writeObject(observations);
            out.writeObject(kernel);
            out.writeObject(kernel.MM);
            out.writeObject(solver);
            out.close();
            out = null;
            getModel().getRuntime().println("IsoGpModelBuilder: wrote " + path);
        } catch (Exception e) {
            getModel().getRuntime().sendHalt(
                    "IsoGpModelBuilder: could not write modelDataFile: " + e.toString());
            return;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // nothing to do
                }
            }
        }

        if (parameterFile != null && parameterFile.getValue() != null
                && !parameterFile.getValue().isEmpty()) {
            BufferedWriter w = null;
            try {
                String p = FileTools.createAbsoluteFileName(
                        getModel().getWorkspaceDirectory().getAbsolutePath(),
                        parameterFile.getValue());
                w = new BufferedWriter(new FileWriter(p));
                for (double t : theta) {
                    w.write(Double.toString(Math.log(t)));
                    w.newLine();
                }
            } catch (Exception e) {
                getModel().getRuntime().println(
                        "IsoGpModelBuilder: could not write parameterFile: " + e.toString());
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (Exception e) {
                        // nothing to do
                    }
                }
            }
        }
    }

    private double[] parseTheta(int d) {
        String raw = (logTheta == null) ? null : logTheta.getValue();
        if (raw == null || raw.trim().isEmpty()) {
            getModel().getRuntime().sendHalt("IsoGpModelBuilder: logTheta is not set.");
            return null;
        }
        String[] parts = raw.trim().split("[,;\\s]+");
        if (parts.length != d + 1) {
            getModel().getRuntime().sendHalt("IsoGpModelBuilder: logTheta has "
                    + parts.length + " values but " + d + " covariates need " + (d + 1)
                    + " (one length scale each, plus sigma).");
            return null;
        }
        double[] theta = new double[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                theta[i] = Math.exp(Double.parseDouble(parts[i]));
            }
        } catch (NumberFormatException e) {
            getModel().getRuntime().sendHalt(
                    "IsoGpModelBuilder: could not parse logTheta \"" + raw + "\"");
            return null;
        }
        return theta;
    }

    private static double[] normalize(double[] x, double[] base, double[] min, double[] max) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            r[i] = 2.0 * (x[i] - base[i]) / (max[i] - min[i]);
        }
        return r;
    }

    private static double[] round(double[] a) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = Math.round(a[i] * 100000.0) / 100000.0;
        }
        return r;
    }
}
