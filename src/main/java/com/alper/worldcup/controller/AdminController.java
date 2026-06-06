package com.alper.worldcup.controller;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.PredictionAudit;
import com.alper.worldcup.entity.UserProfile;
import com.alper.worldcup.service.PredictionAuditService;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.ZoneId;
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
    private final PredictionAuditService predictionAuditService;
    private final UserProfileService userProfileService;

    public AdminController(MatchRepository matchRepository,
                           PredictionService predictionService,
                           PredictionAuditService predictionAuditService,
                           UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.predictionService = predictionService;
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

    @GetMapping("/audit")
    public String audit(Principal principal,
                        @RequestParam(required = false) String username,
                        Model model) {
        List<PredictionAudit> audits = predictionAuditService.getAuditTrail(username);
        Map<String, String> displayNames = new HashMap<>();
        for (UserProfile profile : userProfileService.getAllProfiles()) {
            displayNames.put(profile.getUsername(), userProfileService.getDisplayName(profile.getUsername()));
        }

        model.addAttribute("audits", audits);
        model.addAttribute("players", userProfileService.getAllProfiles());
        model.addAttribute("displayNames", displayNames);
        model.addAttribute("selectedUsername", username != null ? username : "");
        model.addAttribute("zoneId", userProfileService.getUserZoneId(principal.getName()).getId());
        return "admin/audit";
    }
}
