package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpcomingMatchTickerServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Test
    void returnsNextFiveUpcomingMatchesInKickoffOrder() {
        List<Match> matches = List.of(
                match(1, "Mexico", "South Africa", Instant.parse("2099-06-11T19:00:00Z")),
                match(2, "Korea", "Czechia", Instant.parse("2099-06-11T22:00:00Z")),
                match(3, "Canada", "Bosnia", Instant.parse("2099-06-12T01:00:00Z")),
                match(4, "USA", "Paraguay", Instant.parse("2099-06-12T19:00:00Z")),
                match(5, "Qatar", "Switzerland", Instant.parse("2099-06-13T19:00:00Z")),
                match(6, "Brazil", "Morocco", Instant.parse("2099-06-14T19:00:00Z")),
                match(7, "Past", "Game", Instant.parse("2020-01-01T19:00:00Z")));

        when(matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)).thenReturn(matches);

        UpcomingMatchTickerService service = new UpcomingMatchTickerService(matchRepository);
        List<UpcomingMatchTickerEntry> upcoming = service.getUpcomingMatches("America/New_York");

        assertEquals(5, upcoming.size());
        assertEquals("Mexico vs South Africa", upcoming.get(0).label());
        assertEquals("Qatar vs Switzerland", upcoming.get(4).label());
        assertTrue(upcoming.stream().noneMatch(entry -> entry.label().startsWith("Past")));
    }

    private Match match(int id, String home, String away, Instant kickoffUtc) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(kickoffUtc);
        match.setHomeTeam(new Team(home, "A"));
        match.setAwayTeam(new Team(away, "A"));
        return match;
    }
}
