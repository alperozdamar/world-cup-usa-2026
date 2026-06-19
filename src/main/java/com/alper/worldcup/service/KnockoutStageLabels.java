package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;

public final class KnockoutStageLabels {

    private KnockoutStageLabels() {
    }

    public static String label(MatchStage stage) {
        if (stage == null) {
            return "Unknown";
        }
        return switch (stage) {
            case ROUND_OF_32 -> "Round of 32";
            case ROUND_OF_16 -> "Round of 16";
            case QUARTER_FINAL -> "Quarter-final";
            case SEMI_FINAL -> "Semi-final";
            case THIRD_PLACE -> "Third place";
            case FINAL -> "Final";
            default -> stage.name();
        };
    }

    public static int displayOrder(MatchStage stage) {
        if (stage == null) {
            return 99;
        }
        return switch (stage) {
            case ROUND_OF_32 -> 1;
            case ROUND_OF_16 -> 2;
            case QUARTER_FINAL -> 3;
            case SEMI_FINAL -> 4;
            case THIRD_PLACE -> 5;
            case FINAL -> 6;
            default -> 99;
        };
    }

    public static boolean isKnockout(Match match) {
        return match != null && match.getStage() != MatchStage.GROUP_STAGE;
    }
}
