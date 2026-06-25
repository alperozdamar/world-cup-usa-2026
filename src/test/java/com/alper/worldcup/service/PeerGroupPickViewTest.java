package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import org.junit.jupiter.api.Test;

class PeerGroupPickViewTest {

    @Test
    void fromMapsTeamNamesAndPointsWhenScored() {
        GroupStandingPrediction prediction = new GroupStandingPrediction();
        prediction.setFirstPlaceTeam(team("Mexico"));
        prediction.setSecondPlaceTeam(team("South Africa"));
        prediction.setPoints(4);

        PeerGroupPickView view = PeerGroupPickView.from(prediction);

        assertEquals("Mexico", view.firstTeamName());
        assertEquals("South Africa", view.secondTeamName());
        assertEquals(4, view.points());
        assertTrue(view.scored());
    }

    @Test
    void fromLeavesPointsUnsetWhenGroupNotScored() {
        GroupStandingPrediction prediction = new GroupStandingPrediction();
        prediction.setFirstPlaceTeam(team("Mexico"));
        prediction.setSecondPlaceTeam(team("South Africa"));

        PeerGroupPickView view = PeerGroupPickView.from(prediction);

        assertFalse(view.scored());
    }

    private static Team team(String name) {
        Team team = new Team();
        team.setName(name);
        return team;
    }
}
