package com.jongsoft.finance.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

class DateUtilsTest {

    @Test
    void startOfMonth() {
        Assertions.assertThat(DateUtils.startOfMonth(2019, 2))
                .isEqualTo(LocalDate.of(2019, 2, 1));
    }

    @Test
    void endOfMonth() {
        Assertions.assertThat(DateUtils.endOfMonth(2019, 2))
                .isEqualTo(LocalDate.of(2019, 2, 28));
    }

    @Test
    void timestamp() {
        Assertions.assertThat(DateUtils.timestamp(LocalDate.of(2019, 2, 1)))
                .isEqualTo(1548979200000L);
    }

    @Test
    void toLocalDate() {
        Assertions.assertThat(DateUtils.toLocalDate(new Date(1548979200000L)))
                .isEqualTo(LocalDate.of(2019, 2, 1));
    }

    @Test
    void toDate() {
        Assertions.assertThat(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                .isEqualTo(new Date(1548979200000L));
    }

    @Test
    void forMonth() {
        var range = DateUtils.forMonth(2020, 1);

        Assertions.assertThat(range.from()).isEqualTo(LocalDate.of(2020, 1, 1));
        Assertions.assertThat(range.until()).isEqualTo(LocalDate.of(2020, 2, 1));
    }
}