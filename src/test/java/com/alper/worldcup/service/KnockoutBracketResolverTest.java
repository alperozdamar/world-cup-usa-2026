package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KnockoutBracketResolverTest {

    @Test
    void resolvesGroupWinnerAndRunnerUpFromCurrentStandings() {
        Team mexico = team(1, "Mexico");
        Team southAfrica = team(2, "South Africa");
        GroupStandingsView groupA = new GroupStandingsView("A", List.of(
                standing(mexico, 1, 6),
                standing(southAfrica, 2, 3)));

        Match match = new Match();
        match.setId(73);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomePlaceholder("1A");
        match.setAwayPlaceholder("2F");

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolved =
                KnockoutBracketResolver.resolveDisplayNames(
                        List.of(match),
                        Map.of("A", groupA),
                        Map.of(),
                        placeholderFromMatch());

        assertEquals("Mexico", resolved.get(73).homeDisplayName());
        assertEquals("1A", resolved.get(73).homeSlotLabel());
        assertEquals("2F", resolved.get(73).awayDisplayName());
        assertEquals(null, resolved.get(73).awaySlotLabel());
    }

    @Test
    void resolvesWinnerPlaceholderFromEnteredKnockoutScore() {
        Team mexico = team(1, "Mexico");
        Team southAfrica = team(2, "South Africa");
        GroupStandingsView groupA = new GroupStandingsView("A", List.of(
                standing(mexico, 1, 6),
                standing(southAfrica, 2, 3)));

        Match roundOf32 = new Match();
        roundOf32.setId(73);
        roundOf32.setStage(MatchStage.ROUND_OF_32);
        roundOf32.setHomePlaceholder("1A");
        roundOf32.setAwayPlaceholder("2F");
        roundOf32.setHomeScoreActual(2);
        roundOf32.setAwayScoreActual(0);

        Match roundOf16 = new Match();
        roundOf16.setId(89);
        roundOf16.setStage(MatchStage.ROUND_OF_16);
        roundOf16.setHomePlaceholder("W73");
        roundOf16.setAwayPlaceholder("W76");

        Map<Integer, Match> matchesById = new HashMap<>();
        matchesById.put(73, roundOf32);
        matchesById.put(89, roundOf16);

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolved =
                KnockoutBracketResolver.resolveDisplayNames(
                        List.of(roundOf16),
                        Map.of("A", groupA),
                        matchesById,
                        placeholderFromMatch());

        assertEquals("Mexico", resolved.get(89).homeDisplayName());
        assertEquals("W73", resolved.get(89).homeSlotLabel());
        assertEquals("W76", resolved.get(89).awayDisplayName());
        assertEquals(null, resolved.get(89).awaySlotLabel());
    }

    @Test
    void prefersAssignedTeamOverStandingsForDisplay() {
        Team usa = team(10, "USA");
        Team bosnia = team(11, "Bosnia and Herzegovina");
        Match match = new Match();
        match.setId(80);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeTeam(usa);
        match.setAwayTeam(bosnia);
        match.setHomePlaceholder("1D");
        match.setAwayPlaceholder("3BEFIJ");

        Team mexico = team(1, "Mexico");
        Team senegal = team(2, "Senegal");
        GroupStandingsView groupA = new GroupStandingsView("A", List.of(standing(mexico, 1, 6)));
        GroupStandingsView groupI = new GroupStandingsView("I", List.of(
                standing(team(3, "France"), 1, 6),
                standing(team(4, "Norway"), 2, 6),
                standing(senegal, 3, 0)));

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolved =
                KnockoutBracketResolver.resolveDisplayNames(
                        List.of(match),
                        Map.of("A", groupA, "I", groupI),
                        Map.of(80, match),
                        placeholderFromMatch());

        assertEquals("USA", resolved.get(80).homeDisplayName());
        assertEquals("1D", resolved.get(80).homeSlotLabel());
        assertEquals("Bosnia and Herzegovina", resolved.get(80).awayDisplayName());
        assertEquals("3BEFIJ", resolved.get(80).awaySlotLabel());
    }

    @Test
    void resolvesWinnerAfterPenaltyShootoutWhenAdvancerIsSet() {
        Team germany = team(1, "Germany");
        Team paraguay = team(2, "Paraguay");
        Team france = team(3, "France");

        Match roundOf32 = new Match();
        roundOf32.setId(75);
        roundOf32.setStage(MatchStage.ROUND_OF_32);
        roundOf32.setHomeTeam(germany);
        roundOf32.setAwayTeam(paraguay);
        roundOf32.setHomePlaceholder("1E");
        roundOf32.setAwayPlaceholder("3ABCDF");
        roundOf32.setHomeScoreActual(1);
        roundOf32.setAwayScoreActual(1);
        roundOf32.setAdvancingTeamActual(paraguay);

        Match roundOf16 = new Match();
        roundOf16.setId(90);
        roundOf16.setStage(MatchStage.ROUND_OF_16);
        roundOf16.setHomePlaceholder("W75");
        roundOf16.setAwayPlaceholder("W78");

        Match franceMatch = new Match();
        franceMatch.setId(78);
        franceMatch.setStage(MatchStage.ROUND_OF_32);
        franceMatch.setHomeTeam(france);
        franceMatch.setHomeScoreActual(3);
        franceMatch.setAwayScoreActual(0);

        Map<Integer, Match> matchesById = new HashMap<>();
        matchesById.put(75, roundOf32);
        matchesById.put(78, franceMatch);
        matchesById.put(90, roundOf16);

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolved =
                KnockoutBracketResolver.resolveDisplayNames(
                        List.of(roundOf16),
                        Map.of(),
                        matchesById,
                        placeholderFromMatch());

        assertEquals("Paraguay", resolved.get(90).homeDisplayName());
        assertEquals("W75", resolved.get(90).homeSlotLabel());
    }

    @Test
    void fallsBackToAssignedTeamWhenNoPlaceholder() {
        Team usa = team(10, "USA");
        Match match = new Match();
        match.setId(80);
        match.setStage(MatchStage.ROUND_OF_32);
        match.setHomeTeam(usa);

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolved =
                KnockoutBracketResolver.resolveDisplayNames(
                        List.of(match),
                        Map.of(),
                        Map.of(80, match),
                        placeholderFromMatch());

        assertEquals("USA", resolved.get(80).homeDisplayName());
        assertEquals(null, resolved.get(80).homeSlotLabel());
    }

    private static KnockoutBracketResolver.PlaceholderSource placeholderFromMatch() {
        return (match, homeSide) -> homeSide ? match.getHomePlaceholder() : match.getAwayPlaceholder();
    }

    private static GroupTeamStanding standing(Team team, int rank, int points) {
        return new GroupTeamStanding(team, 2, 1, 0, 1, 3, 1, 2, points, rank);
    }

    private static Team team(int id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
}
