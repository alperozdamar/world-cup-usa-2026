package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.GroupStandingPrediction;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostPredictionServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private UserProfileService userProfileService;

    private HostPredictionService service;

    @BeforeEach
    void setUp() {
        service = new HostPredictionService(
                matchRepository,
                predictionRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
                userAccountService,
                userProfileService);
    }

    @Test
    void returnsEmptyWhenNoAdminsConfigured() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of());

        assertTrue(service.getHostMatchPredictions().isEmpty());
        assertTrue(service.getHostGroupPredictions().isEmpty());
        assertTrue(service.getHostFinalPredictions().isEmpty());
    }

    @Test
    void returnsOnlyAdminMatchPredictions() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        Match match = new Match();
        match.setId(1);
        match.setStage(MatchStage.GROUP_STAGE);

        Prediction hostPrediction = new Prediction();
        hostPrediction.setUsername("alper");
        hostPrediction.setHomeScoreGuess(2);
        hostPrediction.setAwayScoreGuess(1);

        Prediction otherPrediction = new Prediction();
        otherPrediction.setUsername("gonenc");
        otherPrediction.setHomeScoreGuess(0);
        otherPrediction.setAwayScoreGuess(0);

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(match));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of(hostPrediction, otherPrediction));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<PeerMatchView> views = service.getHostMatchPredictions();

        assertEquals(1, views.size());
        assertEquals(1, views.get(0).predictions().size());
        assertEquals("alper", views.get(0).predictions().get(0).username());
        assertEquals(2, views.get(0).predictions().get(0).homeGuess());
    }

    @Test
    void returnsOnlyAdminFinalPrediction() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        Team champion = team(1, "Brazil");
        Team runnerUp = team(2, "France");

        FinalPrediction hostFinal = new FinalPrediction();
        hostFinal.setUsername("alper");
        hostFinal.setChampionTeam(champion);
        hostFinal.setRunnerUpTeam(runnerUp);

        FinalPrediction otherFinal = new FinalPrediction();
        otherFinal.setUsername("gonenc");
        otherFinal.setChampionTeam(champion);
        otherFinal.setRunnerUpTeam(runnerUp);

        when(finalPredictionRepository.findAllWithTeamsOrderByUsername())
                .thenReturn(List.of(hostFinal, otherFinal));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<PeerFinalRowView> rows = service.getHostFinalPredictions();

        assertEquals(1, rows.size());
        assertEquals("alper", rows.get(0).username());
        assertEquals("Brazil", rows.get(0).championName());
    }

    @Test
    void returnsOnlyAdminGroupPredictions() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        GroupStandingPrediction hostGroup = new GroupStandingPrediction();
        hostGroup.setUsername("alper");
        hostGroup.setGroupName("A");
        hostGroup.setFirstPlaceTeam(team(1, "Mexico"));
        hostGroup.setSecondPlaceTeam(team(2, "South Africa"));

        GroupStandingPrediction otherGroup = new GroupStandingPrediction();
        otherGroup.setUsername("gonenc");
        otherGroup.setGroupName("A");
        otherGroup.setFirstPlaceTeam(team(1, "Mexico"));
        otherGroup.setSecondPlaceTeam(team(2, "South Africa"));

        when(groupStandingPredictionRepository.findAllWithTeamsOrderByUsernameAndGroup())
                .thenReturn(List.of(hostGroup, otherGroup));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<PeerGroupRowView> rows = service.getHostGroupPredictions();

        assertEquals(1, rows.size());
        assertEquals("alper", rows.get(0).username());
        assertEquals("Mexico", rows.get(0).picksByGroup().get("A").firstTeamName());
    }

    @Test
    void returnsOnlyAdminKnockoutPredictions() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        Team home = team(1, "Brazil");
        Team away = team(2, "Japan");
        Team advancer = team(2, "Japan");

        Match match = new Match();
        match.setId(73);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeTeam(home);
        match.setAwayTeam(away);

        Prediction hostPrediction = new Prediction();
        hostPrediction.setUsername("alper");
        hostPrediction.setHomeScoreGuess(1);
        hostPrediction.setAwayScoreGuess(1);
        hostPrediction.setPenaltyShootoutGuess(true);
        hostPrediction.setAdvancingTeamGuess(advancer);

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(match));
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of(hostPrediction));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<HostKnockoutMatchView> views = service.getHostKnockoutPredictions();

        assertEquals(1, views.size());
        assertEquals(1, views.get(0).picks().size());
        HostKnockoutPickView pick = views.get(0).picks().get(0);
        assertEquals("alper", pick.username());
        assertEquals(1, pick.homeGuess());
        assertEquals(true, pick.penaltyShootoutGuess());
        assertEquals("Japan", pick.advancingTeamName());
    }

    @Test
    void hostMatchPredictionsNewestKickoffFirst() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        Match older = groupMatch(1, Instant.parse("2026-06-11T19:00:00Z"));
        Match newer = groupMatch(2, Instant.parse("2026-06-27T19:00:00Z"));

        Prediction olderPick = prediction("alper", 1, 0);
        Prediction newerPick = prediction("alper", 2, 1);

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(older, newer));
        when(predictionRepository.findByMatchId(1)).thenReturn(List.of(olderPick));
        when(predictionRepository.findByMatchId(2)).thenReturn(List.of(newerPick));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<PeerMatchView> views = service.getHostMatchPredictions();

        assertEquals(2, views.size());
        assertEquals(2, views.get(0).match().getId());
        assertEquals(1, views.get(1).match().getId());
    }

    @Test
    void hostKnockoutPredictionsNewestKickoffFirst() {
        when(userAccountService.findAdminUsernames()).thenReturn(List.of("alper"));

        Match older = knockoutMatch(73, Instant.parse("2026-06-28T19:00:00Z"));
        Match newer = knockoutMatch(74, Instant.parse("2026-06-29T19:00:00Z"));

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(older, newer));
        when(predictionRepository.findByMatchId(73)).thenReturn(List.of(prediction("alper", 1, 0)));
        when(predictionRepository.findByMatchId(74)).thenReturn(List.of(prediction("alper", 2, 1)));
        when(userProfileService.getDisplayName("alper")).thenReturn("Alper Ozdamar");

        List<HostKnockoutMatchView> views = service.getHostKnockoutPredictions();

        assertEquals(2, views.size());
        assertEquals(74, views.get(0).match().getId());
        assertEquals(73, views.get(1).match().getId());
    }

    private static Match groupMatch(int id, Instant kickoff) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(kickoff);
        return match;
    }

    private static Match knockoutMatch(int id, Instant kickoff) {
        Match match = groupMatch(id, kickoff);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeTeam(team(1, "Brazil"));
        match.setAwayTeam(team(2, "Japan"));
        return match;
    }

    private static Prediction prediction(String username, int home, int away) {
        Prediction prediction = new Prediction();
        prediction.setUsername(username);
        prediction.setHomeScoreGuess(home);
        prediction.setAwayScoreGuess(away);
        return prediction;
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
