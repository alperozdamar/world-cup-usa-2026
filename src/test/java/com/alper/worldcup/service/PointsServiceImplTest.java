package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
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

    @Test
    void knockoutCorrectAdvancerDespiteWrong90PathAwardsSoftBonus() {
        Team canada = team(42);
        assertEquals(1, pointsService.calculateKnockoutPoints(
                1, 1, 0, 1, MatchStage.ROUND_OF_32,
                true, null, canada, canada));
    }

    @Test
    void knockoutCorrect90OutcomeBeatsDrawPickWithSoftAdvancerBonus() {
        Team canada = team(42);
        int drawPickPoints = pointsService.calculateKnockoutPoints(
                1, 1, 0, 1, MatchStage.ROUND_OF_32,
                true, null, canada, canada);
        int outcomePickPoints = pointsService.calculateKnockoutPoints(
                0, 2, 0, 1, MatchStage.ROUND_OF_32,
                null, null, null, canada);
        assertEquals(1, drawPickPoints);
        assertEquals(2, outcomePickPoints);
    }

    @Test
    void knockoutDrawPickWrongAdvancerScoresZeroExtras() {
        Team canada = team(42);
        Team southAfrica = team(7);
        assertEquals(0, pointsService.calculateKnockoutPoints(
                1, 1, 1, 2, MatchStage.ROUND_OF_32,
                false, null, southAfrica, canada));
    }

    @Test
    void knockoutExactDrawWithPenaltyAndAdvancer() {
        Team brazil = team(10);
        assertEquals(8, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.ROUND_OF_32,
                true, true, brazil, brazil));
    }

    @Test
    void knockoutPenaltyOnlyWhenActualDraw() {
        Team brazil = team(10);
        // Actual 2–1 (not a draw): no penalty bonus; draw pick with correct advancer gets soft +1 only.
        assertEquals(1, pointsService.calculateKnockoutPoints(
                1, 1, 2, 1, MatchStage.ROUND_OF_32,
                true, true, brazil, brazil));
    }

    private static Team team(int id) {
        Team team = new Team();
        team.setId(id);
        team.setName("Team " + id);
        return team;
    }
}
