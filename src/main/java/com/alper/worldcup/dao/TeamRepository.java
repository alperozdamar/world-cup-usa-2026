package com.alper.worldcup.dao;

import com.alper.worldcup.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    Optional<Team> findByName(String name);

    List<Team> findByGroupNameOrderByNameAsc(String groupName);

    @Query("SELECT DISTINCT t.groupName FROM Team t WHERE t.groupName IS NOT NULL ORDER BY t.groupName")
    List<String> findDistinctGroupNames();
}
