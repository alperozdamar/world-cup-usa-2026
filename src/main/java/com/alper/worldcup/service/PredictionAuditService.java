package com.alper.worldcup.service;

import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.entity.GroupStandingPredictionAudit;
import com.alper.worldcup.entity.PredictionAudit;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionAuditService {

    private final PredictionAuditRepository predictionAuditRepository;
    private final GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository;

    public PredictionAuditService(PredictionAuditRepository predictionAuditRepository,
                                    GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository) {
        this.predictionAuditRepository = predictionAuditRepository;
        this.groupStandingPredictionAuditRepository = groupStandingPredictionAuditRepository;
    }

    @Transactional
    public void recordPredictionChange(String username,
                                       com.alper.worldcup.entity.Match match,
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

    @Transactional
    public void recordGroupStandingChange(String username,
                                          String groupName,
                                          String firstTeamName,
                                          String secondTeamName,
                                          PredictionAuditAction action,
                                          String previousFirstTeamName,
                                          String previousSecondTeamName) {
        GroupStandingPredictionAudit audit = new GroupStandingPredictionAudit();
        audit.setUsername(username);
        audit.setGroupName(groupName);
        audit.setFirstTeamName(firstTeamName);
        audit.setSecondTeamName(secondTeamName);
        audit.setPreviousFirstTeamName(previousFirstTeamName);
        audit.setPreviousSecondTeamName(previousSecondTeamName);
        audit.setAction(action);
        audit.setRecordedAt(Instant.now());
        groupStandingPredictionAuditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<AuditEntryView> getCombinedAuditTrail(String username) {
        List<AuditEntryView> entries = new ArrayList<>();

        List<PredictionAudit> matchAudits = username == null || username.isBlank()
                ? predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()
                : predictionAuditRepository.findByUsernameWithMatchOrderByRecordedAtDesc(username);

        for (PredictionAudit audit : matchAudits) {
            entries.add(new AuditEntryView(
                    audit.getRecordedAt(),
                    audit.getUsername(),
                    audit.getAction(),
                    "Match",
                    audit.getMatchLabel(),
                    audit.getChangeLabel()));
        }

        List<GroupStandingPredictionAudit> groupAudits = username == null || username.isBlank()
                ? groupStandingPredictionAuditRepository.findAllOrderByRecordedAtDesc()
                : groupStandingPredictionAuditRepository.findByUsernameOrderByRecordedAtDesc(username);

        for (GroupStandingPredictionAudit audit : groupAudits) {
            entries.add(new AuditEntryView(
                    audit.getRecordedAt(),
                    audit.getUsername(),
                    audit.getAction(),
                    "Group",
                    audit.getGroupLabel(),
                    audit.getChangeLabel()));
        }

        entries.sort(Comparator.comparing(AuditEntryView::recordedAt).reversed());
        return entries;
    }
}
