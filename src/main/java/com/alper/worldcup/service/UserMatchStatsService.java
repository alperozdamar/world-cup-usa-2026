package com.alper.worldcup.service;

import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMatchStatsService {

    static final int MIN_TEAM_SAMPLE = 3;

    private final PredictionRepository predictionRepository;
    private final PoolMemberRegistry poolMemberRegistry;
    private final PointsServiceImpl pointsService;

    public UserMatchStatsService(PredictionRepository predictionRepository,
                                 PoolMemberRegistry poolMemberRegistry,
                                 PointsServiceImpl pointsService) {
        this.predictionRepository = predictionRepository;
        this.poolMemberRegistry = poolMemberRegistry;
        this.pointsService = pointsService;
    }

    @Transactional(readOnly = true)
    public UserMatchStats getStats(String username) {
        StatsAccumulator accumulator = new StatsAccumulator();
        for (Prediction prediction : predictionRepository.findScoredByUsernameWithMatch(username)) {
            accumulate(accumulator, prediction, prediction.getMatch());
        }
        return accumulator.toStats();
    }

    @Transactional(readOnly = true)
    public Map<String, UserMatchStats> getStatsForPoolMembers() {
        Map<String, StatsAccumulator> accumulators = new HashMap<>();
        for (PoolMember member : poolMemberRegistry.getMembers()) {
            accumulators.put(member.username(), new StatsAccumulator());
        }

        for (Prediction prediction : predictionRepository.findAllScoredWithMatch()) {
            StatsAccumulator accumulator = accumulators.get(prediction.getUsername());
            if (accumulator != null) {
                accumulate(accumulator, prediction, prediction.getMatch());
            }
        }

        Map<String, UserMatchStats> stats = HashMap.newHashMap(accumulators.size());
        for (Map.Entry<String, StatsAccumulator> entry : accumulators.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().toStats());
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, UserMatchStats> getStatsForPoolMembersThrough(LocalDate throughDay, ZoneId zoneId) {
        Map<String, StatsAccumulator> accumulators = new HashMap<>();
        for (PoolMember member : poolMemberRegistry.getMembers()) {
            accumulators.put(member.username(), new StatsAccumulator());
        }

        for (Prediction prediction : predictionRepository.findAllScoredWithMatch()) {
            StatsAccumulator accumulator = accumulators.get(prediction.getUsername());
            if (accumulator == null) {
                continue;
            }
            Match match = prediction.getMatch();
            LocalDate matchDay = match.getKickoffUtc().atZone(zoneId).toLocalDate();
            if (matchDay.isAfter(throughDay)) {
                continue;
            }
            accumulate(accumulator, prediction, match);
        }

        Map<String, UserMatchStats> stats = HashMap.newHashMap(accumulators.size());
        for (Map.Entry<String, StatsAccumulator> entry : accumulators.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().toStats());
        }
        return stats;
    }

    private void accumulate(StatsAccumulator accumulator, Prediction prediction, Match match) {
        if (isExactScore(prediction, match)) {
            accumulator.exactScores++;
        }
        if (hasCorrectOutcome(prediction, match)) {
            accumulator.correctOutcomes++;
        } else {
            accumulator.missedGames++;
        }
        if (prediction.getPoints() != null) {
            addTeamPoints(accumulator, match.getHomeTeam(), prediction.getPoints());
            addTeamPoints(accumulator, match.getAwayTeam(), prediction.getPoints());
        }
    }

    private static void addTeamPoints(StatsAccumulator accumulator, Team team, int points) {
        if (team == null || team.getName() == null || team.getName().isBlank()) {
            return;
        }
        accumulator.teamTotals
                .computeIfAbsent(team.getName(), ignored -> new long[2])
                [0] += points;
        accumulator.teamTotals.get(team.getName())[1]++;
    }

    private boolean isExactScore(Prediction prediction, Match match) {
        return prediction.getHomeScoreGuess().equals(match.getHomeScoreActual())
                && prediction.getAwayScoreGuess().equals(match.getAwayScoreActual());
    }

    private boolean hasCorrectOutcome(Prediction prediction, Match match) {
        return pointsService.calculateBasePoints(
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                match.getHomeScoreActual(),
                match.getAwayScoreActual()) > 0;
    }

    static TeamAffinity resolveLoveTeam(Map<String, long[]> teamTotals) {
        return resolveExtreme(teamTotals, true);
    }

    static TeamAffinity resolveHateTeam(Map<String, long[]> teamTotals) {
        return resolveExtreme(teamTotals, false);
    }

    private static TeamAffinity resolveExtreme(Map<String, long[]> teamTotals, boolean highest) {
        TeamAffinity best = null;
        for (Map.Entry<String, long[]> entry : teamTotals.entrySet()) {
            long sum = entry.getValue()[0];
            long count = entry.getValue()[1];
            if (count < MIN_TEAM_SAMPLE) {
                continue;
            }
            double average = sum / (double) count;
            TeamAffinity candidate = new TeamAffinity(entry.getKey(), average, (int) count);
            if (best == null) {
                best = candidate;
                continue;
            }
            int cmp = Double.compare(candidate.averagePoints(), best.averagePoints());
            if (highest ? cmp > 0 : cmp < 0) {
                best = candidate;
            } else if (cmp == 0 && candidate.teamName().compareToIgnoreCase(best.teamName()) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    private static final class StatsAccumulator {
        private int exactScores;
        private int correctOutcomes;
        private int missedGames;
        private final Map<String, long[]> teamTotals = new HashMap<>();

        private UserMatchStats toStats() {
            TeamAffinity love = resolveLoveTeam(teamTotals);
            TeamAffinity hate = resolveHateTeam(teamTotals);
            if (love != null && hate != null && love.teamName().equals(hate.teamName())) {
                hate = null;
            }
            return new UserMatchStats(exactScores, correctOutcomes, missedGames, love, hate);
        }
    }
}
