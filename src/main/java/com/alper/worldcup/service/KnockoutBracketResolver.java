package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Team;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnockoutBracketResolver {

    private static final Pattern GROUP_RANK = Pattern.compile("^([12])([A-L])$");
    private static final Pattern THIRD_PLACE_SLOT = Pattern.compile("^3([A-L]+)$");
    private static final Pattern WINNER = Pattern.compile("^W(\\d+)$");
    private static final Pattern RUNNER_UP = Pattern.compile("^RU(\\d+)$");

    private final GroupStandingsService groupStandingsService;
    private final MatchRepository matchRepository;
    private final KnockoutPlaceholderCatalog placeholderCatalog;

    public KnockoutBracketResolver(GroupStandingsService groupStandingsService,
                                   MatchRepository matchRepository,
                                   KnockoutPlaceholderCatalog placeholderCatalog) {
        this.groupStandingsService = groupStandingsService;
        this.matchRepository = matchRepository;
        this.placeholderCatalog = placeholderCatalog;
    }

    @Transactional(readOnly = true)
    public Map<Integer, ResolvedKnockoutSides> resolveDisplayNames(List<Match> knockoutMatches) {
        Map<Integer, Match> matchesById = new HashMap<>();
        for (Match match : matchRepository.findAllWithTeams()) {
            matchesById.put(match.getId(), match);
        }
        return resolveDisplayNames(knockoutMatches, standingsByGroup(), matchesById, placeholderCatalog);
    }

    @Transactional(readOnly = true)
    public Optional<Team> resolveTeamForSide(Match match, boolean homeSide) {
        Map<Integer, Match> matchesById = new HashMap<>();
        for (Match loaded : matchRepository.findAllWithTeams()) {
            matchesById.put(loaded.getId(), loaded);
        }
        return resolveTeamForSide(match, homeSide, standingsByGroup(), matchesById, placeholderCatalog);
    }

    static Optional<Team> resolveTeamForSide(Match match,
                                             boolean homeSide,
                                             Map<String, GroupStandingsView> standingsByGroup,
                                             Map<Integer, Match> matchesById,
                                             PlaceholderSource placeholderSource) {
        String placeholder = placeholderSource.placeholder(match, homeSide);
        if (placeholder != null && !placeholder.isBlank()) {
            return resolvePlaceholderTeam(placeholder, standingsByGroup, matchesById, placeholderSource);
        }
        return Optional.ofNullable(homeSide ? match.getHomeTeam() : match.getAwayTeam());
    }

    static Map<Integer, ResolvedKnockoutSides> resolveDisplayNames(List<Match> knockoutMatches,
                                                                   Map<String, GroupStandingsView> standingsByGroup,
                                                                   Map<Integer, Match> matchesById,
                                                                   PlaceholderSource placeholderSource) {
        Map<Integer, ResolvedKnockoutSides> resolved = new HashMap<>();
        for (Match match : knockoutMatches) {
            resolved.put(match.getId(), resolveMatchSides(match, standingsByGroup, matchesById, placeholderSource));
        }
        return resolved;
    }

    private static ResolvedKnockoutSides resolveMatchSides(Match match,
                                                    Map<String, GroupStandingsView> standingsByGroup,
                                                    Map<Integer, Match> matchesById,
                                                    PlaceholderSource placeholderSource) {
        SideDisplay home = formatSideDisplay(match, true, standingsByGroup, matchesById, placeholderSource);
        SideDisplay away = formatSideDisplay(match, false, standingsByGroup, matchesById, placeholderSource);
        return new ResolvedKnockoutSides(
                home.name(),
                away.name(),
                home.slotLabel(),
                away.slotLabel());
    }

    private static SideDisplay formatSideDisplay(Match match,
                                                 boolean homeSide,
                                                 Map<String, GroupStandingsView> standingsByGroup,
                                                 Map<Integer, Match> matchesById,
                                                 PlaceholderSource placeholderSource) {
        String placeholder = placeholderSource.placeholder(match, homeSide);
        String rawName = resolveDisplayName(match, homeSide, placeholder, standingsByGroup, matchesById, placeholderSource);
        if (placeholder == null || placeholder.isBlank()) {
            return new SideDisplay(rawName, null);
        }
        if (rawName.equals(placeholder)) {
            return new SideDisplay(placeholder, null);
        }
        return new SideDisplay(rawName, placeholder);
    }

    private static String resolveDisplayName(Match match,
                                             boolean homeSide,
                                             String placeholder,
                                             Map<String, GroupStandingsView> standingsByGroup,
                                             Map<Integer, Match> matchesById,
                                             PlaceholderSource placeholderSource) {
        if (placeholder != null && !placeholder.isBlank()) {
            return resolvePlaceholder(placeholder, standingsByGroup, matchesById, placeholderSource);
        }
        Team assignedTeam = homeSide ? match.getHomeTeam() : match.getAwayTeam();
        if (assignedTeam != null) {
            return assignedTeam.getName();
        }
        return "TBD";
    }

    private static String resolveSideRaw(Match match,
                                         boolean homeSide,
                                         Map<String, GroupStandingsView> standingsByGroup,
                                         Map<Integer, Match> matchesById,
                                         PlaceholderSource placeholderSource) {
        String placeholder = placeholderSource.placeholder(match, homeSide);
        if (placeholder != null && !placeholder.isBlank()) {
            return resolvePlaceholder(placeholder, standingsByGroup, matchesById, placeholderSource);
        }
        Team assignedTeam = homeSide ? match.getHomeTeam() : match.getAwayTeam();
        if (assignedTeam != null) {
            return assignedTeam.getName();
        }
        return "TBD";
    }

    private static String resolvePlaceholder(String placeholder,
                                      Map<String, GroupStandingsView> standingsByGroup,
                                      Map<Integer, Match> matchesById,
                                      PlaceholderSource placeholderSource) {
        if (placeholder == null || placeholder.isBlank()) {
            return "TBD";
        }

        Matcher groupRank = GROUP_RANK.matcher(placeholder);
        if (groupRank.matches()) {
            int rank = Integer.parseInt(groupRank.group(1));
            String groupName = groupRank.group(2);
            return teamAtGroupRank(standingsByGroup, groupName, rank)
                    .map(Team::getName)
                    .orElse(placeholder);
        }

        Matcher thirdPlace = THIRD_PLACE_SLOT.matcher(placeholder);
        if (thirdPlace.matches()) {
            return bestThirdPlaceAmongGroups(thirdPlace.group(1), standingsByGroup)
                    .map(Team::getName)
                    .orElse(placeholder);
        }

        Matcher winner = WINNER.matcher(placeholder);
        if (winner.matches()) {
            return resolveMatchWinner(Integer.parseInt(winner.group(1)), standingsByGroup, matchesById, placeholderSource)
                    .orElse(placeholder);
        }

        Matcher runnerUp = RUNNER_UP.matcher(placeholder);
        if (runnerUp.matches()) {
            return resolveMatchRunnerUp(Integer.parseInt(runnerUp.group(1)), standingsByGroup, matchesById, placeholderSource)
                    .orElse(placeholder);
        }

        return placeholder;
    }

    private static Optional<Team> resolvePlaceholderTeam(String placeholder,
                                                         Map<String, GroupStandingsView> standingsByGroup,
                                                         Map<Integer, Match> matchesById,
                                                         PlaceholderSource placeholderSource) {
        Matcher groupRank = GROUP_RANK.matcher(placeholder);
        if (groupRank.matches()) {
            int rank = Integer.parseInt(groupRank.group(1));
            String groupName = groupRank.group(2);
            return teamAtGroupRank(standingsByGroup, groupName, rank);
        }

        Matcher thirdPlace = THIRD_PLACE_SLOT.matcher(placeholder);
        if (thirdPlace.matches()) {
            return bestThirdPlaceAmongGroups(thirdPlace.group(1), standingsByGroup);
        }

        Matcher winner = WINNER.matcher(placeholder);
        if (winner.matches()) {
            return resolveMatchWinnerTeam(Integer.parseInt(winner.group(1)), standingsByGroup, matchesById, placeholderSource);
        }

        Matcher runnerUp = RUNNER_UP.matcher(placeholder);
        if (runnerUp.matches()) {
            return resolveMatchRunnerUpTeam(Integer.parseInt(runnerUp.group(1)), standingsByGroup, matchesById, placeholderSource);
        }

        return Optional.empty();
    }

    private static Optional<Team> resolveMatchWinnerTeam(int matchId,
                                                         Map<String, GroupStandingsView> standingsByGroup,
                                                         Map<Integer, Match> matchesById,
                                                         PlaceholderSource placeholderSource) {
        Match match = matchesById.get(matchId);
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        return winnerTeamFromScore(match, standingsByGroup, matchesById, placeholderSource);
    }

    private static Optional<Team> resolveMatchRunnerUpTeam(int matchId,
                                                           Map<String, GroupStandingsView> standingsByGroup,
                                                           Map<Integer, Match> matchesById,
                                                           PlaceholderSource placeholderSource) {
        Match match = matchesById.get(matchId);
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        return runnerUpTeamFromScore(match, standingsByGroup, matchesById, placeholderSource);
    }

    private static Optional<Team> winnerTeamFromScore(Match match,
                                                      Map<String, GroupStandingsView> standingsByGroup,
                                                      Map<Integer, Match> matchesById,
                                                      PlaceholderSource placeholderSource) {
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return resolveTeamForSide(match, true, standingsByGroup, matchesById, placeholderSource);
        }
        if (awayGoals > homeGoals) {
            return resolveTeamForSide(match, false, standingsByGroup, matchesById, placeholderSource);
        }
        return Optional.ofNullable(match.getAdvancingTeamActual());
    }

    private static Optional<Team> runnerUpTeamFromScore(Match match,
                                                        Map<String, GroupStandingsView> standingsByGroup,
                                                        Map<Integer, Match> matchesById,
                                                        PlaceholderSource placeholderSource) {
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return resolveTeamForSide(match, false, standingsByGroup, matchesById, placeholderSource);
        }
        if (awayGoals > homeGoals) {
            return resolveTeamForSide(match, true, standingsByGroup, matchesById, placeholderSource);
        }
        return Optional.empty();
    }

    private static Optional<String> resolveMatchWinner(int matchId,
                                                Map<String, GroupStandingsView> standingsByGroup,
                                                Map<Integer, Match> matchesById,
                                                PlaceholderSource placeholderSource) {
        Match match = matchesById.get(matchId);
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        return Optional.of(winnerFromScore(match, standingsByGroup, matchesById, placeholderSource));
    }

    private static Optional<String> resolveMatchRunnerUp(int matchId,
                                                  Map<String, GroupStandingsView> standingsByGroup,
                                                  Map<Integer, Match> matchesById,
                                                  PlaceholderSource placeholderSource) {
        Match match = matchesById.get(matchId);
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        return Optional.of(runnerUpFromScore(match, standingsByGroup, matchesById, placeholderSource));
    }

    private static String winnerFromScore(Match match,
                                   Map<String, GroupStandingsView> standingsByGroup,
                                   Map<Integer, Match> matchesById,
                                   PlaceholderSource placeholderSource) {
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return resolveSideRaw(match, true, standingsByGroup, matchesById, placeholderSource);
        }
        if (awayGoals > homeGoals) {
            return resolveSideRaw(match, false, standingsByGroup, matchesById, placeholderSource);
        }
        return resolvePlaceholder(placeholderSource.placeholder(match, true), standingsByGroup, matchesById, placeholderSource)
                + " / "
                + resolvePlaceholder(placeholderSource.placeholder(match, false), standingsByGroup, matchesById, placeholderSource);
    }

    private static String runnerUpFromScore(Match match,
                                     Map<String, GroupStandingsView> standingsByGroup,
                                     Map<Integer, Match> matchesById,
                                     PlaceholderSource placeholderSource) {
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return resolveSideRaw(match, false, standingsByGroup, matchesById, placeholderSource);
        }
        if (awayGoals > homeGoals) {
            return resolveSideRaw(match, true, standingsByGroup, matchesById, placeholderSource);
        }
        return "TBD";
    }

    private static Optional<Team> teamAtGroupRank(Map<String, GroupStandingsView> standingsByGroup,
                                           String groupName,
                                           int rank) {
        GroupStandingsView standings = standingsByGroup.get(groupName);
        if (standings == null) {
            return Optional.empty();
        }
        return standings.rows().stream()
                .filter(row -> row.rank() == rank)
                .map(GroupTeamStanding::team)
                .findFirst();
    }

    private static Optional<Team> bestThirdPlaceAmongGroups(String eligibleGroups,
                                                     Map<String, GroupStandingsView> standingsByGroup) {
        return eligibleGroups.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .map(standingsByGroup::get)
                .filter(view -> view != null && view.rows().size() >= 3)
                .map(view -> view.rows().get(2))
                .max(GroupStandingsService.standingComparator())
                .map(GroupTeamStanding::team);
    }

    private Map<String, GroupStandingsView> standingsByGroup() {
        Map<String, GroupStandingsView> standingsByGroup = new HashMap<>();
        for (GroupStandingsView standings : groupStandingsService.getAllGroupStandings()) {
            standingsByGroup.put(standings.groupName(), standings);
        }
        return standingsByGroup;
    }

    public record ResolvedKnockoutSides(
            String homeDisplayName,
            String awayDisplayName,
            String homeSlotLabel,
            String awaySlotLabel) {
    }

    @FunctionalInterface
    interface PlaceholderSource {
        String placeholder(Match match, boolean homeSide);
    }

    private record SideDisplay(String name, String slotLabel) {
    }
}
