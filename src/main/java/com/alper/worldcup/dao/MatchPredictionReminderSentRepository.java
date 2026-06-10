package com.alper.worldcup.dao;

import com.alper.worldcup.entity.MatchPredictionReminderSent;
import com.alper.worldcup.entity.MatchPredictionReminderSentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPredictionReminderSentRepository
        extends JpaRepository<MatchPredictionReminderSent, MatchPredictionReminderSentId> {
}
