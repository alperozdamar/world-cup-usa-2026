package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TeamFlagServiceTest {

    private final TeamFlagService service = new TeamFlagService(new ObjectMapper());

    @Test
    void resolvesCommonTeams() {
        assertTrue(service.hasFlag("Spain"));
        assertEquals("/images/flags/es.svg", service.flagUrl("Spain"));
        assertTrue(service.hasFlag("USA"));
        assertEquals("/images/flags/us.svg", service.flagUrl("USA"));
    }

    @Test
    void resolvesSpecialCases() {
        assertEquals("/images/flags/gb-eng.svg", service.flagUrl("England"));
        assertEquals("/images/flags/ci.svg", service.flagUrl("Côte d'Ivoire"));
        assertEquals("/images/flags/ci.svg", service.flagUrl("Ivory Coast"));
        assertEquals("/images/flags/cv.svg", service.flagUrl("Cabo Verde"));
    }

    @Test
    void unknownTeamHasNoFlag() {
        assertFalse(service.hasFlag("TBD"));
        assertFalse(service.hasFlag(null));
    }
}
