package com.alper.worldcup.controller;

import com.alper.worldcup.service.UserAccountService;
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
    private final UserAccountService userAccountService;

    public ProfileController(UserProfileService userProfileService,
                             UserAccountService userAccountService) {
        this.userProfileService = userProfileService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/settings")
    public String settings(Principal principal, Model model) {
        model.addAttribute("timezoneId", userProfileService.getUserZoneId(principal.getName()).getId());
        model.addAttribute("timezones", userProfileService.getCommonTimezones());
        model.addAttribute("displayName", userProfileService.getDisplayName(principal.getName()));
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

    @GetMapping("/password")
    public String changePasswordForm() {
        return "profile/password";
    }

    @PostMapping("/password")
    public String changePassword(Principal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile/password";
        }
        try {
            userAccountService.changePassword(principal.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/password";
    }
}
