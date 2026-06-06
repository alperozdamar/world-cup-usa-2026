package com.alper.worldcup.dao;

import com.alper.worldcup.entity.GroupResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupResultRepository extends JpaRepository<GroupResult, String> {

    @Query("SELECT g FROM GroupResult g "
            + "JOIN FETCH g.firstPlaceTeam JOIN FETCH g.secondPlaceTeam ORDER BY g.groupName")
    List<GroupResult> findAllWithTeams();
}
