package com.jongsoft.finance.core.date;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A date range is a value class that contains an upper and lower bound date, both bounds are to be included in the range.
 * A date range does not encompass the time element.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateRange {
    private LocalDate start;
    private LocalDate end;

    private DateRange(final LocalDate start, final LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    /**
     * Provide the start of the date range. This will always be at midnight UTC.
     *
     * @return the date
     */
    public Date computeStart() {
        return Date.from(start.atStartOfDay()
                .toInstant(ZoneOffset.UTC));
    }

    /**
     * Provide the end of the date range. This will always be at midnight UTC.
     *
     * @return the date
     */
    public Date computeEnd() {
        return Date.from(end.atStartOfDay()
                .toInstant(ZoneOffset.UTC));
    }

    /**
     * Calculate the starting year for the date range.
     *
     * @return the year
     */
    public int computeStartYear() {
        return start.getYear();
    }

    /**
     * Calculate the starting month for the date range.
     *
     * @return the month
     */
    public int computeStartMonth() {
        return start.getMonthValue();
    }

    /**
     * Calculates the amount of days that are in the date range.
     *
     * @return the amount of days
     */
    public int amountOfDays() {
        return (int) ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Create a stream of date ranges, one for each month between start and end.
     *
     * @return the stream
     */
    public Stream<DateRange> months() {
        var firstMonth = start.withDayOfMonth(1);
        var amountOfMonth = ChronoUnit.MONTHS.between(firstMonth, end.withDayOfMonth(1));

        return IntStream.range(0, (int) amountOfMonth)
                .mapToObj(firstMonth::plusMonths)
                .map(startOfMonth -> DateRange.forMonth(startOfMonth.getYear(), startOfMonth.getMonthValue()));
    }

    public DateRange previous(ChronoUnit unit) {
        var previousStart = start.minus(1, unit);
        var previousEnd = start.minusDays(1);

        return DateRange.of(previousStart, previousEnd);
    }

    public DateRange next(ChronoUnit unit) {
        return DateRange.of(start.plus(1, unit), end.plus(1, unit));
    }

    @Override
    public String toString() {
        return start.format(DateTimeFormatter.ISO_LOCAL_DATE) + "/"
                + end.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DateRange other) {
            return Objects.equals(other.start, start)
                    && Objects.equals(other.end, end);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 17 + start.hashCode() + end.hashCode();
    }

    /**
     * Creates a date range entity containing the current month.
     *
     * @return the created date range
     */
    public static DateRange currentMonth() {
        var start = LocalDate.now();

        return DateRange.forMonth(start.getYear(), start.getMonthValue());
    }

    /**
     * Create a new date range encompassing the entire month specified. The resulting date range will be from the 1st
     * day of the month until the last day of the month.
     *
     * @param year  the year
     * @param month the month
     * @return      the created date range
     */
    public static DateRange forMonth(int year, int month) {
        var start = LocalDate.of(year, month, 1);
        var end = start.plusMonths(1).minusDays(1);

        return DateRange.of(start, end);
    }

    public static DateRange forYear(int year) {
        var startDate = LocalDate.ofYearDay(year, 1);
        var endDate = startDate.plusYears(1).minusDays(1);

        return DateRange.of(startDate, endDate);
    }

    /**
     * Creates a date range entity containing the entire year.
     *
     * @return
     */
    public static DateRange currentYear() {
        return DateRange.forYear(LocalDate.now().getYear());
    }

    /**
     * Create a new date range with the specified start and end date.
     *
     * @param start the start date
     * @param end   the end date
     * @return      the created date range
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

}
