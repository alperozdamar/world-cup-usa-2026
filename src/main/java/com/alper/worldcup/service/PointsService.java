package com.alper.worldcup.service;

import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;

public interface PointsService {

    double calculatePoints(int guessHome, int guessAway, int actualHome, int actualAway, MatchStage stage);

    double calculateKnockoutPoints(int guessHome,
                                   int guessAway,
                                   int actualHome,
                                   int actualAway,
                                   MatchStage stage,
                                   Boolean penaltyGuess,
                                   Boolean penaltyActual,
                                   Team advancingGuess,
                                   Team advancingActual);
}
