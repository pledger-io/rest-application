package com.jongsoft.finance.core.value;

import java.time.temporal.ChronoUnit;

public enum Periodicity {
    MONTHS,
    WEEKS,
    YEARS;

    /**
     * Convert this periodicity to a {@link ChronoUnit} that can be used in the java time API.
     *
     * @return the ChronoUnit representing this periodicity
     */
    public ChronoUnit toChronoUnit() {
        return switch (this) {
            case WEEKS -> ChronoUnit.WEEKS;
            case MONTHS -> ChronoUnit.MONTHS;
            case YEARS -> ChronoUnit.YEARS;
        };
    }
}
