package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.SurveyResponseRepository;
import com.alper.worldcup.entity.SurveyExtraTime;
import com.alper.worldcup.entity.SurveyFairness;
import com.alper.worldcup.entity.SurveyResponse;
import com.alper.worldcup.entity.SurveyScoreVisibility;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
        surveyService = new SurveyService(surveyResponseRepository);
    }

    @Test
    void submitSavesOncePerUser() {
        when(surveyResponseRepository.existsById("alper")).thenReturn(false);

        surveyService.submit(
                "alper",
                SurveyFairness.KATILIYORUM,
                SurveyScoreVisibility.SADECE_ADMIN,
                SurveyExtraTime.GEREK_YOK);

        ArgumentCaptor<SurveyResponse> captor = ArgumentCaptor.forClass(SurveyResponse.class);
        verify(surveyResponseRepository).save(captor.capture());
        assertEquals("alper", captor.getValue().getUsername());
        assertEquals(SurveyFairness.KATILIYORUM, captor.getValue().getFairness());
    }

    @Test
    void submitRejectsSecondResponse() {
        when(surveyResponseRepository.existsById("alper")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> surveyService.submit(
                "alper",
                SurveyFairness.KATILIYORUM,
                SurveyScoreVisibility.HERKES_GOREBILSIN,
                SurveyExtraTime.UZATMA_EKLANSIN));
    }

    @Test
    void getResultsAggregatesCounts() {
        when(surveyResponseRepository.findAll()).thenReturn(List.of(
                new SurveyResponse("alper", SurveyFairness.KATILIYORUM,
                        SurveyScoreVisibility.SADECE_ADMIN, SurveyExtraTime.GEREK_YOK, Instant.now()),
                new SurveyResponse("sadik", SurveyFairness.KATILIYORUM,
                        SurveyScoreVisibility.HERKES_GOREBILSIN, SurveyExtraTime.UZATMA_EKLANSIN, Instant.now())));

        SurveyResultsView results = surveyService.getResults();
        assertEquals(2, results.totalResponses());
        assertTrue(results.fairness().stream()
                .anyMatch(opt -> opt.key().equals("KATILIYORUM") && opt.count() == 2 && opt.percent() == 100));
        assertTrue(results.scoreVisibility().stream()
                .anyMatch(opt -> opt.key().equals("SADECE_ADMIN") && opt.count() == 1 && opt.percent() == 50));
    }
}
