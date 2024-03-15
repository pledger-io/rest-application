package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.model.ScheduledTransactionResponse;
import com.jongsoft.lang.Collections;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Objects;

@Tag(name = "Scheduling agent")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/schedule/transaction")
public class ScheduledTransactionResource {

    private final AccountProvider accountProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final FilterFactory filterFactory;

    public ScheduledTransactionResource(AccountProvider accountProvider, TransactionScheduleProvider scheduleProvider, FilterFactory filterFactory) {
        this.accountProvider = accountProvider;
        this.scheduleProvider = scheduleProvider;
        this.filterFactory = filterFactory;
    }

    @Get
    @Operation(
            operationId = "listTransactionSchedule",
            summary = "List all available transaction schedules"
    )
    public List<ScheduledTransactionResponse> list() {
        return scheduleProvider.lookup()
                .map(ScheduledTransactionResponse::new)
                .toJava();
    }

    @Put
    @Operation(
            operationId = "createTransactionSchedule",
            summary = "Create a new transaction schedule"
    )
    @Status(HttpStatus.CREATED)
    public ScheduledTransactionResponse create(@Valid @Body ScheduledTransactionCreateRequest request) {
        var account = accountProvider.lookup(request.getSource().id());
        var destination = accountProvider.lookup(request.getDestination().id());

        if (!account.isPresent() || !destination.isPresent()) {
            throw StatusException.badRequest("Either source or destination account cannot be located.");
        }

        account.get().createSchedule(
                request.getName(),
                request.getSchedule(),
                destination.get(),
                request.getAmount());

        return scheduleProvider.lookup()
                .filter(schedule -> request.getName().equals(schedule.getName()))
                .map(ScheduledTransactionResponse::new)
                .head();
    }

    @Post
    @Operation(
            operationId = "searchTransactionSchedule",
            summary = "Search schedule"
    )
    public List<ScheduledTransactionResponse> search(@Valid @Body ScheduleSearchRequest request) {
        var filter = filterFactory.schedule()
                .contract(Collections.List(request.getContracts()).map(c -> new EntityRef(c.id())))
                .activeOnly();

        return scheduleProvider.lookup(filter)
                .map(ScheduledTransactionResponse::new)
                .content()
                .toJava();
    }

    @Get("/{scheduleId}")
    @Operation(
            operationId = "fetchTransactionSchedule",
            summary = "Get a single transaction schedule",
            description = "Lookup a transaction schedule in the system by its technical id",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public ScheduledTransactionResponse get(@PathVariable long scheduleId) {
        var scheduleOption = scheduleProvider.lookup()
                .filter(s -> s.getId() == scheduleId)
                .map(ScheduledTransactionResponse::new);

        if (scheduleOption.isEmpty()) {
            throw StatusException.notFound("No scheduled transaction found with id " + scheduleId);
        }

        return scheduleOption.head();
    }

    @Patch("/{scheduleId}")
    @Operation(
            operationId = "patchTransactionSchedule",
            summary = "Update part of a transaction schedule",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public ScheduledTransactionResponse patch(
            @PathVariable long scheduleId,
            @Valid @Body ScheduledTransactionPatchRequest request) {
        var scheduleOption = scheduleProvider.lookup()
                .filter(s -> s.getId() == scheduleId);

        if (scheduleOption.isEmpty()) {
            throw StatusException.notFound("No scheduled transaction found with id " + scheduleId);
        }

        var schedule = scheduleOption.head();
        if (Objects.nonNull(request.getName())) {
            schedule.describe(request.getName(), request.getDescription());
        }

        if (Objects.nonNull(request.getSchedule())) {
            schedule.adjustSchedule(request.getSchedule().periodicity(), request.getSchedule().interval());
        }

        if (Objects.nonNull(request.getRange())) {
            schedule.limit(request.getRange().start(), request.getRange().end());
        }

        return new ScheduledTransactionResponse(schedule);
    }

    @Delete("/{scheduleId}")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "removeTransactionSchedule",
            summary = "Remove a transaction schedule",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public void remove(@PathVariable long scheduleId) {
        var toRemove = scheduleProvider.lookup()
                .filter(schedule -> schedule.getId() == scheduleId);

        if (toRemove.isEmpty()) {
            throw StatusException.notFound("No scheduled transaction found with id " + scheduleId);
        }

        toRemove.get().terminate();
    }

}
