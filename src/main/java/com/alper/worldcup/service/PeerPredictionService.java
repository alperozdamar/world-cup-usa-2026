package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.UserProfile;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeerPredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionAuditRepository predictionAuditRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;
    private final MissingPredictionService missingPredictionService;

    public PeerPredictionService(MatchRepository matchRepository,
                                 PredictionRepository predictionRepository,
                                 PredictionAuditRepository predictionAuditRepository,
                                 GroupStandingPredictionRepository groupStandingPredictionRepository,
                                 FinalPredictionRepository finalPredictionRepository,
                                 UserProfileService userProfileService,
                                 PoolMemberRegistry poolMemberRegistry,
                                 MissingPredictionService missingPredictionService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.predictionAuditRepository = predictionAuditRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
        this.missingPredictionService = missingPredictionService;
    }

    @Transactional(readOnly = true)
    public boolean isTournamentStarted() {
        return matchRepository.findTournamentStartKickoff()
                .map(kickoff -> !kickoff.isAfter(Instant.now()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<PeerMatchView> getVisibleStartedMatchPredictions() {
        Instant now = Instant.now();
        List<Match> groupStarted = matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE).stream()
                .filter(match -> match.hasStarted(now))
                .toList();
        List<Match> knockoutStarted = matchRepository.findKnockoutMatchesWithTeams().stream()
                .filter(match -> match.hasStarted(now))
                .toList();

        List<Match> allStarted = new ArrayList<>(groupStarted.size() + knockoutStarted.size());
        allStarted.addAll(groupStarted);
        allStarted.addAll(knockoutStarted);
        Map<Integer, Map<String, Instant>> savedAtIndex = loadLastSavedAtIndex(allStarted);

        List<PeerMatchView> views = new ArrayList<>();
        groupStarted.stream()
                .map(match -> new PeerMatchView(match, loadPredictions(match, savedAtIndex), false))
                .forEach(views::add);
        knockoutStarted.stream()
                .map(match -> new PeerMatchView(match, loadPredictions(match, savedAtIndex), false))
                .forEach(views::add);

        views.sort(Comparator
                .comparing((PeerMatchView view) -> !KnockoutStageLabels.isKnockout(view.match()))
                .thenComparing((PeerMatchView view) -> view.match().isScoreEntered())
                .thenComparing((PeerMatchView view) -> view.match().getKickoffUtc(), Comparator.reverseOrder()));
        return views;
    }

    @Transactional(readOnly = true)
    public List<PeerMatchView> getVisibleMatchPredictions() {
        Instant now = Instant.now();

        return matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE).stream()
                .filter(match -> match.hasStarted(now))
                .map(match -> new PeerMatchView(match, loadPredictions(match), false))
                .sorted(Comparator.comparing((PeerMatchView view) -> view.match().getKickoffUtc()).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeerMatchView> getVisibleKnockoutPredictions() {
        Instant now = Instant.now();

        return matchRepository.findKnockoutMatchesWithTeams().stream()
                .filter(match -> match.hasStarted(now))
                .map(match -> new PeerMatchView(match, loadPredictions(match), false))
                .sorted(Comparator.comparing((PeerMatchView view) -> view.match().getKickoffUtc()).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeerMatchView> getUpcomingMatchPredictions(ZoneId zoneId) {
        List<Match> upcoming = missingPredictionService.openMatchesOnNextMatchDay(zoneId);
        Map<Integer, Map<String, Instant>> savedAtIndex = loadLastSavedAtIndex(upcoming);
        return upcoming.stream()
                .map(match -> new PeerMatchView(
                        match,
                        loadHiddenPredictions(match, savedAtIndex),
                        true,
                        missingPredictionService.findMissingForMatch(match)))
                .toList();
    }

    private Map<Integer, Map<String, Instant>> loadLastSavedAtIndex(Collection<Match> matches) {
        if (matches.isEmpty()) {
            return Map.of();
        }
        List<Integer> matchIds = matches.stream().map(Match::getId).collect(Collectors.toList());
        Map<Integer, Map<String, Instant>> index = new HashMap<>();
        for (Object[] row : predictionAuditRepository.findLastRecordedAtForMatches(matchIds)) {
            Integer matchId = (Integer) row[0];
            String username = (String) row[1];
            Instant recordedAt = (Instant) row[2];
            index.computeIfAbsent(matchId, ignored -> new HashMap<>()).put(username, recordedAt);
        }
        return index;
    }

    private Instant resolveLastSavedAt(String username,
                                       Integer matchId,
                                       Instant updatedAt,
                                       Map<Integer, Map<String, Instant>> savedAtIndex) {
        Map<String, Instant> byUser = savedAtIndex.get(matchId);
        if (byUser != null) {
            Instant fromAudit = byUser.get(username);
            if (fromAudit != null) {
                return fromAudit;
            }
        }
        return updatedAt;
    }

    private List<PeerPlayerMatchPrediction> loadPredictions(Match match,
                                                            Map<Integer, Map<String, Instant>> savedAtIndex) {
        return predictionRepository.findByMatchId(match.getId()).stream()
                .filter(prediction -> poolMemberRegistry.isMember(prediction.getUsername()))
                .map(prediction -> PeerPlayerMatchPrediction.from(
                        prediction,
                        userProfileService.getDisplayName(prediction.getUsername()),
                        resolveLastSavedAt(prediction.getUsername(), match.getId(),
                                prediction.getUpdatedAt(), savedAtIndex)))
                .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<PeerPlayerMatchPrediction> loadHiddenPredictions(Match match,
                                                                  Map<Integer, Map<String, Instant>> savedAtIndex) {
        return predictionRepository.findByMatchId(match.getId()).stream()
                .filter(prediction -> poolMemberRegistry.isMember(prediction.getUsername()))
                .map(prediction -> PeerPlayerMatchPrediction.hidden(
                        prediction.getUsername(),
                        userProfileService.getDisplayName(prediction.getUsername()),
                        resolveLastSavedAt(prediction.getUsername(), match.getId(),
                                prediction.getUpdatedAt(), savedAtIndex)))
                .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<PeerPlayerMatchPrediction> loadPredictions(Match match) {
        return loadPredictions(match, loadLastSavedAtIndex(List.of(match)));
    }

    @Transactional(readOnly = true)
    public List<PeerGroupRowView> getVisibleGroupPredictions() {
        if (!isTournamentStarted()) {
            return List.of();
        }

        Map<String, Map<String, PeerGroupPickView>> picksByUser = new LinkedHashMap<>();
        for (GroupStandingPrediction prediction
                : groupStandingPredictionRepository.findAllWithTeamsOrderByUsernameAndGroup()) {
            picksByUser
                    .computeIfAbsent(prediction.getUsername(), username -> new LinkedHashMap<>())
                    .put(prediction.getGroupName(), PeerGroupPickView.from(prediction));
        }

        return buildGroupRows(picksByUser);
    }

    @Transactional(readOnly = true)
    public List<PeerFinalRowView> getVisibleFinalPredictions() {
        if (!isTournamentStarted()) {
            return List.of();
        }

        return finalPredictionRepository.findAllWithTeamsOrderByUsername().stream()
                .map(this::toFinalRow)
                .sorted(Comparator.comparing(PeerFinalRowView::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private PeerFinalRowView toFinalRow(FinalPrediction prediction) {
        return new PeerFinalRowView(
                prediction.getUsername(),
                userProfileService.getDisplayName(prediction.getUsername()),
                prediction.getChampionTeam().getName(),
                prediction.getRunnerUpTeam().getName());
    }

    private List<PeerGroupRowView> buildGroupRows(Map<String, Map<String, PeerGroupPickView>> picksByUser) {
        List<UserProfile> profiles = userProfileService.getPoolProfiles();
        List<PeerGroupRowView> rows = new ArrayList<>();

        for (UserProfile profile : profiles) {
            Map<String, PeerGroupPickView> picks = picksByUser.get(profile.getUsername());
            if (picks == null || picks.isEmpty()) {
                continue;
            }
            rows.add(new PeerGroupRowView(
                    profile.getUsername(),
                    userProfileService.getDisplayName(profile.getUsername()),
                    picks));
        }

        for (Map.Entry<String, Map<String, PeerGroupPickView>> entry : picksByUser.entrySet()) {
            if (profiles.stream().noneMatch(profile -> profile.getUsername().equals(entry.getKey()))) {
                rows.add(new PeerGroupRowView(
                        entry.getKey(),
                        userProfileService.getDisplayName(entry.getKey()),
                        entry.getValue()));
            }
        }

        rows.sort(Comparator.comparing(PeerGroupRowView::displayName, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }
}
