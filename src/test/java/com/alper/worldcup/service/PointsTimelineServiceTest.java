package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointsTimelineServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/New_York");

    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private UserProfileService userProfileService;

    private PointsTimelineService service;

    @BeforeEach
    void setUp() {
        service = new PointsTimelineService(
                predictionRepository,
                matchRepository,
                new PoolMemberRegistry("default"),
                userProfileService);
    }

    @Test
    void buildsCumulativeSeriesByMatchDay() {
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(Optional.of(Instant.parse("2026-06-11T19:00:00Z")));
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of(
                scoredPrediction("alper", 1, 5, "2026-06-11T19:00:00Z"),
                scoredPrediction("alper", 2, 2, "2026-06-12T19:00:00Z"),
                scoredPrediction("gonenc", 3, 3, "2026-06-11T23:00:00Z")));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");

        LeaderboardTimelineChart chart = service.buildMatchPointsTimeline(
                ZONE,
                List.of("alper", "gonenc"),
                LocalDate.of(2026, 6, 12));

        assertTrue(chart.visible());
        assertEquals(2, chart.series().size());

        LeaderboardTimelineSeries alper = chart.series().get(0);
        assertEquals("Alper Ozdamar", alper.label());
        assertEquals(List.of(5L, 7L), alper.data());

        LeaderboardTimelineSeries gonenc = chart.series().get(1);
        assertEquals(List.of(3L, 3L), gonenc.data());
    }

    @Test
    void daysInclusiveCoversStartAndEnd() {
        assertEquals(
                List.of(LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 12)),
                PointsTimelineService.daysInclusive(LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 12)));
    }

    private static Prediction scoredPrediction(String username, int matchId, int points, String kickoff) {
        Team home = team(1, "Home");
        Team away = team(2, "Away");
        Match match = new Match();
        match.setId(matchId);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(Instant.parse(kickoff));
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setHomeScoreActual(1);
        match.setAwayScoreActual(0);

        Prediction prediction = new Prediction();
        prediction.setUsername(username);
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(1);
        prediction.setAwayScoreGuess(0);
        prediction.setPoints(points);
        return prediction;
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
