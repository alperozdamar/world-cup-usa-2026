package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import java.util.List;

public record KnockoutRoundView(String label, MatchStage stage, List<KnockoutMatchView> matches) {
}
