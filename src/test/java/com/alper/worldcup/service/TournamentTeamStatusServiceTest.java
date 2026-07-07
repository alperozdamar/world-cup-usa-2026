package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentTeamStatusServiceTest {

    @Mock
    private MatchRepository matchRepository;

    private TournamentTeamStatusService service;

    @BeforeEach
    void setUp() {
        service = new TournamentTeamStatusService(matchRepository);
    }

    @Test
    void knockoutLoserIsEliminated() {
        Team brazil = team(1, "Brazil", null);
        Team norway = team(2, "Norway", null);
        Match r16 = scoredKnockout(91, MatchStage.ROUND_OF_16, brazil, norway, 1, 2, norway);

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(r16));

        Set<String> eliminated = service.findEliminatedTeamNames();

        assertTrue(eliminated.contains("Brazil"));
        assertFalse(eliminated.contains("Norway"));
    }

    @Test
    void groupTeamsNotInRoundOf32AreEliminatedOnceBracketAssigned() {
        Team brazil = team(1, "Brazil", "C");
        Team norway = team(2, "Norway", "I");
        Team haiti = team(3, "Haiti", "C");
        Team scotland = team(4, "Scotland", "C");

        Match r32 = upcomingKnockout(73, brazil, norway);
        Match groupMatch = groupMatch(haiti, scotland);

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(r32));
        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(List.of(groupMatch));

        Set<String> eliminated = service.findEliminatedTeamNames();

        assertTrue(eliminated.contains("Haiti"));
        assertTrue(eliminated.contains("Scotland"));
        assertFalse(eliminated.contains("Brazil"));
    }

    private Match scoredKnockout(int id, MatchStage stage, Team home, Team away,
                                 int homeScore, int awayScore, Team advancer) {
        Match match = upcomingKnockout(id, home, away);
        match.setStage(stage);
        match.setHomeScoreActual(homeScore);
        match.setAwayScoreActual(awayScore);
        match.setAdvancingTeamActual(advancer);
        return match;
    }

    private Match upcomingKnockout(int id, Team home, Team away) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setKickoffUtc(Instant.parse("2099-07-09T20:00:00Z"));
        match.setPredictionsEnabled(true);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }

    private Match groupMatch(Team home, Team away) {
        Match match = new Match();
        match.setId(10);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setGroupName("C");
        match.setKickoffUtc(Instant.parse("2026-06-13T20:00:00Z"));
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }

    private Team team(int id, String name, String group) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setGroupName(group);
        return team;
    }
}
