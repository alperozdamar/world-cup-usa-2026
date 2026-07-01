package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Prediction;
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
    private final MatchRepository matchRepository;
    private final PoolMemberRegistry poolMemberRegistry;
    private final UserProfileService userProfileService;

    public PointsTimelineService(PredictionRepository predictionRepository,
                                 MatchRepository matchRepository,
                                 PoolMemberRegistry poolMemberRegistry,
                                 UserProfileService userProfileService) {
        this.predictionRepository = predictionRepository;
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
        Map<String, Map<LocalDate, Long>> pointsEarnedByDay = pointsEarnedByDay(zoneId);

        boolean hasScoredPoints = pointsEarnedByDay.values().stream()
                .anyMatch(dayPoints -> dayPoints.values().stream().anyMatch(points -> points > 0));

        List<String> labels = days.stream().map(day -> day.format(DAY_LABEL)).toList();
        List<LeaderboardTimelineSeries> series = new ArrayList<>();
        int colorIndex = 0;
        for (String username : playerOrder) {
            if (!poolMemberRegistry.isMember(username)) {
                continue;
            }
            Map<LocalDate, Long> earnedByDay = pointsEarnedByDay.getOrDefault(username, Map.of());
            long cumulative = 0;
            List<Long> cumulativePoints = new ArrayList<>(days.size());
            for (LocalDate day : days) {
                cumulative += earnedByDay.getOrDefault(day, 0L);
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

    private LocalDate tournamentStartDay(ZoneId zoneId) {
        return matchRepository.findTournamentStartKickoff()
                .map(instant -> instant.atZone(zoneId).toLocalDate())
                .orElse(LocalDate.now(zoneId));
    }

    private Map<String, Map<LocalDate, Long>> pointsEarnedByDay(ZoneId zoneId) {
        Map<String, Map<LocalDate, Long>> pointsByUserByDay = new HashMap<>();
        for (Prediction prediction : predictionRepository.findAllScoredWithMatch()) {
            if (!poolMemberRegistry.isMember(prediction.getUsername())) {
                continue;
            }
            if (prediction.getPoints() == null) {
                continue;
            }
            LocalDate matchDay = prediction.getMatch().getKickoffUtc().atZone(zoneId).toLocalDate();
            pointsByUserByDay
                    .computeIfAbsent(prediction.getUsername(), ignored -> new HashMap<>())
                    .merge(matchDay, prediction.getPoints().longValue(), Long::sum);
        }
        return pointsByUserByDay;
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
