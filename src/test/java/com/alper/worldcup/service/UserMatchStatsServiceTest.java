package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMatchStatsServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    private UserMatchStatsService service;

    @BeforeEach
    void setUp() {
        service = new UserMatchStatsService(predictionRepository, new PointsServiceImpl());
    }

    @Test
    void countsExactScoresAndCorrectOutcomesSeparately() {
        when(predictionRepository.findScoredByUsernameWithMatch("alper"))
                .thenReturn(List.of(
                        prediction(1, 2, 2, 2, 2),
                        prediction(2, 2, 1, 3, 1),
                        prediction(3, 1, 1, 2, 1)));

        UserMatchStats stats = service.getStats("alper");

        assertEquals(1, stats.exactScores());
        assertEquals(2, stats.correctOutcomes());
    }

    private Prediction prediction(int matchId, int guessHome, int guessAway, int actualHome, int actualAway) {
        Match match = new Match();
        match.setId(matchId);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(Instant.parse("2026-06-11T19:00:00Z"));
        match.setHomeTeam(team(1, "Home"));
        match.setAwayTeam(team(2, "Away"));
        match.setHomeScoreActual(actualHome);
        match.setAwayScoreActual(actualAway);

        Prediction prediction = new Prediction();
        prediction.setUsername("alper");
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(guessHome);
        prediction.setAwayScoreGuess(guessAway);
        return prediction;
    }

    private Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
