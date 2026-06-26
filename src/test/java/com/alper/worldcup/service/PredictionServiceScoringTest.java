package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import org.junit.jupiter.api.Test;

class PredictionServiceScoringTest {

    @Test
    void resolveActualAdvancerFromWinningScore() {
        Match match = knockoutMatch(1, 2);
        Team away = team(2, "Canada");
        match.setAwayTeam(away);
        match.setHomeTeam(team(1, "South Africa"));

        assertEquals(away, PredictionService.resolveActualAdvancer(match));
    }

    @Test
    void resolveActualAdvancerFromDrawUsesStoredWinner() {
        Match match = knockoutMatch(1, 1);
        Team canada = team(2, "Canada");
        match.setAdvancingTeamActual(canada);

        assertEquals(canada, PredictionService.resolveActualAdvancer(match));
    }

    @Test
    void resolveActualAdvancerNullWhenDrawWithoutStoredWinner() {
        assertNull(PredictionService.resolveActualAdvancer(knockoutMatch(0, 0)));
    }

    private static Match knockoutMatch(int homeScore, int awayScore) {
        Match match = new Match();
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeScoreActual(homeScore);
        match.setAwayScoreActual(awayScore);
        return match;
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
