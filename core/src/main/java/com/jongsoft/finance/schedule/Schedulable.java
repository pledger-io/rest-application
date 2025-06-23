package com.jongsoft.finance.schedule;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import java.time.LocalDate;

public interface Schedulable extends AggregateBase {

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
   * an {@link StatusException}.
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
        throw StatusException.badRequest("Cannot limit schedule on a basic schedule.");
      }

      @Override
      public void adjustSchedule(Periodicity periodicity, int interval) {
        throw StatusException.badRequest("Cannot adjust schedule on a basic schedule.");
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
