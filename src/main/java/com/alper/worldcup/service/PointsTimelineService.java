package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Prediction;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsTimelineService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final String[] LINE_COLORS = {
            "#c8102e", "#1d3557", "#2a9d8f", "#e9c46a", "#f4a261", "#6a4c93", "#118ab2", "#ef476f"
    };

    private final PredictionRepository predictionRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final MatchRepository matchRepository;
    private final PoolMemberRegistry poolMemberRegistry;
    private final UserProfileService userProfileService;

    public PointsTimelineService(PredictionRepository predictionRepository,
                                 GroupStandingPredictionRepository groupStandingPredictionRepository,
                                 FinalPredictionRepository finalPredictionRepository,
                                 MatchRepository matchRepository,
                                 PoolMemberRegistry poolMemberRegistry,
                                 UserProfileService userProfileService) {
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.matchRepository = matchRepository;
        this.poolMemberRegistry = poolMemberRegistry;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public LeaderboardTimelineChart buildMatchPointsTimeline(ZoneId zoneId, List<String> playerOrder) {
        return buildMatchPointsTimeline(zoneId, playerOrder, LocalDate.now(zoneId));
    }

    LeaderboardTimelineChart buildMatchPointsTimeline(ZoneId zoneId,
                                                      List<String> playerOrder,
                                                      LocalDate throughDay) {
        LocalDate start = tournamentStartDay(zoneId);
        LocalDate end = throughDay;
        if (end.isBefore(start)) {
            end = start;
        }

        List<LocalDate> days = daysInclusive(start, end);
        Map<String, Map<LocalDate, Double>> pointsEarnedByDay = pointsEarnedByDay(zoneId);

        boolean hasScoredPoints = pointsEarnedByDay.values().stream()
                .anyMatch(dayPoints -> dayPoints.values().stream().anyMatch(points -> points > 0));

        List<String> labels = days.stream().map(day -> day.format(DAY_LABEL)).toList();
        List<LeaderboardTimelineSeries> series = new ArrayList<>();
        int colorIndex = 0;
        for (String username : playerOrder) {
            if (!poolMemberRegistry.isMember(username)) {
                continue;
            }
            Map<LocalDate, Double> earnedByDay = pointsEarnedByDay.getOrDefault(username, Map.of());
            double cumulative = 0;
            List<Double> cumulativePoints = new ArrayList<>(days.size());
            for (LocalDate day : days) {
                cumulative += earnedByDay.getOrDefault(day, 0.0);
                cumulativePoints.add(cumulative);
            }
            series.add(new LeaderboardTimelineSeries(
                    username,
                    userProfileService.getDisplayName(username),
                    cumulativePoints,
                    LINE_COLORS[colorIndex % LINE_COLORS.length]));
            colorIndex++;
        }

        return new LeaderboardTimelineChart(labels, series, hasScoredPoints);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> cumulativeTotalsThrough(LocalDate throughDay, ZoneId zoneId) {
        Map<String, Map<LocalDate, Double>> earnedByDay = pointsEarnedByDay(zoneId);
        Map<String, Double> totals = new HashMap<>();
        for (var member : poolMemberRegistry.getMembers()) {
            String username = member.username();
            double cumulative = 0;
            for (Map.Entry<LocalDate, Double> entry : earnedByDay.getOrDefault(username, Map.of()).entrySet()) {
                if (!entry.getKey().isAfter(throughDay)) {
                    cumulative += entry.getValue();
                }
            }
            totals.put(username, cumulative);
        }
        return totals;
    }

    LocalDate tournamentStartDay(ZoneId zoneId) {
        return matchRepository.findTournamentStartKickoff()
                .map(instant -> instant.atZone(zoneId).toLocalDate())
                .orElse(LocalDate.now(zoneId));
    }

    private Map<String, Map<LocalDate, Double>> pointsEarnedByDay(ZoneId zoneId) {
        Map<String, Map<LocalDate, Double>> pointsByUserByDay = new HashMap<>();
        addMatchPoints(pointsByUserByDay, zoneId);
        addGroupStandingPoints(pointsByUserByDay, zoneId);
        addFinalPoints(pointsByUserByDay, zoneId);
        return pointsByUserByDay;
    }

    private void addMatchPoints(Map<String, Map<LocalDate, Double>> pointsByUserByDay, ZoneId zoneId) {
        for (Prediction prediction : predictionRepository.findAllScoredWithMatch()) {
            if (!poolMemberRegistry.isMember(prediction.getUsername())) {
                continue;
            }
            if (prediction.getPoints() == null) {
                continue;
            }
            LocalDate matchDay = prediction.getMatch().getKickoffUtc().atZone(zoneId).toLocalDate();
            mergePoints(pointsByUserByDay, prediction.getUsername(), matchDay, prediction.getPoints());
        }
    }

    private void addGroupStandingPoints(Map<String, Map<LocalDate, Double>> pointsByUserByDay, ZoneId zoneId) {
        Map<String, LocalDate> groupLastMatchDay = groupLastMatchDays(zoneId);
        for (GroupStandingPrediction prediction : groupStandingPredictionRepository.findAllScored()) {
            if (!poolMemberRegistry.isMember(prediction.getUsername())) {
                continue;
            }
            LocalDate groupDay = groupLastMatchDay.get(prediction.getGroupName());
            if (groupDay == null) {
                continue;
            }
            if (prediction.getPoints() == null) {
                continue;
            }
            mergePoints(pointsByUserByDay, prediction.getUsername(), groupDay, prediction.getPoints().doubleValue());
        }
    }

    private void addFinalPoints(Map<String, Map<LocalDate, Double>> pointsByUserByDay, ZoneId zoneId) {
        LocalDate finalDay = matchRepository.findFinalMatchKickoff()
                .map(instant -> instant.atZone(zoneId).toLocalDate())
                .orElse(null);
        if (finalDay == null) {
            return;
        }
        for (FinalPrediction prediction : finalPredictionRepository.findAllScored()) {
            if (!poolMemberRegistry.isMember(prediction.getUsername())) {
                continue;
            }
            if (prediction.getPoints() == null) {
                continue;
            }
            mergePoints(pointsByUserByDay, prediction.getUsername(), finalDay, prediction.getPoints().doubleValue());
        }
    }

    private Map<String, LocalDate> groupLastMatchDays(ZoneId zoneId) {
        Map<String, LocalDate> groupLastMatchDay = new HashMap<>();
        for (String groupName : matchRepository.findDistinctGroupStageGroupNames()) {
            matchRepository.findLatestGroupMatchKickoff(groupName)
                    .map(instant -> instant.atZone(zoneId).toLocalDate())
                    .ifPresent(day -> groupLastMatchDay.put(groupName, day));
        }
        return groupLastMatchDay;
    }

    private static void mergePoints(Map<String, Map<LocalDate, Double>> pointsByUserByDay,
                                    String username,
                                    LocalDate day,
                                    Double points) {
        if (points == null || points <= 0) {
            return;
        }
        pointsByUserByDay
                .computeIfAbsent(username, ignored -> new HashMap<>())
                .merge(day, points, Double::sum);
    }

    static List<LocalDate> daysInclusive(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            days.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return days;
    }
}
