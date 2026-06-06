package com.alper.worldcup.dao;

import com.alper.worldcup.entity.FinalResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FinalResultRepository extends JpaRepository<FinalResult, String> {

    @Query("SELECT f FROM FinalResult f "
            + "JOIN FETCH f.championTeam JOIN FETCH f.runnerUpTeam WHERE f.id = :id")
    Optional<FinalResult> findByIdWithTeams(String id);
}
