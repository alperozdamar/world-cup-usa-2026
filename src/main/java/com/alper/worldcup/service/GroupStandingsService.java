package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Team;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupStandingsService {

    private final MatchRepository matchRepository;
    private final GroupStandingPredictionService groupStandingPredictionService;

    public GroupStandingsService(MatchRepository matchRepository,
                                 GroupStandingPredictionService groupStandingPredictionService) {
        this.matchRepository = matchRepository;
        this.groupStandingPredictionService = groupStandingPredictionService;
    }

    @Transactional(readOnly = true)
    public List<GroupStandingsView> getAllGroupStandings() {
        List<String> groupNames = groupStandingPredictionService.getGroupNames();
        List<Match> scoredMatches = matchRepository.findScoredGroupStageMatchesWithTeams();

        Map<String, List<Match>> matchesByGroup = new HashMap<>();
        for (Match match : scoredMatches) {
            if (match.getGroupName() == null) {
                continue;
            }
            matchesByGroup.computeIfAbsent(match.getGroupName(), ignored -> new ArrayList<>()).add(match);
        }

        List<GroupStandingsView> standings = new ArrayList<>(groupNames.size());
        for (String groupName : groupNames) {
            standings.add(buildGroupStandings(
                    groupName,
                    teamsInGroup(groupName),
                    matchesByGroup.getOrDefault(groupName, List.of())));
        }
        return standings;
    }

    GroupStandingsView buildGroupStandings(String groupName, List<Team> teams, List<Match> scoredMatches) {
        Map<Integer, StandingAccumulator> accumulators = new LinkedHashMap<>();
        for (Team team : teams) {
            accumulators.put(team.getId(), new StandingAccumulator(team));
        }

        for (Match match : scoredMatches) {
            applyMatchResult(accumulators, match.getHomeTeam(), match.getAwayTeam(),
                    match.getHomeScoreActual(), match.getAwayScoreActual());
        }

        List<GroupTeamStanding> rows = accumulators.values().stream()
                .map(StandingAccumulator::toStanding)
                .sorted(standingComparator())
                .toList();

        List<GroupTeamStanding> ranked = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            GroupTeamStanding row = rows.get(i);
            ranked.add(new GroupTeamStanding(
                    row.team(),
                    row.played(),
                    row.won(),
                    row.drawn(),
                    row.lost(),
                    row.goalsFor(),
                    row.goalsAgainst(),
                    row.goalDifference(),
                    row.points(),
                    i + 1));
        }
        return new GroupStandingsView(groupName, ranked);
    }

    private void applyMatchResult(Map<Integer, StandingAccumulator> accumulators,
                                  Team homeTeam,
                                  Team awayTeam,
                                  int homeGoals,
                                  int awayGoals) {
        if (homeTeam == null || awayTeam == null) {
            return;
        }
        StandingAccumulator home = accumulators.get(homeTeam.getId());
        StandingAccumulator away = accumulators.get(awayTeam.getId());
        if (home == null || away == null) {
            return;
        }

        home.recordMatch(homeGoals, awayGoals);
        away.recordMatch(awayGoals, homeGoals);
    }

    private List<Team> teamsInGroup(String groupName) {
        Map<Integer, Team> teams = new TreeMap<>();
        for (Match match : matchRepository.findGroupStageMatchesByGroup(groupName)) {
            if (match.getHomeTeam() != null) {
                teams.put(match.getHomeTeam().getId(), match.getHomeTeam());
            }
            if (match.getAwayTeam() != null) {
                teams.put(match.getAwayTeam().getId(), match.getAwayTeam());
            }
        }
        return teams.values().stream()
                .sorted(Comparator.comparing(Team::getName))
                .toList();
    }

    static Comparator<GroupTeamStanding> standingComparator() {
        return Comparator
                .comparingInt(GroupTeamStanding::points).reversed()
                .thenComparing(Comparator.comparingInt(GroupTeamStanding::goalDifference).reversed())
                .thenComparing(Comparator.comparingInt(GroupTeamStanding::goalsFor).reversed())
                .thenComparing(row -> row.team().getName(), String.CASE_INSENSITIVE_ORDER);
    }

    private static final class StandingAccumulator {
        private final Team team;
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;

        private StandingAccumulator(Team team) {
            this.team = team;
        }

        private void recordMatch(int goalsFor, int goalsAgainst) {
            played++;
            this.goalsFor += goalsFor;
            this.goalsAgainst += goalsAgainst;
            int outcome = Integer.compare(goalsFor, goalsAgainst);
            if (outcome > 0) {
                won++;
            } else if (outcome < 0) {
                lost++;
            } else {
                drawn++;
            }
        }

        private GroupTeamStanding toStanding() {
            int goalDifference = goalsFor - goalsAgainst;
            int points = won * 3 + drawn;
            return new GroupTeamStanding(team, played, won, drawn, lost, goalsFor, goalsAgainst, goalDifference, points, 0);
        }
    }
}
