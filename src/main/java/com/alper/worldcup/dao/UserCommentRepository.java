package com.alper.worldcup.dao;

import com.alper.worldcup.entity.UserComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommentRepository extends JpaRepository<UserComment, Integer> {

    List<UserComment> findAllByOrderByCreatedAtDesc();
}
