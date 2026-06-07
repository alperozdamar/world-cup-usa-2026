package com.alper.worldcup.dao;

import com.alper.worldcup.entity.UserProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    @Query("SELECT p FROM UserProfile p WHERE p.email IS NOT NULL AND TRIM(p.email) <> ''")
    List<UserProfile> findAllWithEmail();
}
