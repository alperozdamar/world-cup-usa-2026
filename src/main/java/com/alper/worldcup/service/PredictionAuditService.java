package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionAuditRepository;
import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.entity.FinalPredictionAudit;
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
    private final FinalPredictionAuditRepository finalPredictionAuditRepository;

    public PredictionAuditService(PredictionAuditRepository predictionAuditRepository,
                                    GroupStandingPredictionAuditRepository groupStandingPredictionAuditRepository,
                                    FinalPredictionAuditRepository finalPredictionAuditRepository) {
        this.predictionAuditRepository = predictionAuditRepository;
        this.groupStandingPredictionAuditRepository = groupStandingPredictionAuditRepository;
        this.finalPredictionAuditRepository = finalPredictionAuditRepository;
    }

    @Transactional
    public void recordPredictionChange(String username,
                                       com.alper.worldcup.entity.Match match,
                                       int homeGuess,
                                       int awayGuess,
                                       PredictionAuditAction action,
                                       Integer previousHomeGuess,
                                       Integer previousAwayGuess) {
        recordPredictionChange(username, match, homeGuess, awayGuess, action,
                previousHomeGuess, previousAwayGuess, null, null, null, null);
    }

    @Transactional
    public void recordPredictionChange(String username,
                                       com.alper.worldcup.entity.Match match,
                                       int homeGuess,
                                       int awayGuess,
                                       PredictionAuditAction action,
                                       Integer previousHomeGuess,
                                       Integer previousAwayGuess,
                                       Boolean penaltyShootoutGuess,
                                       String advancingTeamName,
                                       Boolean previousPenaltyShootoutGuess,
                                       String previousAdvancingTeamName) {
        PredictionAudit audit = new PredictionAudit();
        audit.setUsername(username);
        audit.setMatch(match);
        audit.setHomeTeamName(match.getHomeDisplayName());
        audit.setAwayTeamName(match.getAwayDisplayName());
        audit.setHomeScoreGuess(homeGuess);
        audit.setAwayScoreGuess(awayGuess);
        audit.setPreviousHomeScoreGuess(previousHomeGuess);
        audit.setPreviousAwayScoreGuess(previousAwayGuess);
        audit.setPenaltyShootoutGuess(penaltyShootoutGuess);
        audit.setAdvancingTeamName(advancingTeamName);
        audit.setPreviousPenaltyShootoutGuess(previousPenaltyShootoutGuess);
        audit.setPreviousAdvancingTeamName(previousAdvancingTeamName);
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

    @Transactional
    public void recordFinalPredictionChange(String username,
                                            String championTeamName,
                                            String runnerUpTeamName,
                                            PredictionAuditAction action,
                                            String previousChampionTeamName,
                                            String previousRunnerUpTeamName) {
        FinalPredictionAudit audit = new FinalPredictionAudit();
        audit.setUsername(username);
        audit.setChampionTeamName(championTeamName);
        audit.setRunnerUpTeamName(runnerUpTeamName);
        audit.setPreviousChampionTeamName(previousChampionTeamName);
        audit.setPreviousRunnerUpTeamName(previousRunnerUpTeamName);
        audit.setAction(action);
        audit.setRecordedAt(Instant.now());
        finalPredictionAuditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<AuditEntryView> getCombinedAuditTrail(String username) {
        List<AuditEntryView> entries = new ArrayList<>();

        List<PredictionAudit> matchAudits = username == null || username.isBlank()
                ? predictionAuditRepository.findAllWithMatchOrderByRecordedAtDesc()
                : predictionAuditRepository.findByUsernameWithMatchOrderByRecordedAtDesc(username);

        for (PredictionAudit audit : matchAudits) {
            String type = KnockoutStageLabels.isKnockout(audit.getMatch()) ? "Knockout" : "Match";
            entries.add(new AuditEntryView(
                    audit.getRecordedAt(),
                    audit.getUsername(),
                    audit.getAction(),
                    type,
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

        List<FinalPredictionAudit> finalAudits = username == null || username.isBlank()
                ? finalPredictionAuditRepository.findAllByOrderByRecordedAtDesc()
                : finalPredictionAuditRepository.findByUsernameOrderByRecordedAtDesc(username);

        for (FinalPredictionAudit audit : finalAudits) {
            entries.add(new AuditEntryView(
                    audit.getRecordedAt(),
                    audit.getUsername(),
                    audit.getAction(),
                    "Final",
                    audit.getSubjectLabel(),
                    audit.getChangeLabel()));
        }

        entries.sort(Comparator.comparing(AuditEntryView::recordedAt).reversed());
        return entries;
    }
}
