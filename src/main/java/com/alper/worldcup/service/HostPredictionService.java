package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HostPredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final GroupStandingPredictionRepository groupStandingPredictionRepository;
    private final FinalPredictionRepository finalPredictionRepository;
    private final UserAccountService userAccountService;
    private final UserProfileService userProfileService;

    public HostPredictionService(MatchRepository matchRepository,
                                 PredictionRepository predictionRepository,
                                 GroupStandingPredictionRepository groupStandingPredictionRepository,
                                 FinalPredictionRepository finalPredictionRepository,
                                 UserAccountService userAccountService,
                                 UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.groupStandingPredictionRepository = groupStandingPredictionRepository;
        this.finalPredictionRepository = finalPredictionRepository;
        this.userAccountService = userAccountService;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public List<String> getHostUsernames() {
        return userAccountService.findAdminUsernames();
    }

    @Transactional(readOnly = true)
    public List<String> getHostDisplayNames() {
        return userAccountService.findAdminUsernames().stream()
                .map(userProfileService::getDisplayName)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeerMatchView> getHostMatchPredictions() {
        Set<String> hostUsernames = new HashSet<>(userAccountService.findAdminUsernames());
        if (hostUsernames.isEmpty()) {
            return List.of();
        }

        List<PeerMatchView> views = new ArrayList<>();
        for (Match match : matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)) {
            List<PeerPlayerMatchPrediction> hostPicks = predictionRepository.findByMatchId(match.getId()).stream()
                    .filter(prediction -> hostUsernames.contains(prediction.getUsername()))
                    .map(prediction -> PeerPlayerMatchPrediction.from(
                            prediction,
                            userProfileService.getDisplayName(prediction.getUsername())))
                    .sorted(Comparator.comparing(PeerPlayerMatchPrediction::displayName,
                            String.CASE_INSENSITIVE_ORDER))
                    .toList();

            if (!hostPicks.isEmpty()) {
                views.add(new PeerMatchView(match, hostPicks));
            }
        }

        return views;
    }

    @Transactional(readOnly = true)
    public List<PeerGroupRowView> getHostGroupPredictions() {
        Set<String> hostUsernames = new HashSet<>(userAccountService.findAdminUsernames());
        if (hostUsernames.isEmpty()) {
            return List.of();
        }

        Map<String, Map<String, PeerGroupPickView>> picksByUser = new LinkedHashMap<>();
        for (GroupStandingPrediction prediction
                : groupStandingPredictionRepository.findAllWithTeamsOrderByUsernameAndGroup()) {
            if (!hostUsernames.contains(prediction.getUsername())) {
                continue;
            }
            picksByUser
                    .computeIfAbsent(prediction.getUsername(), username -> new LinkedHashMap<>())
                    .put(prediction.getGroupName(), PeerGroupPickView.from(prediction));
        }

        return buildGroupRows(picksByUser);
    }

    @Transactional(readOnly = true)
    public List<PeerFinalRowView> getHostFinalPredictions() {
        Set<String> hostUsernames = new HashSet<>(userAccountService.findAdminUsernames());
        if (hostUsernames.isEmpty()) {
            return List.of();
        }

        return finalPredictionRepository.findAllWithTeamsOrderByUsername().stream()
                .filter(prediction -> hostUsernames.contains(prediction.getUsername()))
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
        List<PeerGroupRowView> rows = new ArrayList<>();
        for (Map.Entry<String, Map<String, PeerGroupPickView>> entry : picksByUser.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            rows.add(new PeerGroupRowView(
                    entry.getKey(),
                    userProfileService.getDisplayName(entry.getKey()),
                    entry.getValue()));
        }

        rows.sort(Comparator.comparing(PeerGroupRowView::displayName, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }
}
