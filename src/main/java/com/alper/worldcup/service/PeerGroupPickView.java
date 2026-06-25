package com.alper.worldcup.service;

import com.alper.worldcup.entity.GroupStandingPrediction;

public record PeerGroupPickView(String firstTeamName, String secondTeamName, Integer points) {

    public boolean scored() {
        return points != null;
    }

    public static PeerGroupPickView from(GroupStandingPrediction prediction) {
        return new PeerGroupPickView(
                prediction.getFirstPlaceTeam().getName(),
                prediction.getSecondPlaceTeam().getName(),
                prediction.getPoints());
    }
}
