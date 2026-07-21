package com.alper.worldcup.controller;

import com.alper.worldcup.entity.UserProfile;
import com.alper.worldcup.service.AuditEntryView;
import com.alper.worldcup.service.PredictionAuditService;
import com.alper.worldcup.service.TournamentCelebrationService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditController {

    private final PredictionAuditService predictionAuditService;
    private final UserProfileService userProfileService;
    private final TournamentCelebrationService celebrationService;

    public AuditController(PredictionAuditService predictionAuditService,
                           UserProfileService userProfileService,
                           TournamentCelebrationService celebrationService) {
        this.predictionAuditService = predictionAuditService;
        this.userProfileService = userProfileService;
        this.celebrationService = celebrationService;
    }

    @GetMapping("/audit")
    public String audit(Principal principal,
                        Authentication authentication,
                        @RequestParam(required = false) String username,
                        Model model) {
        boolean tournamentEnded = celebrationService.isTournamentEnded();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!tournamentEnded && !isAdmin) {
            return "redirect:/predictions/leaderboard";
        }

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
        model.addAttribute("auditPublic", tournamentEnded);
        model.addAttribute("showAdminNav", isAdmin);
        return "audit";
    }
}
