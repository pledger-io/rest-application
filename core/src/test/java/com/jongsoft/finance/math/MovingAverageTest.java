package com.jongsoft.finance.math;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MovingAverageTest {
    @Test
    void movingAverage() {
        var average = new MovingAverage(3);

        assertThat(average.getAverage()).isEqualByComparingTo(BigDecimal.ZERO);

        average.add(BigDecimal.valueOf(3));
        average.add(BigDecimal.valueOf(9));
        average.add(BigDecimal.valueOf(6));

        assertThat(average.getAverage()).isEqualByComparingTo(BigDecimal.valueOf(6));

        average.add(BigDecimal.valueOf(9));

        assertThat(average.getAverage()).isEqualByComparingTo(BigDecimal.valueOf(8));
    }
}