package com.alper.worldcup.dao;

import com.alper.worldcup.entity.FinalPredictionAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalPredictionAuditRepository extends JpaRepository<FinalPredictionAudit, Integer> {

    long countByAction(com.alper.worldcup.entity.PredictionAuditAction action);

    List<FinalPredictionAudit> findAllByOrderByRecordedAtDesc();

    List<FinalPredictionAudit> findByUsernameOrderByRecordedAtDesc(String username);
}
