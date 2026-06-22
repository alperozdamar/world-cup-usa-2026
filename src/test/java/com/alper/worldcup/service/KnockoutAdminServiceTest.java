package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KnockoutAdminServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private KnockoutBracketResolver knockoutBracketResolver;

    private KnockoutAdminService service;

    @BeforeEach
    void setUp() {
        service = new KnockoutAdminService(matchRepository, knockoutBracketResolver, true);
    }

    @Test
    void syncTeamsAssignsResolvedSides() {
        Match match = knockoutMatch(73);
        Team mexico = team(1, "Mexico");
        Team southAfrica = team(2, "South Africa");

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(match));
        when(knockoutBracketResolver.resolveTeamForSide(match, true)).thenReturn(Optional.of(mexico));
        when(knockoutBracketResolver.resolveTeamForSide(match, false)).thenReturn(Optional.of(southAfrica));

        KnockoutAdminService.TeamSyncResult result =
                service.syncTeamsForStage(MatchStage.ROUND_OF_32);

        assertEquals(1, result.matchesUpdated());
        assertEquals(0, result.unresolvedSides());
        ArgumentCaptor<Match> saved = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(saved.capture());
        assertEquals(mexico, saved.getValue().getHomeTeam());
        assertEquals(southAfrica, saved.getValue().getAwayTeam());
    }

    @Test
    void syncTeamsDisabledWhenPropertyOff() {
        KnockoutAdminService disabled = new KnockoutAdminService(matchRepository, knockoutBracketResolver, false);
        assertThrows(IllegalStateException.class,
                () -> disabled.syncTeamsForStage(MatchStage.ROUND_OF_32));
    }

    @Test
    void openPredictionsRequiresTeams() {
        Match ready = knockoutMatch(73);
        ready.setHomeTeam(team(1, "Mexico"));
        ready.setAwayTeam(team(2, "South Africa"));

        Match missingTeams = knockoutMatch(74);

        when(matchRepository.findKnockoutMatchesWithTeams()).thenReturn(List.of(ready, missingTeams));

        KnockoutAdminService.OpenPredictionsResult result =
                service.openPredictionsForStage(MatchStage.ROUND_OF_32);

        assertEquals(1, result.opened());
        assertEquals(0, result.alreadyOpen());
        assertEquals(1, result.missingTeams());
        verify(matchRepository).save(ready);
        verify(matchRepository, never()).save(missingTeams);
    }

    private static Match knockoutMatch(int id) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setKickoffUtc(Instant.parse("2026-07-01T19:00:00Z"));
        return match;
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
