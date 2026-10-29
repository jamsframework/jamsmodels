package Isotopes.tools;

import jams.components.machineLearning.kernels.Kernel;
import jams.components.machineLearning.kernels.MeanModell;

import Jama.CholeskyDecomposition;
import Jama.LUDecomposition;
import Jama.Matrix;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * Prints a serialized GP model in readable form.
 *
 * iso_bias_gp_model_*.dat is a Java ObjectOutputStream stream - it opens with the
 * magic bytes AC ED 00 05 and contains no text at all, so there is no way to
 * inspect it in an editor. That matters because two failures in this workspace
 * were only visible INSIDE the model: a covariance matrix collapsed to the
 * identity (every prediction returns the prior mean) and an alpha vector full of
 * NaN (every prediction returns NaN). Both produced a plausible-looking file of
 * the usual size.
 *
 * This reads the same nine objects IsoMLBiasCorrection.deserializeModel() reads,
 * in the same order, and reports what they contain:
 *
 *     min, max, base, trainX, alpha, observations, kernel, kernel.MM, solver
 *
 * USAGE
 *   java Isotopes.tools.DumpGpModel <model.dat> [featureList.txt]
 *
 * Pass the feature list too and it will also report the standardisation, so the
 * hyperparameters can be read against the scale the covariates actually have.
 *
 * @author  prepared for Andrew Watson
 */
public class DumpGpModel {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: DumpGpModel <model.dat> [featureList.txt]");
            System.exit(2);
        }
        ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(args[0])));

        double[] min = (double[]) in.readObject();
        double[] max = (double[]) in.readObject();
        double[] base = (double[]) in.readObject();
        double[][] trainX = (double[][]) in.readObject();
        Matrix alpha = (Matrix) in.readObject();
        Matrix observations = (Matrix) in.readObject();
        Kernel kernel = (Kernel) in.readObject();
        MeanModell mm = (MeanModell) in.readObject();
        Object solver = in.readObject();
        in.close();

        int n = trainX.length;
        int d = trainX[0].length;

        System.out.println("file        : " + args[0]);
        System.out.println("training set: " + n + " samples x " + d + " covariates");
        System.out.println();

        System.out.println("normalisation the model applies (Learner min/max/base):");
        for (int k = 0; k < d; k++) {
            System.out.printf("   covariate %d   min %10.4f   max %10.4f   base %10.4f%n",
                    k, min[k], max[k], base[k]);
        }
        System.out.println();

        System.out.println("kernel      : " + kernel.getClass().getSimpleName());
        System.out.println("mean model  : " + mm.getClass().getSimpleName());
        String sname = (solver instanceof CholeskyDecomposition) ? "Cholesky"
                : (solver instanceof LUDecomposition) ? "LU" : solver.getClass().getSimpleName();
        System.out.println("solver      : " + sname);
        if (solver instanceof CholeskyDecomposition) {
            System.out.println("              SPD = " + ((CholeskyDecomposition) solver).isSPD()
                    + "   (a well-posed covariance matrix)");
        }
        System.out.println();

        // alpha is where a degenerate model shows itself
        double[][] a = alpha.getArray();
        double amin = Double.POSITIVE_INFINITY, amax = Double.NEGATIVE_INFINITY;
        double asum = 0;
        int bad = 0, cnt = 0;
        for (double[] row : a) {
            for (double v : row) {
                cnt++;
                if (Double.isNaN(v) || Double.isInfinite(v)) {
                    bad++;
                    continue;
                }
                amin = Math.min(amin, v);
                amax = Math.max(amax, v);
                asum += Math.abs(v);
            }
        }
        System.out.println("alpha (the fitted weights - all zero or NaN means a dead model):");
        System.out.printf("   entries %d   non-finite %d   min %+.5f   max %+.5f   "
                + "mean|a| %.5f%n", cnt, bad, amin, amax, asum / Math.max(cnt - bad, 1));
        if (bad > 0) {
            System.out.println("   *** NON-FINITE ENTRIES: this model predicts NaN. Do not use.");
        } else if (asum / Math.max(cnt, 1) < 1e-9) {
            System.out.println("   *** alpha is essentially zero: every prediction will be the "
                    + "prior mean. Do not use.");
        }
        System.out.println();

        double[][] o = observations.getArray();
        double omin = Double.POSITIVE_INFINITY, omax = Double.NEGATIVE_INFINITY;
        for (double[] row : o) {
            for (double v : row) {
                if (Double.isFinite(v)) {
                    omin = Math.min(omin, v);
                    omax = Math.max(omax, v);
                }
            }
        }
        System.out.printf("observations (target after the mean model): %d values, "
                + "range %+.4f .. %+.4f%n", o.length * o[0].length, omin, omax);
        System.out.println("   (standardised units - multiply by the target scale for permil)");
        System.out.println();

        System.out.println("first 3 training rows (standardised covariates):");
        for (int i = 0; i < Math.min(3, n); i++) {
            System.out.println("   " + Arrays.toString(round(trainX[i])));
        }

        if (args.length > 1) {
            System.out.println();
            System.out.println("feature list (" + args[1] + "):");
            for (String line : java.nio.file.Files.readAllLines(
                    java.nio.file.Paths.get(args[1]))) {
                if (!line.trim().isEmpty()) {
                    System.out.println("   " + line.trim());
                }
            }
        }
    }

    private static double[] round(double[] v) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = Math.round(v[i] * 10000.0) / 10000.0;
        }
        return r;
    }
}
