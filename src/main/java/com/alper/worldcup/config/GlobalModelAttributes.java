package com.alper.worldcup.config;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.service.LeaderboardService;
import com.alper.worldcup.service.LeaderboardTickerEntry;
import com.alper.worldcup.service.UpcomingMatchTickerEntry;
import com.alper.worldcup.service.UpcomingMatchTickerService;
import com.alper.worldcup.service.UserProfileService;
import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MatchRepository matchRepository;
    private final LeaderboardService leaderboardService;
    private final UpcomingMatchTickerService upcomingMatchTickerService;
    private final UserProfileService userProfileService;

    @Value("${app.version:unknown}")
    private String appVersion;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    public GlobalModelAttributes(MatchRepository matchRepository,
                                 LeaderboardService leaderboardService,
                                 UpcomingMatchTickerService upcomingMatchTickerService,
                                 UserProfileService userProfileService) {
        this.matchRepository = matchRepository;
        this.leaderboardService = leaderboardService;
        this.upcomingMatchTickerService = upcomingMatchTickerService;
        this.userProfileService = userProfileService;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        if (buildProperties != null && buildProperties.getVersion() != null) {
            return buildProperties.getVersion();
        }
        String version = appVersion;
        if (version.startsWith("@") && version.endsWith("@")) {
            return "dev";
        }
        return version;
    }

    @ModelAttribute("nextMatchKickoffUtc")
    public String nextMatchKickoffUtc() {
        return matchRepository.findNextPredictableMatch(Instant.now())
                .map(match -> match.getKickoffUtc().toString())
                .orElse("");
    }

    @ModelAttribute("leaderboardTicker")
    public List<LeaderboardTickerEntry> leaderboardTicker() {
        return leaderboardService.getTickerEntries();
    }

    @ModelAttribute("upcomingMatchTicker")
    public List<UpcomingMatchTickerEntry> upcomingMatchTicker(Principal principal) {
        if (principal == null) {
            return Collections.emptyList();
        }
        String zoneId = userProfileService.getUserZoneId(principal.getName()).getId();
        return upcomingMatchTickerService.getUpcomingMatches(zoneId);
    }
}
