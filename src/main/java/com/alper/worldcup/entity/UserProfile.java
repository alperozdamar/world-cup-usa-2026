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

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(length = 255)
    private String email;

    public UserProfile() {
    }

    public UserProfile(String username, String timezoneId, String displayName) {
        this.username = username;
        this.timezoneId = timezoneId;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
