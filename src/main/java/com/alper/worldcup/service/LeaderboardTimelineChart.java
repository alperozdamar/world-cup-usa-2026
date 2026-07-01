package com.alper.worldcup.service;

import java.util.List;

public record LeaderboardTimelineChart(
        List<String> labels,
        List<LeaderboardTimelineSeries> series,
        boolean visible) {
}
