package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
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
    private final FinalPredictionRepository finalPredictionRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;

    public LeaderboardService(PredictionRepository predictionRepository,
                              GroupStandingPredictionRepository groupStandingPredictionRepository,
                              FinalPredictionRepository finalPredictionRepository,
                              UserProfileService userProfileService,
                              PoolMemberRegistry poolMemberRegistry) {
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
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
        for (Object[] row : finalPredictionRepository.findLeaderboardTotals()) {
            totals.merge((String) row[0], ((Number) row[1]).longValue(), Long::sum);
        }

        List<Object[]> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totals.entrySet()) {
            if (poolMemberRegistry.isMember(entry.getKey())) {
                leaderboard.add(new Object[]{entry.getKey(), entry.getValue()});
            }
        }

        leaderboard.sort(Comparator.<Object[]>comparingLong(row -> ((Number) row[1]).longValue()).reversed());
        return leaderboard;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardTickerEntry> getTickerEntries() {
        Map<String, Long> totals = new HashMap<>();
        for (Object[] row : getLeaderboard()) {
            totals.put((String) row[0], ((Number) row[1]).longValue());
        }

        List<LeaderboardTickerEntry> entries = userProfileService.getPoolProfiles().stream()
                .map(profile -> new LeaderboardTickerEntry(
                        0,
                        profile.getUsername(),
                        userProfileService.getDisplayName(profile.getUsername()),
                        totals.getOrDefault(profile.getUsername(), 0L)))
                .sorted(Comparator
                        .comparingLong(LeaderboardTickerEntry::points).reversed()
                        .thenComparing(LeaderboardTickerEntry::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<LeaderboardTickerEntry> ranked = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardTickerEntry entry = entries.get(i);
            ranked.add(new LeaderboardTickerEntry(i + 1, entry.username(), entry.displayName(), entry.points()));
        }
        return ranked;
    }
}
