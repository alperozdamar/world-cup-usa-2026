package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PredictionServiceSaveScoreTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private PointsService pointsService;
    @Mock
    private PredictionAuditService predictionAuditService;
    @Mock
    private KnockoutAssignmentService knockoutAssignmentService;

    @InjectMocks
    private PredictionService predictionService;

    @Test
    void saveKnockoutDrawStoresPenaltyShootoutActual() {
        Match match = knockoutMatch();
        when(matchRepository.findById(76)).thenReturn(Optional.of(match));
        when(predictionRepository.findByMatchId(76)).thenReturn(List.of());
        when(knockoutAssignmentService.syncBracketFromStandings(any())).thenReturn(
                new KnockoutSyncResult(0, 0, List.of()));

        predictionService.saveActualScore(76, 1, 1, match.getAwayTeam().getId(), true);

        ArgumentCaptor<Match> saved = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(saved.capture());
        Match updated = saved.getValue();
        assertEquals(1, updated.getHomeScoreActual());
        assertEquals(1, updated.getAwayScoreActual());
        assertEquals(true, updated.getPenaltyShootoutActual());
        assertEquals(match.getAwayTeam(), updated.getAdvancingTeamActual());
    }

    @Test
    void saveKnockoutWinClearsPenaltyShootoutActual() {
        Match match = knockoutMatch();
        match.setPenaltyShootoutActual(true);
        match.setAdvancingTeamActual(match.getAwayTeam());
        when(matchRepository.findById(76)).thenReturn(Optional.of(match));
        when(predictionRepository.findByMatchId(76)).thenReturn(List.of());
        when(knockoutAssignmentService.syncBracketFromStandings(any())).thenReturn(
                new KnockoutSyncResult(0, 0, List.of()));

        predictionService.saveActualScore(76, 2, 1, null, null);

        ArgumentCaptor<Match> saved = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(saved.capture());
        Match updated = saved.getValue();
        assertNull(updated.getPenaltyShootoutActual());
        assertNull(updated.getAdvancingTeamActual());
    }

    @Test
    void saveKnockoutDrawRequiresPenaltyShootoutChoice() {
        Match match = knockoutMatch();
        when(matchRepository.findById(76)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () ->
                predictionService.saveActualScore(76, 1, 1, match.getAwayTeam().getId(), null));
    }

    private static Match knockoutMatch() {
        Team home = team(1, "Netherlands");
        Team away = team(2, "Morocco");
        Match match = new Match();
        match.setId(76);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
