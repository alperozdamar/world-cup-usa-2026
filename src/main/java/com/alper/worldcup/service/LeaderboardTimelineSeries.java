package com.alper.worldcup.service;

import java.util.List;

public record LeaderboardTimelineSeries(String username, String label, List<Long> data, String color) {
}
