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
import java.util.Map;
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
        service = new UserMatchStatsService(
                predictionRepository,
                new PoolMemberRegistry("default"),
                new PointsServiceImpl());
    }

    @Test
    void countsExactScoresCorrectOutcomesAndMissedGames() {
        when(predictionRepository.findScoredByUsernameWithMatch("alper"))
                .thenReturn(List.of(
                        prediction("alper", 1, "Home", "Away", 2, 2, 2, 2, 5),
                        prediction("alper", 2, "Home", "Away", 2, 1, 3, 1, 2),
                        prediction("alper", 3, "Home", "Away", 1, 1, 2, 1, 0)));

        UserMatchStats stats = service.getStats("alper");

        assertEquals(1, stats.exactScores());
        assertEquals(2, stats.correctOutcomes());
        assertEquals(1, stats.missedGames());
    }

    @Test
    void loveAndHateTeamsUseAveragePointsWithMinSample() {
        when(predictionRepository.findScoredByUsernameWithMatch("alper"))
                .thenReturn(List.of(
                        prediction("alper", 1, "Mexico", "Ecuador", 2, 0, 2, 0, 6),
                        prediction("alper", 2, "Mexico", "USA", 1, 0, 2, 0, 3),
                        prediction("alper", 3, "Brazil", "Mexico", 1, 0, 2, 0, 3),
                        prediction("alper", 4, "Germany", "Spain", 2, 0, 1, 1, 0),
                        prediction("alper", 5, "Germany", "France", 1, 0, 0, 1, 0),
                        prediction("alper", 6, "Germany", "Portugal", 2, 1, 0, 0, 1)));

        UserMatchStats stats = service.getStats("alper");

        assertEquals("Mexico", stats.loveTeam().teamName());
        assertEquals(4.0, stats.loveTeam().averagePoints(), 0.01);
        assertEquals(3, stats.loveTeam().matchCount());
        assertEquals("Germany", stats.hateTeam().teamName());
        assertEquals(0.333, stats.hateTeam().averagePoints(), 0.01);
        assertEquals(3, stats.hateTeam().matchCount());
    }

    @Test
    void buildsStatsForAllPoolMembers() {
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of(
                prediction("alper", 1, "Home", "Away", 2, 2, 2, 2, 5),
                prediction("gonenc", 2, "Home", "Away", 1, 1, 2, 1, 0)));

        Map<String, UserMatchStats> statsByUser = service.getStatsForPoolMembers();

        assertEquals(1, statsByUser.get("alper").exactScores());
        assertEquals(0, statsByUser.get("gonenc").exactScores());
        assertEquals(1, statsByUser.get("gonenc").missedGames());
        assertEquals(0, statsByUser.get("kubilay").exactScores());
    }

    private Prediction prediction(String username,
                                  int matchId,
                                  String homeName,
                                  String awayName,
                                  int guessHome,
                                  int guessAway,
                                  int actualHome,
                                  int actualAway,
                                  double points) {
        Match match = new Match();
        match.setId(matchId);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(Instant.parse("2026-06-11T19:00:00Z"));
        match.setHomeTeam(team(matchId * 10, homeName));
        match.setAwayTeam(team(matchId * 10 + 1, awayName));
        match.setHomeScoreActual(actualHome);
        match.setAwayScoreActual(actualAway);

        Prediction prediction = new Prediction();
        prediction.setUsername(username);
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(guessHome);
        prediction.setAwayScoreGuess(guessAway);
        prediction.setPoints(points);
        return prediction;
    }

    private Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
