package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KnockoutAssignmentServiceTest {

    private KnockoutAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new KnockoutAssignmentService(null, null, placeholderFromMatch());
    }

    @Test
    void fillsEmptySlotsWithoutOverwritingExistingTeams() {
        Instant now = Instant.parse("2026-06-27T12:00:00Z");
        Team japan = team(2, "Japan", "F");
        Team brazil = team(3, "Brazil", "C");

        Match match = new Match();
        match.setId(76);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setKickoffUtc(now.plus(2, ChronoUnit.DAYS));
        match.setHomeTeam(brazil);
        match.setHomePlaceholder("1C");
        match.setAwayPlaceholder("2F");
        match.setPredictionsEnabled(false);

        GroupStandingsView groupF = new GroupStandingsView("F", List.of(
                standing(team(99, "France", "F"), 1, 7),
                standing(japan, 2, 6)));

        AtomicInteger saves = new AtomicInteger();
        KnockoutSyncResult result = service.applySync(
                List.of(match),
                Map.of("F", groupF),
                Map.of(76, match),
                now,
                ignored -> saves.incrementAndGet());

        assertEquals(1, result.teamsAssigned());
        assertEquals(1, result.matchesOpened());
        assertEquals(japan, match.getAwayTeam());
        assertEquals(brazil, match.getHomeTeam());
        assertTrue(match.isPredictionsEnabled());
        assertEquals(2, saves.get());
    }

    @Test
    void doesNotChangeMatchesThatAlreadyHaveBothTeams() {
        Instant now = Instant.parse("2026-06-27T12:00:00Z");
        Team home = team(1, "Brazil", "C");
        Team away = team(2, "Japan", "F");

        Match match = new Match();
        match.setId(76);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setKickoffUtc(now.plus(2, ChronoUnit.DAYS));
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setPredictionsEnabled(true);

        AtomicInteger saves = new AtomicInteger();
        KnockoutSyncResult result = service.applySync(
                List.of(match),
                Map.of(),
                Map.of(76, match),
                now,
                ignored -> saves.incrementAndGet());

        assertEquals(0, result.teamsAssigned());
        assertEquals(0, result.matchesOpened());
        assertEquals(0, saves.get());
    }

    @Test
    void propagatesWinnerToRoundOf16AfterScoreEntered() {
        Instant now = Instant.parse("2026-07-05T12:00:00Z");
        Team canada = team(1, "Canada", "B");
        Team morocco = team(2, "Morocco", "C");

        Match roundOf32Home = new Match();
        roundOf32Home.setId(73);
        roundOf32Home.setStage(MatchStage.ROUND_OF_32);
        roundOf32Home.setKickoffUtc(now.minus(3, ChronoUnit.DAYS));
        roundOf32Home.setHomeTeam(team(3, "South Africa", "A"));
        roundOf32Home.setAwayTeam(canada);
        roundOf32Home.setHomeScoreActual(0);
        roundOf32Home.setAwayScoreActual(1);
        roundOf32Home.setPredictionsEnabled(true);

        Match roundOf32Away = new Match();
        roundOf32Away.setId(76);
        roundOf32Away.setStage(MatchStage.ROUND_OF_32);
        roundOf32Away.setKickoffUtc(now.minus(2, ChronoUnit.DAYS));
        roundOf32Away.setHomeTeam(team(4, "Netherlands", "F"));
        roundOf32Away.setAwayTeam(morocco);
        roundOf32Away.setHomeScoreActual(1);
        roundOf32Away.setAwayScoreActual(3);
        roundOf32Away.setPredictionsEnabled(true);

        Match roundOf16 = new Match();
        roundOf16.setId(89);
        roundOf16.setStage(MatchStage.ROUND_OF_16);
        roundOf16.setKickoffUtc(now.plus(3, ChronoUnit.DAYS));
        roundOf16.setHomePlaceholder("W73");
        roundOf16.setAwayPlaceholder("W76");
        roundOf16.setPredictionsEnabled(false);

        Map<Integer, Match> matchesById = new HashMap<>();
        matchesById.put(73, roundOf32Home);
        matchesById.put(76, roundOf32Away);
        matchesById.put(89, roundOf16);

        KnockoutSyncResult result = service.applySync(
                List.of(roundOf32Home, roundOf32Away, roundOf16),
                Map.of(),
                matchesById,
                now,
                ignored -> { });

        assertEquals(2, result.teamsAssigned());
        assertEquals(canada, roundOf16.getHomeTeam());
        assertEquals(morocco, roundOf16.getAwayTeam());
    }

    private static Team team(int id, String name, String group) {
        Team team = new Team(name, group);
        team.setId(id);
        return team;
    }

    private static GroupTeamStanding standing(Team team, int rank, int points) {
        return new GroupTeamStanding(team, 3, 1, 0, 2, 4, 2, 2, points, rank);
    }

    private static KnockoutBracketResolver.PlaceholderSource placeholderFromMatch() {
        return (match, homeSide) -> homeSide ? match.getHomePlaceholder() : match.getAwayPlaceholder();
    }
}
