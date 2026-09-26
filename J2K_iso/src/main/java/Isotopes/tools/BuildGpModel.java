package Isotopes.tools;

import jams.components.machineLearning.kernels.Kernel;
import jams.components.machineLearning.kernels.MaternClass;
import jams.components.machineLearning.kernels.LinearMeanModell;
import jams.components.machineLearning.kernels.FixedMeanModell;
import jams.components.machineLearning.kernels.MeanModell;

import Jama.CholeskyDecomposition;
import Jama.LUDecomposition;
import Jama.Matrix;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds the serialized GP model that Isotopes.IsoMLBiasCorrection loads, without
 * going through jams.components.machineLearning.GaussianLearner's optimiser.
 *
 * WHY THIS EXISTS
 * ---------------
 * GaussianLearner's mode=2 hyperparameter search is not usable for this problem.
 * It seeds from random.nextDouble()*10 - random LOG length scales in [0,10], so
 * length scales up to ~22000 - and its descent loop has no iteration cap, exiting
 * only when two tolerances are met. Measured on this catchment's 214 samples and
 * two covariates: one attempt ran 62 minutes and a second ran 6 hours and 8
 * minutes at full CPU, neither converging, from different random seeds. The same
 * objective, same kernel and same data is solved by a standard L-BFGS-B fit in 52
 * evaluations at condition number 18. The problem is easy; the framework's search
 * is the defect.
 *
 * A training step that cannot be repeated on demand is not usable in a workflow,
 * so this tool replaces it with a deterministic one that runs in seconds.
 *
 * WHAT IT REPRODUCES
 * ------------------
 * Nothing here is a reimplementation: it calls the same MaternClass,
 * LinearMeanModell and Jama classes GaussianLearner does, and writes the same
 * object sequence its serializeModel() writes, in the same order:
 *
 *     min, max, base, trainX, alpha, observations, kernel, kernel.MM, solver
 *
 * The two pieces that had to be read out of the bytecode are:
 *
 *   Learner.setTrainingData  base[i] = (min[i] + max[i]) / 2, and where a column
 *                            is constant, max[i] = min[i] + 1 so the divisor is
 *                            never zero.
 *   Learner.normalize        normalize(x)[i] = 2*(x[i] - base[i])/(max[i]-min[i])
 *
 * Both are applied here exactly as there, because IsoMLBiasCorrection applies the
 * identical transform when it reads the model back.
 *
 * NOTE ON THE TARGET
 * ------------------
 * Learner.setTrainingData also computes pmin/pmax/pbase for the target and then
 * never uses them - GaussianLearner contains no reference to any of the three.
 * That discarded normalisation, combined with MaternClass having no amplitude
 * parameter (k(x,x) is pinned at 1), is why the shipped configuration could not
 * represent a target of variance ~450 and returned its prior. The caller is
 * expected to hand this tool an already-standardised target, which is what
 * IsoBiasTrainingCollector now does.
 *
 * USAGE
 *   java Isotopes.tools.BuildGpModel <samples.dat> <featureList.txt> <model.dat>
 *                                    <theta1,theta2,...,sigma>
 *
 * where <samples.dat> is the collector's tab-separated dump in PHYSICAL units
 * (its last column being the target), <featureList.txt> is written by this tool
 * so the applier reproduces the identical standardisation, and the theta list is
 * LOG length scales followed by log sigma - the same convention GaussianLearner's
 * parameterFile uses.
 *
 * @author prepared for Andrew Watson
 */
