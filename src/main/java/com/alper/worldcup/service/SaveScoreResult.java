package com.alper.worldcup.service;

public record SaveScoreResult(KnockoutSyncResult knockoutSync) {

    public String successMessage() {
        if (knockoutSync == null
                || (knockoutSync.teamsAssigned() == 0 && knockoutSync.matchesOpened() == 0)) {
            return "Score saved.";
        }
        return "Score saved. " + knockoutSync.summaryMessage();
    }
}
