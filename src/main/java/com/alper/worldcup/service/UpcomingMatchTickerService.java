package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpcomingMatchTickerService {

    static final int UPCOMING_MATCH_LIMIT = 5;

    private static final DateTimeFormatter KICKOFF_FORMAT =
            DateTimeFormatter.ofPattern("EEE MMM d · HH:mm z");

    private final MatchRepository matchRepository;

    public UpcomingMatchTickerService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<UpcomingMatchTickerEntry> getUpcomingMatches(String timezoneId) {
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of(timezoneId);

        return matchRepository.findAllWithTeams().stream()
                .filter(match -> match.getKickoffUtc().isAfter(now))
                .filter(match -> match.getHomeTeam() != null && match.getAwayTeam() != null)
                .sorted(Comparator.comparing(Match::getKickoffUtc))
                .limit(UPCOMING_MATCH_LIMIT)
                .map(match -> toEntry(match, zoneId))
                .toList();
    }

    private UpcomingMatchTickerEntry toEntry(Match match, ZoneId zoneId) {
        String kickoffLabel = match.getKickoffUtc().atZone(zoneId).format(KICKOFF_FORMAT);
        return new UpcomingMatchTickerEntry(
                match.getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getName(),
                kickoffLabel,
                KnockoutStageLabels.isKnockout(match));
    }
}
