package com.alper.worldcup.controller;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import com.alper.worldcup.service.BirdWatchService;
import com.alper.worldcup.service.FinalPredictionService;
import com.alper.worldcup.service.GroupStandingPredictionService;
import com.alper.worldcup.service.GroupStandingsService;
import com.alper.worldcup.service.HostPredictionService;
import com.alper.worldcup.service.KnockoutRoundView;
import com.alper.worldcup.service.KnockoutService;
import com.alper.worldcup.service.LeaderboardRowView;
import com.alper.worldcup.service.LeaderboardService;
import com.alper.worldcup.service.MissingPredictionService;
import com.alper.worldcup.service.PeerPredictionService;
import com.alper.worldcup.service.PointsTimelineService;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserAccountService;
import com.alper.worldcup.service.UserMatchStatsService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
    private final GroupStandingsService groupStandingsService;
    private final FinalPredictionService finalPredictionService;
    private final PeerPredictionService peerPredictionService;
    private final HostPredictionService hostPredictionService;
    private final LeaderboardService leaderboardService;
    private final PointsTimelineService pointsTimelineService;
    private final BirdWatchService birdWatchService;
    private final UserProfileService userProfileService;
    private final UserAccountService userAccountService;
    private final UserMatchStatsService userMatchStatsService;
    private final KnockoutService knockoutService;
    private final MissingPredictionService missingPredictionService;
    private final Environment environment;

    public PredictionController(PredictionService predictionService,
                                GroupStandingPredictionService groupStandingPredictionService,
                                GroupStandingsService groupStandingsService,
                                FinalPredictionService finalPredictionService,
                                PeerPredictionService peerPredictionService,
                                HostPredictionService hostPredictionService,
                                LeaderboardService leaderboardService,
                                PointsTimelineService pointsTimelineService,
                                BirdWatchService birdWatchService,
                                UserProfileService userProfileService,
                                UserAccountService userAccountService,
                                UserMatchStatsService userMatchStatsService,
                                KnockoutService knockoutService,
                                MissingPredictionService missingPredictionService,
                                Environment environment) {
        this.predictionService = predictionService;
        this.groupStandingPredictionService = groupStandingPredictionService;
        this.groupStandingsService = groupStandingsService;
        this.finalPredictionService = finalPredictionService;
        this.peerPredictionService = peerPredictionService;
        this.hostPredictionService = hostPredictionService;
        this.leaderboardService = leaderboardService;
        this.pointsTimelineService = pointsTimelineService;
        this.birdWatchService = birdWatchService;
        this.userProfileService = userProfileService;
        this.userAccountService = userAccountService;
        this.userMatchStatsService = userMatchStatsService;
        this.knockoutService = knockoutService;
        this.missingPredictionService = missingPredictionService;
        this.environment = environment;
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
        model.addAttribute("matchStats", userMatchStatsService.getStats(username));
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

    @GetMapping("/standings")
    public String standings(Model model) {
        model.addAttribute("groups", groupStandingsService.getAllGroupStandings());
        return "predictions/standings";
    }

    @GetMapping("/knockout")
    public String knockout(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        List<KnockoutRoundView> rounds = knockoutService.getKnockoutRounds(username, zoneId);

        model.addAttribute("rounds", rounds);
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("username", username);
        model.addAttribute("userMissingNextDayMatches",
                missingPredictionService.describeMissingMatchesForUser(username, zoneId));
        model.addAttribute("knockoutDevPreview", environment.acceptsProfiles(Profiles.of("local")));
        return "predictions/knockout";
    }

    @PostMapping("/knockout/save")
    public String saveKnockout(Principal principal,
                               @RequestParam Integer matchId,
                               @RequestParam Integer homeScore,
                               @RequestParam Integer awayScore,
                               @RequestParam(required = false) Boolean penaltyShootout,
                               @RequestParam(required = false) Integer advancingTeamId,
                               RedirectAttributes redirectAttributes) {
        try {
            knockoutService.saveKnockoutPrediction(
                    principal.getName(), matchId, homeScore, awayScore, penaltyShootout, advancingTeamId);
            redirectAttributes.addFlashAttribute("successMessage", "Knockout prediction saved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/predictions/knockout";
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
        List<String> hostUsernames = hostPredictionService.getHostUsernames();
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("hostUsernames", hostUsernames);
        model.addAttribute("hostDisplayNames", hostPredictionService.getHostDisplayNames());
        model.addAttribute("hostDisplayNamesByUsername", userProfileService.getDisplayNamesForUsernames(hostUsernames));
        model.addAttribute("matchViews", hostPredictionService.getHostMatchPredictions());
        model.addAttribute("knockoutMatchViews", hostPredictionService.getHostKnockoutPredictions());
        model.addAttribute("groupNames", groupStandingPredictionService.getGroupNames());
        model.addAttribute("groupRows", hostPredictionService.getHostGroupPredictions());
        model.addAttribute("finalRows", hostPredictionService.getHostFinalPredictions());
        return "predictions/host";
    }

    @GetMapping("/others")
    public String others(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("username", username);
        model.addAttribute("tournamentStarted", peerPredictionService.isTournamentStarted());
        model.addAttribute("tournamentStartLabel", finalPredictionService.getTournamentStartKickoff()
                .map(kickoff -> kickoff.atZone(zoneId).format(java.time.format.DateTimeFormatter
                        .ofPattern("EEE, MMM d yyyy HH:mm z")))
                .orElse(null));
        model.addAttribute("startedMatchViews", peerPredictionService.getVisibleStartedMatchPredictions());
        model.addAttribute("upcomingMatchViews", peerPredictionService.getUpcomingMatchPredictions(zoneId));
        model.addAttribute("groupNames", groupStandingPredictionService.getGroupNames());
        model.addAttribute("groupRows", peerPredictionService.getVisibleGroupPredictions());
        model.addAttribute("finalRows", peerPredictionService.getVisibleFinalPredictions());
        return "predictions/others";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Principal principal, Model model) {
        ZoneId zoneId = userProfileService.getUserZoneId(principal.getName());
        List<LeaderboardRowView> leaderboardRows = leaderboardService.getLeaderboardRows(zoneId);
        List<String> usernames = leaderboardRows.stream()
                .map(LeaderboardRowView::username)
                .toList();
        model.addAttribute("leaderboardRows", leaderboardRows);
        model.addAttribute("displayNames", userProfileService.getDisplayNamesForUsernames(usernames));
        model.addAttribute("matchStatsByUsername", userMatchStatsService.getStatsForPoolMembers());
        model.addAttribute("categories", birdWatchService.buildCategories());
        model.addAttribute("timelineChart", pointsTimelineService.buildMatchPointsTimeline(zoneId, usernames));
        model.addAttribute("zoneId", zoneId.getId());
        model.addAttribute("computedAtLabel", ZonedDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy HH:mm z")));
        return "predictions/leaderboard";
    }

    @GetMapping("/bird-watch")
    public String birdWatch() {
        return "redirect:/predictions/leaderboard#bird-watch";
    }
}
