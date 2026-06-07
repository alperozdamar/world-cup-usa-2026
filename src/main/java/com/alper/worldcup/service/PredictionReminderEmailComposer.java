package com.alper.worldcup.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PredictionReminderEmailComposer {

    private final String appBaseUrl;

    public PredictionReminderEmailComposer(@Value("${app.base-url:http://localhost:8090}") String appBaseUrl) {
        this.appBaseUrl = trimTrailingSlash(appBaseUrl);
    }

    public String subject() {
        return "World Cup 2026 — prediction reminder";
    }

    public String compose(PredictionReminderContent reminder) {
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(reminder.displayName()).append(",\n\n");
        body.append("You still have prediction picks to complete:\n\n");

        if (reminder.missingFinal()) {
            body.append("• Final (champion & runner-up)\n");
            body.append("  ").append(appBaseUrl).append("/predictions/final\n\n");
        }

        for (String groupName : reminder.missingGroupNames()) {
            body.append("• Group ").append(groupName).append(" — 1st & 2nd place\n");
        }

        if (!reminder.missingGroupNames().isEmpty()) {
            body.append("  ").append(appBaseUrl).append("/predictions/groups\n\n");
        }

        body.append("Picks lock before kickoff — enter them while you still can.\n");
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
