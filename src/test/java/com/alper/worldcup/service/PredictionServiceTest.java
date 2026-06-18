package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PredictionServiceTest {

    @Test
    void groupStageMatchesListUpcomingBeforeStarted() {
        Instant now = Instant.parse("2026-06-18T12:00:00Z");
        Match startedEarly = match(1, Instant.parse("2026-06-11T19:00:00Z"));
        Match upcomingSoon = match(2, Instant.parse("2026-06-20T19:00:00Z"));
        Match upcomingLater = match(3, Instant.parse("2026-06-25T19:00:00Z"));
        Match startedLate = match(4, Instant.parse("2026-06-15T19:00:00Z"));

        List<Match> matches = PredictionService.sortGroupStageMatchesForList(
                List.of(startedEarly, upcomingLater, startedLate, upcomingSoon),
                now);

        assertEquals(List.of(upcomingSoon, upcomingLater, startedEarly, startedLate), matches);
    }

    private Match match(int id, Instant kickoffUtc) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(kickoffUtc);
        match.setPredictionsEnabled(true);
        match.setHomeTeam(team(1, "Home"));
        match.setAwayTeam(team(2, "Away"));
        return match;
    }

    private Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
