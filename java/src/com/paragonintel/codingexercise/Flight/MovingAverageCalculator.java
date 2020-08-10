package com.paragonintel.codingexercise.Flight;

import java.util.LinkedList;

/**
 * This purpose of this class is to maintain a moving average of values. The window-size
 * is configured when constructed, and whenever a new value is added, we remove elements
 * whose time is outside the window. Example usage: average speed in the last 3 minutes.
 */
public class MovingAverageCalculator {

    private LinkedList<Double> values;
    private LinkedList<Long> times;

    private Integer runningCount;
    private Double runningValueSum;
    private Long windowSize;

    public MovingAverageCalculator(Long windowSizeMs) {
        runningValueSum = 0D;
        runningCount = 0;

        values = new LinkedList<>();
        times = new LinkedList<>();

        this.windowSize = windowSizeMs;
    }

    public void ingest(Double value, Long timestamp) {
        values.add(value);
        times.add(timestamp);

        runningValueSum += value;
        runningCount += 1;

        removeDataOlderThanTime(timestamp - windowSize);
    }

    public Double getAverageValue() {
        if (runningCount == 0) return Double.NaN;
        return runningValueSum / runningCount;
    }

    private void removeDataOlderThanTime(Long oldestAllowedTime) {
        Long oldestTime;
        while ((oldestTime = times.peek()) != null) {
            if (oldestTime < oldestAllowedTime) {
                times.pop();
                runningValueSum -= values.pop();;
                runningCount -= 1;
            } else {
                break;
            }
        }
    }
}