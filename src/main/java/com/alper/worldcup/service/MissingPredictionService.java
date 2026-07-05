package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissingPredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;

    public MissingPredictionService(MatchRepository matchRepository,
                                    PredictionRepository predictionRepository,
                                    UserProfileService userProfileService,
                                    PoolMemberRegistry poolMemberRegistry) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
    }

    @Transactional(readOnly = true)
    public LocalDate nextOpenMatchDay(ZoneId zoneId) {
        Instant now = Instant.now();
        return collectUpcomingOpenMatches(now).stream()
                .min(Comparator.comparing(Match::getKickoffUtc))
                .map(match -> match.getKickoffUtc().atZone(zoneId).toLocalDate())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Match> openMatchesOnNextMatchDay(ZoneId zoneId) {
        Instant now = Instant.now();
        LocalDate nextDay = nextOpenMatchDay(zoneId);
        if (nextDay == null) {
            return List.of();
        }
        return collectUpcomingOpenMatches(now).stream()
                .filter(match -> match.getKickoffUtc().atZone(zoneId).toLocalDate().equals(nextDay))
                .sorted(Comparator.comparing(Match::getKickoffUtc))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MissingPredictionMember> findMissingForMatch(Match match) {
        Set<String> predicted = new HashSet<>();
        for (Prediction prediction : predictionRepository.findByMatchId(match.getId())) {
            if (poolMemberRegistry.isMember(prediction.getUsername())) {
                predicted.add(prediction.getUsername());
            }
        }

        return poolMemberRegistry.getMembers().stream()
                .filter(member -> !predicted.contains(member.username()))
                .map(member -> new MissingPredictionMember(
                        member.username(),
                        userProfileService.getDisplayName(member.username())))
                .sorted(Comparator.comparing(MissingPredictionMember::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> describeMissingMatchesForUser(String username, ZoneId zoneId) {
        List<String> descriptions = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        for (Match match : openMatchesOnNextMatchDay(zoneId)) {
            if (hasPrediction(username, match.getId())) {
                continue;
            }
            String kickoff = match.getKickoffUtc().atZone(zoneId).format(timeFormatter);
            descriptions.add(match.getHomeDisplayName() + " vs " + match.getAwayDisplayName()
                    + " (locks " + kickoff + ")");
        }
        return descriptions;
    }

    @Transactional(readOnly = true)
    public boolean isOnNextOpenMatchDay(Match match, ZoneId zoneId) {
        LocalDate nextDay = nextOpenMatchDay(zoneId);
        if (nextDay == null) {
            return false;
        }
        return match.getKickoffUtc().atZone(zoneId).toLocalDate().equals(nextDay);
    }

    private boolean hasPrediction(String username, Integer matchId) {
        return predictionRepository.findByUsernameAndMatchId(username, matchId).isPresent();
    }

    private List<Match> collectUpcomingOpenMatches(Instant now) {
        List<Match> upcoming = new ArrayList<>();

        matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE).stream()
                .filter(match -> match.getKickoffUtc().isAfter(now))
                .filter(Match::isPredictionsEnabled)
                .forEach(upcoming::add);

        matchRepository.findKnockoutMatchesWithTeams().stream()
                .filter(match -> match.getKickoffUtc().isAfter(now))
                .filter(Match::isPredictionsEnabled)
                .filter(match -> match.getHomeTeam() != null && match.getAwayTeam() != null)
                .forEach(upcoming::add);

        return upcoming;
    }
}
