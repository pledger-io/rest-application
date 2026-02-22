package com.jongsoft.finance.core.value;

import java.time.LocalDate;

public interface Schedulable extends WithId {

    /**
     * Limits the execution of the schedule to start no earlier then the start date and execute no
     * later then the provided end date.
     *
     * @param start the start date
     * @param end the end date
     */
    void limit(LocalDate start, LocalDate end);

    /**
     * Adjust the scheduling starting with the first next cycle. The current cycle is still
     * completed by the old settings.
     *
     * @param periodicity the new periodicity
     * @param interval the new interval of the periodicity
     */
    void adjustSchedule(Periodicity periodicity, int interval);

    LocalDate getStart();

    LocalDate getEnd();

    Schedule getSchedule();

    /**
     * Create a basic schedule without any modification options. Please note that the {@link
     * #limit(LocalDate, LocalDate)} and {@link #adjustSchedule(Periodicity, int)} will always throw
     * an {@link IllegalStateException}.
     *
     * @param id the id of the entity
     * @param endDate the end date of the schedule
     * @param schedule the actual schedule
     * @return
     */
    static Schedulable basicSchedule(long id, LocalDate endDate, Schedule schedule) {
        return new Schedulable() {
            @Override
            public void limit(LocalDate start, LocalDate end) {
                throw new IllegalStateException("Cannot limit schedule on a basic schedule.");
            }

            @Override
            public void adjustSchedule(Periodicity periodicity, int interval) {
                throw new IllegalStateException("Cannot adjust schedule on a basic schedule.");
            }

            @Override
            public LocalDate getStart() {
                return LocalDate.now().minusDays(1);
            }

            @Override
            public LocalDate getEnd() {
                return endDate;
            }

            @Override
            public Schedule getSchedule() {
                return schedule;
            }

            @Override
            public Long getId() {
                return id;
            }
        };
    }
}
