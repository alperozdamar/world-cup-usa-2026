package com.alper.worldcup.service;

import com.alper.worldcup.entity.Prediction;

public record HostKnockoutPickView(
        String username,
        String displayName,
        Integer homeGuess,
        Integer awayGuess,
        Boolean penaltyShootoutGuess,
        String advancingTeamName,
        Integer points) {

    static HostKnockoutPickView from(Prediction prediction, String displayName) {
        String advancer = prediction.getAdvancingTeamGuess() != null
                ? prediction.getAdvancingTeamGuess().getName()
                : null;
        return new HostKnockoutPickView(
                prediction.getUsername(),
                displayName,
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                prediction.getPenaltyShootoutGuess(),
                advancer,
                prediction.getPoints());
    }
}
