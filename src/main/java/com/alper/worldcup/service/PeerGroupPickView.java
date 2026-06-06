package com.alper.worldcup.service;

import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Prediction;

public record PeerGroupPickView(String firstTeamName, String secondTeamName) {

    public static PeerGroupPickView from(GroupStandingPrediction prediction) {
        return new PeerGroupPickView(
                prediction.getFirstPlaceTeam().getName(),
                prediction.getSecondPlaceTeam().getName());
    }
}
