package com.alper.worldcup.service;

public record MatchPredictionReminderContent(
        String username,
        String displayName,
        String email,
        int matchId,
        String matchLabel,
        String kickoffLabel,
        int hoursBeforeKickoff) {
}
