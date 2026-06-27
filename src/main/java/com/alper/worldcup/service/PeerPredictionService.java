package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.UserProfile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeerPredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;

    public PeerPredictionService(MatchRepository matchRepository,
                                 PredictionRepository predictionRepository,
                                 GroupStandingPredictionRepository groupStandingPredictionRepository,
                                 FinalPredictionRepository finalPredictionRepository,
                                 UserProfileService userProfileService,
                                 PoolMemberRegistry poolMemberRegistry) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
    }

    @Transactional(readOnly = true)
    public boolean isTournamentStarted() {
        return matchRepository.findTournamentStartKickoff()
                .map(kickoff -> !kickoff.isAfter(Instant.now()))
                .orElse(false);
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
    public Optional<PeerMatchView> getUpcomingMatchPrediction() {
        Instant now = Instant.now();

        Optional<Match> nextGroup = matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE).stream()
                .filter(match -> match.getKickoffUtc().isAfter(now))
                .filter(Match::isPredictionsEnabled)
                .min(Comparator.comparing(Match::getKickoffUtc));

        Optional<Match> nextKnockout = matchRepository.findKnockoutMatchesWithTeams().stream()
                .filter(match -> match.getKickoffUtc().isAfter(now))
                .filter(Match::isPredictionsEnabled)
                .min(Comparator.comparing(Match::getKickoffUtc));

        Optional<Match> next = nextGroup.flatMap(group -> nextKnockout
                .map(knockout -> group.getKickoffUtc().isBefore(knockout.getKickoffUtc()) ? group : knockout)
                .or(() -> Optional.of(group)))
                .or(() -> nextKnockout);

        return next.map(match -> new PeerMatchView(match, loadHiddenPredictions(match), true));
    }

    private List<PeerPlayerMatchPrediction> loadPredictions(Match match) {
        return predictionRepository.findByMatchId(match.getId()).stream()
                .filter(prediction -> poolMemberRegistry.isMember(prediction.getUsername()))
                .map(prediction -> PeerPlayerMatchPrediction.from(
                        prediction,
                        userProfileService.getDisplayName(prediction.getUsername())))
                .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<PeerPlayerMatchPrediction> loadHiddenPredictions(Match match) {
        return predictionRepository.findByMatchId(match.getId()).stream()
                .filter(prediction -> poolMemberRegistry.isMember(prediction.getUsername()))
                .map(prediction -> PeerPlayerMatchPrediction.hidden(
                        prediction.getUsername(),
                        userProfileService.getDisplayName(prediction.getUsername())))
                .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
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
