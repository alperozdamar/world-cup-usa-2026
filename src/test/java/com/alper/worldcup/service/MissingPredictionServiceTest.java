package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.time.ZoneId;
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
class MissingPredictionServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private UserProfileService userProfileService;

    private MissingPredictionService service;

    @BeforeEach
    void setUp() {
        service = new MissingPredictionService(
                matchRepository,
                predictionRepository,
                userProfileService,
                new PoolMemberRegistry("default"));
    }

    @Test
    void findMissingForMatchListsPoolMembersWithoutPrediction() {
        Match match = knockoutMatch(91, Instant.parse("2099-07-05T20:00:00Z"));
        when(predictionRepository.findByMatchId(91)).thenReturn(List.of(prediction("gonenc")));
        when(userProfileService.getDisplayName(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> (String) invocation.getArgument(0));

        List<MissingPredictionMember> missing = service.findMissingForMatch(match);

        assertFalse(missing.isEmpty());
        assertTrue(missing.stream().anyMatch(member -> "adem".equals(member.username())));
        assertTrue(missing.stream().noneMatch(member -> "gonenc".equals(member.username())));
    }

    @Test
    void openMatchesOnNextMatchDayUsesUserTimezone() {
        ZoneId zone = ZoneId.of("America/New_York");
        Match todayEarly = knockoutMatch(91, Instant.parse("2099-07-05T20:00:00Z"));
        Match tomorrow = knockoutMatch(92, Instant.parse("2099-07-06T16:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of());
        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(todayEarly, tomorrow));

        List<Match> matches = service.openMatchesOnNextMatchDay(zone);

        assertEquals(1, matches.size());
        assertEquals(91, matches.getFirst().getId());
    }

    @Test
    void describeMissingMatchesForUserNamesOpenGamesWithoutPick() {
        ZoneId zone = ZoneId.of("America/New_York");
        Match match = knockoutMatch(91, Instant.parse("2099-07-05T20:00:00Z"));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of());
        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(match));
        when(predictionRepository.findByUsernameAndMatchId("adem", 91)).thenReturn(Optional.empty());

        List<String> lines = service.describeMissingMatchesForUser("adem", zone);

        assertEquals(1, lines.size());
        assertTrue(lines.getFirst().contains("Home vs Away"));
    }

    private Prediction prediction(String username) {
        Prediction prediction = new Prediction();
        prediction.setUsername(username);
        return prediction;
    }

    private Match knockoutMatch(int id, Instant kickoff) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.ROUND_OF_16);
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
