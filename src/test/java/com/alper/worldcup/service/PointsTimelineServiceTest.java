package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.GroupStandingPrediction;
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
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private UserProfileService userProfileService;

    private PointsTimelineService service;

    @BeforeEach
    void setUp() {
        service = new PointsTimelineService(
                predictionRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
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
        when(groupStandingPredictionRepository.findAllScored()).thenReturn(List.of());
        when(matchRepository.findDistinctGroupStageGroupNames()).thenReturn(List.of());
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
        assertEquals(List.of(5.0, 7.0), alper.data());

        LeaderboardTimelineSeries gonenc = chart.series().get(1);
        assertEquals(List.of(3.0, 3.0), gonenc.data());
    }

    @Test
    void includesGroupStandingPointsOnGroupLastMatchDay() {
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(Optional.of(Instant.parse("2026-06-11T19:00:00Z")));
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of(
                scoredPrediction("sadik", 1, 5, "2026-06-11T19:00:00Z")));
        when(groupStandingPredictionRepository.findAllScored()).thenReturn(List.of(
                groupPrediction("sadik", "A", 3)));
        when(matchRepository.findDistinctGroupStageGroupNames()).thenReturn(List.of("A"));
        when(matchRepository.findLatestGroupMatchKickoff("A"))
                .thenReturn(Optional.of(Instant.parse("2026-06-14T23:00:00Z")));
        when(userProfileService.getDisplayName("sadik")).thenReturn("Sadik Demirdogen");

        LeaderboardTimelineChart chart = service.buildMatchPointsTimeline(
                ZONE,
                List.of("sadik"),
                LocalDate.of(2026, 6, 14));

        assertEquals(List.of(5.0, 5.0, 5.0, 8.0), chart.series().get(0).data());
    }

    @Test
    void daysInclusiveCoversStartAndEnd() {
        assertEquals(
                List.of(LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 12)),
                PointsTimelineService.daysInclusive(LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 12)));
    }

    @Test
    void leaderDaysRankingCountsDaysAtopLeaderboard() {
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(Optional.of(Instant.parse("2026-06-11T19:00:00Z")));
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of(
                scoredPrediction("alper", 1, 5, "2026-06-11T19:00:00Z"),
                scoredPrediction("gonenc", 2, 2, "2026-06-11T23:00:00Z"),
                scoredPrediction("gonenc", 3, 10, "2026-06-12T19:00:00Z")));
        when(groupStandingPredictionRepository.findAllScored()).thenReturn(List.of());
        when(matchRepository.findDistinctGroupStageGroupNames()).thenReturn(List.of());
        when(matchRepository.findFinalMatchKickoff()).thenReturn(Optional.empty());
        when(userProfileService.getDisplayName(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<LeaderDaysRow> rows = service.leaderDaysRanking(ZONE, LocalDate.of(2026, 6, 12));

        assertEquals("alper", rows.get(0).username());
        assertEquals(1, rows.get(0).daysAsLeader()); // day 11 only (5 > 2)
        assertEquals("gonenc", rows.get(1).username());
        assertEquals(1, rows.get(1).daysAsLeader()); // day 12 (12 > 5)
    }

    private static Prediction scoredPrediction(String username, int matchId, double points, String kickoff) {
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

    private static GroupStandingPrediction groupPrediction(String username, String groupName, int points) {
        GroupStandingPrediction prediction = new GroupStandingPrediction();
        prediction.setUsername(username);
        prediction.setGroupName(groupName);
        prediction.setFirstPlaceTeam(team(1, "First"));
        prediction.setSecondPlaceTeam(team(2, "Second"));
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
