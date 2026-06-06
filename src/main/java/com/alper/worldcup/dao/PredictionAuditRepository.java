package com.alper.worldcup.dao;

import com.alper.worldcup.entity.PredictionAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PredictionAuditRepository extends JpaRepository<PredictionAudit, Integer> {

    @Query("SELECT a FROM PredictionAudit a JOIN FETCH a.match ORDER BY a.recordedAt DESC")
    List<PredictionAudit> findAllWithMatchOrderByRecordedAtDesc();

    @Query("SELECT a FROM PredictionAudit a JOIN FETCH a.match "
            + "WHERE a.username = :username ORDER BY a.recordedAt DESC")
    List<PredictionAudit> findByUsernameWithMatchOrderByRecordedAtDesc(String username);
}
