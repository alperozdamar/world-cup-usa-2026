package com.alper.worldcup.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PredictionAuditTest {

    @Test
    void formatPredictionLabel_includesKnockoutExtras() {
        assertEquals("1 - 1 · Penalties: Yes · Advances: Canada",
                PredictionAudit.formatPredictionLabel(1, 1, true, "Canada"));
    }

    @Test
    void formatPredictionLabel_groupStageScoreOnly() {
        assertEquals("2 - 1", PredictionAudit.formatPredictionLabel(2, 1, null, null));
    }

    @Test
    void changeLabel_showsKnockoutBeforeAndAfter() {
        PredictionAudit audit = new PredictionAudit();
        audit.setAction(PredictionAuditAction.UPDATED);
        audit.setHomeScoreGuess(1);
        audit.setAwayScoreGuess(1);
        audit.setPenaltyShootoutGuess(true);
        audit.setAdvancingTeamName("Canada");
        audit.setPreviousHomeScoreGuess(2);
        audit.setPreviousAwayScoreGuess(1);
        audit.setPreviousAdvancingTeamName("Brazil");

        assertEquals("2 - 1 · Advances: Brazil → 1 - 1 · Penalties: Yes · Advances: Canada",
                audit.getChangeLabel());
    }
}
