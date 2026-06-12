package com.alper.worldcup.service;

import com.alper.worldcup.entity.Prediction;

public record PeerPlayerMatchPrediction(
        String username,
        String displayName,
        Integer homeGuess,
        Integer awayGuess,
        Integer points,
        boolean hidden) {

    public static PeerPlayerMatchPrediction from(Prediction prediction, String displayName) {
        return new PeerPlayerMatchPrediction(
                prediction.getUsername(),
                displayName,
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                prediction.getPoints(),
                false);
    }

    public static PeerPlayerMatchPrediction hidden(String username, String displayName) {
        return new PeerPlayerMatchPrediction(username, displayName, null, null, null, true);
    }

    public boolean hidden() {
        return hidden;
    }
}
