package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchPredictionReminderSentRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchPredictionReminderSentId;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import com.alper.worldcup.entity.UserProfile;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchPredictionReminderServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private MatchPredictionReminderSentRepository reminderSentRepository;
    @Mock
    private UserProfileService userProfileService;

    private MatchPredictionReminderService service;

    @BeforeEach
    void setUp() {
        service = new MatchPredictionReminderService(
                matchRepository,
                predictionRepository,
                userProfileRepository,
                reminderSentRepository,
                userProfileService,
                new PoolMemberRegistry("group2"),
                3);
    }

    @Test
    void hoursBeforeBucketUsesThreeWindows() {
        Instant kickoff = Instant.parse("2026-06-11T19:00:00Z");

        assertEquals(OptionalInt.of(3), MatchPredictionReminderService.hoursBeforeBucket(
                Instant.parse("2026-06-11T16:01:00Z"), kickoff));
        assertEquals(OptionalInt.of(2), MatchPredictionReminderService.hoursBeforeBucket(
                Instant.parse("2026-06-11T17:01:00Z"), kickoff));
        assertEquals(OptionalInt.of(1), MatchPredictionReminderService.hoursBeforeBucket(
                Instant.parse("2026-06-11T18:01:00Z"), kickoff));
        assertTrue(MatchPredictionReminderService.hoursBeforeBucket(
                Instant.parse("2026-06-11T15:00:00Z"), kickoff).isEmpty());
    }

    @Test
    void sendsReminderForMissingPredictionInCurrentWindow() {
        Instant now = Instant.parse("2026-06-11T16:30:00Z");
        Match match = groupMatch(10, Instant.parse("2026-06-11T19:00:00Z"));
        UserProfile profile = new UserProfile("emre", "America/New_York", "Emre");
        profile.setEmail("emronelli@gmail.com");

        when(matchRepository.findOpenGroupStageMatchesKickingOffBetween(eq(now), any()))
                .thenReturn(List.of(match));
        when(userProfileRepository.findAllWithEmail()).thenReturn(List.of(profile));
        when(predictionRepository.findByUsernameAndMatchId("emre", 10)).thenReturn(Optional.empty());
        when(reminderSentRepository.existsById(new MatchPredictionReminderSentId("emre", 10, 3)))
                .thenReturn(false);
        when(userProfileService.getUserZoneId("emre")).thenReturn(ZoneId.of("America/New_York"));
        when(userProfileService.getDisplayName("emre")).thenReturn("Emre");

        List<MatchPredictionReminderContent> reminders = service.buildHourlyReminders(now);

        assertEquals(1, reminders.size());
        assertEquals("emre", reminders.get(0).username());
        assertEquals(3, reminders.get(0).hoursBeforeKickoff());
        assertEquals("Mexico vs South Africa", reminders.get(0).matchLabel());
    }

    @Test
    void skipsWhenReminderAlreadySentForWindow() {
        Instant now = Instant.parse("2026-06-11T16:30:00Z");
        Match match = groupMatch(10, Instant.parse("2026-06-11T19:00:00Z"));
        UserProfile profile = new UserProfile("emre", "America/New_York", "Emre");
        profile.setEmail("emronelli@gmail.com");

        when(matchRepository.findOpenGroupStageMatchesKickingOffBetween(eq(now), any()))
                .thenReturn(List.of(match));
        when(userProfileRepository.findAllWithEmail()).thenReturn(List.of(profile));
        when(predictionRepository.findByUsernameAndMatchId("emre", 10)).thenReturn(Optional.empty());
        when(reminderSentRepository.existsById(new MatchPredictionReminderSentId("emre", 10, 3)))
                .thenReturn(true);

        List<MatchPredictionReminderContent> reminders = service.buildHourlyReminders(now);

        assertTrue(reminders.isEmpty());
        verify(userProfileService, never()).getDisplayName("emre");
    }

    private Match groupMatch(int id, Instant kickoff) {
        Match match = new Match();
        match.setId(id);
        match.setStage(MatchStage.GROUP_STAGE);
        match.setKickoffUtc(kickoff);
        match.setPredictionsEnabled(true);
        Team home = new Team();
        home.setName("Mexico");
        Team away = new Team();
        away.setName("South Africa");
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        return match;
    }
}
