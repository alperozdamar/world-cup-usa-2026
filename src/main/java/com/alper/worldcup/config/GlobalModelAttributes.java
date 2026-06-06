package com.alper.worldcup.config;

import com.alper.worldcup.dao.MatchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Instant;

@ControllerAdvice
public class GlobalModelAttributes {

    private final MatchRepository matchRepository;

    @Value("${app.version:unknown}")
    private String appVersion;

    public GlobalModelAttributes(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        return appVersion;
    }

    @ModelAttribute("tournamentKickoffUtc")
    public String tournamentKickoffUtc() {
        return matchRepository.findTournamentStartKickoff()
                .map(Instant::toString)
                .orElse("");
    }
}
