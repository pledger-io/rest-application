package com.jongsoft.finance.core.date;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public interface Dates {

    static LocalDate startOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }
    
    static LocalDate endOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1)
                .plusMonths(1)
                .minusDays(1);
    }
    
    static Long timestamp(LocalDate localDate) {
        return toDate(localDate).getTime();
    }

    static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

}
