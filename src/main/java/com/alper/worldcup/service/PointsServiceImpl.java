package com.alper.worldcup.service;

import org.springframework.stereotype.Service;

@Service
public class PointsServiceImpl implements PointsService {

    @Override
    public int calculatePoints(int guessHome, int guessAway, int actualHome, int actualAway) {
        // Scoring rules will be defined later.
        return 0;
    }
}
