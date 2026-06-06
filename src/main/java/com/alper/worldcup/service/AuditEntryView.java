package com.alper.worldcup.service;

import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Instant;

public record AuditEntryView(
        Instant recordedAt,
        String username,
        PredictionAuditAction action,
        String type,
        String subjectLabel,
        String changeLabel) {
}
