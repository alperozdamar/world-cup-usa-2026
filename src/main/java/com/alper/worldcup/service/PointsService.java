package com.alper.worldcup.service;

public interface PointsService {

    int calculatePoints(int guessHome, int guessAway, int actualHome, int actualAway);
}
