package Isotopes;

import jams.data.*;
import jams.model.*;

import java.util.ArrayList;

@JAMSComponentDescription(
    title="TimeVariableFYW",
    author="Andrew Watson, Christian Birkel",
    description="Time variable FYW",
    date="2026-07-08",
    version="1.0"
)


public class TimeVariableFYW extends JAMSComponent {

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ,
            description="Precipitation isotope")
    public Attribute.Double rain2h;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ,
            description="Stream isotope")
    public Attribute.Double stream2h;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ,
            description="Simulation time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.READ,
            description="Window length")
    public Attribute.Integer windowLength;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE,
            description="Fraction of Young Water")
    public Attribute.Double fyw;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE,
            description="Number of valid (non-NaN) precipitation isotope observations currently in the window")
    public Attribute.Integer nPrecipObs;

    @JAMSVarDescription(access=JAMSVarDescription.AccessType.WRITE,
            description="Number of valid (non-NaN) streamflow isotope observations currently in the window")
    public Attribute.Integer nStreamObs;

    private static final double PERIOD = 365.25;
    private static final int MIN_POINTS = 10; // minimum valid points needed for a stable 3-parameter fit

    // {day, value} pairs, kept independently per series so a NaN/missing day in one
    // series (e.g. rain2h on a non-rain day) never drops a valid day from the other.
    private final ArrayList<double[]> precipHistory = new ArrayList<>();
    private final ArrayList<double[]> streamHistory = new ArrayList<>();

    @Override
    public void run() {

        double today = time.getTimeInMillis() / 86400000.0;

        appendIfValid(precipHistory, today, rain2h.getValue());
        appendIfValid(streamHistory, today, stream2h.getValue());

        double windowStart = today - windowLength.getValue();
        trimBefore(precipHistory, windowStart);
        trimBefore(streamHistory, windowStart);

        nPrecipObs.setValue(precipHistory.size());
        nStreamObs.setValue(streamHistory.size());

        if (precipHistory.size() < MIN_POINTS || streamHistory.size() < MIN_POINTS) {
            fyw.setValue(Double.NaN);
            return;
        }

        double ampPrecip = fitAmplitude(precipHistory);
        double ampStream = fitAmplitude(streamHistory);

        fyw.setValue(ampStream / ampPrecip);
    }

    private static void appendIfValid(ArrayList<double[]> history, double day, double value) {
        if (Double.isNaN(value)) {
            return;
        }
        history.add(new double[]{day, value});
    }

    private static void trimBefore(ArrayList<double[]> history, double windowStart) {
        int trimBefore = 0;
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i)[0] < windowStart) {
                trimBefore = i + 1;
            } else {
                break;
            }
        }
        if (trimBefore > 0) {
            history.subList(0, trimBefore).clear();
        }
    }

    /**
     * Plain (unweighted) least-squares fit of y(t) = a + b*cos(2*pi*t/period) + c*sin(2*pi*t/period),
     * returning the seasonal amplitude sqrt(b^2 + c^2).
     */
    private static double fitAmplitude(ArrayList<double[]> series) {
        int n = series.size();
        double s1 = n, sx = 0, ss = 0, sxx = 0, sss = 0, sxs = 0;
        double sy = 0, sxy = 0, ssy = 0;

        for (int i = 0; i < n; i++) {
            double ang = 2.0 * Math.PI * series.get(i)[0] / PERIOD;
            double cosA = Math.cos(ang);
            double sinA = Math.sin(ang);
            double yi = series.get(i)[1];

            sx += cosA;
            ss += sinA;
            sxx += cosA * cosA;
            sss += sinA * sinA;
            sxs += cosA * sinA;
            sy += yi;
            sxy += cosA * yi;
            ssy += sinA * yi;
        }

        double[][] A = {
            {s1, sx, ss},
            {sx, sxx, sxs},
            {ss, sxs, sss}
        };
        double[] rhs = {sy, sxy, ssy};
        double[] beta = solve3x3(A, rhs);

        return Math.hypot(beta[1], beta[2]);
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
            if (Math.abs(diag) < 1e-12) diag = 1e-12;
            for (int c = col; c < 4; c++) M[col][c] /= diag;
            for (int row = 0; row < 3; row++) {
                if (row == col) continue;
                double factor = M[row][col];
                for (int c = col; c < 4; c++) M[row][c] -= factor * M[col][c];
            }
        }
        return new double[]{M[0][3], M[1][3], M[2][3]};
    }
}
