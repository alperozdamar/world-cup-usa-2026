package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alper.worldcup.entity.MatchStage;
import org.junit.jupiter.api.Test;

class KnockoutStageLabelsTest {

    @Test
    void labelsAndOrder() {
        assertEquals("Round of 32", KnockoutStageLabels.label(MatchStage.ROUND_OF_32));
        assertEquals("Final", KnockoutStageLabels.label(MatchStage.FINAL));
        assertEquals(1, KnockoutStageLabels.displayOrder(MatchStage.ROUND_OF_32));
        assertEquals(6, KnockoutStageLabels.displayOrder(MatchStage.FINAL));
    }
}
