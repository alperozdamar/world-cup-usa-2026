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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeerPredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final UserProfileService userProfileService;

    public PeerPredictionService(MatchRepository matchRepository,
                                 PredictionRepository predictionRepository,
                                 GroupStandingPredictionRepository groupStandingPredictionRepository,
                                 FinalPredictionRepository finalPredictionRepository,
                                 UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userProfileService = userProfileService;
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
        List<PeerMatchView> views = new ArrayList<>();

        for (Match match : matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)) {
            if (!match.hasStarted(now)) {
                continue;
            }

            List<PeerPlayerMatchPrediction> predictions = predictionRepository.findByMatchId(match.getId()).stream()
                    .map(prediction -> PeerPlayerMatchPrediction.from(
                            prediction,
                            userProfileService.getDisplayName(prediction.getUsername())))
                    .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                            String.CASE_INSENSITIVE_ORDER))
                    .toList();

            views.add(new PeerMatchView(match, predictions));
        }

        return views;
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
        List<UserProfile> profiles = userProfileService.getAllProfiles();
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
