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
        assertEquals(List.of(todayEarly, todayLate, future, past), sorted);
    }

    @Test
    void sortMatchesForAdminScores_ordersPastMostRecentFirst() {
        LocalDate today = LocalDate.of(2026, 6, 14);
        Instant now = ZonedDateTime.of(today, LocalTime.of(18, 0), ZONE).toInstant();

        Match olderPast = match(1, today.minusDays(3), 15, 0);
        Match newerPast = match(2, today.minusDays(1), 15, 0);
        Match upcoming = match(3, today.plusDays(1), 15, 0);

        List<Match> sorted = AdminController.sortMatchesForAdminScores(
                List.of(olderPast, newerPast, upcoming), ZONE, now);
        assertEquals(List.of(upcoming, newerPast, olderPast), sorted);
    }

    private static Match match(int id, LocalDate day, int hour, int minute) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(ZonedDateTime.of(day, LocalTime.of(hour, minute), ZONE).toInstant());
        return match;
    }
}
