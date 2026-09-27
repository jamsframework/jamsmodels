package calc;

/*
 * SafeDivide.java
 * Created on 31.08.2026
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

/**
 * Division that cannot emit NaN or infinity.
 *
 * jams.components.calc.DoubleDivide happily divides by zero. In a normal run
 * that rarely matters, but under an optimiser it does: a parameter set that
 * empties a storage makes the denominator zero, the quotient becomes NaN, and
 * optas.efficiencies.setObjective converts a NaN efficiency into
 * Double.MAX_VALUE. That is 1.8e308 sitting in an objective column - it does
 * not fail, it silently destroys the scale of that objective and, in NSGA-II,
 * the crowding distance along that dimension.
 *
 * The intended use is a concentration computed as mass over volume, where the
 * volume can legitimately go to zero for a bad parameter set. Returning the
 * fallback rather than NaN lets that parameter set score badly on its own
 * merits - a fixed, obviously wrong concentration produces a large but finite
 * error - instead of poisoning the objective for every other solution.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
@JAMSComponentDescription(
        title = "SafeDivide",
        author = "Andrew Watson",
        description = "Divides two doubles, substituting a configurable fallback "
        + "whenever the denominator is too small or the result would be NaN or "
        + "infinite. Written for ratios that feed an optimiser objective: the stock "
        + "DoubleDivide emits NaN on a zero denominator, and the OPTAS efficiency "
        + "calculators turn a NaN efficiency into Double.MAX_VALUE, which wrecks "
        + "the scale of that objective rather than merely penalising the parameter "
        + "set that caused it.",
        date = "2026-08-31",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SafeDivide extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Numerator")
    public Attribute.Double d1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Denominator")
    public Attribute.Double d2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "The quotient, or the fallback when the division is not valid")
    public Attribute.Double result;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Denominators with a smaller absolute value than this are "
            + "treated as zero.",
            defaultValue = "1.0E-9")
    public Attribute.Double minDenominator;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Value substituted when the division is not valid. Choose "
            + "something clearly wrong but physically finite, so the parameter set "
            + "that triggered it scores badly rather than producing NaN.",
            defaultValue = "0.0")
    public Attribute.Double fallback;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Counts how often the fallback was used. Trace it: a large "
            + "count means the objective is being driven by degenerate states rather "
            + "than by fit.")
    public Attribute.Double fallbackCount = null;

    private double count;

    @Override
    public void init() {
        count = 0.0;
        if (fallbackCount != null) {
            fallbackCount.setValue(0.0);
        }
    }

    @Override
    public void run() {
        double a = d1.getValue();
        double b = d2.getValue();
        double out;

        if (Math.abs(b) < Math.abs(minDenominator.getValue())
                || Double.isNaN(a) || Double.isNaN(b)
                || Double.isInfinite(a) || Double.isInfinite(b)) {
            out = fallback.getValue();
            count++;
        } else {
            out = a / b;
            if (Double.isNaN(out) || Double.isInfinite(out)) {
                out = fallback.getValue();
                count++;
            }
        }

        result.setValue(out);
        if (fallbackCount != null) {
            fallbackCount.setValue(count);
        }
    }
}
