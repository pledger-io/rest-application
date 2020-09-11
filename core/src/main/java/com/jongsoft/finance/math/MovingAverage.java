package com.jongsoft.finance.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Queue;

public class MovingAverage {
    private final Queue<BigDecimal> window = new ArrayDeque<>();
    private final int period;
    private BigDecimal sum = BigDecimal.ZERO;

    public MovingAverage(int period) {
        this.period = period;
    }

    public void add(BigDecimal num) {
        sum = sum.add(num);
        window.add(num);

        if (window.size() > period) {
            sum = sum.subtract(window.remove());
        }
    }

    public BigDecimal getAverage() {
        if (window.isEmpty()) {
            return BigDecimal.ZERO;
        }

        var divisor = BigDecimal.valueOf(window.size());
        return sum.divide(divisor, 2, RoundingMode.HALF_UP);
    }

}
