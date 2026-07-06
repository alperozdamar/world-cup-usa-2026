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
    private static final DateTimeFormatter RECORDED_AT_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy HH:mm z");
    private static final DateTimeFormatter SAVED_AT_SHORT_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, HH:mm");

    public String formatKickoffDate(Match match, String timezoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(ZoneId.of(timezoneId));
        return zdt.format(DATE_FORMAT);
    }

    public String formatKickoffTime(Match match, String timezoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(ZoneId.of(timezoneId));
        return zdt.format(TIME_FORMAT);
    }

    public String formatKickoffUtcIso(Match match) {
        return match.getKickoffUtc().toString();
    }

    public String formatRecordedAt(Instant instant, String timezoneId) {
        return instant.atZone(ZoneId.of(timezoneId)).format(RECORDED_AT_FORMAT);
    }

    public String formatSavedAtShort(Instant instant, String timezoneId) {
        return instant.atZone(ZoneId.of(timezoneId)).format(SAVED_AT_SHORT_FORMAT);
    }

    public String formatStageLabel(Match match) {
        return KnockoutStageLabels.label(match.getStage());
    }

    public boolean isEditable(Match match) {
        return match.isPredictionsEnabled()
                && match.getStage() == MatchStage.GROUP_STAGE
                && !match.hasStarted(Instant.now());
    }

    public boolean hasStarted(Match match) {
        return match.hasStarted(Instant.now());
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
