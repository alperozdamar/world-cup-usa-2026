package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.MatchStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PointsServiceImplTest {

    private PointsServiceImpl pointsService;

    @BeforeEach
    void setUp() {
        pointsService = new PointsServiceImpl();
    }

    @Test
    void exactScoreAwardsFivePoints() {
        assertEquals(5, pointsService.calculatePoints(2, 1, 2, 1, MatchStage.GROUP_STAGE));
    }

    @Test
    void correctOutcomeWrongScoreAwardsTwoPoints() {
        assertEquals(2, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.GROUP_STAGE));
    }

    @Test
    void correctOutcomeAndGoalDifferenceAwardsThreePoints() {
        assertEquals(3, pointsService.calculatePoints(3, 1, 2, 0, MatchStage.GROUP_STAGE));
    }

    @Test
    void wrongOutcomeAwardsZeroPoints() {
        assertEquals(0, pointsService.calculatePoints(2, 1, 1, 2, MatchStage.GROUP_STAGE));
    }

    @Test
    void drawExactScoreAwardsFivePoints() {
        assertEquals(5, pointsService.calculatePoints(1, 1, 1, 1, MatchStage.GROUP_STAGE));
    }

    @Test
    void finalExactScoreUsesDoubleMultiplier() {
        assertEquals(10, pointsService.calculatePoints(2, 1, 2, 1, MatchStage.FINAL));
    }

    @Test
    void roundOf16CorrectOutcomeUsesMultiplier() {
        assertEquals(3, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.ROUND_OF_16));
    }
}
