package com.alper.worldcup.dao;

import com.alper.worldcup.entity.GroupStandingPredictionAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupStandingPredictionAuditRepository
        extends JpaRepository<GroupStandingPredictionAudit, Integer> {

    long countByAction(com.alper.worldcup.entity.PredictionAuditAction action);

    @Query("SELECT a FROM GroupStandingPredictionAudit a ORDER BY a.recordedAt DESC")
    List<GroupStandingPredictionAudit> findAllOrderByRecordedAtDesc();

    List<GroupStandingPredictionAudit> findByUsernameOrderByRecordedAtDesc(String username);
}
