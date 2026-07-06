package com.alper.worldcup.service;

import com.alper.worldcup.entity.Prediction;
import java.time.Instant;

public record PeerPlayerMatchPrediction(
        String username,
        String displayName,
        Integer homeGuess,
        Integer awayGuess,
        Boolean penaltyShootoutGuess,
        String advancingTeamName,
        Double points,
        boolean hidden,
        Instant lastSavedAt) {

    public static PeerPlayerMatchPrediction from(Prediction prediction, String displayName, Instant lastSavedAt) {
        String advancer = prediction.getAdvancingTeamGuess() != null
                ? prediction.getAdvancingTeamGuess().getName()
                : null;
        return new PeerPlayerMatchPrediction(
                prediction.getUsername(),
                displayName,
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                prediction.getPenaltyShootoutGuess(),
                advancer,
                prediction.getPoints(),
                false,
                lastSavedAt);
    }

    public static PeerPlayerMatchPrediction from(Prediction prediction, String displayName) {
        return from(prediction, displayName, prediction.getUpdatedAt());
    }

    public static PeerPlayerMatchPrediction hidden(String username, String displayName, Instant lastSavedAt) {
        return new PeerPlayerMatchPrediction(username, displayName, null, null, null, null, null, true, lastSavedAt);
    }

    public static PeerPlayerMatchPrediction hidden(String username, String displayName) {
        return hidden(username, displayName, null);
    }

    public boolean hidden() {
        return hidden;
    }
}
