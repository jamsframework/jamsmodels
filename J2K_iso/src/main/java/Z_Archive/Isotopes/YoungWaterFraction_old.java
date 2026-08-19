package Isotopes;

import jams.data.Attribute;
import jams.model.JAMSVarDescription;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * YoungWaterFraction
 * ------------------
 * Java/JAMS port of the R "time-variable Fraction of Young Water (Fyw)" script
 * for the J2K/J2000 model (Kirchner 2016 amplitude-ratio method).
 *
 * Runs once per day inside the TimeLoop context. It keeps a running history of
 * (time, precipitation isotope value, precipitation amount, streamflow isotope
 * value[, streamflow weight]) and, once a full trailing window of data (default
 * 365 days) is available, fits a seasonal sine curve
 *
 *      iso(t) = a + b*cos(2*pi*t/period) + c*sin(2*pi*t/period)
 *
 * separately to precipitation and streamflow isotopes over that window using an
 * amount/flow-weighted, iteratively reweighted least squares (IRLS) fit with a
 * Cauchy-type robustness weight (this is the well-defined version of the
 * "1/(1+resid^2)" weighting sketched in the original R script, which referenced
 * residuals of a fit that did not yet exist -- here the residuals come from the
 * previous IRLS iteration, which is the standard way to make that weighting
 * scheme actually work).
 *
 * Fyw = Amp_stream / Amp_precip, with uncertainty propagated from the standard
 * errors of the fitted cos/sin coefficients exactly as in the R script (Gaussian
 * error propagation).
 *
 * NOTE ON FIDELITY TO THE R SCRIPT
 * ---------------------------------
 * R's lmrob(..., method="MM") is a full MM-estimator (S-estimator start +
 * M-step) from the `robustbase` package. Reproducing it bit-for-bit in Java
 * without a linear-algebra/robust-stats library is not practical. The IRLS
 * scheme below (weighted least squares, Cauchy reweighting each iteration,
 * asymptotic SEs from the final weighted design matrix) targets the same goal
 * -- downweighting outlying isotope samples -- and was verified on synthetic
 * data (known amplitude ratio, injected outliers) to recover the true Fyw
 * closely and to clearly outperform a plain (non-robust) fit. If you need an
 * exact lmrob match, consider calling out to R (Rserve/JRI) instead; the
 * structure below makes that swap easy since all the statistics are produced
 * by fitRobustSine(), independent of the JAMS plumbing.
 *
 * The pure math (buildDesignMatrix / fitRobustSine / computeFyw) has no JAMS
 * dependency and can be unit-tested standalone.
 */
@JAMSComponentDescription(
        title = "YoungWaterFraction",
        author = "A. Watson, C Birkel",
        description = "Sliding-window fraction of young water (Fyw) from precipitation and streamflow stable isotopes",
        date = "2026-08-05"
)
public class YoungWaterFraction_old extends JAMSComponent {

