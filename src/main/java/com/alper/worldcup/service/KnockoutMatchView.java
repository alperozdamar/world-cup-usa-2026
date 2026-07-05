package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import java.util.List;

public record KnockoutMatchView(
        Match match,
        Prediction prediction,
        boolean editable,
        String statusLabel,
        String homeDisplayName,
        String awayDisplayName,
        String homeSlotLabel,
        String awaySlotLabel,
        List<MissingPredictionMember> missingMembers) {
}
