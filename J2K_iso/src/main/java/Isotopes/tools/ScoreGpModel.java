package Isotopes.tools;

import jams.components.machineLearning.kernels.Kernel;
import jams.components.machineLearning.kernels.MaternClass;
import jams.components.machineLearning.kernels.FixedMeanModell;
import jams.components.machineLearning.kernels.LinearMeanModell;
import jams.components.machineLearning.kernels.MeanModell;

import Jama.CholeskyDecomposition;
import Jama.LUDecomposition;
import Jama.Matrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces genuinely out-of-sample predictions for the precipitation-isotope GP,
 * computed with the SAME jams.components.machineLearning classes the model runs -
 * MaternClass, LinearMeanModell and Jama - through the same arithmetic
 * IsoMLBiasCorrection uses at prediction time.
 *
 * WHY THIS EXISTS
 * ---------------
 * Every skill figure quoted for this correction so far came from an offline
 * Python reconstruction of MaternClass. The reconstruction is faithful and it
 * agrees with an independent scikit-learn implementation, but it is still not the
 * thing that runs in the model, and nobody should be asked to take a paper result
 * on that basis. GaussianLearner's own validation output is unavailable because
 * its optimiser never converges on this problem, so this tool fills the gap: real
 * classes, real kernel, real serialisation arithmetic, held out by station.
 *
 * HOW THE HOLD-OUT WORKS
 * ----------------------
 * Folds are grouped by STATION, never by row. Samples from one station share an
 * elevation, a location and a local climate, so a random split would leave near
 * neighbours of every test row in the training set and flatter the score. What
 * the correction actually has to do is predict the bias where no isotope station
 * exists, and only a station-grouped split measures that.
 *
 * Within each fold, the standardisation AND the min/max normalisation are fitted
 * on the training stations alone and then applied to the held-out ones. Fitting
 * either on all the data first would leak the held-out stations into the model
 * and inflate the result - which is the most common way a cross-validated score
 * ends up meaning nothing.
 *
 * The noise term is added to the training diagonal here for the same reason
 * BuildGpModel adds it: MaternClass computes theta[last]^2 when i == j and then
 * discards it without adding it to the returned value, so k(x,x) = 1 exactly. The
 * covariate set is catchment-wide, many rows share covariates, and without that
 * term the covariance matrix is exactly singular.
 *
 * USAGE
 *   java Isotopes.tools.ScoreGpModel <samples.dat> <comma,separated,covariates>
 *                                    <logTheta,comma,separated> <stationColumn>
 *                                    <out.csv> [folds] [clip]
 *
 * Pass "clip" as the last argument to clip each held-out covariate to the range
 * of its training fold before predicting, as IsoMLBiasCorrection 2.3_0 does by
 * default (clipToTrainingRange). Without it the scorer predicts unclipped, which
 * reproduces every skill figure computed before 2.3_0. Pass "fixedmean" to use
 * FixedMeanModell (a constant mean) instead of LinearMeanModell.
 *
 * Writes one row per sample: station, the covariates, observed bias, the
 * out-of-sample prediction, and the GP's predictive variance - all in PHYSICAL
 * units (per mil), ready to be scored and plotted.
 *
 * @author prepared for Andrew Watson
 */
