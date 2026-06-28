package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionAuditRepository;
import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.FinalResultRepository;
import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.PredictionAudit;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BirdWatchServiceTest {

    @Mock
    private PredictionAuditRepository predictionAuditRepository;
    @Mock
    private GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository;
    @Mock
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionAuditRepository finalPredictionAuditRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private FinalResultRepository finalResultRepository;
    @Mock
    private UserProfileService userProfileService;

    private BirdWatchService service;

    @BeforeEach
    void setUp() {
        service = new BirdWatchService(
                predictionAuditRepository,
                groupStandingPredictionAuditRepository,
                groupStandingPredictionRepository,
                finalPredictionAuditRepository,
                predictionRepository,
                finalPredictionRepository,
                finalResultRepository,
                userProfileService,
                new PoolMemberRegistry("default"),
                new PointsServiceImpl());
    }

    @Test
    void averageLeadTimeFavorsEarlierFirstPick() {
        Instant kickoff = Instant.parse("2026-06-11T19:00:00Z");
        Match match1 = match(1, kickoff);
        Match match2 = match(2, kickoff);
        Match match3 = match(3, kickoff);

        List<PredictionAudit> audits = List.of(
                audit("alper", match1, Instant.parse("2026-06-01T19:00:00Z")),
                audit("alper", match2, Instant.parse("2026-06-02T19:00:00Z")),
                audit("alper", match3, Instant.parse("2026-06-03T19:00:00Z")),
                audit("gonenc", match1, Instant.parse("2026-06-10T18:00:00Z")),
                audit("gonenc", match2, Instant.parse("2026-06-10T18:30:00Z")),
                audit("gonenc", match3, Instant.parse("2026-06-10T18:45:00Z")));

        Map<String, Duration> averages = service.averageFirstPickLeadTime(audits);

        assertTrue(averages.get("alper").compareTo(averages.get("gonenc")) > 0);
    }

    @Test
    void detectsOneGoalOffNearMiss() {
        Match match = scoredMatch(2, 1);
        Prediction prediction = prediction("alper", match, 2, 0);

        assertTrue(service.isOneGoalOff(prediction, match));
        assertFalse(service.isOneGoalOff(prediction("alper", match, 2, 1), match));
    }

    @Test
    void detectsLuckyTwoPointer() {
        Match match = scoredMatch(3, 1);
        Prediction lucky = prediction("alper", match, 2, 1);
        Prediction exact = prediction("gonenc", match, 3, 1);

        assertTrue(service.isLuckyTwoPointer(lucky, match));
        assertFalse(service.isLuckyTwoPointer(exact, match));
    }

    @Test
    void crystalBallPendingUntilFinalResultExists() {
        when(predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()).thenReturn(List.of());
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of());
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(List.of());
        when(predictionRepository.findKnockoutPointsTotalsByUser()).thenReturn(List.of());
        when(finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)).thenReturn(Optional.empty());

        BirdWatchCategory crystalBall = service.buildCategories().stream()
                .filter(category -> category.id().equals("crystal-ball-condors"))
                .findFirst()
                .orElseThrow();

        assertTrue(crystalBall.pending());
        assertEquals("Waiting for the official champion — check back after the final.", crystalBall.pendingMessage());
    }

    @Test
    void groupSageGrousePendingUntilGroupResultsScored() {
        when(predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()).thenReturn(List.of());
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of());
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(List.of());
        when(predictionRepository.findKnockoutPointsTotalsByUser()).thenReturn(List.of());
        when(finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)).thenReturn(Optional.empty());

        BirdWatchCategory groupSage = service.buildCategories().stream()
                .filter(category -> category.id().equals("group-sage-grouse"))
                .findFirst()
                .orElseThrow();

        assertTrue(groupSage.pending());
    }

    @Test
    void matchMagpiesShowsTopGroupStageMatchPointLeaders() {
        when(predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()).thenReturn(List.of());
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of());
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(List.of(
                new Object[] {"alper", 124L},
                new Object[] {"gonenc", 114L},
                new Object[] {"tcan", 113L}));
        when(predictionRepository.findKnockoutPointsTotalsByUser()).thenReturn(List.of());
        when(finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)).thenReturn(Optional.empty());
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");
        when(userProfileService.getDisplayName("tcan")).thenReturn("Tayyip Can");

        BirdWatchCategory matchMagpies = service.buildCategories().stream()
                .filter(category -> category.id().equals("match-magpies"))
                .findFirst()
                .orElseThrow();

        assertFalse(matchMagpies.pending());
        assertEquals(3, matchMagpies.leaders().size());
        assertEquals("alper", matchMagpies.leaders().get(0).username());
        assertEquals("124 pts", matchMagpies.leaders().get(0).statLabel());
    }

    @Test
    void matchMagpiesPendingUntilGroupStageResultsScored() {
        when(predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()).thenReturn(List.of());
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of());
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(List.of());
        when(predictionRepository.findKnockoutPointsTotalsByUser()).thenReturn(List.of());
        when(finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)).thenReturn(Optional.empty());

        BirdWatchCategory matchMagpies = service.buildCategories().stream()
                .filter(category -> category.id().equals("match-magpies"))
                .findFirst()
                .orElseThrow();

        assertTrue(matchMagpies.pending());
    }

    @Test
    void knockoutKestrelsShowsZeroPointLeaders() {
        when(predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()).thenReturn(List.of());
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()).thenReturn(List.of());
        when(predictionRepository.findAllScoredWithMatch()).thenReturn(List.of());
        when(predictionRepository.findKnockoutPointsTotalsByUser()).thenReturn(List.of(
                new Object[] {"alper", 0L},
                new Object[] {"gonenc", 0L}));
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(List.of());
        when(finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)).thenReturn(Optional.empty());
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");

        BirdWatchCategory knockout = service.buildCategories().stream()
                .filter(category -> category.id().equals("knockout-kestrels"))
                .findFirst()
                .orElseThrow();

        assertFalse(knockout.pending());
        assertEquals(2, knockout.leaders().size());
        assertEquals("0 pts", knockout.leaders().get(0).statLabel());
    }

    private static PredictionAudit audit(String username, Match match, Instant recordedAt) {
        PredictionAudit audit = new PredictionAudit();
        audit.setUsername(username);
        audit.setMatch(match);
        audit.setRecordedAt(recordedAt);
        audit.setAction(PredictionAuditAction.CREATED);
        audit.setHomeScoreGuess(1);
        audit.setAwayScoreGuess(0);
        audit.setHomeTeamName("Home");
        audit.setAwayTeamName("Away");
        return audit;
    }

    private static Match match(int id, Instant kickoff) {
        Match match = new Match();
        match.setId(id);
        match.setKickoffUtc(kickoff);
        match.setStage(MatchStage.GROUP_STAGE);
        return match;
    }

    private static Match scoredMatch(int home, int away) {
        Match match = match(1, Instant.parse("2026-06-11T19:00:00Z"));
        match.setHomeScoreActual(home);
        match.setAwayScoreActual(away);
        return match;
    }

    private static Prediction prediction(String username, Match match, int home, int away) {
        Prediction prediction = new Prediction();
        prediction.setUsername(username);
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(home);
        prediction.setAwayScoreGuess(away);
        return prediction;
    }
}
