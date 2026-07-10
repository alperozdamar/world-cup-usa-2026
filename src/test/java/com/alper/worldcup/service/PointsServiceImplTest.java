package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PointsServiceImplTest {

    private static final double DELTA = 0.0001;

    private PointsServiceImpl pointsService;

    @BeforeEach
    void setUp() {
        pointsService = new PointsServiceImpl();
    }

    @Test
    void exactScoreAwardsFivePoints() {
        assertEquals(5, pointsService.calculatePoints(2, 1, 2, 1, MatchStage.GROUP_STAGE), DELTA);
    }

    @Test
    void correctOutcomeWrongScoreAwardsTwoPoints() {
        assertEquals(2, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.GROUP_STAGE), DELTA);
    }

    @Test
    void correctOutcomeAndGoalDifferenceAwardsThreePoints() {
        assertEquals(3, pointsService.calculatePoints(3, 1, 2, 0, MatchStage.GROUP_STAGE), DELTA);
    }

    @Test
    void wrongOutcomeAwardsZeroPoints() {
        assertEquals(0, pointsService.calculatePoints(2, 1, 1, 2, MatchStage.GROUP_STAGE), DELTA);
    }

    @Test
    void drawExactScoreAwardsFivePoints() {
        assertEquals(5, pointsService.calculatePoints(1, 1, 1, 1, MatchStage.GROUP_STAGE), DELTA);
    }

    @Test
    void finalExactScoreUsesDoubleMultiplier() {
        assertEquals(10, pointsService.calculatePoints(2, 1, 2, 1, MatchStage.FINAL), DELTA);
    }

    @Test
    void roundOf16KeepsFractionalMultiplier() {
        assertEquals(2.5, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.ROUND_OF_16), DELTA);
    }

    @Test
    void thirdPlaceUsesSameMultiplierAsSemiFinal() {
        assertEquals(3.5, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.THIRD_PLACE), DELTA);
        assertEquals(3.5, pointsService.calculatePoints(2, 1, 3, 1, MatchStage.SEMI_FINAL), DELTA);
    }

    @Test
    void knockoutRoundOf16KeepsFractionalPoints() {
        Team mexico = team(42);
        assertEquals(7.5, pointsService.calculateKnockoutPoints(
                2, 0, 2, 0, MatchStage.ROUND_OF_16,
                null, null, mexico, mexico), DELTA);
        assertEquals(6.25, pointsService.calculateKnockoutPoints(
                0, 0, 1, 1, MatchStage.ROUND_OF_16,
                true, true, mexico, mexico), DELTA);
        assertEquals(1.25, pointsService.calculateKnockoutPoints(
                0, 1, 1, 1, MatchStage.ROUND_OF_16,
                null, true, mexico, mexico), DELTA);
    }

    @Test
    void knockoutCorrectAdvancerDespiteWrong90PathAwardsOnePoint() {
        Team canada = team(42);
        assertEquals(1, pointsService.calculateKnockoutPoints(
                1, 1, 0, 1, MatchStage.ROUND_OF_32,
                true, null, canada, canada), DELTA);
    }

    @Test
    void knockoutNonDrawPickWrong90ButCorrectAdvancerAwardsOnePoint() {
        Team egypt = team(42);
        assertEquals(1, pointsService.calculateKnockoutPoints(
                0, 1, 1, 1, MatchStage.ROUND_OF_32,
                null, true, egypt, egypt), DELTA);
    }

    @Test
    void knockoutDrawPickWrongScoreButCorrectPensAndAdvancer() {
        Team egypt = team(42);
        assertEquals(5, pointsService.calculateKnockoutPoints(
                0, 0, 1, 1, MatchStage.ROUND_OF_32,
                true, true, egypt, egypt), DELTA);
    }

    @Test
    void knockoutCorrect90OutcomeBeatsDrawPickWithAdvancerOnly() {
        Team canada = team(42);
        double drawPickPoints = pointsService.calculateKnockoutPoints(
                1, 1, 0, 1, MatchStage.ROUND_OF_32,
                true, null, canada, canada);
        double outcomePickPoints = pointsService.calculateKnockoutPoints(
                0, 2, 0, 1, MatchStage.ROUND_OF_32,
                null, null, canada, canada);
        assertEquals(1, drawPickPoints, DELTA);
        assertEquals(3, outcomePickPoints, DELTA);
    }

    @Test
    void knockoutExact90WinnerIncludesAdvancerBonus() {
        Team mexico = team(42);
        assertEquals(6, pointsService.calculateKnockoutPoints(
                2, 0, 2, 0, MatchStage.ROUND_OF_32,
                null, null, mexico, mexico), DELTA);
    }

    @Test
    void knockoutCorrect90WinnerWrongScoreIncludesAdvancerBonus() {
        Team mexico = team(42);
        assertEquals(3, pointsService.calculateKnockoutPoints(
                1, 0, 2, 0, MatchStage.ROUND_OF_32,
                null, null, mexico, mexico), DELTA);
    }

    @Test
    void knockoutDrawPickWrongAdvancerScoresZeroExtras() {
        Team canada = team(42);
        Team southAfrica = team(7);
        assertEquals(0, pointsService.calculateKnockoutPoints(
                1, 1, 1, 2, MatchStage.ROUND_OF_32,
                false, null, southAfrica, canada), DELTA);
    }

    @Test
    void knockoutExactDrawWithPenaltyAndAdvancer() {
        Team brazil = team(10);
        assertEquals(7, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.ROUND_OF_32,
                true, true, brazil, brazil), DELTA);
    }

    @Test
    void knockoutPenaltyOnlyWhenActualDraw() {
        Team brazil = team(10);
        assertEquals(1, pointsService.calculateKnockoutPoints(
                1, 1, 2, 1, MatchStage.ROUND_OF_32,
                true, true, brazil, brazil), DELTA);
    }

    private static Team team(int id) {
        Team team = new Team();
        team.setId(id);
        team.setName("Team " + id);
        return team;
    }
}
