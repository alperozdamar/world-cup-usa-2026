package com.alper.worldcup.dao;

import com.alper.worldcup.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    Optional<Team> findByName(String name);
}
