package com.alper.worldcup.controller;

import com.alper.worldcup.service.UserAccountService;
import com.alper.worldcup.service.UserMatchStatsService;
import com.alper.worldcup.service.UserProfilePhotoHelper;
import com.alper.worldcup.service.UserProfilePhotoService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileService userProfileService;
    private final UserAccountService userAccountService;
    private final UserProfilePhotoService userProfilePhotoService;
    private final UserProfilePhotoHelper userProfilePhotoHelper;
    private final UserMatchStatsService userMatchStatsService;

    public ProfileController(UserProfileService userProfileService,
                             UserAccountService userAccountService,
                             UserProfilePhotoService userProfilePhotoService,
                             UserProfilePhotoHelper userProfilePhotoHelper,
                             UserMatchStatsService userMatchStatsService) {
        this.userProfileService = userProfileService;
        this.userAccountService = userAccountService;
        this.userProfilePhotoService = userProfilePhotoService;
        this.userProfilePhotoHelper = userProfilePhotoHelper;
        this.userMatchStatsService = userMatchStatsService;
    }

    @GetMapping("/settings")
    public String settings(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("timezoneId", userProfileService.getUserZoneId(username).getId());
        model.addAttribute("timezones", userProfileService.getCommonTimezones());
        model.addAttribute("displayName", userProfileService.getDisplayName(username));
        model.addAttribute("email", userProfileService.getEmail(username));
        model.addAttribute("username", username);
        model.addAttribute("hasUploadedPhoto", userProfilePhotoHelper.hasUploadedPhoto(username));
        model.addAttribute("matchStats", userMatchStatsService.getStats(username));
        return "profile/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(Principal principal,
                               @RequestParam String timezoneId,
                               @RequestParam(required = false) String email,
                               RedirectAttributes redirectAttributes) {
        try {
            userProfileService.saveProfileSettings(principal.getName(), timezoneId, email);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/settings";
    }

    @PostMapping("/photo")
    public String uploadPhoto(Principal principal,
                              @RequestParam("photo") MultipartFile photo,
                              RedirectAttributes redirectAttributes) {
        try {
            userProfilePhotoService.savePhoto(principal.getName(), photo);
            redirectAttributes.addFlashAttribute("successMessage", "Profile photo updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile/settings";
    }

    @PostMapping("/photo/remove")
    public String removePhoto(Principal principal, RedirectAttributes redirectAttributes) {
        userProfilePhotoService.removeUploadedPhoto(principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Uploaded photo removed. Default photo will show if one exists.");
        return "redirect:/profile/settings";
    }

    @GetMapping("/photos/{username}")
    public ResponseEntity<byte[]> photo(@PathVariable String username) {
        return userProfilePhotoService.findStoredPhoto(username)
                .map(photo -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(photo.contentType()))
                        .header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue())
                        .body(photo.data()))
                .orElse(ResponseEntity.notFound().build());
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
