package com.alper.worldcup.service;

import java.util.List;

public record KnockoutSyncResult(int teamsAssigned, int matchesOpened, List<String> details) {

    public String summaryMessage() {
        return "Synced knockout bracket: " + teamsAssigned + " team slot(s) filled, "
                + matchesOpened + " match(es) opened for predictions. Existing picks were not changed.";
    }
}
