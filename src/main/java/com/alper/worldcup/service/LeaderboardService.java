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
        Map<String, Long> totals = buildPointTotals();

        List<Object[]> leaderboard = new ArrayList<>();
        for (var profile : userProfileService.getPoolProfiles()) {
            leaderboard.add(new Object[]{
                    profile.getUsername(),
                    totals.getOrDefault(profile.getUsername(), 0L)});
        }

        leaderboard.sort(Comparator
                .<Object[]>comparingLong(row -> ((Number) row[1]).longValue()).reversed()
                .thenComparing(row -> userProfileService.getDisplayName((String) row[0]),
                        String.CASE_INSENSITIVE_ORDER));
        return leaderboard;
    }

    private Map<String, Long> buildPointTotals() {
        Map<String, Long> totals = new HashMap<>();

        for (Object[] row : predictionRepository.findLeaderboardTotals()) {
            mergePoolTotal(totals, row);
        }
        for (Object[] row : groupStandingPredictionRepository.findLeaderboardTotals()) {
            mergePoolTotal(totals, row);
        }
        for (Object[] row : finalPredictionRepository.findLeaderboardTotals()) {
            mergePoolTotal(totals, row);
        }

        return totals;
    }

    private void mergePoolTotal(Map<String, Long> totals, Object[] row) {
        String username = (String) row[0];
        if (poolMemberRegistry.isMember(username)) {
            totals.merge(username, ((Number) row[1]).longValue(), Long::sum);
        }
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
