package com.jongsoft.finance.core.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {

    @Test
    void forMonth() {
        final DateRange dateRange = DateRange.forMonth(2012, 1);

        assertThat(dateRange.computeStartMonth()).isEqualTo(1);
        assertThat(dateRange.computeStartYear()).isEqualTo(2012);
        assertThat(dateRange.computeStart()).isEqualTo(new Date(1325376000000L));
        assertThat(dateRange.computeEnd()).isEqualTo(new Date(1327968000000L));
        assertThat(dateRange.getStart()).isEqualTo(LocalDate.of(2012, 1, 1));
        assertThat(dateRange.getEnd()).isEqualTo(LocalDate.of(2012, 1, 31));
        assertThat(dateRange).hasToString("2012-01-01/2012-01-31");
    }

    @Test
    void currentYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        final DateRange dateRange = DateRange.currentYear();

        assertThat(dateRange).hasToString(year + "-01-01/" + year + "-12-31");
    }

    @Test
    void currentMonth() {
        LocalDate now = LocalDate.now();

        final DateRange dateRange = DateRange.currentMonth();
        assertThat(dateRange).isEqualTo(DateRange.forMonth(now.getYear(), now.getMonthValue()));
    }

    @Test
    void isSame() {
        final DateRange dateRange = DateRange.forMonth(2012, 1);
        final DateRange dateRange2 = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 1, 31));

        assertThat(dateRange).isEqualTo(dateRange2);
        assertThat(dateRange).hasSameHashCodeAs(dateRange2.hashCode());
    }

    @Test
    void isDifferent() {
        final DateRange dateRange = DateRange.forMonth(2012, 1);
        final DateRange dateRange2 = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 1, 30));

        assertThat(dateRange).isNotEqualTo(dateRange2);
        assertThat(dateRange.hashCode()).isNotEqualTo(dateRange2.hashCode());
    }

    @Test
    void amountOfDays() {
        assertThat(DateRange.forMonth(2012, 1).amountOfDays()).isEqualTo(30);
    }

    @Test
    void months() {
        final List<DateRange> dateRanges = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2013, 1, 1))
                .months()
                .collect(Collectors.toList());

        assertThat(dateRanges)
                .hasSize(12)
                .contains(
                        DateRange.forMonth(2012, 1),
                        DateRange.forMonth(2012, 2),
                        DateRange.forMonth(2012, 3),
                        DateRange.forMonth(2012, 4),
                        DateRange.forMonth(2012, 5),
                        DateRange.forMonth(2012, 6),
                        DateRange.forMonth(2012, 7),
                        DateRange.forMonth(2012, 8),
                        DateRange.forMonth(2012, 9),
                        DateRange.forMonth(2012, 10),
                        DateRange.forMonth(2012, 11),
                        DateRange.forMonth(2012, 12));
    }

    @Test
    void previous() {
        var range = DateRange.forMonth(2019, 1).previous(ChronoUnit.MONTHS);

        assertThat(range).isEqualTo(DateRange.forMonth(2018, 12));
    }

}