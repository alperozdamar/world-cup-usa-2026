package com.alper.worldcup.controller;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.service.PredictionService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.ZoneId;
import java.util.List;
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
    private final UserProfileService userProfileService;

    public AdminController(MatchRepository matchRepository,
                           PredictionService predictionService,
                           UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.predictionService = predictionService;
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
}
