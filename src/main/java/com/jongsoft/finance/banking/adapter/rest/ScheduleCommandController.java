package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.model.ScheduleValue;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.rest.ScheduleCommandApi;
import com.jongsoft.finance.rest.model.TransactionSchedulePatchRequest;
import com.jongsoft.finance.rest.model.TransactionScheduleRequest;
import com.jongsoft.finance.rest.model.TransactionScheduleResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;

import java.time.LocalDate;

@Controller
class ScheduleCommandController implements ScheduleCommandApi {

    private final Logger logger;
    private final AccountProvider accountProvider;
    private final TransactionScheduleProvider scheduleProvider;

    ScheduleCommandController(
            AccountProvider accountProvider, TransactionScheduleProvider scheduleProvider) {
        this.accountProvider = accountProvider;
        this.scheduleProvider = scheduleProvider;
        this.logger = org.slf4j.LoggerFactory.getLogger(ScheduleCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TransactionScheduleResponse> createSchedule(
            TransactionScheduleRequest scheduleRequest) {
        logger.info("Creating new transaction schedule {}.", scheduleRequest.getName());

        var source = accountProvider
                .lookup(scheduleRequest.getTransferBetween().getSource().getId())
                .getOrThrow(() -> StatusException.badRequest(
                        "The source account cannot be found.", "contract.source.not.found"));
        var destination = accountProvider
                .lookup(scheduleRequest.getTransferBetween().getDestination().getId())
                .getOrThrow(() -> StatusException.badRequest(
                        "The destination account cannot be found.",
                        "contract.destination.not.found"));

        TransactionSchedule.create(
                scheduleRequest.getName(),
                new ScheduleValue(
                        Periodicity.valueOf(
                                scheduleRequest.getSchedule().getPeriodicity().name()),
                        scheduleRequest.getSchedule().getInterval()),
                source,
                destination,
                scheduleRequest.getAmount());

        var schedule = scheduleProvider
                .lookup()
                .first(s -> s.getName().equals(scheduleRequest.getName()))
                .getOrThrow(
                        () -> StatusException.internalError("Could not locate created schedule"));
        return HttpResponse.created(ScheduleMapper.toScheduleResponse(schedule));
    }

    @Override
    public HttpResponse<Void> deleteSchedule(Long id) {
        logger.info("Deleting transaction schedule {}.", id);

        var schedule = lookupScheduledTransactionOrThrow(id);
        schedule.terminate();
        return HttpResponse.noContent();
    }

    @Override
    public TransactionScheduleResponse updateSchedule(
            Long id, TransactionSchedulePatchRequest schedulePatchRequest) {
        logger.info("Updating transaction schedule {}.", id);
        var schedule = lookupScheduledTransactionOrThrow(id);

        if (schedulePatchRequest.getName() != null) {
            schedule.describe(
                    schedulePatchRequest.getName(), schedulePatchRequest.getDescription());
        }

        if (schedulePatchRequest.getSchedule() != null) {
            schedule.adjustSchedule(
                    Periodicity.valueOf(
                            schedulePatchRequest.getSchedule().getPeriodicity().name()),
                    schedulePatchRequest.getSchedule().getInterval());
        }

        if (schedulePatchRequest.getActiveBetween() != null) {
            schedule.limit(
                    schedulePatchRequest.getActiveBetween().getStartDate(),
                    schedulePatchRequest.getActiveBetween().getEndDate());
        }

        return ScheduleMapper.toScheduleResponse(schedule);
    }

    private TransactionSchedule lookupScheduledTransactionOrThrow(Long id) {
        var schedule = scheduleProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("The schedule cannot be found."));
        if (schedule.getEnd() != null && schedule.getEnd().isBefore(LocalDate.now())) {
            throw StatusException.gone("The schedule has already ended.");
        }

        return schedule;
    }
}
