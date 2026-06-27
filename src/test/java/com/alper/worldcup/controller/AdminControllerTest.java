package com.alper.worldcup.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminControllerTest {

    private static final ZoneId ZONE = ZoneId.of("America/New_York");

    @Test
    void sortMatchesForAdminScores_putsTodayFirstThenUpcomingThenPast() {
        LocalDate today = LocalDate.of(2026, 6, 14);
        Instant now = ZonedDateTime.of(today, LocalTime.of(18, 0), ZONE).toInstant();

        Match past = match(1, today.minusDays(2), 15, 0);
        Match todayEarly = match(2, today, 13, 0);
        Match todayLate = match(3, today, 22, 0);
        Match future = match(4, today.plusDays(1), 15, 0);

        List<Match> sorted = AdminController.sortMatchesForAdminScores(
                List.of(past, future, todayLate, todayEarly), ZONE, now);
        assertEquals(List.of(todayEarly, todayLate, past, future), sorted);
    }

    @Test
    void sortMatchesForAdminScores_ordersScoredPastMostRecentFirstAtBottom() {
        LocalDate today = LocalDate.of(2026, 6, 14);
        Instant now = ZonedDateTime.of(today, LocalTime.of(18, 0), ZONE).toInstant();

        Match olderPast = scored(match(1, today.minusDays(3), 15, 0));
        Match newerPast = scored(match(2, today.minusDays(1), 15, 0));
        Match upcoming = match(3, today.plusDays(1), 15, 0);

        List<Match> sorted = AdminController.sortMatchesForAdminScores(
                List.of(olderPast, newerPast, upcoming), ZONE, now);
        assertEquals(List.of(upcoming, newerPast, olderPast), sorted);
    }

    @Test
    void sortMatchesForAdminScores_keepsUnscoredPastAboveScoredPast() {
        LocalDate today = LocalDate.of(2026, 6, 27);
        Instant now = ZonedDateTime.of(today, LocalTime.of(12, 0), ZONE).toInstant();

        Match unscoredPast = match(1, LocalDate.of(2026, 6, 26), 23, 0);
        Match scoredPast = scored(match(2, LocalDate.of(2026, 6, 25), 15, 0));
        Match future = match(3, LocalDate.of(2026, 6, 28), 15, 0);

        List<Match> sorted = AdminController.sortMatchesForAdminScores(
                List.of(scoredPast, unscoredPast, future), ZONE, now);
        assertEquals(List.of(unscoredPast, future, scoredPast), sorted);
    }

    private static Match match(int id, LocalDate day, int hour, int minute) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(ZonedDateTime.of(day, LocalTime.of(hour, minute), ZONE).toInstant());
        return match;
    }

    private static Match scored(Match match) {
        match.setHomeScoreActual(1);
        match.setAwayScoreActual(0);
        return match;
    }
}
