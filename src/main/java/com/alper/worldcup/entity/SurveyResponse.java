package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {

    @Id
    @Column(length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "fairness", nullable = false, length = 40)
    private SurveyFairness fairness;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_visibility", nullable = false, length = 40)
    private SurveyScoreVisibility scoreVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_time", nullable = false, length = 40)
    private SurveyExtraTime extraTime;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    public SurveyResponse() {
    }

    public SurveyResponse(String username,
                          SurveyFairness fairness,
                          SurveyScoreVisibility scoreVisibility,
                          SurveyExtraTime extraTime,
                          Instant submittedAt) {
        this.username = username;
        this.fairness = fairness;
        this.scoreVisibility = scoreVisibility;
        this.extraTime = extraTime;
        this.submittedAt = submittedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SurveyFairness getFairness() {
        return fairness;
    }

    public void setFairness(SurveyFairness fairness) {
        this.fairness = fairness;
    }

    public SurveyScoreVisibility getScoreVisibility() {
        return scoreVisibility;
    }

    public void setScoreVisibility(SurveyScoreVisibility scoreVisibility) {
        this.scoreVisibility = scoreVisibility;
    }

    public SurveyExtraTime getExtraTime() {
        return extraTime;
    }

    public void setExtraTime(SurveyExtraTime extraTime) {
        this.extraTime = extraTime;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
