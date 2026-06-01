package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.entity.MatchStage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FixtureImportServiceTest {

    @Test
    void parseFixturesLoadsGroupStageAndKnockoutMatches() {
        FixtureImportService service = new FixtureImportService(
                Mockito.mock(MatchRepository.class),
                Mockito.mock(TeamRepository.class),
                "America/New_York");

        List<FixtureImportService.ParsedFixture> fixtures = service.parseFixtures();

        assertEquals(104, fixtures.size());
        long groupCount = fixtures.stream().filter(f -> f.stage() == MatchStage.GROUP_STAGE).count();
        assertEquals(72, groupCount);
        assertTrue(fixtures.stream().anyMatch(f -> f.stage() == MatchStage.ROUND_OF_32));
        assertTrue(fixtures.stream().anyMatch(f ->
                "Mexico".equals(f.homeName()) && "South Africa".equals(f.awayName())));
    }
}
