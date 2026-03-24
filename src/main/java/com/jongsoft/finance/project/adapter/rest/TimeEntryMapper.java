package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.jongsoft.finance.rest.model.TimeEntryResponse;

public interface TimeEntryMapper {

    static TimeEntryResponse toTimeEntryResponse(TimeEntry timeEntry) {
        var response = new TimeEntryResponse(
                timeEntry.getId(),
                ProjectMapper.toProjectResponse(timeEntry.getProject()),
                timeEntry.getDate(),
                timeEntry.getHours().doubleValue());
        response.setDescription(timeEntry.getDescription());
        response.setInvoiced(timeEntry.isInvoiced());
        return response;
    }
}
