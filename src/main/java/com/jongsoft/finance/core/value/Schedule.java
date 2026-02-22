package com.jongsoft.finance.core.value;

import java.io.Serializable;
import java.time.LocalDate;

public interface Schedule extends Serializable {

    /**
     * Fetch the periodicity of the schedule. Combined with the {@link #interval()} it determines
     * the duration between two triggers.
     *
     * @return the periodicity
     */
    Periodicity periodicity();

    /**
     * Fetch the interval of the schedule. Combined with the {@link #periodicity()} it determines
     * the duration between two triggers.
     *
     * @return the interval
     */
    int interval();

    default LocalDate previous(LocalDate current) {
        return current.minus(interval(), periodicity().toChronoUnit());
    }

    default LocalDate next(LocalDate current) {
        return current.plus(interval(), periodicity().toChronoUnit());
    }
}
