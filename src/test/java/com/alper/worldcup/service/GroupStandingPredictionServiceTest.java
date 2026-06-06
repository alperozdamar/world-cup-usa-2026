package com.alper.worldcup.service;

import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupStandingPredictionServiceTest {

    private final GroupStandingPredictionService service = new GroupStandingPredictionService(
            null, null, null, null, null);

    @Test
    void exactFirstAndSecondScoresFive() {
        assertEquals(5, score(
                team(1, "A"), team(2, "B"),
                team(1, "A"), team(2, "B")));
    }

    @Test
    void swappedPositionsScoreTwo() {
        assertEquals(2, score(
                team(1, "A"), team(2, "B"),
                team(2, "B"), team(1, "A")));
    }

    @Test
    void exactFirstAndWrongSecondTeamScoresThree() {
        assertEquals(3, score(
                team(1, "A"), team(2, "B"),
                team(1, "A"), team(3, "C")));
    }

    @Test
    void exactSecondAndWrongFirstTeamScoresTwo() {
        assertEquals(2, score(
                team(1, "A"), team(2, "B"),
                team(3, "C"), team(2, "B")));
    }

    private int score(Team predictedFirst, Team predictedSecond, Team actualFirst, Team actualSecond) {
        GroupStandingPrediction prediction = new GroupStandingPrediction();
        prediction.setFirstPlaceTeam(predictedFirst);
        prediction.setSecondPlaceTeam(predictedSecond);

        GroupResult result = new GroupResult("A", actualFirst, actualSecond);
        return service.calculatePoints(prediction, result);
    }

    private Team team(int id, String name) {
        Team team = new Team(name, "A");
        team.setId(id);
        return team;
    }
}
