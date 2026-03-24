package com.jongsoft.finance.project.adapter.api;

import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import java.time.LocalDate;

public interface TimeEntryProvider {

    Optional<TimeEntry> lookup(long id);

    Sequence<TimeEntry> lookup(
            LocalDate startDate, LocalDate endDate, Long projectId, Boolean invoiced);
}
