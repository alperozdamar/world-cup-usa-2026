package com.alper.worldcup.service;

import com.alper.worldcup.dao.SurveyResponseRepository;
import com.alper.worldcup.entity.SurveyExtraTime;
import com.alper.worldcup.entity.SurveyFairness;
import com.alper.worldcup.entity.SurveyResponse;
import com.alper.worldcup.entity.SurveyScoreVisibility;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {

    private final SurveyResponseRepository surveyResponseRepository;

    public SurveyService(SurveyResponseRepository surveyResponseRepository) {
        this.surveyResponseRepository = surveyResponseRepository;
    }

    @Transactional(readOnly = true)
    public Optional<SurveyResponse> findByUsername(String username) {
        return surveyResponseRepository.findById(username);
    }

    @Transactional(readOnly = true)
    public boolean hasResponded(String username) {
        return surveyResponseRepository.existsById(username);
    }

    @Transactional
    public void submit(String username,
                       SurveyFairness fairness,
                       SurveyScoreVisibility scoreVisibility,
                       SurveyExtraTime extraTime) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Kullanıcı gerekli.");
        }
        if (fairness == null || scoreVisibility == null || extraTime == null) {
            throw new IllegalArgumentException("Lütfen tüm soruları yanıtlayın.");
        }
        if (surveyResponseRepository.existsById(username)) {
            throw new IllegalStateException("Anketi zaten doldurdunuz.");
        }
        surveyResponseRepository.save(new SurveyResponse(
                username, fairness, scoreVisibility, extraTime, Instant.now()));
    }

    @Transactional(readOnly = true)
    public SurveyResultsView getResults() {
        List<SurveyResponse> all = surveyResponseRepository.findAll();
        long total = all.size();

        Map<SurveyFairness, Long> fairnessCounts = new EnumMap<>(SurveyFairness.class);
        Map<SurveyScoreVisibility, Long> visibilityCounts = new EnumMap<>(SurveyScoreVisibility.class);
        Map<SurveyExtraTime, Long> extraTimeCounts = new EnumMap<>(SurveyExtraTime.class);
        for (SurveyFairness value : SurveyFairness.values()) {
            fairnessCounts.put(value, 0L);
        }
        for (SurveyScoreVisibility value : SurveyScoreVisibility.values()) {
            visibilityCounts.put(value, 0L);
        }
        for (SurveyExtraTime value : SurveyExtraTime.values()) {
            extraTimeCounts.put(value, 0L);
        }
        for (SurveyResponse response : all) {
            fairnessCounts.merge(response.getFairness(), 1L, Long::sum);
            visibilityCounts.merge(response.getScoreVisibility(), 1L, Long::sum);
            extraTimeCounts.merge(response.getExtraTime(), 1L, Long::sum);
        }

        return new SurveyResultsView(
                total,
                toCounts(SurveyFairness.values(), fairnessCounts, this::fairnessLabel, total),
                toCounts(SurveyScoreVisibility.values(), visibilityCounts, this::visibilityLabel, total),
                toCounts(SurveyExtraTime.values(), extraTimeCounts, this::extraTimeLabel, total));
    }

    private <E extends Enum<E>> List<SurveyOptionCount> toCounts(E[] values,
                                                                  Map<E, Long> counts,
                                                                  java.util.function.Function<E, String> labeler,
                                                                  long total) {
        return Arrays.stream(values)
                .map(value -> {
                    long count = counts.getOrDefault(value, 0L);
                    int percent = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
                    return new SurveyOptionCount(value.name(), labeler.apply(value), count, percent);
                })
                .toList();
    }

    String fairnessLabel(SurveyFairness value) {
        return switch (value) {
            case KATILIYORUM -> "Katılıyorum";
            case KISMEN_KATILIYORUM -> "Kısmen katılıyorum";
            case KISMEN_KATILMIYORUM -> "Kısmen katılmıyorum";
            case HIC_KATILMIYORUM -> "Hiç katılmıyorum";
        };
    }

    String visibilityLabel(SurveyScoreVisibility value) {
        return switch (value) {
            case HERKES_GOREBILSIN -> "Herkes herkesi görebilsin";
            case SADECE_ADMIN -> "Aynen devam (sadece admin görebilsin)";
            case ENCRYPTION -> "Admin / developer de göremesin (DB encryption)";
        };
    }

    String extraTimeLabel(SurveyExtraTime value) {
        return switch (value) {
            case GEREK_YOK -> "Gerek yok, aynen devam";
            case UZATMA_EKLANSIN -> "Uzatma skoru tahmini de eklensin";
        };
    }
}
