package com.alper.worldcup.service;

import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.PredictionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaderboardService {

    private final PredictionRepository predictionRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;

    public LeaderboardService(PredictionRepository predictionRepository,
                              GroupStandingPredictionRepository groupStandingPredictionRepository) {
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getLeaderboard() {
        Map<String, Long> totals = new HashMap<>();

        for (Object[] row : predictionRepository.findLeaderboardTotals()) {
            totals.merge((String) row[0], ((Number) row[1]).longValue(), Long::sum);
        }
        for (Object[] row : groupStandingPredictionRepository.findLeaderboardTotals()) {
            totals.merge((String) row[0], ((Number) row[1]).longValue(), Long::sum);
        }

        List<Object[]> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totals.entrySet()) {
            leaderboard.add(new Object[]{entry.getKey(), entry.getValue()});
        }

        leaderboard.sort(Comparator.<Object[]>comparingLong(row -> ((Number) row[1]).longValue()).reversed());
        return leaderboard;
    }
}
