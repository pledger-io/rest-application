package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.model.ScheduledTransactionResponse;
import com.jongsoft.lang.Collections;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Objects;

@Tag(name = "Scheduling agent")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/schedule/transaction")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScheduledTransactionResource {

    private final AccountProvider accountProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final FilterFactory filterFactory;

    @Get
    @Operation(
            operationId = "listTransactionSchedule",
            summary = "List all available transaction schedules"
    )
    public Publisher<ScheduledTransactionResponse> list() {
        return Flux.create(emitter -> {
            scheduleProvider.lookup()
                    .map(ScheduledTransactionResponse::new)
                    .forEach(emitter::next);
            emitter.complete();
        });
    }

    @Put
    @Operation(
            operationId = "createTransactionSchedule",
            summary = "Create a new transaction schedule"
    )
    public Publisher<HttpResponse<ScheduledTransactionResponse>> create(@Valid @Body ScheduledTransactionCreateRequest request) {
        return Mono.create(emitter -> {
            var account = accountProvider.lookup(request.getSource().getId());
            var destination = accountProvider.lookup(request.getDestination().getId());

            if (!account.isPresent() || !destination.isPresent()) {
                emitter.error(new IllegalArgumentException("Either source or destination account cannot be located."));
            } else {
                account.get().createSchedule(
                        request.getName(),
                        request.getSchedule(),
                        destination.get(),
                        request.getAmount());

                emitter.success(HttpResponse.created(scheduleProvider.lookup()
                        .filter(schedule -> request.getName().equals(schedule.getName()))
                        .map(ScheduledTransactionResponse::new)
                        .head()));
            }
        });
    }

    @Post
    @Operation(
            operationId = "searchTransactionSchedule",
            summary = "Search schedule"
    )
    public Publisher<ScheduledTransactionResponse> search(@Valid @Body ScheduleSearchRequest request) {
        var filter = filterFactory.schedule()
                .contract(Collections.List(request.getContracts()).map(c -> new EntityRef(c.getId())))
                .activeOnly();

        return Flux.create(emitter -> {
            try {
                scheduleProvider.lookup(filter)
                        .map(ScheduledTransactionResponse::new)
                        .content()
                        .forEach(emitter::next);
            } finally {
                emitter.complete();
            }

        });
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
    public Publisher<ScheduledTransactionResponse> get(@PathVariable long scheduleId) {
        return Mono.create(emitter -> {
            var scheduleOption = scheduleProvider.lookup()
                    .filter(s -> s.getId() == scheduleId)
                    .map(ScheduledTransactionResponse::new);

            if (scheduleOption.isEmpty()) {
                emitter.error(StatusException.notFound("No scheduled transaction found with id " + scheduleId));
            } else {
                emitter.success(scheduleOption.head());
            }
        });
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
    public Publisher<HttpResponse<ScheduledTransactionResponse>> patch(
            @PathVariable long scheduleId,
            @Valid @Body ScheduledTransactionPatchRequest request) {
        return Mono.create(emitter -> {
            var scheduleOption = scheduleProvider.lookup()
                    .filter(s -> s.getId() == scheduleId);
            if (scheduleOption.isEmpty()) {
                emitter.success(HttpResponse.notFound());
            } else {
                var schedule = scheduleOption.head();
                if (Objects.nonNull(request.getName())) {
                    schedule.describe(request.getName(), request.getDescription());
                }

                if (Objects.nonNull(request.getSchedule())) {
                    schedule.adjustSchedule(request.getSchedule().periodicity(), request.getSchedule().interval());
                }

                if (Objects.nonNull(request.getRange())) {
                    schedule.limit(request.getRange().getStart(), request.getRange().getEnd());
                }

                emitter.success(HttpResponse.ok(new ScheduledTransactionResponse(schedule)));
            }
        });
    }

    @Delete("/{scheduleId}")
    @Operation(
            operationId = "removeTransactionSchedule",
            summary = "Remove a transaction schedule",
            parameters = @Parameter(
                    name = "scheduleId",
                    description = "The technical id of the transaction schedule",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH)
    )
    public HttpResponse<Void> remove(@PathVariable long scheduleId) {
        var toRemove = scheduleProvider.lookup()
                .filter(schedule -> schedule.getId() == scheduleId);

        if (toRemove.isEmpty()) {
            return HttpResponse.notFound();
        } else {
            var schedule = toRemove.get();
            schedule.terminate();
            return HttpResponse.ok();
        }
    }

}
