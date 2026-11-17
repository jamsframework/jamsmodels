package Isotopes;

/*
 * MonthlyIndexLookup.java
 * Created on 28.08.2026
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Small, dependency-free loader for a "station,date,...,value,..." CSV such as
 * combined_iEMI_results_level1.csv, filtered to one station and one value
 * column, keyed by "yyyy-MM" for lookup against the current model month.
 * Not a JAMSComponent - a plain shared helper used by both
 * IsoBiasTrainingCollector (training) and IsoMLBiasCorrection (application),
 * so the same monthly value is available at both stages without duplicating
 * the CSV-reading logic.
 *
 * CSV parsing here is intentionally naive (split on comma, strip surrounding
 * quotes) - fine for a plain numeric/date/short-string file like this one,
 * but it will break on any field containing an embedded comma or quote.
 *
 * @author Andrew Watson <awatson@sun.ac.za>
 */
public final class MonthlyIndexLookup {

    private MonthlyIndexLookup() {
    }

    /**
     * @param csvPath absolute or relative (to JVM working dir) path to the CSV
     * @param stationFilter only rows whose station column equals this value are kept
     * @param stationColumn header name of the station column
     * @param dateColumn header name of the date column (expects "yyyy-MM-dd..." - only the first 7 characters are used)
     * @param valueColumn header name of the value column to extract
     * @return map from "yyyy-MM" to the parsed value, for every row that matched the station filter and had a parseable, non-"NA" value
     */
    public static Map<String, Double> load(String csvPath, String stationFilter,
            String stationColumn, String dateColumn, String valueColumn) throws IOException {

        Map<String, Double> result = new HashMap<String, Double>();
        BufferedReader reader = new BufferedReader(new FileReader(csvPath));

        try {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result;
            }
            String[] headers = headerLine.split(",");
            int stationIdx = indexOf(headers, stationColumn);
            int dateIdx = indexOf(headers, dateColumn);
            int valueIdx = indexOf(headers, valueColumn);

            if (stationIdx < 0 || dateIdx < 0 || valueIdx < 0) {
                throw new IOException("MonthlyIndexLookup: column not found in " + csvPath
                        + " (looked for station=\"" + stationColumn + "\", date=\"" + dateColumn
                        + "\", value=\"" + valueColumn + "\"; header was: " + headerLine + ")");
            }

            String line;
            int maxIdx = Math.max(stationIdx, Math.max(dateIdx, valueIdx));
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length <= maxIdx) {
                    continue;
                }

                String station = unquote(parts[stationIdx]);
                if (!station.equals(stationFilter)) {
                    continue;
                }

                String dateStr = unquote(parts[dateIdx]);
                String valueStr = unquote(parts[valueIdx]);
                if (valueStr.equalsIgnoreCase("NA") || valueStr.isEmpty()) {
                    continue;
                }

                String yearMonth = toYearMonth(dateStr);
                if (yearMonth == null) {
                    continue;
                }
                try {
                    result.put(yearMonth, Double.parseDouble(valueStr));
                } catch (NumberFormatException e) {
                    // skip malformed value
                }
            }
        } finally {
            reader.close();
        }

        return result;
    }

    /**
     * Normalise a date field to a "yyyy-MM" key. Handles both ISO
     * (yyyy-MM-dd...) and slash-separated US-style (M/D/yyyy) dates, because
     * these index files are exported from different tools and do not agree on
     * a format - blindly taking the first seven characters turns "6/15/1995"
     * into "6/15/19", which silently matches nothing and empties the whole
     * training set rather than failing loudly.
     */
    static String toYearMonth(java.lang.String dateStr) {
        String s = dateStr.trim();
        if (s.length() >= 7 && s.charAt(4) == '-') {
            return s.substring(0, 7);
        }
        if (s.indexOf('/') > 0) {
            // drop any trailing time component before splitting the date itself
            int sp = s.indexOf(' ');
            String datePart = (sp > 0) ? s.substring(0, sp) : s;
            String[] p = datePart.split("/");
            if (p.length >= 3) {
                try {
                    int a = Integer.parseInt(p[0].trim());
                    int b = Integer.parseInt(p[1].trim());
                    int y = Integer.parseInt(p[2].trim());
                    // a value above 12 can only be the day, so use the other
                    // field as the month rather than guessing an ordering
                    int month = (a <= 12) ? a : b;
                    return yearMonthKey(y, month);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return (s.length() >= 7) ? s.substring(0, 7) : null;
    }

    public static String yearMonthKey(int year, int month1to12) {
        return String.format("%04d-%02d", year, month1to12);
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static int indexOf(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (unquote(arr[i]).equalsIgnoreCase(target)) {
                return i;
            }
        }
        return -1;
    }
}
