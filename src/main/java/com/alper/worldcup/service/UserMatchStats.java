package com.alper.worldcup.service;

public record UserMatchStats(int exactScores, int correctOutcomes, int missedGames) {

    public int scoredMatches() {
        return correctOutcomes + missedGames;
    }

    public Integer successRatePercent() {
        int total = scoredMatches();
        if (total == 0) {
            return null;
        }
        return (int) Math.round(correctOutcomes * 100.0 / total);
    }
}
