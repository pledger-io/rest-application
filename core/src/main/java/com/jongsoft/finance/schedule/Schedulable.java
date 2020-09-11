package com.jongsoft.finance.schedule;

import java.time.LocalDate;

public interface Schedulable {

    /**
     * Limits the execution of the schedule to start no earlier then the start date and execute no later then the
     * provided end date.
     *
     * @param start the start date
     * @param end   the end date
     */
    void limit(LocalDate start, LocalDate end);

    /**
     * Adjust the scheduling starting with the first next cycle. The current cycle is still completed by the old
     * settings.
     *
     * @param periodicity the new periodicity
     * @param interval    the new interval of the periodicity
     */
    void adjustSchedule(Periodicity periodicity, int interval);

    LocalDate getStart();

    LocalDate getEnd();

    Schedule getSchedule();

}
