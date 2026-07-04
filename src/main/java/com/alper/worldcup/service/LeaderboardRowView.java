package com.alper.worldcup.service;

public record LeaderboardRowView(
        String username,
        double matchPoints,
        double knockoutPoints,
        double groupPoints,
        double finalPoints,
        double totalPoints,
        RankMovement rankMovement,
        Integer previousRank) {
}