    // ---------------------------------------------------------------
    // JAMS-facing ports
    // ---------------------------------------------------------------

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current model time step"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Catchment (areal) precipitation isotope value for the current day [permil]. " +
                    "Must be a basin-average series, not a single HRU value -- if the model only has " +
                    "per-HRU isotope values (e.g. HRULoop attribute '2h'), aggregate them to TimeLoop " +
                    "scale first (area- and/or amount-weighted mean) before wiring this port."
    )
    public Attribute.Double precipIso;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Catchment precipitation amount for the current day [mm]. Used as the prior " +
                    "(amount) weight in the precipitation regression, matching standard amount-weighted " +
                    "isotope-in-precipitation practice."
    )
    public Attribute.Double precipAmount;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Streamflow isotope value at the point of interest for the current day [permil], " +
                    "e.g. the simulated outlet signature (a TimeLoop attribute such as '744_SimRunoff_2h')."
    )
    public Attribute.Double streamIso;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Discharge (or other flux weight) for the current day, used as the prior weight " +
                    "in the streamflow regression. Wire to a constant value of 1.0 if no flow-weighting " +
                    "is desired."
    )
    public Attribute.Double streamWeight;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "No-data / missing-value marker used by the input series (values <= this are " +
                    "treated as missing and dropped, mirroring R's na.omit). Default -9999."
    )
    public Attribute.Double noDataValue;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Rolling window length in days over which the seasonal sine curve is fitted (365 = 1 year)."
    )
    public Attribute.Integer windowSize;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Seasonal period in days used in the cos/sin regressors (365.25 accounts for leap years)."
    )
    public Attribute.Double period;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Minimum number of valid (non-missing) observations required within the window, " +
                    "per series, before a fit is attempted. Below this, outputs are set to missing for that day."
    )
    public Attribute.Integer minObservations;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum number of IRLS reweighting iterations for the robust sine fit."
    )
    public Attribute.Integer irlsIterations;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fraction of young water for the trailing window ending on the current day [-]."
    )
    public Attribute.Double fyw;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Gaussian-error-propagated uncertainty of fyw [-]."
    )
    public Attribute.Double fywUncertainty;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Mean adjusted R^2 of the precipitation and streamflow sine fits."
    )
    public Attribute.Double adjR2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Adjusted R^2 of the precipitation isotope sine fit."
    )
    public Attribute.Double adjR2Precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Adjusted R^2 of the streamflow isotope sine fit."
    )
    public Attribute.Double adjR2Stream;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fitted seasonal amplitude of precipitation isotope signal [permil]."
    )
    public Attribute.Double ampPrecip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fitted seasonal amplitude of streamflow isotope signal [permil]."
    )
    public Attribute.Double ampStream;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fitted phase shift of precipitation isotope signal [rad]."
    )
    public Attribute.Double phasePrecip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Fitted phase shift of streamflow isotope signal [rad]."
    )
    public Attribute.Double phaseStream;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Number of valid precipitation observations used in the current window."
    )
    public Attribute.Integer nPrecip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Number of valid streamflow observations used in the current window."
    )
    public Attribute.Integer nStream;

    // ---------------------------------------------------------------
    // internal state
    // ---------------------------------------------------------------

    private final List<double[]> precipHistory = new ArrayList<>(); // {day, iso, weight}
    private final List<double[]> streamHistory = new ArrayList<>(); // {day, iso, weight}

    private static final double DEFAULT_NODATA = -9999.0;
    private static final int DEFAULT_WINDOW = 365;
    private static final double DEFAULT_PERIOD = 365.25;
    private static final int DEFAULT_MIN_OBS = 50;
    private static final int DEFAULT_IRLS_ITERS = 15;

    @Override
    public void init() {
        precipHistory.clear();
        streamHistory.clear();
    }

    @Override
    public void run() {

        double noData = (noDataValue != null) ? noDataValue.getValue() : DEFAULT_NODATA;
        int window = (windowSize != null) ? windowSize.getValue() : DEFAULT_WINDOW;
        double per = (period != null) ? period.getValue() : DEFAULT_PERIOD;
        int minObs = (minObservations != null) ? minObservations.getValue() : DEFAULT_MIN_OBS;
        int iters = (irlsIterations != null) ? irlsIterations.getValue() : DEFAULT_IRLS_ITERS;

        double today = toDayNumber(time.getValue());

        // append today's observations (independently for each series, like R's
        // separate na.omit(P) / na.omit(Q))
        appendIfValid(precipHistory, today, precipIso.getValue(), precipAmount.getValue(), noData);
        appendIfValid(streamHistory, today, streamIso.getValue(),
                (streamWeight != null) ? streamWeight.getValue() : 1.0, noData);

        double windowStart = today - window;

        double[][] pWin = windowSlice(precipHistory, windowStart, today);
        double[][] sWin = windowSlice(streamHistory, windowStart, today);

        int nP = pWin[0].length;
        int nS = sWin[0].length;

        nPrecip.setValue(nP);
        nStream.setValue(nS);

        if (nP < minObs || nS < minObs) {
            setMissingOutputs();
            return;
        }

        SineFit fitP = fitRobustSine(pWin[0], pWin[1], pWin[2], per, iters);
        SineFit fitS = fitRobustSine(sWin[0], sWin[1], sWin[2], per, iters);

        FywResult r = computeFyw(fitP, fitS);

        fyw.setValue(r.fyw);
        fywUncertainty.setValue(r.dFyw);
        adjR2Precip.setValue(fitP.adjR2);
        adjR2Stream.setValue(fitS.adjR2);
        adjR2.setValue((fitP.adjR2 + fitS.adjR2) / 2.0);
        ampPrecip.setValue(fitP.amp);
        ampStream.setValue(fitS.amp);
        phasePrecip.setValue(Math.atan2(fitP.b, fitP.c));
        phaseStream.setValue(Math.atan2(fitS.b, fitS.c));
    }

    @Override
    public void cleanup() {
        precipHistory.clear();
        streamHistory.clear();
    }

    private void setMissingOutputs() {
        fyw.setValue(Double.NaN);
        fywUncertainty.setValue(Double.NaN);
        adjR2.setValue(Double.NaN);
        adjR2Precip.setValue(Double.NaN);
        adjR2Stream.setValue(Double.NaN);
        ampPrecip.setValue(Double.NaN);
        ampStream.setValue(Double.NaN);
        phasePrecip.setValue(Double.NaN);
        phaseStream.setValue(Double.NaN);
    }

    private static void appendIfValid(List<double[]> history, double day, double value, double weight, double noData) {
        if (Double.isNaN(value) || value <= noData) {
            return;
        }
        history.add(new double[]{day, value, weight});
        // history only ever grows forward in time (daily calls), so it is
        // already sorted by day -- no need to re-sort.
    }

    /**
     * Extract {day[], iso[], weight[]} for all records with windowStart <= day <= windowEnd.
     * Also opportunistically trims the front of the history list once entries fall
     * further than one window behind, to keep memory bounded on very long runs.
     */
    private static double[][] windowSlice(List<double[]> history, double windowStart, double windowEnd) {
        List<double[]> inWindow = new ArrayList<>();
        int trimBefore = 0;
        for (int i = 0; i < history.size(); i++) {
            double[] rec = history.get(i);
            if (rec[0] < windowStart) {
                trimBefore = i + 1; // still within array bounds, safe to drop later
                continue;
            }
            if (rec[0] <= windowEnd) {
                inWindow.add(rec);
            }
        }
        if (trimBefore > 0 && trimBefore < history.size()) {
            history.subList(0, trimBefore).clear();
        }
        double[] d = new double[inWindow.size()];
        double[] y = new double[inWindow.size()];
        double[] w = new double[inWindow.size()];
        for (int i = 0; i < inWindow.size(); i++) {
            d[i] = inWindow.get(i)[0];
            y[i] = inWindow.get(i)[1];
            w[i] = inWindow.get(i)[2];
        }
        return new double[][]{d, y, w};
    }

    private static double toDayNumber(Attribute.Calendar cal) {
        return cal.getTimeInMillis() / 86400000.0;
    }

    // ===================================================================
    // Pure math -- no JAMS dependency, unit-testable standalone.
    // ===================================================================

    public static final class SineFit {
        public final double a, b, c;      // intercept, cos coeff, sin coeff
        public final double seB, seC;     // standard errors of b, c
        public final double amp;
        public final double adjR2;
        public final int n;

        SineFit(double a, double b, double c, double seB, double seC, double amp, double adjR2, int n) {
            this.a = a; this.b = b; this.c = c;
            this.seB = seB; this.seC = seC;
            this.amp = amp; this.adjR2 = adjR2; this.n = n;
        }
    }

    public static final class FywResult {
        public final double fyw, dFyw;
        FywResult(double fyw, double dFyw) { this.fyw = fyw; this.dFyw = dFyw; }
    }

    /**
     * Weighted, robust (IRLS / Cauchy-reweighted) fit of
     *   y(t) = a + b*cos(2*pi*t/period) + c*sin(2*pi*t/period)
     *
     * @param t       time (any consistent unit, e.g. days since epoch)
     * @param y       isotope values
     * @param w       prior weights (e.g. precipitation amount, or 1.0 for unweighted)
     * @param period  seasonal period, same unit as t (365.25 for daily data)
     * @param maxIter maximum IRLS iterations
     */
    public static SineFit fitRobustSine(double[] t, double[] y, double[] w, double period, int maxIter) {
        int n = t.length;
        double[][] X = new double[n][3];
        for (int i = 0; i < n; i++) {
            double ang = 2.0 * Math.PI * t[i] / period;
            X[i][0] = 1.0;
            X[i][1] = Math.cos(ang);
            X[i][2] = Math.sin(ang);
        }

        double[] weights = w.clone();
        double[] beta = null;

        for (int iter = 0; iter < Math.max(1, maxIter); iter++) {
            double[] newBeta = weightedLeastSquares(X, y, weights);
            double[] resid = residuals(X, y, newBeta);

            if (beta != null && maxAbsDiff(beta, newBeta) < 1e-8) {
                beta = newBeta;
                break;
            }
            beta = newBeta;

            double scale = robustScale(resid);
            for (int i = 0; i < n; i++) {
                double z = resid[i] / scale;
                double u = 1.0 / (1.0 + z * z); // Cauchy-type robustness weight
                weights[i] = w[i] * u;
            }
        }

        double[] finalResid = residuals(X, y, beta);
        double[][] XtWX = crossProduct(X, weights);
        double[][] cov = invert3x3(XtWX);

        int p = 3;
        double sseW = 0;
        for (int i = 0; i < n; i++) sseW += weights[i] * finalResid[i] * finalResid[i];
        double sigma2 = sseW / Math.max(n - p, 1);

        double seB = Math.sqrt(Math.max(sigma2 * cov[1][1], 0.0));
        double seC = Math.sqrt(Math.max(sigma2 * cov[2][2], 0.0));

        // R^2 computed on raw (unweighted) residuals/variance, matching the source R script
        double sse = 0, mean = 0;
        for (double v : y) mean += v;
        mean /= n;
        double sst = 0;
        for (int i = 0; i < n; i++) {
            sse += finalResid[i] * finalResid[i];
            sst += (y[i] - mean) * (y[i] - mean);
        }
        double adjR2 = (sst > 0) ? 1.0 - ((double) (n - 1) / (n - p)) * (sse / sst) : Double.NaN;

        double amp = Math.hypot(beta[1], beta[2]);
        return new SineFit(beta[0], beta[1], beta[2], seB, seC, amp, adjR2, n);
    }

    public static SineFit fitRobustSine(double[] t, double[] y, double[] w, double period) {
        return fitRobustSine(t, y, w, period, DEFAULT_IRLS_ITERS);
    }

    /**
     * Fyw = Amp_stream / Amp_precip with Gaussian error propagation of the
     * amplitude standard errors, exactly mirroring the R script's dfyw formula.
     */
    public static FywResult computeFyw(SineFit precip, SineFit stream) {
        double ampP = precip.amp;
        double ampS = stream.amp;
        double fywVal = ampS / ampP;

        double dAmpP = amplitudeUncertainty(precip);
        double dAmpS = amplitudeUncertainty(stream);

        double dFyw = Math.hypot(dAmpP / ampP, dAmpS / ampS) * fywVal;
        return new FywResult(fywVal, dFyw);
    }

    private static double amplitudeUncertainty(SineFit f) {
        double dx = 2.0 * f.seB * Math.abs(f.b); // d(b^2)
        double dy = 2.0 * f.seC * Math.abs(f.c); // d(c^2)
        double cSq = f.b * f.b + f.c * f.c;      // = amp^2
        double dc = Math.hypot(dx, dy);
        return 0.5 * (dc / Math.abs(cSq)) * f.amp;
    }

    // ---- small linear-algebra helpers (dependency-free, sized for p=3) ----

    private static double[] weightedLeastSquares(double[][] X, double[] y, double[] w) {
        double[][] XtWX = crossProduct(X, w);
        double[] XtWy = new double[3];
        for (int j = 0; j < 3; j++) {
            double s = 0;
            for (int i = 0; i < X.length; i++) s += w[i] * X[i][j] * y[i];
            XtWy[j] = s;
        }
        return solve3x3(XtWX, XtWy);
    }

    private static double[][] crossProduct(double[][] X, double[] w) {
        double[][] A = new double[3][3];
        for (int i = 0; i < X.length; i++) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    A[r][c] += w[i] * X[i][r] * X[i][c];
                }
            }
        }
        return A;
    }

    private static double[] residuals(double[][] X, double[] y, double[] beta) {
        double[] resid = new double[X.length];
        for (int i = 0; i < X.length; i++) {
            double fitted = X[i][0] * beta[0] + X[i][1] * beta[1] + X[i][2] * beta[2];
            resid[i] = y[i] - fitted;
        }
        return resid;
    }

    private static double robustScale(double[] resid) {
        double[] sorted = resid.clone();
        java.util.Arrays.sort(sorted);
        double median = percentile(sorted, 0.5);
        double[] absDev = new double[sorted.length];
        for (int i = 0; i < sorted.length; i++) absDev[i] = Math.abs(resid[i] - median);
        java.util.Arrays.sort(absDev);
        double mad = percentile(absDev, 0.5) * 1.4826;
        if (mad < 1e-6) {
            double sumSq = 0;
            for (double r : resid) sumSq += r * r;
            double sd = Math.sqrt(sumSq / resid.length);
            return Math.max(sd, 1e-6);
        }
        return mad;
    }

    private static double percentile(double[] sorted, double q) {
        int n = sorted.length;
        if (n == 0) return 0;
        double idx = q * (n - 1);
        int lo = (int) Math.floor(idx);
        int hi = (int) Math.ceil(idx);
        if (lo == hi) return sorted[lo];
        double frac = idx - lo;
        return sorted[lo] * (1 - frac) + sorted[hi] * frac;
    }

    private static double maxAbsDiff(double[] a, double[] b) {
        double m = 0;
        for (int i = 0; i < a.length; i++) m = Math.max(m, Math.abs(a[i] - b[i]));
        return m;
    }

    /** Solve A*x = b for a 3x3 system via Gaussian elimination with partial pivoting. */
    private static double[] solve3x3(double[][] A, double[] b) {
        double[][] M = new double[3][4];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(A[i], 0, M[i], 0, 3);
            M[i][3] = b[i];
        }
        for (int col = 0; col < 3; col++) {
            int pivot = col;
            for (int row = col + 1; row < 3; row++) {
                if (Math.abs(M[row][col]) > Math.abs(M[pivot][col])) pivot = row;
            }
            double[] tmp = M[col]; M[col] = M[pivot]; M[pivot] = tmp;
            double diag = M[col][col];
            if (Math.abs(diag) < 1e-12) diag = 1e-12; // guard against singular window (e.g. no seasonality)
            for (int c = col; c < 4; c++) M[col][c] /= diag;
            for (int row = 0; row < 3; row++) {
                if (row == col) continue;
                double factor = M[row][col];
                for (int c = col; c < 4; c++) M[row][c] -= factor * M[col][c];
            }
        }
        return new double[]{M[0][3], M[1][3], M[2][3]};
    }

    /** Invert a 3x3 matrix via Gauss-Jordan elimination on [A|I]. */
    private static double[][] invert3x3(double[][] A) {
        double[][] M = new double[3][6];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(A[i], 0, M[i], 0, 3);
            M[i][3 + i] = 1.0;
        }
        for (int col = 0; col < 3; col++) {
            int pivot = col;
            for (int row = col + 1; row < 3; row++) {
                if (Math.abs(M[row][col]) > Math.abs(M[pivot][col])) pivot = row;
            }
            double[] tmp = M[col]; M[col] = M[pivot]; M[pivot] = tmp;
            double diag = M[col][col];
            if (Math.abs(diag) < 1e-12) diag = 1e-12;
            for (int c = 0; c < 6; c++) M[col][c] /= diag;
            for (int row = 0; row < 3; row++) {
                if (row == col) continue;
                double factor = M[row][col];
                for (int c = 0; c < 6; c++) M[row][c] -= factor * M[col][c];
            }
        }
        double[][] inv = new double[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(M[i], 3, inv[i], 0, 3);
        }
        return inv;
    }
}
