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
    public List<LeaderboardRowView> getLeaderboardRows() {
        Map<String, Long> matchPoints = poolTotals(predictionRepository.findLeaderboardTotals());
        Map<String, Long> groupPoints = poolTotals(groupStandingPredictionRepository.findLeaderboardTotals());
        Map<String, Long> finalPoints = poolTotals(finalPredictionRepository.findLeaderboardTotals());

        List<LeaderboardRowView> rows = new ArrayList<>();
        for (var profile : userProfileService.getPoolProfiles()) {
            String username = profile.getUsername();
            long match = matchPoints.getOrDefault(username, 0L);
            long group = groupPoints.getOrDefault(username, 0L);
            long fin = finalPoints.getOrDefault(username, 0L);
            rows.add(new LeaderboardRowView(username, match, group, fin, match + group + fin));
        }

        rows.sort(Comparator
                .comparingLong(LeaderboardRowView::totalPoints).reversed()
                .thenComparing(row -> userProfileService.getDisplayName(row.username()),
                        String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getLeaderboard() {
        return getLeaderboardRows().stream()
                .map(row -> new Object[]{row.username(), row.totalPoints()})
                .toList();
    }

    private Map<String, Long> poolTotals(List<Object[]> totals) {
        Map<String, Long> pointsByUser = new HashMap<>();
        for (Object[] row : totals) {
            mergePoolTotal(pointsByUser, row);
        }
        return pointsByUser;
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
