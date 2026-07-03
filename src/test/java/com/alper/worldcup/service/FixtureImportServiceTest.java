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

    @Test
    void roundOf16PlaceholdersMatchBracketTree() {
        FixtureImportService service = new FixtureImportService(
                Mockito.mock(MatchRepository.class),
                Mockito.mock(TeamRepository.class),
                "America/New_York");

        List<FixtureImportService.ParsedFixture> fixtures = service.parseFixtures();
        List<FixtureImportService.ParsedFixture> roundOf16 = fixtures.stream()
                .filter(f -> f.stage() == MatchStage.ROUND_OF_16)
                .toList();

        assertEquals(8, roundOf16.size());
        assertEquals("W73", roundOf16.get(0).homeName());
        assertEquals("W76", roundOf16.get(0).awayName());
        assertEquals("W75", roundOf16.get(1).homeName());
        assertEquals("W78", roundOf16.get(1).awayName());
        assertEquals("W74", roundOf16.get(2).homeName());
        assertEquals("W77", roundOf16.get(2).awayName());
        assertEquals("W79", roundOf16.get(3).homeName());
        assertEquals("W80", roundOf16.get(3).awayName());
    }
}
