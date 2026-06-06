package com.alper.worldcup.service;

import com.alper.worldcup.entity.Prediction;

public record PeerPlayerMatchPrediction(
        String username,
        String displayName,
        Integer homeGuess,
        Integer awayGuess,
        Integer points) {

    public static PeerPlayerMatchPrediction from(Prediction prediction, String displayName) {
        return new PeerPlayerMatchPrediction(
                prediction.getUsername(),
                displayName,
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                prediction.getPoints());
    }
}
