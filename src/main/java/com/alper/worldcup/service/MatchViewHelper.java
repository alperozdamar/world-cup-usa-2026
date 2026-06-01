package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class MatchViewHelper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm z");

    private MatchViewHelper() {
    }

    public static String formatKickoffDate(Match match, ZoneId zoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(zoneId);
        return zdt.format(DATE_FORMAT);
    }

    public static String formatKickoffTime(Match match, ZoneId zoneId) {
        ZonedDateTime zdt = match.getKickoffUtc().atZone(zoneId);
        return zdt.format(TIME_FORMAT);
    }

    public static boolean isEditable(Match match) {
        return match.isPredictionsEnabled()
                && match.getStage() == com.alper.worldcup.entity.MatchStage.GROUP_STAGE
                && !match.hasStarted(Instant.now());
    }

    public static String statusLabel(Match match, Prediction prediction) {
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
