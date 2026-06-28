package com.alper.worldcup.service;

public record UpcomingMatchTickerEntry(
        int matchId,
        String homeTeamName,
        String awayTeamName,
        String kickoffLabel,
        boolean knockout) {

    public String label() {
        return homeTeamName + " vs " + awayTeamName;
    }
}
