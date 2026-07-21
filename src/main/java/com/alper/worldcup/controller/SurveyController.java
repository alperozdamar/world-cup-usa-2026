package com.alper.worldcup.controller;

import com.alper.worldcup.entity.SurveyExtraTime;
import com.alper.worldcup.entity.SurveyFairness;
import com.alper.worldcup.entity.SurveyScoreVisibility;
import com.alper.worldcup.service.SurveyService;
import com.alper.worldcup.service.TournamentCelebrationService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final TournamentCelebrationService celebrationService;

    public SurveyController(SurveyService surveyService,
                            TournamentCelebrationService celebrationService) {
        this.surveyService = surveyService;
        this.celebrationService = celebrationService;
    }

    @GetMapping
    public String survey(Principal principal, Model model) {
        if (!celebrationService.isTournamentEnded()) {
            return "redirect:/predictions/leaderboard";
        }
        model.addAttribute("alreadySubmitted", surveyService.hasResponded(principal.getName()));
        model.addAttribute("results", surveyService.getResults());
        return "survey";
    }

    @PostMapping
    public String submit(Principal principal,
                         @RequestParam SurveyFairness fairness,
                         @RequestParam SurveyScoreVisibility scoreVisibility,
                         @RequestParam SurveyExtraTime extraTime,
                         RedirectAttributes redirectAttributes) {
        if (!celebrationService.isTournamentEnded()) {
            return "redirect:/predictions/leaderboard";
        }
        try {
            surveyService.submit(principal.getName(), fairness, scoreVisibility, extraTime);
            redirectAttributes.addFlashAttribute("successMessage", "Teşekkürler — anketiniz kaydedildi.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/survey";
    }
}
