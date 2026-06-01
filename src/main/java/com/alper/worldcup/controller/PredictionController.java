package com.alper.worldcup.controller;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.service.MatchViewHelper;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.ZoneId;
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
    private final UserProfileService userProfileService;

    public PredictionController(PredictionService predictionService,
                                UserProfileService userProfileService) {
        this.predictionService = predictionService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/list")
    public String list(Principal principal, Model model) {
        String username = principal.getName();
        ZoneId zoneId = userProfileService.getUserZoneId(username);
        List<Match> matches = predictionService.getGroupStageMatches();
        Map<Integer, Prediction> predictions = predictionService.getPredictionsForUser(username);

        model.addAttribute("matches", matches);
        model.addAttribute("predictions", predictions);
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("username", username);
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

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaderboard", predictionService.getLeaderboard());
        return "predictions/leaderboard";
    }
}
