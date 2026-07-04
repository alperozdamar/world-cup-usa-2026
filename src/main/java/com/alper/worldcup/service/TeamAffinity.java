package com.alper.worldcup.service;

public record TeamAffinity(String teamName, double averagePoints, int matchCount) {

    public String formatAverage() {
        if (averagePoints == Math.rint(averagePoints)) {
            return String.valueOf((long) averagePoints);
        }
        return String.format(java.util.Locale.US, "%.1f", averagePoints);
    }

    public String formatLabel() {
        return teamName + " · " + formatAverage() + " avg (" + matchCount
                + " game" + (matchCount == 1 ? "" : "s") + ")";
    }
}
