package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionAuditRepository;
import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.FinalResultRepository;
import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPredictionAudit;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.GroupStandingPredictionAudit;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.PredictionAudit;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BirdWatchService {

    static final int MIN_MATCH_SAMPLE = 3;
    static final int MAX_LEADERS = 3;

    private final PredictionAuditRepository predictionAuditRepository;
    private final GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionAuditRepository finalPredictionAuditRepository;
    private final PredictionRepository predictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final FinalResultRepository finalResultRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;
    private final PointsServiceImpl pointsService;

    public BirdWatchService(PredictionAuditRepository predictionAuditRepository,
                            GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository,
                            GroupStandingPredictionRepository groupStandingPredictionRepository,
                            FinalPredictionAuditRepository finalPredictionAuditRepository,
                            PredictionRepository predictionRepository,
                            FinalPredictionRepository finalPredictionRepository,
                            FinalResultRepository finalResultRepository,
                            UserProfileService userProfileService,
                            PoolMemberRegistry poolMemberRegistry,
                            PointsServiceImpl pointsService) {
        this.predictionAuditRepository = predictionAuditRepository;
        this.groupStandingPredictionAuditRepository = groupStandingPredictionAuditRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionAuditRepository = finalPredictionAuditRepository;
        this.predictionRepository = predictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.finalResultRepository = finalResultRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
        this.pointsService = pointsService;
    }

    @Transactional(readOnly = true)
    public List<BirdWatchCategory> buildCategories() {
        List<PredictionAudit> matchAudits = predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc();
        List<GroupStandingPredictionAudit> groupAudits =
                groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc();
        List<FinalPredictionAudit> finalAudits = finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc();
        List<Prediction> scoredPredictions = predictionRepository.findAllScoredWithMatch();

        Map<String, Long> updateCounts = countUpdates(matchAudits, groupAudits, finalAudits);
        Map<String, Duration> avgLeadTime = averageFirstPickLeadTime(matchAudits);

        List<BirdWatchCategory> categories = new ArrayList<>();
        categories.add(buildEarlyBirds(avgLeadTime));
        categories.add(buildLastMinuteLarks(avgLeadTime));
        categories.add(buildFlipFlopFinches(updateCounts, matchAudits));
        categories.add(buildSetAndForgetStorks(updateCounts, matchAudits));
        categories.add(buildBullseyeBirds(scoredPredictions));
        categories.add(buildLuckyDucks(scoredPredictions));
        categories.add(buildSoCloseSeabirds(scoredPredictions));
        categories.add(buildGroupSageGrouse());
        categories.add(buildKnockoutKestrels());
        categories.add(buildCrystalBallCondors());
        return categories;
    }

    private BirdWatchCategory buildEarlyBirds(Map<String, Duration> avgLeadTime) {
        String explanation = "Players who submit their first match pick earliest on average — "
                + "measured as time before kickoff. Requires at least " + MIN_MATCH_SAMPLE
                + " match picks. Higher is earlier.";
        return BirdWatchCategory.ready(
                "early-birds",
                "Early Birds",
                explanation,
                topByDuration(avgLeadTime, Comparator.reverseOrder(), this::formatLeadTime));
    }

    private BirdWatchCategory buildLastMinuteLarks(Map<String, Duration> avgLeadTime) {
        String explanation = "Players who leave their first match pick until the last moment on average — "
                + "closest to kickoff before the lock. Requires at least " + MIN_MATCH_SAMPLE
                + " match picks. Lower average lead time means a later bird.";
        return BirdWatchCategory.ready(
                "last-minute-larks",
                "Last-Minute Larks",
                explanation,
                topByDuration(avgLeadTime, Comparator.naturalOrder(), this::formatLeadTime));
    }

    private BirdWatchCategory buildFlipFlopFinches(Map<String, Long> updateCounts,
                                                   List<PredictionAudit> matchAudits) {
        String explanation = "Players who change their mind the most — total edits across match, group, "
                + "and final predictions. Requires at least " + MIN_MATCH_SAMPLE + " match picks.";
        Map<String, Long> eligible = filterByMatchSample(updateCounts, matchAudits);
        return BirdWatchCategory.ready(
                "flip-flop-finches",
                "Flip-Flop Finches",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(), count -> count + " edit" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildSetAndForgetStorks(Map<String, Long> updateCounts,
                                                      List<PredictionAudit> matchAudits) {
        String explanation = "The steadiest pickers — fewest prediction edits after the first save. "
                + "Requires at least " + MIN_MATCH_SAMPLE + " match picks. Fewer edits is better.";
        Map<String, Long> eligible = filterByMatchSample(updateCounts, matchAudits);
        return BirdWatchCategory.ready(
                "set-and-forget-storks",
                "Set-and-Forget Storks",
                explanation,
                topByCount(eligible, Comparator.naturalOrder(), count -> count + " edit" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildBullseyeBirds(List<Prediction> scoredPredictions) {
        String explanation = "Most exact score hits — predictions that matched the final score line-for-line "
                + "(5 base points before knockout multipliers). Requires at least " + MIN_MATCH_SAMPLE
                + " scored match predictions.";
        Map<String, Long> exactCounts = new HashMap<>();
        Map<String, Long> scoredCounts = new HashMap<>();
        for (Prediction prediction : scoredPredictions) {
            Match match = prediction.getMatch();
            scoredCounts.merge(prediction.getUsername(), 1L, Long::sum);
            if (isExactScore(prediction, match)) {
                exactCounts.merge(prediction.getUsername(), 1L, Long::sum);
            }
        }
        Map<String, Long> eligible = filterByScoredSample(exactCounts, scoredCounts);
        return BirdWatchCategory.ready(
                "bullseye-birds",
                "Bullseye Birds",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(),
                        count -> count + " exact score" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildLuckyDucks(List<Prediction> scoredPredictions) {
        String explanation = "Most \"right idea, wrong digits\" picks — correct win/draw/loss but wrong score, "
                + "without the goal-difference bonus (2 base points). Requires at least " + MIN_MATCH_SAMPLE
                + " scored match predictions.";
        Map<String, Long> luckyCounts = new HashMap<>();
        Map<String, Long> scoredCounts = new HashMap<>();
        for (Prediction prediction : scoredPredictions) {
            Match match = prediction.getMatch();
            scoredCounts.merge(prediction.getUsername(), 1L, Long::sum);
            if (isLuckyTwoPointer(prediction, match)) {
                luckyCounts.merge(prediction.getUsername(), 1L, Long::sum);
            }
        }
        Map<String, Long> eligible = filterByScoredSample(luckyCounts, scoredCounts);
        return BirdWatchCategory.ready(
                "lucky-ducks",
                "Lucky Ducks",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(),
                        count -> count + " lucky hit" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildSoCloseSeabirds(List<Prediction> scoredPredictions) {
        String explanation = "The unluckiest near-misses — most scored predictions that were exactly one goal "
                + "off in total (e.g. predicted 2–1, actual 2–0). Requires at least " + MIN_MATCH_SAMPLE
                + " scored match predictions.";
        Map<String, Long> nearMissCounts = new HashMap<>();
        Map<String, Long> scoredCounts = new HashMap<>();
        for (Prediction prediction : scoredPredictions) {
            Match match = prediction.getMatch();
            scoredCounts.merge(prediction.getUsername(), 1L, Long::sum);
            if (isOneGoalOff(prediction, match)) {
                nearMissCounts.merge(prediction.getUsername(), 1L, Long::sum);
            }
        }
        Map<String, Long> eligible = filterByScoredSample(nearMissCounts, scoredCounts);
        return BirdWatchCategory.ready(
                "so-close-seabirds",
                "So-Close Seabirds",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(),
                        count -> count + " near miss" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildGroupSageGrouse() {
        String explanation = "Most points from Group 1st & 2nd predictions — who read the groups best "
                + "after official results are entered (up to 5 points per group).";
        Map<String, Long> groupPoints = poolTotals(groupStandingPredictionRepository.findLeaderboardTotals());
        if (groupPoints.isEmpty()) {
            return BirdWatchCategory.pending(
                    "group-sage-grouse",
                    "Group Sage Grouse",
                    explanation,
                    "Waiting for official group results — check back as admins confirm each group.");
        }
        return BirdWatchCategory.ready(
                "group-sage-grouse",
                "Group Sage Grouse",
                explanation,
                topByCount(groupPoints, Comparator.reverseOrder(), this::formatPoints));
    }

    private BirdWatchCategory buildKnockoutKestrels() {
        String explanation = "Most knockout match points — 90′ score picks with round multipliers "
                + "(R32 through Final). Updates after each scored knockout game.";
        Map<String, Long> knockoutPoints = poolTotals(predictionRepository.findKnockoutPointsTotalsByUser());
        return BirdWatchCategory.ready(
                "knockout-kestrels",
                "Knockout Kestrels",
                explanation,
                topByCount(knockoutPoints, Comparator.reverseOrder(), this::formatPoints));
    }

    private BirdWatchCategory buildCrystalBallCondors() {
        String explanation = "Called the tournament champion correctly in the Final Prediction. "
                + "Revealed only after the admin enters the official champion.";
        Optional<FinalResult> finalResult = finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID);
        if (finalResult.isEmpty()) {
            return BirdWatchCategory.pending(
                    "crystal-ball-condors",
                    "Crystal Ball Condors",
                    explanation,
                    "Waiting for the official champion — check back after the final.");
        }

        String championName = finalResult.get().getChampionTeam().getName();
        Integer championId = finalResult.get().getChampionTeam().getId();
        List<BirdWatchLeader> leaders = finalPredictionRepository.findAllWithTeamsOrderByUsername().stream()
                .filter(prediction -> championId.equals(prediction.getChampionTeam().getId()))
                .sorted(Comparator.comparing(prediction -> userProfileService.getDisplayName(prediction.getUsername()),
                        String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LEADERS)
                .map(prediction -> toLeader(
                        prediction.getUsername(),
                        "Called it: " + championName))
                .toList();

        return BirdWatchCategory.ready("crystal-ball-condors", "Crystal Ball Condors", explanation, leaders);
    }

    private static Map<String, Long> poolTotals(List<Object[]> rows) {
        Map<String, Long> totals = new HashMap<>();
        for (Object[] row : rows) {
            totals.put((String) row[0], ((Number) row[1]).longValue());
        }
        return totals;
    }

    private String formatPoints(long points) {
        return points + " pt" + pluralSuffix(points);
    }

    Map<String, Duration> averageFirstPickLeadTime(List<PredictionAudit> matchAudits) {
        Map<Integer, Instant> kickoffs = new HashMap<>();
        Map<String, Map<Integer, Instant>> firstPickInstant = new HashMap<>();
        for (PredictionAudit audit : matchAudits) {
            kickoffs.putIfAbsent(audit.getMatch().getId(), audit.getMatch().getKickoffUtc());
            firstPickInstant
                    .computeIfAbsent(audit.getUsername(), username -> new HashMap<>())
                    .merge(audit.getMatch().getId(), audit.getRecordedAt(),
                            (existing, candidate) -> candidate.isBefore(existing) ? candidate : existing);
        }

        Map<String, Duration> averages = new HashMap<>();
        for (Map.Entry<String, Map<Integer, Instant>> entry : firstPickInstant.entrySet()) {
            if (entry.getValue().size() < MIN_MATCH_SAMPLE) {
                continue;
            }
            Duration total = Duration.ZERO;
            int counted = 0;
            for (Map.Entry<Integer, Instant> pick : entry.getValue().entrySet()) {
                Instant kickoff = kickoffs.get(pick.getKey());
                if (kickoff == null) {
                    continue;
                }
                Duration lead = Duration.between(pick.getValue(), kickoff);
                if (!lead.isNegative()) {
                    total = total.plus(lead);
                    counted++;
                }
            }
            if (counted >= MIN_MATCH_SAMPLE) {
                averages.put(entry.getKey(), total.dividedBy(counted));
            }
        }
        return averages;
    }

    Map<String, Long> countUpdates(List<PredictionAudit> matchAudits,
                                   List<GroupStandingPredictionAudit> groupAudits,
                                   List<FinalPredictionAudit> finalAudits) {
        Map<String, Long> counts = new HashMap<>();
        for (PredictionAudit audit : matchAudits) {
            if (audit.getAction() == PredictionAuditAction.UPDATED) {
                counts.merge(audit.getUsername(), 1L, Long::sum);
            }
        }
        for (GroupStandingPredictionAudit audit : groupAudits) {
            if (audit.getAction() == PredictionAuditAction.UPDATED) {
                counts.merge(audit.getUsername(), 1L, Long::sum);
            }
        }
        for (FinalPredictionAudit audit : finalAudits) {
            if (audit.getAction() == PredictionAuditAction.UPDATED) {
                counts.merge(audit.getUsername(), 1L, Long::sum);
            }
        }
        return counts;
    }

    Map<String, Long> filterByMatchSample(Map<String, Long> values, List<PredictionAudit> matchAudits) {
        Map<String, Long> matchCounts = matchCountsByUser(matchAudits);
        Map<String, Long> eligible = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : matchCounts.entrySet()) {
            if (entry.getValue() >= MIN_MATCH_SAMPLE) {
                eligible.put(entry.getKey(), values.getOrDefault(entry.getKey(), 0L));
            }
        }
        return eligible;
    }

    Map<String, Long> matchCountsByUser(List<PredictionAudit> matchAudits) {
        Map<String, java.util.Set<Integer>> matchesByUser = new HashMap<>();
        for (PredictionAudit audit : matchAudits) {
            matchesByUser
                    .computeIfAbsent(audit.getUsername(), username -> new java.util.HashSet<>())
                    .add(audit.getMatch().getId());
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Map.Entry<String, java.util.Set<Integer>> entry : matchesByUser.entrySet()) {
            counts.put(entry.getKey(), (long) entry.getValue().size());
        }
        return counts;
    }

    Map<String, Long> filterByScoredSample(Map<String, Long> values, Map<String, Long> scoredCounts) {
        Map<String, Long> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : scoredCounts.entrySet()) {
            if (entry.getValue() >= MIN_MATCH_SAMPLE) {
                filtered.put(entry.getKey(), values.getOrDefault(entry.getKey(), 0L));
            }
        }
        return filtered;
    }

    private List<BirdWatchLeader> topByDuration(Map<String, Duration> values,
                                                Comparator<Duration> comparator,
                                                java.util.function.Function<Duration, String> formatter) {
        return values.entrySet().stream()
                .filter(entry -> poolMemberRegistry.isMember(entry.getKey()))
                .sorted(Map.Entry.<String, Duration>comparingByValue(comparator)
                        .thenComparing(entry -> userProfileService.getDisplayName(entry.getKey()),
                                String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LEADERS)
                .map(entry -> toLeader(entry.getKey(), formatter.apply(entry.getValue())))
                .toList();
    }

    private List<BirdWatchLeader> topByCount(Map<String, Long> values,
                                             Comparator<Long> comparator,
                                             java.util.function.Function<Long, String> formatter) {
        return values.entrySet().stream()
                .filter(entry -> poolMemberRegistry.isMember(entry.getKey()))
                .sorted(Map.Entry.<String, Long>comparingByValue(comparator)
                        .thenComparing(entry -> userProfileService.getDisplayName(entry.getKey()),
                                String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LEADERS)
                .map(entry -> toLeader(entry.getKey(), formatter.apply(entry.getValue())))
                .toList();
    }

    private BirdWatchLeader toLeader(String username, String statLabel) {
        return new BirdWatchLeader(username, userProfileService.getDisplayName(username), statLabel);
    }

    boolean isExactScore(Prediction prediction, Match match) {
        return prediction.getHomeScoreGuess().equals(match.getHomeScoreActual())
                && prediction.getAwayScoreGuess().equals(match.getAwayScoreActual());
    }

    boolean isLuckyTwoPointer(Prediction prediction, Match match) {
        return pointsService.calculateBasePoints(
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                match.getHomeScoreActual(),
                match.getAwayScoreActual()) == 2;
    }

    boolean isOneGoalOff(Prediction prediction, Match match) {
        int homeDiff = Math.abs(prediction.getHomeScoreGuess() - match.getHomeScoreActual());
        int awayDiff = Math.abs(prediction.getAwayScoreGuess() - match.getAwayScoreActual());
        return homeDiff + awayDiff == 1;
    }

    String formatLeadTime(Duration duration) {
        long totalMinutes = Math.max(0, duration.toMinutes());
        if (totalMinutes >= 24 * 60) {
            long days = totalMinutes / (24 * 60);
            long hours = (totalMinutes % (24 * 60)) / 60;
            if (hours == 0) {
                return days + " day" + pluralSuffix(days) + " before kickoff on avg";
            }
            return days + "d " + hours + "h before kickoff on avg";
        }
        if (totalMinutes >= 60) {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            if (minutes == 0) {
                return hours + " hour" + pluralSuffix(hours) + " before kickoff on avg";
            }
            return hours + "h " + minutes + "m before kickoff on avg";
        }
        return totalMinutes + " min before kickoff on avg";
    }

    private static String pluralSuffix(long count) {
        return count == 1 ? "" : "s";
    }
}
