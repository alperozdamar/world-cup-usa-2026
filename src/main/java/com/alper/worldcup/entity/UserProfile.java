package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(length = 50)
    private String username;

    @Column(name = "timezone_id", nullable = false, length = 64)
    private String timezoneId;

    public UserProfile() {
    }

    public UserProfile(String username, String timezoneId) {
        this.username = username;
        this.timezoneId = timezoneId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }
}
