package com.alper.worldcup.service;

import java.util.List;

public record SurveyResultsView(
        long totalResponses,
        List<SurveyOptionCount> fairness,
        List<SurveyOptionCount> scoreVisibility,
        List<SurveyOptionCount> extraTime) {
}
