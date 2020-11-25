package com.jongsoft.finance.core.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeOldTest {

    @Test
    void forMonth() {
        final DateRangeOld dateRange = DateRangeOld.forMonth(2012, 1);

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

        final DateRangeOld dateRange = DateRangeOld.currentYear();

        assertThat(dateRange).hasToString(year + "-01-01/" + year + "-12-31");
    }

    @Test
    void currentMonth() {
        LocalDate now = LocalDate.now();

        final DateRangeOld dateRange = DateRangeOld.currentMonth();
        assertThat(dateRange).isEqualTo(DateRangeOld.forMonth(now.getYear(), now.getMonthValue()));
    }

    @Test
    void isSame() {
        final DateRangeOld dateRange = DateRangeOld.forMonth(2012, 1);
        final DateRangeOld dateRange2 = DateRangeOld.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 1, 31));

        assertThat(dateRange).isEqualTo(dateRange2);
        assertThat(dateRange).hasSameHashCodeAs(dateRange2.hashCode());
    }

    @Test
    void isDifferent() {
        final DateRangeOld dateRange = DateRangeOld.forMonth(2012, 1);
        final DateRangeOld dateRange2 = DateRangeOld.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 1, 30));

        assertThat(dateRange).isNotEqualTo(dateRange2);
        assertThat(dateRange.hashCode()).isNotEqualTo(dateRange2.hashCode());
    }

    @Test
    void amountOfDays() {
        assertThat(DateRangeOld.forMonth(2012, 1).amountOfDays()).isEqualTo(30);
    }

    @Test
    void months() {
        final List<DateRangeOld> dateRanges = DateRangeOld.of(LocalDate.of(2012, 1, 1), LocalDate.of(2013, 1, 1))
                .months()
                .collect(Collectors.toList());

        assertThat(dateRanges)
                .hasSize(12)
                .contains(
                        DateRangeOld.forMonth(2012, 1),
                        DateRangeOld.forMonth(2012, 2),
                        DateRangeOld.forMonth(2012, 3),
                        DateRangeOld.forMonth(2012, 4),
                        DateRangeOld.forMonth(2012, 5),
                        DateRangeOld.forMonth(2012, 6),
                        DateRangeOld.forMonth(2012, 7),
                        DateRangeOld.forMonth(2012, 8),
                        DateRangeOld.forMonth(2012, 9),
                        DateRangeOld.forMonth(2012, 10),
                        DateRangeOld.forMonth(2012, 11),
                        DateRangeOld.forMonth(2012, 12));
    }

    @Test
    void previous() {
        var range = DateRangeOld.forMonth(2019, 1).previous(ChronoUnit.MONTHS);

        assertThat(range).isEqualTo(DateRangeOld.forMonth(2018, 12));
    }

}
