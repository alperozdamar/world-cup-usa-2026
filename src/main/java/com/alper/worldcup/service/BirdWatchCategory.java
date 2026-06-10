package com.alper.worldcup.service;

import java.util.List;

public record BirdWatchCategory(
        String id,
        String title,
        String explanation,
        List<BirdWatchLeader> leaders,
        boolean pending,
        String pendingMessage) {

    public static BirdWatchCategory ready(String id, String title, String explanation, List<BirdWatchLeader> leaders) {
        return new BirdWatchCategory(id, title, explanation, leaders, false, null);
    }

    public static BirdWatchCategory pending(String id, String title, String explanation, String pendingMessage) {
        return new BirdWatchCategory(id, title, explanation, List.of(), true, pendingMessage);
    }
}
