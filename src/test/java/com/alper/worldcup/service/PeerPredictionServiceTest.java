package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerPredictionServiceTest {

    @Mock
    private MatchRepository matchRepository;

    private PeerPredictionService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new PeerPredictionService(
                matchRepository, null, null, null, null);
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
}
