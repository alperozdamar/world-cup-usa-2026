package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnockoutAssignmentService {

    private static final Pattern GROUP_RANK = Pattern.compile("^([12])([A-L])$");
    private static final Pattern THIRD_PLACE_SLOT = Pattern.compile("^3([A-L]+)$");
    private static final Pattern WINNER = Pattern.compile("^W(\\d+)$");
    private static final Pattern RUNNER_UP = Pattern.compile("^RU(\\d+)$");

    private static final List<MatchStage> STAGE_ORDER = List.of(
            MatchStage.ROUND_OF_32,
            MatchStage.ROUND_OF_16,
            MatchStage.QUARTER_FINAL,
            MatchStage.SEMI_FINAL,
            MatchStage.THIRD_PLACE,
            MatchStage.FINAL);

    private final MatchRepository matchRepository;
    private final GroupStandingsService groupStandingsService;
    private final KnockoutBracketResolver.PlaceholderSource placeholderSource;

    public KnockoutAssignmentService(MatchRepository matchRepository,
                                     GroupStandingsService groupStandingsService,
                                     KnockoutBracketResolver.PlaceholderSource placeholderSource) {
        this.matchRepository = matchRepository;
        this.groupStandingsService = groupStandingsService;
        this.placeholderSource = placeholderSource;
    }

    /**
     * Fills empty {@code home_team_id} / {@code away_team_id} from standings and scored knockouts.
     * Never overwrites teams already assigned. Never deletes or changes player predictions.
     */
    @Transactional
    public KnockoutSyncResult syncBracketFromStandings(Instant now) {
        List<Match> knockoutMatches = matchRepository.findKnockoutMatchesWithTeams().stream()
                .sorted(Comparator
                        .comparingInt((Match match) -> STAGE_ORDER.indexOf(match.getStage()))
                        .thenComparing(Match::getKickoffUtc))
                .toList();

        Map<String, GroupStandingsView> standingsByGroup = standingsByGroup();
        Map<Integer, Match> matchesById = new HashMap<>();
        for (Match match : matchRepository.findAllWithTeams()) {
            matchesById.put(match.getId(), match);
        }

        return applySync(knockoutMatches, standingsByGroup, matchesById, now, matchRepository::save);
    }

    KnockoutSyncResult applySync(List<Match> knockoutMatches,
                                 Map<String, GroupStandingsView> standingsByGroup,
                                 Map<Integer, Match> matchesById,
                                 Instant now,
                                 java.util.function.Consumer<Match> onSave) {
        Set<Integer> assignedThirdPlaceTeamIds = collectAssignedThirdPlaceTeams(knockoutMatches, standingsByGroup);

        int teamsAssigned = 0;
        List<String> details = new ArrayList<>();

        for (Match match : knockoutMatches) {
            boolean changed = false;

            if (match.getHomeTeam() == null) {
                Optional<Team> team = resolveTeamForSide(
                        match, true, standingsByGroup, matchesById, assignedThirdPlaceTeamIds);
                if (team.isPresent()) {
                    match.setHomeTeam(team.get());
                    trackThirdPlaceAssignment(match, true, team.get(), assignedThirdPlaceTeamIds);
                    teamsAssigned++;
                    changed = true;
                    details.add("Match " + match.getId() + " home → " + team.get().getName());
                }
            }

            if (match.getAwayTeam() == null) {
                Optional<Team> team = resolveTeamForSide(
                        match, false, standingsByGroup, matchesById, assignedThirdPlaceTeamIds);
                if (team.isPresent()) {
                    match.setAwayTeam(team.get());
                    trackThirdPlaceAssignment(match, false, team.get(), assignedThirdPlaceTeamIds);
                    teamsAssigned++;
                    changed = true;
                    details.add("Match " + match.getId() + " away → " + team.get().getName());
                }
            }

            if (changed) {
                onSave.accept(match);
            }
        }

        int matchesOpened = 0;
        for (Match match : knockoutMatches) {
            if (shouldOpenPredictions(match, now)) {
                match.setPredictionsEnabled(true);
                onSave.accept(match);
                matchesOpened++;
            }
        }

        return new KnockoutSyncResult(teamsAssigned, matchesOpened, details);
    }

    private boolean shouldOpenPredictions(Match match, Instant now) {
        return !match.isPredictionsEnabled()
                && match.getHomeTeam() != null
                && match.getAwayTeam() != null
                && match.getKickoffUtc().isAfter(now);
    }

    private Set<Integer> collectAssignedThirdPlaceTeams(List<Match> knockoutMatches,
                                                        Map<String, GroupStandingsView> standingsByGroup) {
        Set<Integer> assigned = new HashSet<>();
        for (Match match : knockoutMatches) {
            if (match.getHomeTeam() != null && isThirdPlaceTeam(match.getHomeTeam(), standingsByGroup)) {
                assigned.add(match.getHomeTeam().getId());
            }
            if (match.getAwayTeam() != null && isThirdPlaceTeam(match.getAwayTeam(), standingsByGroup)) {
                assigned.add(match.getAwayTeam().getId());
            }
        }
        return assigned;
    }

    private void trackThirdPlaceAssignment(Match match,
                                           boolean homeSide,
                                           Team team,
                                           Set<Integer> assignedThirdPlaceTeamIds) {
        String placeholder = placeholderSource.placeholder(match, homeSide);
        if (placeholder != null && THIRD_PLACE_SLOT.matcher(placeholder).matches()) {
            assignedThirdPlaceTeamIds.add(team.getId());
        }
    }

    private Optional<Team> resolveTeamForSide(Match match,
                                              boolean homeSide,
                                              Map<String, GroupStandingsView> standingsByGroup,
                                              Map<Integer, Match> matchesById,
                                              Set<Integer> assignedThirdPlaceTeamIds) {
        String placeholder = placeholderSource.placeholder(match, homeSide);
        if (placeholder == null || placeholder.isBlank()) {
            return Optional.empty();
        }
        return resolvePlaceholderTeam(placeholder, standingsByGroup, matchesById, assignedThirdPlaceTeamIds);
    }

    private Optional<Team> resolvePlaceholderTeam(String placeholder,
                                                  Map<String, GroupStandingsView> standingsByGroup,
                                                  Map<Integer, Match> matchesById,
                                                  Set<Integer> assignedThirdPlaceTeamIds) {
        Matcher groupRank = GROUP_RANK.matcher(placeholder);
        if (groupRank.matches()) {
            int rank = Integer.parseInt(groupRank.group(1));
            String groupName = groupRank.group(2);
            return teamAtGroupRank(standingsByGroup, groupName, rank);
        }

        Matcher thirdPlace = THIRD_PLACE_SLOT.matcher(placeholder);
        if (thirdPlace.matches()) {
            return bestUnassignedThirdPlace(thirdPlace.group(1), standingsByGroup, assignedThirdPlaceTeamIds);
        }

        Matcher winner = WINNER.matcher(placeholder);
        if (winner.matches()) {
            return winnerTeam(matchesById.get(Integer.parseInt(winner.group(1))));
        }

        Matcher runnerUp = RUNNER_UP.matcher(placeholder);
        if (runnerUp.matches()) {
            return runnerUpTeam(matchesById.get(Integer.parseInt(runnerUp.group(1))));
        }

        return Optional.empty();
    }

    private Optional<Team> teamAtGroupRank(Map<String, GroupStandingsView> standingsByGroup,
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

    private Optional<Team> bestUnassignedThirdPlace(String eligibleGroups,
                                                    Map<String, GroupStandingsView> standingsByGroup,
                                                    Set<Integer> assignedThirdPlaceTeamIds) {
        return eligibleGroups.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .map(standingsByGroup::get)
                .filter(view -> view != null && view.rows().size() >= 3)
                .map(view -> view.rows().get(2))
                .filter(row -> !assignedThirdPlaceTeamIds.contains(row.team().getId()))
                .max(GroupStandingsService.standingComparator())
                .map(GroupTeamStanding::team);
    }

    private Optional<Team> winnerTeam(Match match) {
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return Optional.ofNullable(match.getHomeTeam());
        }
        if (awayGoals > homeGoals) {
            return Optional.ofNullable(match.getAwayTeam());
        }
        return Optional.ofNullable(match.getAdvancingTeamActual());
    }

    private Optional<Team> runnerUpTeam(Match match) {
        if (match == null || !match.isScoreEntered()) {
            return Optional.empty();
        }
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return Optional.ofNullable(match.getAwayTeam());
        }
        if (awayGoals > homeGoals) {
            return Optional.ofNullable(match.getHomeTeam());
        }
        return Optional.empty();
    }

    private boolean isThirdPlaceTeam(Team team, Map<String, GroupStandingsView> standingsByGroup) {
        if (team.getGroupName() == null) {
            return false;
        }
        GroupStandingsView standings = standingsByGroup.get(team.getGroupName());
        if (standings == null || standings.rows().size() < 3) {
            return false;
        }
        return standings.rows().get(2).team().getId().equals(team.getId());
    }

    private Map<String, GroupStandingsView> standingsByGroup() {
        Map<String, GroupStandingsView> standingsByGroup = new HashMap<>();
        for (GroupStandingsView standings : groupStandingsService.getAllGroupStandings()) {
            standingsByGroup.put(standings.groupName(), standings);
        }
        return standingsByGroup;
    }
}
