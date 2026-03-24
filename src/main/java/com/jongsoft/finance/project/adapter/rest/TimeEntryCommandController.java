package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.ProjectProvider;
import com.jongsoft.finance.project.adapter.api.TimeEntryProvider;
import com.jongsoft.finance.project.domain.model.TimeEntry;
import com.jongsoft.finance.rest.TimeEntryCommandApi;
import com.jongsoft.finance.rest.model.TimeEntryRequest;
import com.jongsoft.finance.rest.model.TimeEntryResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Controller
public class TimeEntryCommandController implements TimeEntryCommandApi {

    private final Logger logger;
    private final ProjectProvider projectProvider;
    private final TimeEntryProvider timeEntryProvider;

    public TimeEntryCommandController(
            ProjectProvider projectProvider, TimeEntryProvider timeEntryProvider) {
        this.projectProvider = projectProvider;
        this.timeEntryProvider = timeEntryProvider;
        this.logger = LoggerFactory.getLogger(TimeEntryCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TimeEntryResponse> createTimeEntry(
            TimeEntryRequest timeEntryRequest) {
        logger.info("Creating time entry for project {}.", timeEntryRequest.getProjectId());

        var project = projectProvider
                .lookup(timeEntryRequest.getProjectId())
                .getOrThrow(() -> StatusException.notFound("Project is not found"));

        var description =
                timeEntryRequest.getDescription() != null ? timeEntryRequest.getDescription() : "";
        TimeEntry.create(
                project,
                timeEntryRequest.getDate(),
                BigDecimal.valueOf(timeEntryRequest.getHours()),
                description);

        var entries = timeEntryProvider
                .lookup(
                        timeEntryRequest.getDate(),
                        timeEntryRequest.getDate(),
                        timeEntryRequest.getProjectId(),
                        false)
                .toJava();
        if (entries.isEmpty()) {
            throw StatusException.internalError("Failed to create time entry");
        }
        var created = entries.get(entries.size() - 1);
        return HttpResponse.created(TimeEntryMapper.toTimeEntryResponse(created));
    }

    @Override
    public TimeEntryResponse updateTimeEntry(Long id, TimeEntryRequest timeEntryRequest) {
        logger.info("Updating time entry {}.", id);

        var entry = locateByIdOrThrow(id);
        entry.update(
                timeEntryRequest.getDate(),
                BigDecimal.valueOf(timeEntryRequest.getHours()),
                timeEntryRequest.getDescription() != null ? timeEntryRequest.getDescription() : "");

        return TimeEntryMapper.toTimeEntryResponse(entry);
    }

    @Override
    public HttpResponse<Void> deleteTimeEntryById(Long id) {
        logger.info("Deleting time entry {}.", id);

        locateByIdOrThrow(id).delete();
        return HttpResponse.noContent();
    }

    private TimeEntry locateByIdOrThrow(Long id) {
        return timeEntryProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Time entry is not found"));
    }
}
