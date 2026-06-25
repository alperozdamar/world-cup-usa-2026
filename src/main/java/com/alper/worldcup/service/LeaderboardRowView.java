package com.alper.worldcup.service;

public record LeaderboardRowView(
        String username,
        long matchPoints,
        long groupPoints,
        long finalPoints,
        long totalPoints) {
}
