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

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
