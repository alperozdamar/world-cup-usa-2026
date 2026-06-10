package com.alper.worldcup.controller;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import com.alper.worldcup.service.FinalPredictionService;
import com.alper.worldcup.service.GroupStandingPredictionService;
import com.alper.worldcup.service.HostPredictionService;
import com.alper.worldcup.service.LeaderboardService;
import com.alper.worldcup.service.PeerPredictionService;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserAccountService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/predictions")
public class PredictionController {

    private final PredictionService predictionService;
    private final GroupStandingPredictionService groupStandingPredictionService;
    private final FinalPredictionService finalPredictionService;
    private final PeerPredictionService peerPredictionService;
    private final HostPredictionService hostPredictionService;
    private final LeaderboardService leaderboardService;
    private final UserProfileService userProfileService;
    private final UserAccountService userAccountService;

    public PredictionController(PredictionService predictionService,
                                GroupStandingPredictionService groupStandingPredictionService,
                                FinalPredictionService finalPredictionService,
                                PeerPredictionService peerPredictionService,
                                HostPredictionService hostPredictionService,
                                LeaderboardService leaderboardService,
                                UserProfileService userProfileService,
                                UserAccountService userAccountService) {
        this.predictionService = predictionService;
        this.groupStandingPredictionService = groupStandingPredictionService;
        this.finalPredictionService = finalPredictionService;
        this.peerPredictionService = peerPredictionService;
        this.hostPredictionService = hostPredictionService;
        this.leaderboardService = leaderboardService;
        this.userProfileService = userProfileService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/list")
    public String list(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        List<Match> matches = predictionService.getGroupStageMatches();
        Map<Integer, Prediction> predictions = predictionService.getPredictionsForUser(username);

        model.addAttribute("matches", matches);
        model.addAttribute("predictions", predictions);
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("username", username);
        model.addAttribute("displayName", userProfileService.getDisplayName(username));
        return "predictions/list";
    }

    @PostMapping("/save")
    public String save(Principal principal,
                       @RequestParam Integer matchId,
                       @RequestParam Integer homeScore,
                       @RequestParam Integer awayScore,
                       RedirectAttributes redirectAttributes) {
        try {
            predictionService.savePrediction(principal.getName(), matchId, homeScore, awayScore);
            redirectAttributes.addFlashAttribute("successMessage", "Prediction saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/predictions/list";
    }

    @GetMapping("/groups")
    public String groupStandings(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        List<String> groupNames = groupStandingPredictionService.getGroupNames();
        Map<String, List<Team>> teamsByGroup = groupStandingPredictionService.getTeamsByGroup();
        Map<String, GroupStandingPrediction> predictions =
                groupStandingPredictionService.getPredictionsForUser(username);
        Map<String, GroupResult> groupResults = groupStandingPredictionService.getGroupResults();

        List<GroupPredictionView> groups = new ArrayList<>();
        for (String groupName : groupNames) {
            String firstKickoffLabel = groupStandingPredictionService.getFirstKickoffForGroup(groupName)
                    .map(kickoff -> kickoff.atZone(zoneId).format(java.time.format.DateTimeFormatter
                            .ofPattern("EEE, MMM d yyyy HH:mm z")))
                    .orElse(null);
            groups.add(new GroupPredictionView(
                    groupName,
                    teamsByGroup.getOrDefault(groupName, List.of()),
                    predictions.get(groupName),
                    groupResults.get(groupName),
                    firstKickoffLabel,
                    groupStandingPredictionService.isGroupEditable(groupName)));
        }

        model.addAttribute("groups", groups);
        model.addAttribute("zoneId", zoneId.getId());
        return "predictions/groups";
    }

    @PostMapping("/groups/save")
    public String saveGroupStandings(Principal principal,
                                     @RequestParam String groupName,
                                     @RequestParam Integer firstTeamId,
                                     @RequestParam Integer secondTeamId,
                                     RedirectAttributes redirectAttributes) {
        try {
            groupStandingPredictionService.savePrediction(
                    principal.getName(), groupName, firstTeamId, secondTeamId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Group " + groupName + " prediction saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/predictions/groups";
    }

    @GetMapping("/final")
    public String finalPrediction(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        FinalPrediction prediction = finalPredictionService.getPredictionForUser(username).orElse(null);
        FinalResult finalResult = finalPredictionService.getFinalResult().orElse(null);

        model.addAttribute("teams", finalPredictionService.getAllTeams());
        model.addAttribute("prediction", prediction);
        model.addAttribute("finalResult", finalResult);
        model.addAttribute("editable", finalPredictionService.isEditable());
        model.addAttribute("statusLabel", finalPredictionService.statusLabel(prediction));
        model.addAttribute("lockKickoffLabel", finalPredictionService.getTournamentStartKickoff()
                .map(kickoff -> kickoff.atZone(zoneId).format(java.time.format.DateTimeFormatter
                        .ofPattern("EEE, MMM d yyyy HH:mm z")))
                .orElse(null));
        model.addAttribute("zoneId", zoneId.getId());
        return "predictions/final";
    }

    @PostMapping("/final/save")
    public String saveFinalPrediction(Principal principal,
                                      @RequestParam Integer championTeamId,
                                      @RequestParam Integer runnerUpTeamId,
                                      RedirectAttributes redirectAttributes) {
        try {
            finalPredictionService.savePrediction(
                    principal.getName(), championTeamId, runnerUpTeamId);
            redirectAttributes.addFlashAttribute("successMessage", "Final prediction saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/predictions/final";
    }

    @GetMapping("/host")
    public String host(Principal principal, Model model) {
        if (userAccountService.findAdminUsernames().contains(principal.getName())) {
            return "redirect:/predictions/list";
        }

        ZoneId zoneId = userProfileService.getUserZoneId(principal.getName());
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("hostDisplayNames", hostPredictionService.getHostDisplayNames());
        model.addAttribute("matchViews", hostPredictionService.getHostMatchPredictions());
        model.addAttribute("groupNames", groupStandingPredictionService.getGroupNames());
        model.addAttribute("groupRows", hostPredictionService.getHostGroupPredictions());
        model.addAttribute("finalRows", hostPredictionService.getHostFinalPredictions());
        return "predictions/host";
    }

    @GetMapping("/others")
    public String others(Principal principal, Model model) {
        ZoneId zoneId = userProfileService.getUserZoneId(principal.getName());
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("tournamentStarted", peerPredictionService.isTournamentStarted());
        model.addAttribute("tournamentStartLabel", finalPredictionService.getTournamentStartKickoff()
                .map(kickoff -> kickoff.atZone(zoneId).format(java.time.format.DateTimeFormatter
                        .ofPattern("EEE, MMM d yyyy HH:mm z")))
                .orElse(null));
        model.addAttribute("matchViews", peerPredictionService.getVisibleMatchPredictions());
        model.addAttribute("groupNames", groupStandingPredictionService.getGroupNames());
        model.addAttribute("groupRows", peerPredictionService.getVisibleGroupPredictions());
        model.addAttribute("finalRows", peerPredictionService.getVisibleFinalPredictions());
        return "predictions/others";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        List<Object[]> leaderboard = leaderboardService.getLeaderboard();
        List<String> usernames = leaderboard.stream()
                .map(row -> (String) row[0])
                .toList();
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("displayNames", userProfileService.getDisplayNamesForUsernames(usernames));
        return "predictions/leaderboard";
    }
}
