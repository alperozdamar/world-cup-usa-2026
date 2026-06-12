package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerPredictionServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private UserProfileService userProfileService;

    private PeerPredictionService service;

    @BeforeEach
    void setUp() {
        service = new PeerPredictionService(
                matchRepository,
                predictionRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
                userProfileService,
                new PoolMemberRegistry("default"));
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

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(upcoming));
        when(predictionRepository.findByMatchId(3)).thenReturn(List.of(prediction));
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc Gorgulu");

        PeerMatchView view = service.getUpcomingMatchPrediction().orElseThrow();

        assertEquals(3, view.match().getId());
        assertTrue(view.predictionsHidden());
        assertEquals(1, view.predictions().size());
        assertTrue(view.predictions().get(0).hidden());
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
