package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.FinalPredictionAuditRepository;
import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.dao.UserCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class TournamentWrapUpServiceTest {

    @Test
    void parseChangelogCountsWcTickets() {
        TournamentWrapUpService service = new TournamentWrapUpService(
                mock(MatchRepository.class),
                mock(PredictionRepository.class),
                mock(PredictionAuditRepository.class),
                mock(GroupStandingPredictionAuditRepository.class),
                mock(FinalPredictionAuditRepository.class),
                mock(TeamRepository.class),
                mock(UserCommentRepository.class),
                mock(PoolMemberRegistry.class),
                new DefaultResourceLoader(),
                95,
                "2026-05-31");

        TournamentWrapUpService.ChangelogCounts counts = service.parseChangelog();
        assertTrue(counts.features() >= 100, "expected WC feature tickets in Changelog");
        assertTrue(counts.fixes() >= 1, "expected at least one fix line in Changelog");
    }

    @Test
    void getStatsBuildsFunnyCards() {
        MatchRepository matchRepository = mock(MatchRepository.class);
        PredictionRepository predictionRepository = mock(PredictionRepository.class);
        PredictionAuditRepository predictionAuditRepository = mock(PredictionAuditRepository.class);
        GroupStandingPredictionAuditRepository groupAuditRepository =
                mock(GroupStandingPredictionAuditRepository.class);
        FinalPredictionAuditRepository finalAuditRepository = mock(FinalPredictionAuditRepository.class);
        TeamRepository teamRepository = mock(TeamRepository.class);
        UserCommentRepository userCommentRepository = mock(UserCommentRepository.class);
        PoolMemberRegistry poolMemberRegistry = mock(PoolMemberRegistry.class);

        when(matchRepository.count()).thenReturn(104L);
        when(matchRepository.countScoredMatches()).thenReturn(100L);
        when(matchRepository.sumGoalsScored()).thenReturn(287L);
        when(matchRepository.countPenaltyShootouts()).thenReturn(8L);
        when(matchRepository.findTournamentStartKickoff())
                .thenReturn(java.util.Optional.of(java.time.Instant.parse("2026-06-11T19:00:00Z")));
        when(predictionRepository.count()).thenReturn(800L);
        when(predictionRepository.countExactScorePredictions()).thenReturn(42L);
        when(predictionAuditRepository.countByAction(org.mockito.ArgumentMatchers.any()))
                .thenReturn(100L);
        when(groupAuditRepository.countByAction(org.mockito.ArgumentMatchers.any())).thenReturn(10L);
        when(finalAuditRepository.countByAction(org.mockito.ArgumentMatchers.any())).thenReturn(5L);
        when(teamRepository.count()).thenReturn(48L);
        when(poolMemberRegistry.getMembers()).thenReturn(java.util.List.of());
        when(userCommentRepository.count()).thenReturn(3L);

        TournamentWrapUpService service = new TournamentWrapUpService(
                matchRepository,
                predictionRepository,
                predictionAuditRepository,
                groupAuditRepository,
                finalAuditRepository,
                teamRepository,
                userCommentRepository,
                poolMemberRegistry,
                new DefaultResourceLoader(),
                95,
                "2026-05-31");

        java.util.List<WrapUpStat> stats = service.getStats();
        assertTrue(stats.size() >= 12);
        assertTrue(stats.stream().anyMatch(s -> s.label().contains("Git commits")));
        assertTrue(stats.stream().anyMatch(s -> s.label().contains("Mind-changes")));
        assertTrue(stats.stream().anyMatch(s -> s.quip().contains("this page")
                || s.quip().contains("this recap")));
    }
}
