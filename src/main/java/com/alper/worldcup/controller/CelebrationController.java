package com.alper.worldcup.controller;

import com.alper.worldcup.service.TournamentCelebrationService;
import com.alper.worldcup.service.TournamentCelebrationService.CelebrationData;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CelebrationController {

    private final TournamentCelebrationService celebrationService;

    public CelebrationController(TournamentCelebrationService celebrationService) {
        this.celebrationService = celebrationService;
    }

    @GetMapping("/celebration")
    public String celebration(Model model) {
        Optional<CelebrationData> data = celebrationService.getCelebrationData();
        if (data.isEmpty()) {
            return "redirect:/predictions/leaderboard";
        }
        model.addAttribute("celebration", data.get());
        return "celebration";
    }
}
