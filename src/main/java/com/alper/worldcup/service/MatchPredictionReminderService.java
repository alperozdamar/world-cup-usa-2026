package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchPredictionReminderSentRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchPredictionReminderSent;
import com.alper.worldcup.entity.MatchPredictionReminderSentId;
import com.alper.worldcup.entity.UserProfile;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchPredictionReminderService {

    private static final DateTimeFormatter KICKOFF_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy HH:mm z");

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final UserProfileRepository userProfileRepository;
    private final MatchPredictionReminderSentRepository reminderSentRepository;
    private final UserProfileService userProfileService;
    private final PoolMemberRegistry poolMemberRegistry;
    private final int hoursBeforeKickoff;

    public MatchPredictionReminderService(MatchRepository matchRepository,
                                          PredictionRepository predictionRepository,
                                          UserProfileRepository userProfileRepository,
                                          MatchPredictionReminderSentRepository reminderSentRepository,
                                          UserProfileService userProfileService,
                                          PoolMemberRegistry poolMemberRegistry,
                                          @Value("${app.reminder.match.hours-before:3}") int hoursBeforeKickoff) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.userProfileRepository = userProfileRepository;
        this.reminderSentRepository = reminderSentRepository;
        this.userProfileService = userProfileService;
        this.poolMemberRegistry = poolMemberRegistry;
        this.hoursBeforeKickoff = hoursBeforeKickoff;
    }

    @Transactional(readOnly = true)
    public List<MatchPredictionReminderContent> buildHourlyReminders(Instant now) {
        Instant cutoff = now.plus(Duration.ofHours(hoursBeforeKickoff));
        List<Match> matches = matchRepository.findOpenGroupStageMatchesKickingOffBetween(now, cutoff);
        List<MatchPredictionReminderContent> reminders = new ArrayList<>();

        for (UserProfile profile : userProfileRepository.findAllWithEmail()) {
            if (!poolMemberRegistry.isMember(profile.getUsername())) {
                continue;
            }
            for (Match match : matches) {
                if (predictionRepository.findByUsernameAndMatchId(profile.getUsername(), match.getId()).isPresent()) {
                    continue;
                }
                OptionalInt hoursBefore = hoursBeforeBucket(now, match.getKickoffUtc());
                if (hoursBefore.isEmpty()) {
                    continue;
                }
                MatchPredictionReminderSentId sentId = new MatchPredictionReminderSentId(
                        profile.getUsername(), match.getId(), hoursBefore.getAsInt());
                if (reminderSentRepository.existsById(sentId)) {
                    continue;
                }
                ZoneId zoneId = userProfileService.getUserZoneId(profile.getUsername());
                reminders.add(new MatchPredictionReminderContent(
                        profile.getUsername(),
                        userProfileService.getDisplayName(profile.getUsername()),
                        profile.getEmail(),
                        match.getId(),
                        match.getHomeDisplayName() + " vs " + match.getAwayDisplayName(),
                        match.getKickoffUtc().atZone(zoneId).format(KICKOFF_FORMAT),
                        hoursBefore.getAsInt()));
            }
        }

        return reminders;
    }

    @Transactional
    public void markSent(MatchPredictionReminderContent reminder, Instant sentAt) {
        reminderSentRepository.save(new MatchPredictionReminderSent(
                new MatchPredictionReminderSentId(
                        reminder.username(), reminder.matchId(), reminder.hoursBeforeKickoff()),
                sentAt));
    }

    static OptionalInt hoursBeforeBucket(Instant now, Instant kickoffUtc) {
        long minutesUntil = Duration.between(now, kickoffUtc).toMinutes();
        if (minutesUntil <= 0 || minutesUntil > 180) {
            return OptionalInt.empty();
        }
        if (minutesUntil > 120) {
            return OptionalInt.of(3);
        }
        if (minutesUntil > 60) {
            return OptionalInt.of(2);
        }
        return OptionalInt.of(1);
    }
}
