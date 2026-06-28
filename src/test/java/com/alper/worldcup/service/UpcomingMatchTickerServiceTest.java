package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpcomingMatchTickerServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Test
    void returnsNextFiveUpcomingMatchesInKickoffOrder() {
        List<Match> matches = List.of(
                match(1, MatchStage.GROUP_STAGE, "Mexico", "South Africa", Instant.parse("2099-06-11T19:00:00Z")),
                match(2, MatchStage.GROUP_STAGE, "Korea", "Czechia", Instant.parse("2099-06-11T22:00:00Z")),
                match(3, MatchStage.GROUP_STAGE, "Canada", "Bosnia", Instant.parse("2099-06-12T01:00:00Z")),
                match(4, MatchStage.GROUP_STAGE, "USA", "Paraguay", Instant.parse("2099-06-12T19:00:00Z")),
                match(5, MatchStage.GROUP_STAGE, "Qatar", "Switzerland", Instant.parse("2099-06-13T19:00:00Z")),
                match(6, MatchStage.GROUP_STAGE, "Brazil", "Morocco", Instant.parse("2099-06-14T19:00:00Z")),
                match(7, MatchStage.GROUP_STAGE, "Past", "Game", Instant.parse("2020-01-01T19:00:00Z")));

        when(matchRepository.findAllWithTeams()).thenReturn(matches);

        UpcomingMatchTickerService service = new UpcomingMatchTickerService(matchRepository);
        List<UpcomingMatchTickerEntry> upcoming = service.getUpcomingMatches("America/New_York");

        assertEquals(5, upcoming.size());
        assertEquals("Mexico", upcoming.get(0).homeTeamName());
        assertEquals("South Africa", upcoming.get(0).awayTeamName());
        assertEquals("Qatar vs Switzerland", upcoming.get(4).label());
        assertTrue(upcoming.stream().noneMatch(entry -> entry.homeTeamName().equals("Past")));
    }

    @Test
    void includesKnockoutMatchesWithBothTeamsSet() {
        List<Match> matches = List.of(
                match(10, MatchStage.ROUND_OF_32, "Brazil", "Japan", Instant.parse("2099-07-01T17:00:00Z")),
                match(11, MatchStage.ROUND_OF_32, "Germany", "Paraguay", Instant.parse("2099-07-02T20:30:00Z")));

        when(matchRepository.findAllWithTeams()).thenReturn(matches);

        UpcomingMatchTickerService service = new UpcomingMatchTickerService(matchRepository);
        List<UpcomingMatchTickerEntry> upcoming = service.getUpcomingMatches("America/New_York");

        assertEquals(2, upcoming.size());
        assertTrue(upcoming.get(0).knockout());
        assertEquals("Brazil", upcoming.get(0).homeTeamName());
    }

    private Match match(int id, MatchStage stage, String home, String away, Instant kickoffUtc) {
        Match match = new Match();
        match.setId(id);
        match.setStage(stage);
        match.setKickoffUtc(kickoffUtc);
        match.setHomeTeam(new Team(home, "A"));
        match.setAwayTeam(new Team(away, "A"));
        return match;
    }
}
