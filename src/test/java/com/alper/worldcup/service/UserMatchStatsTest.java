package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserMatchStatsTest {

    @Test
    void successRateIsCorrectResultsOverScoredMatches() {
        UserMatchStats stats = new UserMatchStats(1, 5, 6);

        assertEquals(11, stats.scoredMatches());
        assertEquals(45, stats.successRatePercent());
    }

    @Test
    void successRateWithNoScoredMatchesIsNull() {
        UserMatchStats stats = new UserMatchStats(0, 0, 0);

        assertEquals(0, stats.scoredMatches());
        assertNull(stats.successRatePercent());
    }

    @Test
    void successRateExampleFromDiscussion() {
        UserMatchStats stats = new UserMatchStats(1, 3, 7);

        assertEquals(10, stats.scoredMatches());
        assertEquals(30, stats.successRatePercent());
    }
}
