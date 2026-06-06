package com.alper.worldcup.service;

import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.PredictionAudit;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionAuditService {

    private final PredictionAuditRepository predictionAuditRepository;

    public PredictionAuditService(PredictionAuditRepository predictionAuditRepository) {
        this.predictionAuditRepository = predictionAuditRepository;
    }

    @Transactional
    public void recordPredictionChange(String username,
                                       Match match,
                                       int homeGuess,
                                       int awayGuess,
                                       PredictionAuditAction action,
                                       Integer previousHomeGuess,
                                       Integer previousAwayGuess) {
        PredictionAudit audit = new PredictionAudit();
        audit.setUsername(username);
        audit.setMatch(match);
        audit.setHomeTeamName(match.getHomeDisplayName());
        audit.setAwayTeamName(match.getAwayDisplayName());
        audit.setHomeScoreGuess(homeGuess);
        audit.setAwayScoreGuess(awayGuess);
        audit.setPreviousHomeScoreGuess(previousHomeGuess);
        audit.setPreviousAwayScoreGuess(previousAwayGuess);
        audit.setAction(action);
        audit.setRecordedAt(Instant.now());
        predictionAuditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<PredictionAudit> getAuditTrail(String username) {
        if (username == null || username.isBlank()) {
            return predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc();
        }
        return predictionAuditRepository.findByUsernameWithMatchOrderByRecordedAtDesc(username);
    }
}
