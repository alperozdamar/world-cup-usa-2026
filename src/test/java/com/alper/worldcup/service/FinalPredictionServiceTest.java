package com.alper.worldcup.service;

import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.Team;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinalPredictionServiceTest {

    private final FinalPredictionService service = new FinalPredictionService(
            null, null, null, null, null);

    @Test
    void exactChampionAndRunnerUpScoresFifteen() {
        assertEquals(15, score(
                team(1, "Spain"), team(2, "Turkey"),
                team(1, "Spain"), team(2, "Turkey")));
    }

    @Test
    void swappedFinalistsScoreSix() {
        assertEquals(6, score(
                team(1, "Spain"), team(2, "Turkey"),
                team(2, "Turkey"), team(1, "Spain")));
    }

    @Test
    void championWrongPlaceOnlyScoresThree() {
        assertEquals(3, score(
                team(1, "Spain"), team(3, "Brazil"),
                team(2, "Turkey"), team(1, "Spain")));
    }

    @Test
    void exactChampionAndWrongRunnerUpScoresTen() {
        assertEquals(10, score(
                team(1, "Spain"), team(3, "Brazil"),
                team(1, "Spain"), team(2, "Turkey")));
    }

    @Test
    void noFinalistsScoresZero() {
        assertEquals(0, score(
                team(3, "Brazil"), team(4, "Germany"),
                team(1, "Spain"), team(2, "Turkey")));
    }

    private int score(Team predictedChampion, Team predictedRunnerUp,
                      Team actualChampion, Team actualRunnerUp) {
        FinalPrediction prediction = new FinalPrediction();
        prediction.setChampionTeam(predictedChampion);
        prediction.setRunnerUpTeam(predictedRunnerUp);

        FinalResult result = new FinalResult(actualChampion, actualRunnerUp);
        return service.calculatePoints(prediction, result);
    }

    private Team team(int id, String name) {
        Team team = new Team(name, null);
        team.setId(id);
        return team;
    }
}
