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
    private final UserMatchStatsService userMatchStatsService;
    private final FinalPredictionService finalPredictionService;

    public LeaderboardService(PredictionRepository predictionRepository,
                              GroupStandingPredictionRepository groupStandingPredictionRepository,
                              FinalPredictionRepository finalPredictionRepository,
                              UserProfileService userProfileService,
                              PoolMemberRegistry poolMemberRegistry,
                              UserMatchStatsService userMatchStatsService,
                              FinalPredictionService finalPredictionService) {
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
        this.userMatchStatsService = userMatchStatsService;
        this.finalPredictionService = finalPredictionService;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardRowView> getLeaderboardRows() {
        Map<String, Long> matchPoints = poolTotals(predictionRepository.findLeaderboardTotals());
        Map<String, Long> groupPoints = poolTotals(groupStandingPredictionRepository.findLeaderboardTotals());
        Map<String, Long> finalPoints = poolTotals(finalPredictionRepository.findLeaderboardTotals());
        Map<String, UserMatchStats> matchStats = userMatchStatsService.getStatsForPoolMembers();
        Map<String, Boolean> championCorrect = finalPredictionService.getChampionCorrectByUsername();

        List<LeaderboardRowView> rows = new ArrayList<>();
        for (var profile : userProfileService.getPoolProfiles()) {
            String username = profile.getUsername();
            long match = matchPoints.getOrDefault(username, 0L);
            long group = groupPoints.getOrDefault(username, 0L);
            long fin = finalPoints.getOrDefault(username, 0L);
            rows.add(new LeaderboardRowView(username, match, group, fin, match + group + fin));
        }

        rows.sort(leaderboardComparator(matchStats, championCorrect));
        return rows;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getLeaderboard() {
        return getLeaderboardRows().stream()
                .map(row -> new Object[]{row.username(), row.totalPoints()})
                .toList();
    }

    private Comparator<LeaderboardRowView> leaderboardComparator(Map<String, UserMatchStats> matchStats,
                                                               Map<String, Boolean> championCorrect) {
        return Comparator
                .comparingLong(LeaderboardRowView::totalPoints).reversed()
                .thenComparing(row -> successRateForSort(matchStats.get(row.username())), Comparator.reverseOrder())
                .thenComparing(row -> championCorrect.getOrDefault(row.username(), false), Comparator.reverseOrder())
                .thenComparing(row -> userProfileService.getDisplayName(row.username()),
                        String.CASE_INSENSITIVE_ORDER);
    }

    static int successRateForSort(UserMatchStats stats) {
        if (stats == null) {
            return -1;
        }
        Integer rate = stats.successRatePercent();
        return rate != null ? rate : -1;
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
        List<LeaderboardRowView> rows = getLeaderboardRows();
        List<LeaderboardTickerEntry> ranked = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardRowView row = rows.get(i);
            ranked.add(new LeaderboardTickerEntry(
                    i + 1,
                    row.username(),
                    userProfileService.getDisplayName(row.username()),
                    row.totalPoints()));
        }
        return ranked;
    }
}
