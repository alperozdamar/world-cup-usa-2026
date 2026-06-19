package com.alper.worldcup.service;

import com.alper.worldcup.entity.Team;

public record GroupTeamStanding(
        Team team,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points,
        int rank) {
}
