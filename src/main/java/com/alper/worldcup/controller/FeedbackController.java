package com.alper.worldcup.controller;

import com.alper.worldcup.entity.UserComment;
import com.alper.worldcup.service.CommentService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final CommentService commentService;
    private final UserProfileService userProfileService;

    public FeedbackController(CommentService commentService,
                              UserProfileService userProfileService) {
        this.commentService = commentService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String feedback(Principal principal, Model model) {
        String username = principal.getName();
        List<UserComment> comments = commentService.getAllComments();
        List<String> usernames = comments.stream()
                .map(UserComment::getUsername)
                .distinct()
                .toList();

        model.addAttribute("comments", comments);
        model.addAttribute("displayNames", userProfileService.getDisplayNamesForUsernames(usernames));
        model.addAttribute("zoneId", userProfileService.getUserZoneId(username).getId());
        model.addAttribute("displayName", userProfileService.getDisplayName(username));
        return "feedback/list";
    }

    @PostMapping
    public String submitFeedback(Principal principal,
                                 @RequestParam String content,
                                 RedirectAttributes redirectAttributes) {
        try {
            commentService.addComment(principal.getName(), content);
            redirectAttributes.addFlashAttribute("successMessage", "Thanks — your feedback was posted.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/feedback";
    }
}
