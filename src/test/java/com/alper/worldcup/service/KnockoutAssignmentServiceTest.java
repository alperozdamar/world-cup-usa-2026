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
        Team mexico = team(1, "Mexico", "A");
        Team japan = team(2, "Japan", "F");

        Match roundOf32 = new Match();
        roundOf32.setId(73);
        roundOf32.setStage(MatchStage.ROUND_OF_32);
        roundOf32.setKickoffUtc(now.minus(2, ChronoUnit.DAYS));
        roundOf32.setHomeTeam(mexico);
        roundOf32.setAwayTeam(japan);
        roundOf32.setHomeScoreActual(2);
        roundOf32.setAwayScoreActual(0);
        roundOf32.setPredictionsEnabled(true);

        Match roundOf16 = new Match();
        roundOf16.setId(89);
        roundOf16.setStage(MatchStage.ROUND_OF_16);
        roundOf16.setKickoffUtc(now.plus(3, ChronoUnit.DAYS));
        roundOf16.setHomePlaceholder("W73");
        roundOf16.setAwayPlaceholder("W75");
        roundOf16.setPredictionsEnabled(false);

        Map<Integer, Match> matchesById = new HashMap<>();
        matchesById.put(73, roundOf32);
        matchesById.put(89, roundOf16);

        KnockoutSyncResult result = service.applySync(
                List.of(roundOf32, roundOf16),
                Map.of(),
                matchesById,
                now,
                ignored -> { });

        assertEquals(1, result.teamsAssigned());
        assertEquals(mexico, roundOf16.getHomeTeam());
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
