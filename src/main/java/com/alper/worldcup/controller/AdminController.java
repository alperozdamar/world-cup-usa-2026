package com.alper.worldcup.controller;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.UserProfile;
import com.alper.worldcup.service.AuditEntryView;
import com.alper.worldcup.service.FinalPredictionService;
import com.alper.worldcup.service.GroupStandingPredictionService;
import com.alper.worldcup.service.PredictionAuditService;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserProfileService;
import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.Team;
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
@RequestMapping("/admin")
public class AdminController {

    private final MatchRepository matchRepository;
    private final PredictionService predictionService;
    private final GroupStandingPredictionService groupStandingPredictionService;
    private final FinalPredictionService finalPredictionService;
    private final PredictionAuditService predictionAuditService;
    private final UserProfileService userProfileService;

    public AdminController(MatchRepository matchRepository,
                           PredictionService predictionService,
                           GroupStandingPredictionService groupStandingPredictionService,
                           FinalPredictionService finalPredictionService,
                           PredictionAuditService predictionAuditService,
                           UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.predictionService = predictionService;
        this.groupStandingPredictionService = groupStandingPredictionService;
        this.finalPredictionService = finalPredictionService;
        this.predictionAuditService = predictionAuditService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/scores")
    public String scores(Principal principal, Model model) {
        List<Match> matches = matchRepository.findAllWithTeams();
        ZoneId zoneId = userProfileService.getUserZoneId(principal.getName());
        model.addAttribute("matches", matches);
        model.addAttribute("zoneId", zoneId.getId());
        return "admin/scores";
    }

    @PostMapping("/scores/save")
    public String saveScore(@RequestParam Integer matchId,
                            @RequestParam Integer homeScore,
                            @RequestParam Integer awayScore,
                            RedirectAttributes redirectAttributes) {
        try {
            predictionService.saveActualScore(matchId, homeScore, awayScore);
            redirectAttributes.addFlashAttribute("successMessage", "Score saved.");
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
    public String audit(Principal principal,
                        @RequestParam(required = false) String username,
                        Model model) {
        List<AuditEntryView> audits = predictionAuditService.getCombinedAuditTrail(username);
        Map<String, String> displayNames = new HashMap<>();
        for (UserProfile profile : userProfileService.getPoolProfiles()) {
            displayNames.put(profile.getUsername(), userProfileService.getDisplayName(profile.getUsername()));
        }

        model.addAttribute("audits", audits);
        model.addAttribute("players", userProfileService.getPoolProfiles());
        model.addAttribute("displayNames", displayNames);
        model.addAttribute("selectedUsername", username != null ? username : "");
        model.addAttribute("zoneId", userProfileService.getUserZoneId(principal.getName()).getId());
        return "admin/audit";
    }
}
