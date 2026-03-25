package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.TimeEntryProvider;
import com.jongsoft.finance.rest.TimeEntryFetcherApi;
import com.jongsoft.finance.rest.model.TimeEntryResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Controller
class TimeEntryFetcherController implements TimeEntryFetcherApi {

    private final Logger logger;
    private final TimeEntryProvider timeEntryProvider;

    public TimeEntryFetcherController(TimeEntryProvider timeEntryProvider) {
        this.timeEntryProvider = timeEntryProvider;
        this.logger = LoggerFactory.getLogger(TimeEntryFetcherController.class);
    }

    @Override
    public List<TimeEntryResponse> findTimeEntries(
            LocalDate startDate, LocalDate endDate, Long projectId, Boolean invoiced) {
        logger.info("Fetching time entries with filters.");
        return timeEntryProvider
                .lookup(startDate, endDate, projectId, invoiced)
                .map(TimeEntryMapper::toTimeEntryResponse)
                .toJava();
    }

    @Override
    public TimeEntryResponse getTimeEntryById(Long id) {
        logger.info("Fetching time entry {}.", id);
        return timeEntryProvider
                .lookup(id)
                .map(TimeEntryMapper::toTimeEntryResponse)
                .getOrThrow(() -> StatusException.notFound("Time entry is not found"));
    }
}
