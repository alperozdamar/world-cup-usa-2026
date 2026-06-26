package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

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

    @Column(name = "photo_content_type", length = 32)
    private String photoContentType;

    @Lob
    @Column(name = "photo_data", columnDefinition = "MEDIUMBLOB")
    private byte[] photoData;

    @Column(name = "photo_updated_at")
    private Instant photoUpdatedAt;

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

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public Instant getPhotoUpdatedAt() {
        return photoUpdatedAt;
    }

    public void setPhotoUpdatedAt(Instant photoUpdatedAt) {
        this.photoUpdatedAt = photoUpdatedAt;
    }
}
