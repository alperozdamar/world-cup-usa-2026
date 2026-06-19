package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupStandingsServiceTest {

    private final GroupStandingsService service = new GroupStandingsService(null, null);

    @Test
    void standingComparatorRanksByPointsGoalDifferenceAndGoalsFor() {
        Team mexico = team(1, "Mexico");
        Team korea = team(2, "South Korea");
        Team czechia = team(3, "Czechia");
        Team southAfrica = team(4, "South Africa");

        List<GroupTeamStanding> ranked = List.of(
                        row(czechia, 2, 0, 1, 1, 2, 3, -1, 1),
                        row(southAfrica, 2, 0, 1, 1, 1, 3, -2, 1),
                        row(korea, 2, 1, 0, 1, 2, 2, 0, 3),
                        row(mexico, 2, 2, 0, 0, 3, 0, 3, 6))
                .stream()
                .sorted(GroupStandingsService.standingComparator())
                .toList();

        assertEquals("Mexico", ranked.get(0).team().getName());
        assertEquals("South Korea", ranked.get(1).team().getName());
        assertEquals("Czechia", ranked.get(2).team().getName());
        assertEquals("South Africa", ranked.get(3).team().getName());
    }

    @Test
    void buildGroupStandingsFromScoredMatches() {
        Team home = team(1, "Mexico");
        Team away = team(2, "South Africa");
        Match match = new Match();
        match.setStage(MatchStage.GROUP_STAGE);
        match.setGroupName("A");
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setHomeScoreActual(2);
        match.setAwayScoreActual(0);

        GroupStandingsView standings = service.buildGroupStandings("A", List.of(home, away), List.of(match));

        assertEquals(2, standings.rows().size());
        assertEquals("Mexico", standings.rows().get(0).team().getName());
        assertEquals(3, standings.rows().get(0).points());
        assertEquals("South Africa", standings.rows().get(1).team().getName());
        assertEquals(0, standings.rows().get(1).points());
    }

    private static GroupTeamStanding row(Team team,
                                         int played,
                                         int won,
                                         int drawn,
                                         int lost,
                                         int gf,
                                         int ga,
                                         int gd,
                                         int pts) {
        return new GroupTeamStanding(team, played, won, drawn, lost, gf, ga, gd, pts, 0);
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
