package com.alper.worldcup.service;

public record LeaderboardRowView(
        String username,
        long matchPoints,
        long knockoutPoints,
        long groupPoints,
        long finalPoints,
        long totalPoints,
        RankMovement rankMovement,
        Integer previousRank) {
}
