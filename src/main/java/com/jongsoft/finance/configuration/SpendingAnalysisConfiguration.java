package com.jongsoft.finance.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("spending.analysis")
public class SpendingAnalysisConfiguration {

    /** Legacy sensitivity factor for per-transaction amount z-scores (pre-refactor UnusualAmount). */
    private static final double AMOUNT_SENSITIVITY = 0.15;

    /** Legacy sensitivity factor for monthly frequency and total z-scores. */
    private static final double MONTHLY_SENSITIVITY = 0.7;

    private int baselineMonths;
    private double amountZThreshold;
    private double monthlyTotalZThreshold;
    private double frequencyZThreshold;
    private int patternLookbackMonths;
    private double patternSimilarityThreshold;
    private int patternMinMatches;

    public SpendingAnalysisConfiguration() {
        this.baselineMonths = 12;
        this.amountZThreshold = 2.0;
        this.monthlyTotalZThreshold = 1.5;
        this.frequencyZThreshold = 1.5;
        this.patternLookbackMonths = 12;
        this.patternSimilarityThreshold = 0.9;
        this.patternMinMatches = 3;
    }

    public void setAmountZThreshold(double amountZThreshold) {
        this.amountZThreshold = amountZThreshold;
    }

    public void setBaselineMonths(int baselineMonths) {
        this.baselineMonths = baselineMonths;
    }

    public void setFrequencyZThreshold(double frequencyZThreshold) {
        this.frequencyZThreshold = frequencyZThreshold;
    }

    public void setMonthlyTotalZThreshold(double monthlyTotalZThreshold) {
        this.monthlyTotalZThreshold = monthlyTotalZThreshold;
    }

    public void setPatternLookbackMonths(int patternLookbackMonths) {
        this.patternLookbackMonths = patternLookbackMonths;
    }

    public void setPatternSimilarityThreshold(double patternSimilarityThreshold) {
        this.patternSimilarityThreshold = patternSimilarityThreshold;
    }

    public void setPatternMinMatches(int patternMinMatches) {
        this.patternMinMatches = patternMinMatches;
    }

    public int baselineMonths() {
        return baselineMonths;
    }

    public double amountZThreshold() {
        return amountZThreshold;
    }

    public double monthlyTotalZThreshold() {
        return monthlyTotalZThreshold;
    }

    public double frequencyZThreshold() {
        return frequencyZThreshold;
    }

    public int patternLookbackMonths() {
        return patternLookbackMonths;
    }

    public double patternSimilarityThreshold() {
        return patternSimilarityThreshold;
    }

    public int patternMinMatches() {
        return patternMinMatches;
    }

    public double adjustedAmountThreshold() {
        return amountZThreshold * (2.0 - AMOUNT_SENSITIVITY);
    }

    public double adjustedFrequencyThreshold() {
        return frequencyZThreshold * (2.0 - MONTHLY_SENSITIVITY);
    }

    public double adjustedMonthlyTotalThreshold() {
        return monthlyTotalZThreshold * (2.0 - MONTHLY_SENSITIVITY);
    }
}
