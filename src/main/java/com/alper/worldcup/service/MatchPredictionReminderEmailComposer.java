package com.alper.worldcup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MatchPredictionReminderEmailComposer {

    private final String appBaseUrl;

    public MatchPredictionReminderEmailComposer(@Value("${app.base-url:http://localhost:8090}") String appBaseUrl) {
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
    }

    public String subject(MatchPredictionReminderContent reminder) {
        return "World Cup 2026 — " + reminder.hoursBeforeKickoff() + "h to kickoff: " + reminder.matchLabel();
    }

    public String compose(MatchPredictionReminderContent reminder) {
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(reminder.displayName()).append(",\n\n");
        body.append("You have not entered a score prediction for:\n\n");
        body.append("  ").append(reminder.matchLabel()).append("\n");
        body.append("  Kickoff: ").append(reminder.kickoffLabel()).append("\n\n");
        body.append("About ").append(reminder.hoursBeforeKickoff())
                .append(" hour").append(reminder.hoursBeforeKickoff() == 1 ? "" : "s")
                .append(" until lock — enter your pick now:\n");
        body.append(appBaseUrl).append("/predictions/list\n\n");
        body.append("— World Cup 2026 prediction pool\n");
        return body.toString();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8090";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
