package com.alper.worldcup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailDeliveryService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                @Value("${app.mail.enabled:false}") boolean mailEnabled,
                                @Value("${app.mail.from:noreply@worldcup.local}") String fromAddress) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public boolean isEnabled() {
        return mailEnabled && mailSender != null;
    }

    public void sendPlainText(String to, String subject, String body) {
        if (!isEnabled()) {
            log.debug("Mail disabled; skipped message to {}", to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Sent email to {}", to);
    }
}
