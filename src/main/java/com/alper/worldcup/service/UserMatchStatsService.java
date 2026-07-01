package com.alper.worldcup.service;

import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMatchStatsService {

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

    private static final class StatsAccumulator {
        private int exactScores;
        private int correctOutcomes;
        private int missedGames;

        private UserMatchStats toStats() {
            return new UserMatchStats(exactScores, correctOutcomes, missedGames);
        }
    }
}
