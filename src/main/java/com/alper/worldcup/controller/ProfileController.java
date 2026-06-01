package com.alper.worldcup.controller;

import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/settings")
    public String settings(Principal principal, Model model) {
        model.addAttribute("timezoneId", userProfileService.getUserZoneId(principal.getName()).getId());
        model.addAttribute("timezones", userProfileService.getCommonTimezones());
        return "profile/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(Principal principal,
                               @RequestParam String timezoneId,
                               RedirectAttributes redirectAttributes) {
        try {
            userProfileService.saveTimezone(principal.getName(), timezoneId);
            redirectAttributes.addFlashAttribute("successMessage", "Timezone updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/settings";
    }
}
