package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MatchViewHelper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm z");

    public String formatKickoffDate(Match match, String timezoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(ZoneId.of(timezoneId));
        return zdt.format(DATE_FORMAT);
    }

    public String formatKickoffTime(Match match, String timezoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(ZoneId.of(timezoneId));
        return zdt.format(TIME_FORMAT);
    }

    public boolean isEditable(Match match) {
        return match.isPredictionsEnabled()
                && match.getStage() == MatchStage.GROUP_STAGE
                && !match.hasStarted(Instant.now());
    }

    public String statusLabel(Match match, Prediction prediction) {
        if (!match.isPredictionsEnabled()) {
            return "Not open yet";
        }
        if (match.hasStarted(Instant.now())) {
            if (prediction != null) {
                return "Locked";
            }
            return "Missed";
        }
        if (prediction != null) {
            return "Saved";
        }
        return "Open";
    }
}
