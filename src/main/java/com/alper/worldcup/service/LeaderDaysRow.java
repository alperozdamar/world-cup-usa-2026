package com.alper.worldcup.service;

public record LeaderDaysRow(
        int rank,
        String username,
        String displayName,
        int daysAsLeader) {
}
