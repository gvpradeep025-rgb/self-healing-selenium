package com.pradeep.healing.core;

import java.util.Map;

import static com.pradeep.healing.core.ElementFingerprint.Attr.*;

/**
 * Scores how closely a real element matches the expected fingerprint, from 0.0 (nothing alike) to 1.0 (identical).
 * Only attributes present in the fingerprint count, and each one is weighted by how much it says about identity.
 */
public final class SimilarityScorer {

    private static final Map<String, Double> WEIGHTS = Map.of(
            TAG, 0.15,
            TEXT, 0.25,
            ARIA_LABEL, 0.20,
            NAME, 0.15,
            TYPE, 0.10,
            PLACEHOLDER, 0.15);

    private SimilarityScorer() { }

    public static double score(ElementFingerprint fingerprint, Map<String, String> actual) {
        double weighted = 0;
        double totalWeight = 0;
        for (Map.Entry<String, String> e : fingerprint.expected().entrySet()) {
            double weight = WEIGHTS.getOrDefault(e.getKey(), 0.10);
            totalWeight += weight;
            weighted += weight * stringSimilarity(e.getValue(), actual.get(e.getKey()));
        }
        return totalWeight == 0 ? 0 : round(weighted / totalWeight);
    }

    /** 1.0 for equal strings (ignoring case and surrounding spaces), falling with edit distance. */
    public static double stringSimilarity(String expected, String actual) {
        if (expected == null || actual == null) {
            return 0;
        }
        String a = normalise(expected);
        String b = normalise(actual);
        if (a.equals(b)) {
            return 1.0;
        }
        int longest = Math.max(a.length(), b.length());
        if (longest == 0) {
            return 1.0;
        }
        return 1.0 - (double) levenshtein(a, b) / longest;
    }

    static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }

    private static String normalise(String s) {
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static double round(double v) {
        return Math.round(v * 1000) / 1000.0;
    }
}
