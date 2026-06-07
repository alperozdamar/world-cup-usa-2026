package com.alper.worldcup.scheduler;

import com.alper.worldcup.service.EmailDeliveryService;
import com.alper.worldcup.service.PredictionReminderContent;
import com.alper.worldcup.service.PredictionReminderEmailComposer;
import com.alper.worldcup.service.PredictionReminderService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyPredictionReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyPredictionReminderScheduler.class);

    private final PredictionReminderService predictionReminderService;
    private final PredictionReminderEmailComposer emailComposer;
    private final EmailDeliveryService emailDeliveryService;
    private final boolean remindersEnabled;

    public DailyPredictionReminderScheduler(PredictionReminderService predictionReminderService,
                                            PredictionReminderEmailComposer emailComposer,
                                            EmailDeliveryService emailDeliveryService,
                                            @Value("${app.reminder.enabled:true}") boolean remindersEnabled) {
        this.predictionReminderService = predictionReminderService;
        this.emailComposer = emailComposer;
        this.emailDeliveryService = emailDeliveryService;
        this.remindersEnabled = remindersEnabled;
    }

    @Scheduled(cron = "${app.reminder.cron:0 0 14 * * *}", zone = "${app.reminder.zone:UTC}")
    public void sendDailyReminders() {
        if (!remindersEnabled) {
            return;
        }
        if (!emailDeliveryService.isEnabled()) {
            log.debug("Daily reminders skipped — app.mail.enabled is false");
            return;
        }

        List<PredictionReminderContent> reminders = predictionReminderService.buildDailyReminders();
        if (reminders.isEmpty()) {
            log.info("Daily reminders: nothing to send");
            return;
        }

        int sent = 0;
        for (PredictionReminderContent reminder : reminders) {
            try {
                emailDeliveryService.sendPlainText(
                        reminder.email(),
                        emailComposer.subject(),
                        emailComposer.compose(reminder));
                sent++;
            } catch (Exception ex) {
                log.warn("Failed to send reminder to {}: {}", reminder.email(), ex.getMessage());
            }
        }

        log.info("Daily reminders sent: {}/{}", sent, reminders.size());
    }
}
