package com.alper.worldcup.controller;

import com.alper.worldcup.service.PointsTimelineService;
import com.alper.worldcup.service.TournamentWrapUpService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WrapUpController {

    private final TournamentWrapUpService wrapUpService;
    private final PointsTimelineService pointsTimelineService;
    private final UserProfileService userProfileService;

    public WrapUpController(TournamentWrapUpService wrapUpService,
                            PointsTimelineService pointsTimelineService,
                            UserProfileService userProfileService) {
        this.wrapUpService = wrapUpService;
        this.pointsTimelineService = pointsTimelineService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/wrap-up")
    public String wrapUp(Principal principal, Model model) {
        var zoneId = userProfileService.getUserZoneId(principal.getName());
        model.addAttribute("wrapUpStats", wrapUpService.getStats());
        model.addAttribute("leaderDays", pointsTimelineService.leaderDaysRanking(zoneId));
        model.addAttribute("zoneId", zoneId.getId());
        return "wrap-up";
    }
}
