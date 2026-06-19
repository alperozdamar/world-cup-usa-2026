package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnockoutPlaceholderCatalog implements KnockoutBracketResolver.PlaceholderSource {

    private static final Logger log = LoggerFactory.getLogger(KnockoutPlaceholderCatalog.class);

    private final FixtureImportService fixtureImportService;
    private final MatchRepository matchRepository;
    private final Map<SlotKey, SlotPair> slotsByKickoff = new HashMap<>();

    public KnockoutPlaceholderCatalog(FixtureImportService fixtureImportService,
                                      MatchRepository matchRepository) {
        this.fixtureImportService = fixtureImportService;
        this.matchRepository = matchRepository;
    }

    @PostConstruct
    void loadFromFixtures() {
        slotsByKickoff.clear();
        for (FixtureImportService.ParsedFixture fixture : fixtureImportService.parseFixtures()) {
            if (fixture.stage() == MatchStage.GROUP_STAGE) {
                continue;
            }
            slotsByKickoff.put(new SlotKey(fixture.kickoffUtc(), fixture.stage()),
                    new SlotPair(fixture.homeName(), fixture.awayName()));
        }
        log.info("Loaded {} knockout bracket slot definitions from fixtures", slotsByKickoff.size());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void restorePlaceholdersOnStartup() {
        restoreMissingPlaceholders();
    }

    @Transactional
    public void restoreMissingPlaceholders() {
        int restored = 0;
        for (Match match : matchRepository.findKnockoutMatchesWithTeams()) {
            SlotPair slots = slotsForMatch(match);
            if (slots == null) {
                continue;
            }
            boolean changed = false;
            if (match.getHomePlaceholder() == null && slots.home() != null) {
                match.setHomePlaceholder(slots.home());
                changed = true;
            }
            if (match.getAwayPlaceholder() == null && slots.away() != null) {
                match.setAwayPlaceholder(slots.away());
                changed = true;
            }
            if (changed) {
                matchRepository.save(match);
                restored++;
            }
        }
        if (restored > 0) {
            log.info("Restored knockout placeholders on {} matches", restored);
        }
    }

    @Override
    public String placeholder(Match match, boolean homeSide) {
        return effectivePlaceholder(match, homeSide);
    }

    public String effectivePlaceholder(Match match, boolean homeSide) {
        String stored = homeSide ? match.getHomePlaceholder() : match.getAwayPlaceholder();
        if (stored != null && !stored.isBlank()) {
            return stored;
        }
        SlotPair slots = slotsForMatch(match);
        if (slots == null) {
            return null;
        }
        return homeSide ? slots.home() : slots.away();
    }

    private SlotPair slotsForMatch(Match match) {
        return slotsByKickoff.get(new SlotKey(match.getKickoffUtc(), match.getStage()));
    }

    private record SlotKey(java.time.Instant kickoffUtc, MatchStage stage) {
    }

    record SlotPair(String home, String away) {
    }
}
