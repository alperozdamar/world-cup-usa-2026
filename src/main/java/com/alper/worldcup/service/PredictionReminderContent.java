package com.alper.worldcup.service;

import java.util.List;

public record PredictionReminderContent(
        String displayName,
        String email,
        boolean missingFinal,
        List<String> missingGroupNames) {

    public boolean hasReminders() {
        return missingFinal || !missingGroupNames.isEmpty();
    }
}
