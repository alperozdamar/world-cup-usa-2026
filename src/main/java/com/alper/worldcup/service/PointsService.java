package com.alper.worldcup.service;

import com.alper.worldcup.entity.MatchStage;

public interface PointsService {

    int calculatePoints(int guessHome, int guessAway, int actualHome, int actualAway, MatchStage stage);
}
