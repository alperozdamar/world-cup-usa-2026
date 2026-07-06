package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerPredictionServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private PredictionAuditRepository predictionAuditRepository;
    @Mock
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private MissingPredictionService missingPredictionService;

    private PeerPredictionService service;

    @BeforeEach
    void setUp() {
        service = new PeerPredictionService(
                matchRepository,
                predictionRepository,
                predictionAuditRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
                userProfileService,
                new PoolMemberRegistry("default"),
                missingPredictionService);
    }

    @Test
    void tournamentStartedWhenKickoffPassed() {
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(Optional.of(Instant.parse("2020-01-01T12:00:00Z")));
        assertTrue(service.isTournamentStarted());
    }

    @Test
    void tournamentNotStartedWhenKickoffFuture() {
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(Optional.of(Instant.parse("2099-01-01T12:00:00Z")));
        assertFalse(service.isTournamentStarted());
    }

    @Test
    void tournamentNotStartedWhenNoKickoff() {
        when(matchRepository.findTournamentStartKickoff()).thenReturn(Optional.empty());
        assertFalse(service.isTournamentStarted());
    }

    @Test
    void startedMatchesNewestFirst() {
        Match older = upcomingMatch(1, Instant.parse("2020-06-11T19:00:00Z"));
        Match newer = upcomingMatch(2, Instant.parse("2020-06-12T19:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(older, newer));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(2)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(any())).thenReturn(List.of());

        List<PeerMatchView> views = service.getVisibleMatchPredictions();

        assertEquals(2, views.size());
        assertEquals(2, views.get(0).match().getId());
        assertEquals(1, views.get(1).match().getId());
        assertFalse(views.get(0).predictionsHidden());
    }

    @Test
    void upcomingMatchHidesPredictions() {
        Match upcoming = upcomingMatch(3, Instant.parse("2099-06-11T19:00:00Z"));
        Prediction prediction = new Prediction();
        prediction.setUsername("gonenc");
        prediction.setHomeScoreGuess(2);
        prediction.setAwayScoreGuess(1);

        when(missingPredictionService.openMatchesOnNextMatchDay(ZoneId.of("America/New_York")))
                .thenReturn(List.of(upcoming));
        when(predictionRepository.findByMatchId(3)).thenReturn(List.of(prediction));
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(3))).thenReturn(List.of());
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");
        when(missingPredictionService.findMissingForMatch(upcoming)).thenReturn(List.of());

        PeerMatchView view = service.getUpcomingMatchPredictions(ZoneId.of("America/New_York")).getFirst();

        assertEquals(3, view.match().getId());
        assertTrue(view.predictionsHidden());
        assertEquals(1, view.predictions().size());
        assertTrue(view.predictions().get(0).hidden());
    }

    @Test
    void startedMatchesMergeGroupAndKnockoutNewestFirst() {
        Match groupOlder = upcomingMatch(1, Instant.parse("2026-06-11T19:00:00Z"));
        Match knockoutNewer = knockoutMatch(73, Instant.parse("2026-06-28T19:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(groupOlder));
        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(knockoutNewer));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(1, 73))).thenReturn(List.of());

        List<PeerMatchView> views = service.getVisibleStartedMatchPredictions();

        assertEquals(2, views.size());
        assertEquals(73, views.get(0).match().getId());
        assertEquals(1, views.get(1).match().getId());
    }

    @Test
    void startedMatchesPutKnockoutAboveGroupEvenWhenGroupIsNewer() {
        Match newerGroup = upcomingMatch(1, Instant.parse("2020-06-29T19:00:00Z"));
        Match olderKnockout = knockoutMatch(73, Instant.parse("2020-06-28T19:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(newerGroup));
        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(olderKnockout));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(1, 73))).thenReturn(List.of());

        List<PeerMatchView> views = service.getVisibleStartedMatchPredictions();

        assertEquals(2, views.size());
        assertEquals(73, views.get(0).match().getId());
        assertEquals(1, views.get(1).match().getId());
    }

    @Test
    void startedMatchesWithoutScoreStayAboveOlderScoredGames() {
        Match scoredGroup = upcomingMatch(1, Instant.parse("2020-06-28T19:00:00Z"));
        scoredGroup.setHomeScoreActual(2);
        scoredGroup.setAwayScoreActual(0);

        Match awaitingScore = knockoutMatch(73, Instant.parse("2020-06-28T22:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(scoredGroup));
        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(awaitingScore));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(1, 73))).thenReturn(List.of());

        List<PeerMatchView> views = service.getVisibleStartedMatchPredictions();

        assertEquals(2, views.size());
        assertEquals(73, views.get(0).match().getId());
        assertFalse(views.get(0).match().isScoreEntered());
    }

    @Test
    void startedKnockoutMatchesNewestFirst() {
        Match older = knockoutMatch(10, Instant.parse("2020-06-28T19:00:00Z"));
        Match newer = knockoutMatch(11, Instant.parse("2020-06-29T19:00:00Z"));

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(older, newer));
        when(predictionRepository.findByMatchId(10)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(11)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(10))).thenReturn(List.of());

        List<PeerMatchView> views = service.getVisibleKnockoutPredictions();

        assertEquals(2, views.size());
        assertEquals(11, views.get(0).match().getId());
        assertEquals(10, views.get(1).match().getId());
    }

    @Test
    void upcomingMatchPrefersEarliestKnockoutWhenSooner() {
        Match groupLater = upcomingMatch(1, Instant.parse("2099-06-12T19:00:00Z"));
        Match knockoutSooner = knockoutMatch(73, Instant.parse("2099-06-11T19:00:00Z"));

        when(missingPredictionService.openMatchesOnNextMatchDay(ZoneId.of("America/New_York")))
                .thenReturn(List.of(knockoutSooner));
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(73))).thenReturn(List.of());
        when(missingPredictionService.findMissingForMatch(knockoutSooner)).thenReturn(List.of());

        PeerMatchView view = service.getUpcomingMatchPredictions(ZoneId.of("America/New_York")).getFirst();

        assertEquals(73, view.match().getId());
        assertTrue(view.predictionsHidden());
    }

    @Test
    void upcomingMatchesReturnsAllGamesOnNextMatchDayInUserTimezone() {
        ZoneId zone = ZoneId.of("America/New_York");
        Match firstOnDay = knockoutMatch(73, Instant.parse("2099-06-11T14:00:00Z"));
        Match secondOnDay = knockoutMatch(74, Instant.parse("2099-06-12T03:00:00Z"));

        when(missingPredictionService.openMatchesOnNextMatchDay(zone))
                .thenReturn(List.of(firstOnDay, secondOnDay));
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of());
        when(predictionRepository.findByMatchId(74)).thenReturn(List.of());
        when(predictionAuditRepository.findLastRecordedAtForMatches(List.of(73, 74))).thenReturn(List.of());
        when(missingPredictionService.findMissingForMatch(firstOnDay)).thenReturn(List.of());
        when(missingPredictionService.findMissingForMatch(secondOnDay)).thenReturn(List.of());

        List<PeerMatchView> views = service.getUpcomingMatchPredictions(zone);

        assertEquals(2, views.size());
        assertEquals(73, views.get(0).match().getId());
        assertEquals(74, views.get(1).match().getId());
        assertTrue(views.get(0).predictionsHidden());
        assertTrue(views.get(1).predictionsHidden());
    }

    @Test
    void startedMatchPredictionUsesLatestAuditTimeWhenAvailable() {
        Match match = upcomingMatch(5, Instant.parse("2020-06-11T19:00:00Z"));
        Prediction prediction = new Prediction();
        prediction.setUsername("gonenc");
        prediction.setHomeScoreGuess(2);
        prediction.setAwayScoreGuess(1);
        prediction.setUpdatedAt(Instant.parse("2020-06-10T12:00:00Z"));
        Instant lastAudit = Instant.parse("2020-06-11T08:30:00Z");

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(match));
        when(predictionRepository.findByMatchId(5)).thenReturn(List.of(prediction));
        Object[] auditRow = new Object[] {5, "gonenc", lastAudit};
        List<Object[]> auditRows = new ArrayList<>();
        auditRows.add(auditRow);
        when(predictionAuditRepository.findLastRecordedAtForMatches(any())).thenReturn(auditRows);
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");

        PeerMatchView view = service.getVisibleMatchPredictions().getFirst();

        assertEquals(lastAudit, view.predictions().getFirst().lastSavedAt());
    }

    private Match knockoutMatch(int id, Instant kickoff) {
        Match match = upcomingMatch(id, kickoff);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setGroupName(null);
        return match;
    }

    private Match upcomingMatch(int id, Instant kickoff) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setGroupName("A");
        match.setKickoffUtc(kickoff);
        match.setPredictionsEnabled(true);
        Team home = new Team();
        home.setName("Home");
        Team away = new Team();
        away.setName("Away");
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }
}