public class ScoreGpModel {

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("usage: ScoreGpModel <samples.dat> <covariates> "
                    + "<logTheta> <stationColumn> <out.csv> [folds] [clip] [fixedmean]");
            System.exit(2);
        }
        String samplesFile = args[0];
        String[] cov = args[1].split(",");
        double[] logTheta = parseDoubles(args[2]);
        String stationCol = args[3].trim();
        String outFile = args[4];
        int folds = (args.length > 5) ? Integer.parseInt(args[5]) : 6;
        boolean clip = false, fixedMean = false;
        for (int a = 6; a < args.length; a++) {
            clip |= args[a].trim().equalsIgnoreCase("clip");
            fixedMean |= args[a].trim().equalsIgnoreCase("fixedmean");
        }

        for (int i = 0; i < cov.length; i++) {
            cov[i] = cov[i].trim();
        }
        if (logTheta.length != cov.length + 1) {
            throw new IllegalArgumentException("expected " + (cov.length + 1)
                    + " log-theta values, got " + logTheta.length);
        }

        // ------------------------------------------------------------ read
        List<String> header = new ArrayList<String>();
        List<double[]> raw = new ArrayList<double[]>();
        BufferedReader in = new BufferedReader(new FileReader(samplesFile));
        String line = in.readLine();
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
            raw.add(r);
        }
        in.close();

        int[] covIdx = new int[cov.length];
        for (int k = 0; k < cov.length; k++) {
            covIdx[k] = header.indexOf(cov[k]);
            if (covIdx[k] < 0) {
                throw new IllegalArgumentException("covariate \"" + cov[k]
                        + "\" not in " + header);
            }
        }
        int stIdx = header.indexOf(stationCol);
        if (stIdx < 0) {
            throw new IllegalArgumentException("station column \"" + stationCol
                    + "\" not in " + header);
        }
        int targetIdx = header.size() - 1;      // last column is the target

        int n = raw.size();
        int d = cov.length;
        double[][] X = new double[n][d];
        double[] y = new double[n];
        String[] station = new String[n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                X[i][k] = raw.get(i)[covIdx[k]];
            }
            y[i] = raw.get(i)[targetIdx];
            station[i] = String.valueOf(raw.get(i)[stIdx]);
        }

        // ------------------------------------------------- station-grouped folds
        Map<String, Integer> stationFold = new LinkedHashMap<String, Integer>();
        int next = 0;
        for (String s : station) {
            if (!stationFold.containsKey(s)) {
                stationFold.put(s, next % folds);
                next++;
            }
        }
        int nStations = stationFold.size();
        folds = Math.min(folds, nStations);
        System.out.println("samples    : " + n + " rows, " + nStations + " stations, "
                + folds + " station-grouped folds");
        System.out.println("covariates : " + Arrays.toString(cov)
                + (clip ? "  (held-out covariates clipped to the training range)" : ""));

        double[] pred = new double[n];
        double[] pvar = new double[n];
        Arrays.fill(pred, Double.NaN);
        Arrays.fill(pvar, Double.NaN);

        for (int f = 0; f < folds; f++) {
            List<Integer> tr = new ArrayList<Integer>();
            List<Integer> te = new ArrayList<Integer>();
            for (int i = 0; i < n; i++) {
                if (stationFold.get(station[i]) == f) {
                    te.add(i);
                } else {
                    tr.add(i);
                }
            }
            if (tr.isEmpty() || te.isEmpty()) {
                continue;
            }
            fold(X, y, tr, te, logTheta, d, pred, pvar, clip, fixedMean);
            System.out.println("  fold " + f + ": train " + tr.size()
                    + ", held out " + te.size());
        }

        // ------------------------------------------------------------ write
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
        out.write("station");
        for (String c : cov) {
            out.write("," + c);
        }
        out.write(",observed,predicted,variance");
        out.newLine();
        int finite = 0;
        for (int i = 0; i < n; i++) {
            out.write(station[i]);
            for (int k = 0; k < d; k++) {
                out.write("," + X[i][k]);
            }
            out.write("," + y[i] + "," + pred[i] + "," + pvar[i]);
            out.newLine();
            if (!Double.isNaN(pred[i])) {
                finite++;
            }
        }
        out.close();
        System.out.println("wrote      : " + outFile + "  (" + finite
                + " out-of-sample predictions)");
    }

    /** One station-held-out fold: fit on tr, predict te, no leakage either way. */
    private static void fold(double[][] X, double[] y, List<Integer> tr,
            List<Integer> te, double[] logTheta, int d, double[] pred, double[] pvar,
            boolean clip, boolean fixedMean) {

        int m = tr.size();

        // standardisation fitted on the TRAINING stations only
        double[] fMean = new double[d];
        double[] fSd = new double[d];
        for (int k = 0; k < d; k++) {
            double s = 0;
            for (int i : tr) {
                s += X[i][k];
            }
            fMean[k] = s / m;
            double ss = 0;
            for (int i : tr) {
                double dv = X[i][k] - fMean[k];
                ss += dv * dv;
            }
            double sd = Math.sqrt(ss / m);
            fSd[k] = (sd > 1e-12) ? sd : 1.0;
        }
        double ts = 0;
        for (int i : tr) {
            ts += y[i];
        }
        double tMean = ts / m;
        double tss = 0;
        for (int i : tr) {
            double dv = y[i] - tMean;
            tss += dv * dv;
        }
        double tSd = Math.sqrt(tss / m);
        if (!(tSd > 1e-12)) {
            tSd = 1.0;
        }

        double[][] Xtr = new double[m][d];
        double[] ytr = new double[m];
        for (int a = 0; a < m; a++) {
            int i = tr.get(a);
            for (int k = 0; k < d; k++) {
                Xtr[a][k] = (X[i][k] - fMean[k]) / fSd[k];
            }
            ytr[a] = (y[i] - tMean) / tSd;
        }

        // min/max/base, as Learner computes them - on the training rows only
        double[] min = new double[d];
        double[] max = new double[d];
        double[] base = new double[d];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int a = 0; a < m; a++) {
            for (int k = 0; k < d; k++) {
                min[k] = Math.min(min[k], Xtr[a][k]);
                max[k] = Math.max(max[k], Xtr[a][k]);
            }
        }
        double[] lo = min.clone();
        double[] hi = max.clone();
        for (int k = 0; k < d; k++) {
            base[k] = (min[k] + max[k]) / 2.0;
            if (max[k] - min[k] == 0.0) {
                max[k] = min[k] + 1.0;
            }
        }

        double[] theta = new double[logTheta.length];
        for (int i = 0; i < logTheta.length; i++) {
            theta[i] = Math.exp(logTheta[i]);
        }
        MaternClass kernel = new MaternClass(d);
        MeanModell mm = fixedMean ? new FixedMeanModell(d) : new LinearMeanModell(d);
        kernel.SetMeanModell(mm);
        kernel.SetParameter(theta);
        mm.create(Xtr, ytr);
        Matrix obs = mm.Transform(Xtr, ytr);

        double[][] Kd = new double[m][m];
        for (int a = 0; a < m; a++) {
            double[] xa = normalize(Xtr[a], base, min, max);
            for (int b = 0; b < m; b++) {
                Kd[a][b] = kernel.kernel(xa, normalize(Xtr[b], base, min, max), a, b);
            }
        }
        // the noise MaternClass computes and discards
        double noise = theta[theta.length - 1] * theta[theta.length - 1];
        for (int a = 0; a < m; a++) {
            Kd[a][a] += noise;
        }
        for (int a = 0; a < m; a++) {
            for (int b = a + 1; b < m; b++) {
                double avg = 0.5 * (Kd[a][b] + Kd[b][a]);
                Kd[a][b] = avg;
                Kd[b][a] = avg;
            }
        }
        Matrix K = new Matrix(Kd);
        Matrix alpha;
        Matrix Kinv;
        CholeskyDecomposition chol = K.chol();
        if (chol.isSPD()) {
            alpha = chol.solve(obs);
            Kinv = chol.solve(Matrix.identity(m, m));
        } else {
            LUDecomposition lu = K.lu();
            alpha = lu.solve(obs);
            Kinv = lu.solve(Matrix.identity(m, m));
        }

        for (int i : te) {
            double[] xs = new double[d];
            for (int k = 0; k < d; k++) {
                xs[k] = (X[i][k] - fMean[k]) / fSd[k];
                if (clip) {
                    xs[k] = Math.min(Math.max(xs[k], lo[k]), hi[k]);
                }
            }
            double[] xn = normalize(xs, base, min, max);

            Matrix kstar = new Matrix(1, m);
            for (int a = 0; a < m; a++) {
                kstar.set(0, a, kernel.kernel(normalize(Xtr[a], base, min, max), xn, a, -1));
            }
            // mean: exactly IsoMLBiasCorrection.predictMean, then undo the target scaling
            double p = kernel.MM.ReTransform(new double[][]{xs}, kstar.times(alpha))[0];
            pred[i] = p * tSd + tMean;

            // variance: k(x,x) - kstar K^-1 kstar', scaled back to permil^2
            double kxx = kernel.kernel(xn, xn, -1, -2) + noise;
            double reduction = kstar.times(Kinv).times(kstar.transpose()).get(0, 0);
            pvar[i] = Math.max(kxx - reduction, 0.0) * tSd * tSd;
        }
    }

    private static double[] normalize(double[] x, double[] base, double[] min, double[] max) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            r[i] = 2.0 * (x[i] - base[i]) / (max[i] - min[i]);
        }
        return r;
    }

    private static double[] parseDoubles(String s) {
        String[] p = s.split(",");
        double[] r = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            r[i] = Double.parseDouble(p[i].trim());
        }
        return r;
    }
}
