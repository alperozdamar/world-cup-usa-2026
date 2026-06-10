package com.alper.worldcup.scheduler;

import com.alper.worldcup.service.EmailDeliveryService;
import com.alper.worldcup.service.MatchPredictionReminderContent;
import com.alper.worldcup.service.MatchPredictionReminderEmailComposer;
import com.alper.worldcup.service.MatchPredictionReminderService;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HourlyMatchPredictionReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(HourlyMatchPredictionReminderScheduler.class);

    private final MatchPredictionReminderService matchPredictionReminderService;
    private final MatchPredictionReminderEmailComposer emailComposer;
    private final EmailDeliveryService emailDeliveryService;
    private final boolean remindersEnabled;
    private final boolean matchRemindersEnabled;

    public HourlyMatchPredictionReminderScheduler(
            MatchPredictionReminderService matchPredictionReminderService,
            MatchPredictionReminderEmailComposer emailComposer,
            EmailDeliveryService emailDeliveryService,
            @Value("${app.reminder.enabled:true}") boolean remindersEnabled,
            @Value("${app.reminder.match.enabled:true}") boolean matchRemindersEnabled) {
        this.matchPredictionReminderService = matchPredictionReminderService;
        this.emailComposer = emailComposer;
        this.emailDeliveryService = emailDeliveryService;
        this.remindersEnabled = remindersEnabled;
        this.matchRemindersEnabled = matchRemindersEnabled;
    }

    @Scheduled(cron = "${app.reminder.match.cron:0 0 * * * *}", zone = "${app.reminder.zone:UTC}")
    public void sendHourlyMatchReminders() {
        if (!remindersEnabled || !matchRemindersEnabled) {
            return;
        }
        if (!emailDeliveryService.isEnabled()) {
            log.debug("Hourly match reminders skipped — app.mail.enabled is false");
            return;
        }

        Instant now = Instant.now();
        List<MatchPredictionReminderContent> reminders =
                matchPredictionReminderService.buildHourlyReminders(now);
        if (reminders.isEmpty()) {
            log.debug("Hourly match reminders: nothing to send");
            return;
        }

        int sent = 0;
        for (MatchPredictionReminderContent reminder : reminders) {
            try {
                emailDeliveryService.sendPlainText(
                        reminder.email(),
                        emailComposer.subject(reminder),
                        emailComposer.compose(reminder));
                matchPredictionReminderService.markSent(reminder, now);
                sent++;
            } catch (Exception ex) {
                log.warn("Failed to send match reminder to {} for match {}: {}",
                        reminder.email(), reminder.matchId(), ex.getMessage());
            }
        }

        log.info("Hourly match reminders sent: {}/{}", sent, reminders.size());
    }
}
