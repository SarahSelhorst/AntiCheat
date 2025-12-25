package org.example.AntiCheat.util;

public class MathUtil {

    public static double calculateHorizontalDistance(double dx, double dz) {
        return Math.hypot(dx, dz);
    }

    public static double calculateAverage(Iterable<Double> values) {
        double sum = 0;
        int count = 0;
        for (double value : values) {
            sum += value;
            count++;
        }
        return count > 0 ? sum / count : 0;
    }

    public static double calculateVariance(Iterable<Double> values, double average) {
        double variance = 0;
        int count = 0;
        for (double value : values) {
            variance += Math.pow(value - average, 2);
            count++;
        }
        return count > 0 ? variance / count : 0;
    }

    public static double calculateStandardDeviation(double variance) {
        return Math.sqrt(variance);
    }
}