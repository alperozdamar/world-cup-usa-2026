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
    void knockoutDrawWithPenaltyAndAdvancerBonusesBeforeMultiplier() {
        Team brazil = team(1);
        Team france = team(2);

        assertEquals(12, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.QUARTER_FINAL,
                true, true, brazil, brazil));
        assertEquals(11, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.QUARTER_FINAL,
                true, false, brazil, brazil));
        assertEquals(9, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.QUARTER_FINAL,
                true, true, brazil, france));
    }

    @Test
    void knockoutExtrasOnlyWhenActualIsDrawAtNinety() {
        Team brazil = team(1);

        assertEquals(0, pointsService.calculateKnockoutPoints(
                1, 1, 2, 1, MatchStage.ROUND_OF_16,
                true, true, brazil, brazil));
        assertEquals(6, pointsService.calculateKnockoutPoints(
                2, 1, 2, 1, MatchStage.ROUND_OF_16,
                true, true, brazil, brazil));
    }

    @Test
    void knockoutExtrasOnlyWhenGuessIsDrawAtNinety() {
        Team brazil = team(1);

        assertEquals(12, pointsService.calculateKnockoutPoints(
                1, 1, 1, 1, MatchStage.QUARTER_FINAL,
                true, true, brazil, brazil));
        assertEquals(0, pointsService.calculateKnockoutPoints(
                2, 1, 1, 1, MatchStage.QUARTER_FINAL,
                true, true, brazil, brazil));
    }

    private static Team team(int id) {
        Team team = new Team();
        team.setId(id);
        team.setName("Team " + id);
        return team;
    }
}
