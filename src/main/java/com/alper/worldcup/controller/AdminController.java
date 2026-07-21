package com.alper.worldcup.controller;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.service.FinalPredictionService;
import com.alper.worldcup.service.GroupStandingPredictionService;
import com.alper.worldcup.service.KnockoutAssignmentService;
import com.alper.worldcup.service.KnockoutSyncResult;
import com.alper.worldcup.service.SaveScoreResult;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.TournamentCelebrationService;
import com.alper.worldcup.service.UserProfileService;
import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.Team;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
@RequestMapping("/admin")
public class AdminController {

    private final MatchRepository matchRepository;
    private final PredictionService predictionService;
    private final GroupStandingPredictionService groupStandingPredictionService;
    private final FinalPredictionService finalPredictionService;
    private final UserProfileService userProfileService;
    private final KnockoutAssignmentService knockoutAssignmentService;
    private final TournamentCelebrationService celebrationService;

    public AdminController(MatchRepository matchRepository,
                           PredictionService predictionService,
                           GroupStandingPredictionService groupStandingPredictionService,
                           FinalPredictionService finalPredictionService,
                           UserProfileService userProfileService,
                           KnockoutAssignmentService knockoutAssignmentService,
                           TournamentCelebrationService celebrationService) {
        this.matchRepository = matchRepository;
        this.predictionService = predictionService;
        this.groupStandingPredictionService = groupStandingPredictionService;
        this.finalPredictionService = finalPredictionService;
        this.userProfileService = userProfileService;
        this.knockoutAssignmentService = knockoutAssignmentService;
        this.celebrationService = celebrationService;
    }

    @GetMapping("/scores")
    public String scores(Principal principal, Model model) {
        ZoneId zoneId = userProfileService.getUserZoneId(principal.getName());
        List<Match> matches = sortMatchesForAdminScores(matchRepository.findAllWithTeams(), zoneId);
        model.addAttribute("matches", matches);
        model.addAttribute("zoneId", zoneId.getId());
        return "admin/scores";
    }

    /**
     * Today first, then unscored and upcoming, then scored past matches at the bottom.
     */
    static List<Match> sortMatchesForAdminScores(List<Match> matches, ZoneId zoneId) {
        return sortMatchesForAdminScores(matches, zoneId, Instant.now());
    }

    static List<Match> sortMatchesForAdminScores(List<Match> matches, ZoneId zoneId, Instant now) {
        LocalDate today = now.atZone(zoneId).toLocalDate();
        return matches.stream()
                .sorted((left, right) -> {
                    int leftBucket = adminScoresBucket(left, today, zoneId);
                    int rightBucket = adminScoresBucket(right, today, zoneId);
                    if (leftBucket != rightBucket) {
                        return Integer.compare(leftBucket, rightBucket);
                    }
                    if (leftBucket == 2) {
                        return right.getKickoffUtc().compareTo(left.getKickoffUtc());
                    }
                    return left.getKickoffUtc().compareTo(right.getKickoffUtc());
                })
                .toList();
    }

    private static int adminScoresBucket(Match match, LocalDate today, ZoneId zoneId) {
        LocalDate matchDay = match.getKickoffUtc().atZone(zoneId).toLocalDate();
        if (matchDay.equals(today)) {
            return 0;
        }
        if (matchDay.isAfter(today) || !match.isScoreEntered()) {
            return 1;
        }
        return 2;
    }

    @PostMapping("/scores/save")
    public String saveScore(@RequestParam Integer matchId,
                            @RequestParam Integer homeScore,
                            @RequestParam Integer awayScore,
                            @RequestParam(required = false) Integer advancingTeamId,
                            @RequestParam(required = false) Boolean penaltyShootout,
                            RedirectAttributes redirectAttributes) {
        try {
            SaveScoreResult result = predictionService.saveActualScore(
                    matchId, homeScore, awayScore, advancingTeamId, penaltyShootout);
            redirectAttributes.addFlashAttribute("successMessage", result.successMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/scores";
    }

    @PostMapping("/knockout/sync-bracket")
    public String syncKnockoutBracket(RedirectAttributes redirectAttributes) {
        try {
            KnockoutSyncResult result = knockoutAssignmentService.syncBracketFromStandings(Instant.now());
            redirectAttributes.addFlashAttribute("successMessage", result.summaryMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/scores";
    }

    @GetMapping("/group-results")
    public String groupResults(Model model) {
        Map<String, List<Team>> teamsByGroup = groupStandingPredictionService.getTeamsByGroup();
        Map<String, GroupResult> groupResults = groupStandingPredictionService.getGroupResults();
        List<AdminGroupResultView> groups = new ArrayList<>();
        for (String groupName : groupStandingPredictionService.getGroupNames()) {
            groups.add(new AdminGroupResultView(
                    groupName,
                    teamsByGroup.getOrDefault(groupName, List.of()),
                    groupResults.get(groupName)));
        }
        model.addAttribute("groups", groups);
        return "admin/group-results";
    }

    @PostMapping("/group-results/save")
    public String saveGroupResult(@RequestParam String groupName,
                                  @RequestParam Integer firstTeamId,
                                  @RequestParam Integer secondTeamId,
                                  RedirectAttributes redirectAttributes) {
        try {
            groupStandingPredictionService.saveGroupResult(groupName, firstTeamId, secondTeamId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Group " + groupName + " result saved and points updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/group-results";
    }

    @GetMapping("/final-result")
    public String finalResult(Model model) {
        model.addAttribute("teams", finalPredictionService.getAllTeams());
        model.addAttribute("finalResult", finalPredictionService.getFinalResult().orElse(null));
        return "admin/final-result";
    }

    @PostMapping("/final-result/save")
    public String saveFinalResult(@RequestParam Integer championTeamId,
                                  @RequestParam Integer runnerUpTeamId,
                                  RedirectAttributes redirectAttributes) {
        try {
            finalPredictionService.saveFinalResult(championTeamId, runnerUpTeamId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Final result saved and player points updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/final-result";
    }

    @GetMapping("/audit")
    public String audit(@RequestParam(required = false) String username) {
        if (username != null && !username.isBlank()) {
            return "redirect:/audit?username=" + username;
        }
        return "redirect:/audit";
    }

    @GetMapping("/celebration")
    public String celebrationAdmin(Model model) {
        model.addAttribute("tournamentEnded", celebrationService.isTournamentEnded());
        model.addAttribute("finalScored", celebrationService.isFinalMatchScored());
        model.addAttribute("celebrationData",
                celebrationService.getCelebrationData().orElse(null));
        return "admin/celebration";
    }

    @PostMapping("/end-tournament")
    public String endTournament(RedirectAttributes redirectAttributes) {
        celebrationService.endTournament();
        redirectAttributes.addFlashAttribute("successMessage",
                "Tournament ended! Celebration mode is now ACTIVE for all users.");
        return "redirect:/admin/celebration";
    }

    @PostMapping("/reset-tournament-end")
    public String resetTournamentEnd(RedirectAttributes redirectAttributes) {
        celebrationService.resetTournamentEnd();
        redirectAttributes.addFlashAttribute("successMessage",
                "Tournament end reset. Celebration mode is OFF.");
        return "redirect:/admin/celebration";
    }
}
