package com.alper.worldcup.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PointsFormat {

    private PointsFormat() {
    }

    public static String format(Double points) {
        if (points == null) {
            return "—";
        }
        return format(points.doubleValue());
    }

    public static String format(double points) {
        BigDecimal value = BigDecimal.valueOf(points).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return value.toPlainString();
    }

    public static String formatWithUnit(double points) {
        String amount = format(points);
        return amount + " pt" + (points == 1.0 ? "" : "s");
    }
}
