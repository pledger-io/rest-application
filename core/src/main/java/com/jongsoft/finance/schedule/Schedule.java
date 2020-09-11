package com.jongsoft.finance.schedule;

import java.io.Serializable;

public interface Schedule extends Serializable {

    /**
     * Fetch the periodicity of the schedule. Combined with the {@link #interval()} it
     * determines the duration between two triggers.
     *
     * @return the periodicity
     */
    Periodicity periodicity();

    /**
     * Fetch the interval of the schedule. Combined with the {@link #periodicity()} it
     * determines the duration between two triggers.
     *
     * @return the interval
     */
    int interval();

}
