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
    private final UserMatchStatsService userMatchStatsService;

    public BirdWatchService(PredictionAuditRepository predictionAuditRepository,
                            GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository,
                            GroupStandingPredictionRepository groupStandingPredictionRepository,
                            FinalPredictionAuditRepository finalPredictionAuditRepository,
                            PredictionRepository predictionRepository,
                            FinalPredictionRepository finalPredictionRepository,
                            FinalResultRepository finalResultRepository,
                            UserProfileService userProfileService,
                            PoolMemberRegistry poolMemberRegistry,
                            PointsServiceImpl pointsService,
                            UserMatchStatsService userMatchStatsService) {
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
        this.userMatchStatsService = userMatchStatsService;
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
        Map<String, Long> firstPickWins = countFirstPickWins(matchAudits);
        Map<String, Long> lastTouchWins = countLastTouchWins(matchAudits);

        List<BirdWatchCategory> categories = new ArrayList<>();
        categories.add(buildEarlyBirds(avgLeadTime));
        categories.add(buildLastMinuteLarks(avgLeadTime));
        categories.add(buildFirstPickFalcons(firstPickWins, matchAudits));
        categories.add(buildNightOwls(lastTouchWins, matchAudits));
        categories.add(buildFlipFlopFinches(updateCounts, matchAudits));
        categories.add(buildSetAndForgetStorks(updateCounts, matchAudits));
        categories.add(buildBullseyeBirds(scoredPredictions));
        categories.add(buildLuckyDucks(scoredPredictions));
        categories.add(buildSoCloseSeabirds(scoredPredictions));
        categories.add(buildMatchMagpies());
        categories.add(buildGroupSageGrouse());
        categories.add(buildKnockoutKestrels());
        categories.add(buildCrystalBallCondors());
        Map<String, UserMatchStats> matchStats = userMatchStatsService.getStatsForPoolMembers();
        categories.add(buildLoveBirds(matchStats));
        categories.add(buildTeamNemeses(matchStats));
        return categories;
    }

    private BirdWatchCategory buildLoveBirds(Map<String, UserMatchStats> matchStats) {
        String explanation = "Highest average points when a favorite team plays (both sides of the match). "
                + "Needs at least " + UserMatchStatsService.MIN_TEAM_SAMPLE
                + " scored games involving that team.";
        Map<String, TeamAffinity> loves = new HashMap<>();
        for (Map.Entry<String, UserMatchStats> entry : matchStats.entrySet()) {
            if (entry.getValue().loveTeam() != null) {
                loves.put(entry.getKey(), entry.getValue().loveTeam());
            }
        }
        if (loves.isEmpty()) {
            return BirdWatchCategory.pending(
                    "love-birds",
                    "Love Birds",
                    explanation,
                    "Need more scored matches per team (min "
                            + UserMatchStatsService.MIN_TEAM_SAMPLE + ").");
        }
        return BirdWatchCategory.ready(
                "love-birds",
                "Love Birds",
                explanation,
                topByAffinity(loves, Comparator.reverseOrder()));
    }

    private BirdWatchCategory buildTeamNemeses(Map<String, UserMatchStats> matchStats) {
        String explanation = "Lowest average points when a nemesis team plays. "
                + "Needs at least " + UserMatchStatsService.MIN_TEAM_SAMPLE
                + " scored games involving that team.";
        Map<String, TeamAffinity> hates = new HashMap<>();
        for (Map.Entry<String, UserMatchStats> entry : matchStats.entrySet()) {
            if (entry.getValue().hateTeam() != null) {
                hates.put(entry.getKey(), entry.getValue().hateTeam());
            }
        }
        if (hates.isEmpty()) {
            return BirdWatchCategory.pending(
                    "team-nemeses",
                    "Team Nemeses",
                    explanation,
                    "Need more scored matches per team (min "
                            + UserMatchStatsService.MIN_TEAM_SAMPLE + ").");
        }
        return BirdWatchCategory.ready(
                "team-nemeses",
                "Team Nemeses",
                explanation,
                topByAffinity(hates, Comparator.naturalOrder()));
    }

    private List<BirdWatchLeader> topByAffinity(Map<String, TeamAffinity> affinities,
                                                Comparator<Double> averageComparator) {
        return affinities.entrySet().stream()
                .filter(entry -> poolMemberRegistry.isMember(entry.getKey()))
                .sorted(Comparator
                        .comparing((Map.Entry<String, TeamAffinity> entry) -> entry.getValue().averagePoints(),
                                averageComparator)
                        .thenComparing(entry -> userProfileService.getDisplayName(entry.getKey()),
                                String.CASE_INSENSITIVE_ORDER))
                .map(entry -> toLeader(entry.getKey(), entry.getValue().formatLabel()))
                .toList();
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

    private BirdWatchCategory buildFirstPickFalcons(Map<String, Long> firstPickWins,
                                                    List<PredictionAudit> matchAudits) {
        String explanation = "Most times you were the first person to enter a pick for a match. "
                + "Later edits still count if you were first — only the original create matters. "
                + "Requires at least " + MIN_MATCH_SAMPLE + " match picks.";
        Map<String, Long> eligible = filterByMatchSample(firstPickWins, matchAudits);
        return BirdWatchCategory.ready(
                "first-pick-falcons",
                "First-Pick Falcons",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(),
                        count -> count + " first pick" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildNightOwls(Map<String, Long> lastTouchWins,
                                             List<PredictionAudit> matchAudits) {
        String explanation = "Most times you were the last person to save a pick for a match "
                + "(create or update). Requires at least " + MIN_MATCH_SAMPLE + " match picks.";
        Map<String, Long> eligible = filterByMatchSample(lastTouchWins, matchAudits);
        return BirdWatchCategory.ready(
                "night-owls",
                "Night Owls",
                explanation,
                topByCount(eligible, Comparator.reverseOrder(),
                        count -> count + " last touch" + pluralSuffix(count)));
    }

    private BirdWatchCategory buildFlipFlopFinches(Map<String, Long> updateCounts,
                                                   List<PredictionAudit> matchAudits) {
        String explanation = "Players who change their mind the most — total edits across match, group, "
                + "and champion & runner-up predictions. Requires at least " + MIN_MATCH_SAMPLE + " match picks.";
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

    private BirdWatchCategory buildMatchMagpies() {
        String explanation = "Most group-stage match points — the \"Match\" column on the standings table "
                + "(exact scores, correct results, and goal-difference bonuses). Updates after each scored group game.";
        Map<String, Double> matchPoints = poolPointTotals(predictionRepository.findGroupStageLeaderboardTotals());
        if (matchPoints.isEmpty()) {
            return BirdWatchCategory.pending(
                    "match-magpies",
                    "Match Magpies",
                    explanation,
                    "Waiting for the first group-stage results — check back as admins enter scores.");
        }
        return BirdWatchCategory.ready(
                "match-magpies",
                "Match Magpies",
                explanation,
                topByPoints(matchPoints, Comparator.reverseOrder()));
    }

    private BirdWatchCategory buildGroupSageGrouse() {
        String explanation = "Most points from Group 1st & 2nd predictions — who read the groups best "
                + "after official results are entered (up to 5 points per group).";
        Map<String, Double> groupPoints = poolPointTotals(groupStandingPredictionRepository.findLeaderboardTotals());
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
                topByPoints(groupPoints, Comparator.reverseOrder()));
    }

    private BirdWatchCategory buildKnockoutKestrels() {
        String explanation = "Most knockout match points — 90′ score picks with round multipliers "
                + "(R32 through Final). Fractional points kept (e.g. R16 ×1.25). "
                + "Updates after each scored knockout game.";
        Map<String, Double> knockoutPoints = poolPointTotals(predictionRepository.findKnockoutPointsTotalsByUser());
        return BirdWatchCategory.ready(
                "knockout-kestrels",
                "Knockout Kestrels",
                explanation,
                topByPoints(knockoutPoints, Comparator.reverseOrder()));
    }

    private BirdWatchCategory buildCrystalBallCondors() {
        String explanation = "Called the tournament champion correctly in Champion & Runner-up. "
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
                .filter(prediction -> poolMemberRegistry.isMember(prediction.getUsername()))
                .sorted(Comparator.comparing(prediction -> userProfileService.getDisplayName(prediction.getUsername()),
                        String.CASE_INSENSITIVE_ORDER))
                .map(prediction -> toLeader(
                        prediction.getUsername(),
                        "Called it: " + championName))
                .toList();

        return BirdWatchCategory.ready("crystal-ball-condors", "Crystal Ball Condors", explanation, leaders);
    }

    private static Map<String, Double> poolPointTotals(List<Object[]> rows) {
        Map<String, Double> totals = new HashMap<>();
        for (Object[] row : rows) {
            totals.put((String) row[0], ((Number) row[1]).doubleValue());
        }
        return totals;
    }

    private List<BirdWatchLeader> topByPoints(Map<String, Double> values, Comparator<Double> comparator) {
        return values.entrySet().stream()
                .filter(entry -> poolMemberRegistry.isMember(entry.getKey()))
                .sorted(Map.Entry.<String, Double>comparingByValue(comparator)
                        .thenComparing(entry -> userProfileService.getDisplayName(entry.getKey()),
                                String.CASE_INSENSITIVE_ORDER))
                .map(entry -> toLeader(entry.getKey(), PointsFormat.formatWithUnit(entry.getValue())))
                .toList();
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

    /**
     * Per match: username with the earliest CREATED audit wins.
     * Updates never change who was first.
     */
    Map<String, Long> countFirstPickWins(List<PredictionAudit> matchAudits) {
        Map<Integer, PredictionAudit> firstCreateByMatch = new HashMap<>();
        for (PredictionAudit audit : matchAudits) {
            if (audit.getAction() != PredictionAuditAction.CREATED) {
                continue;
            }
            int matchId = audit.getMatch().getId();
            PredictionAudit existing = firstCreateByMatch.get(matchId);
            if (existing == null || isEarlierAudit(audit, existing)) {
                firstCreateByMatch.put(matchId, audit);
            }
        }
        Map<String, Long> wins = new HashMap<>();
        for (PredictionAudit audit : firstCreateByMatch.values()) {
            wins.merge(audit.getUsername(), 1L, Long::sum);
        }
        return wins;
    }

    /**
     * Per match: username with the latest audit (create or update) wins.
     */
    Map<String, Long> countLastTouchWins(List<PredictionAudit> matchAudits) {
        Map<Integer, PredictionAudit> lastTouchByMatch = new HashMap<>();
        for (PredictionAudit audit : matchAudits) {
            int matchId = audit.getMatch().getId();
            PredictionAudit existing = lastTouchByMatch.get(matchId);
            if (existing == null || isLaterAudit(audit, existing)) {
                lastTouchByMatch.put(matchId, audit);
            }
        }
        Map<String, Long> wins = new HashMap<>();
        for (PredictionAudit audit : lastTouchByMatch.values()) {
            wins.merge(audit.getUsername(), 1L, Long::sum);
        }
        return wins;
    }

    private static boolean isEarlierAudit(PredictionAudit candidate, PredictionAudit existing) {
        int cmp = candidate.getRecordedAt().compareTo(existing.getRecordedAt());
        if (cmp != 0) {
            return cmp < 0;
        }
        return candidate.getUsername().compareToIgnoreCase(existing.getUsername()) < 0;
    }

    private static boolean isLaterAudit(PredictionAudit candidate, PredictionAudit existing) {
        int cmp = candidate.getRecordedAt().compareTo(existing.getRecordedAt());
        if (cmp != 0) {
            return cmp > 0;
        }
        return candidate.getUsername().compareToIgnoreCase(existing.getUsername()) > 0;
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