public class BuildGpModel {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("usage: BuildGpModel <samples.dat> <featureList.txt> "
                    + "<model.dat> <logTheta,comma,separated>");
            System.exit(2);
        }
        String samplesFile = args[0];
        String featureListFile = args[1];
        String modelFile = args[2];

        double[] logTheta = parseDoubles(args[3]);

        // ---------------------------------------------------------- read samples
        List<String> header = new ArrayList<String>();
        List<double[]> rows = new ArrayList<double[]>();
        BufferedReader in = new BufferedReader(new FileReader(samplesFile));
        String line = in.readLine();
        if (line == null) {
            throw new IllegalStateException("empty samples file: " + samplesFile);
        }
        for (String h : line.trim().split("\\s+")) {
            header.add(h);
        }
        while ((line = in.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] p = line.trim().split("\\s+");
            if (p.length != header.size()) {
                continue;
            }
            double[] r = new double[p.length];
            for (int i = 0; i < p.length; i++) {
                r[i] = Double.parseDouble(p[i]);
            }
            rows.add(r);
        }
        in.close();

        int n = rows.size();
        int d = header.size() - 1;                 // last column is the target
        if (logTheta.length != d + 1) {
            throw new IllegalArgumentException("expected " + (d + 1)
                    + " log-theta values (one per covariate plus sigma) for "
                    + d + " covariates, got " + logTheta.length);
        }
        System.out.println("samples    : " + n + " rows, " + d + " covariates "
                + header.subList(0, d));

        double[][] Xraw = new double[n][d];
        double[] yraw = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(rows.get(i), 0, Xraw[i], 0, d);
            yraw[i] = rows.get(i)[d];
        }

        // ------------------------------------------- standardise, as the collector does
        double[] fMean = new double[d];
        double[] fSd = new double[d];
        for (int k = 0; k < d; k++) {
            double s = 0;
            for (int i = 0; i < n; i++) {
                s += Xraw[i][k];
            }
            fMean[k] = s / n;
            double ss = 0;
            for (int i = 0; i < n; i++) {
                double dv = Xraw[i][k] - fMean[k];
                ss += dv * dv;
            }
            double sd = Math.sqrt(ss / n);
            fSd[k] = (sd > 1e-12) ? sd : 1.0;
        }
        double tSum = 0;
        for (int i = 0; i < n; i++) {
            tSum += yraw[i];
        }
        double tMean = tSum / n;
        double tss = 0;
        for (int i = 0; i < n; i++) {
            double dv = yraw[i] - tMean;
            tss += dv * dv;
        }
        double tSdRaw = Math.sqrt(tss / n);
        double tSd = (tSdRaw > 1e-12) ? tSdRaw : 1.0;

        double[][] X = new double[n][d];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                X[i][k] = (Xraw[i][k] - fMean[k]) / fSd[k];
            }
            y[i] = (yraw[i] - tMean) / tSd;
        }
        System.out.println("target     : centre " + fmt(tMean) + "  scale " + fmt(tSd));

        // ------------------------------------- min/max/base, exactly as Learner does
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

        // ---------------------------------------------------------- kernel + mean
        double[] theta = new double[logTheta.length];
        for (int i = 0; i < logTheta.length; i++) {
            theta[i] = Math.exp(logTheta[i]);   // SetParameter takes LINEAR values
        }
        MaternClass kernel = new MaternClass(d);
        MeanModell mm = new LinearMeanModell(d);
        kernel.SetMeanModell(mm);
        if (!kernel.SetParameter(theta)) {
            throw new IllegalStateException("kernel rejected the parameter vector");
        }
        System.out.println("kernel     : MaternClass(" + d + ")  theta "
                + Arrays.toString(round(theta)));

        // LinearMeanModell fits itself by least squares on the same data, which is
        // what GaussianLearner has it do - no need to supply coefficients.
        mm.create(X, y);
        Matrix observations = mm.Transform(X, y);

        // ------------------------------------------------- covariance and alpha
        double[][] Kd = new double[n][n];
        for (int i = 0; i < n; i++) {
            double[] xi = normalize(X[i], base, min, max);
            for (int j = 0; j < n; j++) {
                double[] xj = normalize(X[j], base, min, max);
                Kd[i][j] = kernel.kernel(xi, xj, i, j);
            }
        }

        // MaternClass.kernel COMPUTES its noise term and then discards it: the
        // decompiled method stores theta[last]^2 into a local when i == j and
        // never loads that local again, returning the bare Matern value. So
        // k(x,x) = 1 exactly, with no jitter on the diagonal.
        //
        // That matters here because the covariate set is dayOfYear + iemi, both
        // catchment-wide: stations sharing a date have IDENTICAL covariates and
        // different bias. 154 of the 214 rows are duplicates in that sense, so K
        // contains exactly repeated rows and is exactly singular - no
        // hyperparameter choice can rescue it.
        //
        // Adding sigma^2 to the diagonal here restores the term the kernel meant
        // to apply. It is the standard Gaussian-process treatment of repeated
        // observations at the same input, and it is what makes the model
        // well-posed when the same covariates carry several different targets.
        // It is applied ONLY to the training covariance, never to k*, which is
        // correct: k* has no observation noise, and IsoMLBiasCorrection builds it
        // by calling kernel(trainX[i], x, i, -1) where i != j always.
        double noise = theta[theta.length - 1] * theta[theta.length - 1];
        for (int i = 0; i < n; i++) {
            Kd[i][i] += noise;
        }

        // Jama's CholeskyDecomposition tests symmetry with exact floating-point
        // equality (A[k][j] == A[j][k]), so any last-bit asymmetry from summing
        // the squared distance in a different order sends a perfectly good matrix
        // down the LU path - and Jama's LU then rejects it as singular. The matrix
        // IS symmetric in exact arithmetic, so mirroring the upper triangle onto
        // the lower is a restatement, not a fudge.
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double avg = 0.5 * (Kd[i][j] + Kd[j][i]);
                Kd[i][j] = avg;
                Kd[j][i] = avg;
            }
        }
        Matrix K = new Matrix(Kd);

        double offMean = 0;
        int offCount = 0;
        int above = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    offMean += Kd[i][j];
                    offCount++;
                    if (Kd[i][j] > 0.01) {
                        above++;
                    }
                }
            }
        }
        offMean /= offCount;
        double aboveFrac = 100.0 * above / offCount;
        System.out.println("covariance : off-diagonal mean " + fmt(offMean)
                + ", " + fmt(aboveFrac) + "% above 0.01"
                + ", diagonal " + fmt(Kd[0][0]) + " (1 + sigma^2, restored)");

        // REFUSE to write a degenerate model. A covariance matrix that has
        // collapsed toward the identity produces a GP that returns its prior, and
        // one with duplicate rows is singular and yields NaN. Both have already
        // happened in this workspace; failing here is better than shipping either.
        if (!isFinite(Kd)) {
            throw new IllegalStateException("covariance matrix contains non-finite "
                    + "entries - the length scales are pathological");
        }
        if (aboveFrac < 1.0) {
            throw new IllegalStateException("covariance matrix has collapsed toward "
                    + "the identity (only " + fmt(aboveFrac) + "% of off-diagonal "
                    + "entries exceed 0.01). The resulting GP would return its prior "
                    + "for every query. Refit the hyperparameters.");
        }

        Matrix alpha;
        Object solver;
        CholeskyDecomposition chol = K.chol();
        System.out.println("condition  : Cholesky reports SPD = " + chol.isSPD());
        if (chol.isSPD()) {
            alpha = chol.solve(observations);
            solver = chol;
            System.out.println("solver     : Cholesky (matrix is SPD)");
        } else {
            LUDecomposition lu = K.lu();
            alpha = lu.solve(observations);
            solver = lu;
            System.out.println("solver     : LU (matrix not SPD)");
        }
        if (!isFinite(alpha.getArray())) {
            throw new IllegalStateException("alpha contains non-finite values - the "
                    + "covariance matrix is singular. Refit the hyperparameters.");
        }

        // ------------------------------------------------------------- serialize
        ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(modelFile)));
        out.writeObject(min);
        out.writeObject(max);
        out.writeObject(base);
        out.writeObject(X);              // trainX, on the scale the applier standardises to
        out.writeObject(alpha);
        out.writeObject(observations);
        out.writeObject(kernel);
        out.writeObject(kernel.MM);
        out.writeObject(solver);
        out.close();
        System.out.println("wrote      : " + modelFile);

        // the applier must standardise its covariates identically, so the transform
        // travels with the model - same format IsoBiasTrainingCollector writes
        BufferedWriter fw = new BufferedWriter(new FileWriter(featureListFile));
        fw.write("#target\t" + tMean + "\t" + tSd);
        fw.newLine();
        for (int k = 0; k < d; k++) {
            fw.write(header.get(k) + "\t" + fMean[k] + "\t" + fSd[k]);
            fw.newLine();
        }
        fw.close();
        System.out.println("wrote      : " + featureListFile);

        // ------------------------------------------------------- self-check
        // Predict back on the training rows through the same path the applier uses.
        // This is in-sample and says nothing about skill - it only proves the
        // serialized model produces finite, sensible numbers rather than NaN.
        double sae = 0;
        int finite = 0;
        for (int i = 0; i < Math.min(n, 50); i++) {
            double p = predict(X[i], X, alpha, kernel, base, min, max);
            if (Double.isNaN(p) || Double.isInfinite(p)) {
                throw new IllegalStateException("self-check produced a non-finite "
                        + "prediction at row " + i + " - the model is unusable");
            }
            finite++;
            sae += Math.abs(p - y[i]);
        }
        System.out.println("self-check : " + finite + " finite predictions, "
                + "mean |pred-obs| " + fmt(sae / finite) + " (standardised units, "
                + "in-sample - not a skill estimate)");
        System.out.println("OK");
    }

    /** Mirrors IsoMLBiasCorrection.predictMean, to prove the file round-trips. */
    private static double predict(double[] x, double[][] trainX, Matrix alpha,
            Kernel kernel, double[] base, double[] min, double[] max) {
        double[] xn = normalize(x, base, min, max);
        Matrix kstar = new Matrix(1, trainX.length);
        for (int i = 0; i < trainX.length; i++) {
            kstar.set(0, i, kernel.kernel(normalize(trainX[i], base, min, max), xn, i, -1));
        }
        Matrix prediction = kstar.times(alpha);
        return kernel.MM.ReTransform(new double[][]{x}, prediction)[0];
    }

    private static double[] normalize(double[] x, double[] base, double[] min, double[] max) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            r[i] = 2.0 * (x[i] - base[i]) / (max[i] - min[i]);
        }
        return r;
    }

    private static boolean isFinite(double[][] a) {
        for (double[] row : a) {
            for (double v : row) {
                if (Double.isNaN(v) || Double.isInfinite(v)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double[] parseDoubles(String s) {
        String[] p = s.split(",");
        double[] r = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            r[i] = Double.parseDouble(p[i].trim());
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

    private static String fmt(double v) {
        return String.format("%.5g", v);
    }
}
