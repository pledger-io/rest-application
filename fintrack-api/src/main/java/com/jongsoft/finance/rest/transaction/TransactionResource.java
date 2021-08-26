package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Executors;

@Tag(name = "Transactions")
@Controller("/api/transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TransactionResource {

    private final SettingProvider settingProvider;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;
    private final AccountTypeProvider accountTypeProvider;
    private final RuntimeResource runtimeResource;
    private final AuthenticationFacade authenticationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Post
    @Operation(
            operationId = "searchTransactions",
            summary = "Search transactions",
            description = "Search in all transactions using the given search request."
    )
    ResultPageResponse<TransactionResponse> search(@Valid @Body TransactionSearchRequest request) {
        var command = filterFactory.transaction()
                .ownAccounts()
                .range(Dates.range(
                        request.getDateRange().getStart(),
                        request.getDateRange().getEnd()))
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        Control.Option(request.getCategory())
                .map(e -> new EntityRef(e.getId()))
                .ifPresent(category -> command.categories(Collections.List(category)));
        Control.Option(request.getBudget())
                .map(e -> new EntityRef(e.getId()))
                .ifPresent(category -> command.expenses(Collections.List(category)));

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            command.description(request.getDescription(), false);
        }

        if (request.getAccount() != null && !request.getAccount().isBlank()) {
            command.name(request.getAccount(), false);
        }

        if (request.isTransfers()) {
            command.transfers();
        }

        if (request.isOnlyIncome()) {
            command.onlyIncome(true);
        } else if (request.isOnlyExpense()) {
            command.onlyIncome(false);
        }

        if (request.getCurrency() != null) {
            command.currency(request.getCurrency());
        }

        var response = transactionProvider.lookup(command)
                .map(TransactionResponse::new);

        return new ResultPageResponse<>(response);
    }

    @Patch
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "patchTransactions",
            summary = "Patch given transactions",
            description = "Update the transactions with the given transaction ids using the request."
    )
    void patch(@Body TransactionBulkEditRequest request) {
        for (var id : request.getTransactions()) {
            var isPresent = transactionProvider.lookup(id);
            if (!isPresent.isPresent()) {
                continue;
            }

            var transaction = isPresent.get();
            Control.Option(request.getBudget())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToBudget);

            Control.Option(request.getCategory())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToCategory);

            Control.Option(request.getContract())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToContract);

            Control.Option(request.getTags())
                    .ifPresent(adding -> {
                        var tags = Control.Option(transaction.getTags())
                                .getOrSupply(Collections::List)
                                .union(adding);

                        transaction.tag(tags);
                    });
        }
    }

    @Post("/locate-first")
    @Operation(
            operationId = "getFirstTransactionDate",
            summary = "Get oldest date",
            description = "Get the oldest transaction in the system based upon the provided request."
    )
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = JsonError.class)))
    Publisher<LocalDate> firstTransaction(@Body TransactionSearchRequest request) {
        var command = filterFactory.transaction();

        if (request.getAccount() != null) {
            command.name(request.getAccount(), true);
        } else {
            command.ownAccounts();
        }

        if (request.getDateRange() != null) {
            command.range(Dates.range(
                    request.getDateRange().getStart(),
                    request.getDateRange().getEnd()));
        }

        if (request.isTransfers()) {
            command.transfers();
        }

        return transactionProvider.first(command)
                .map(Transaction::getDate)
                .switchIfEmpty(Mono.error(StatusException.notFound("No transaction found")));
    }

    @Get(value = "/export", produces = MediaType.TEXT_PLAIN)
    @Operation(
            operationId = "exportTransactions",
            summary = "Export transactions",
            description = "Creates a CSV export of all transactions in the system."
    )
    Publisher<String> export() {
        return Flux.create(emitter -> {
            emitter.next("Date,Booking Date,Interest Date,From name,From IBAN," +
                    "To name,To IBAN,Description,Category,Budget,Contract,Amount\n");

            var filterCommand = filterFactory.transaction()
                    .accounts(accountProvider.lookup(filterFactory.account()
                            .types(accountTypeProvider.lookup(false)))
                            .content()
                            .map(account -> new EntityRef(account.getId())))
                    .pageSize(100);

            int currentPage = 0;
            filterCommand.page(currentPage);
            var page = transactionProvider.lookup(filterCommand);
            do {
                for (Transaction transaction : page.content()) {
                    emitter.next(convertTransaction(transaction));
                }

                filterCommand.page(++currentPage);
                page = transactionProvider.lookup(filterCommand);
            } while (page.hasNext());

            emitter.complete();
        });
    }

    @Get("/apply-all-rules")
    @Operation(hidden = true)
    void applyRules() {
        var executors = Executors.newFixedThreadPool(25);

        executors.execute(() -> {
            eventPublisher.publishEvent(new InternalAuthenticationEvent(
                    this,
                    authenticationFacade.authenticated()));

            var filterCommand = filterFactory.transaction()
                    .accounts(accountProvider.lookup(filterFactory.account()
                            .types(accountTypeProvider.lookup(false)))
                            .content()
                            .map(account -> new EntityRef(account.getId())))
                    .pageSize(100);

            int currentPage = 0;
            var page = transactionProvider.lookup(filterCommand);
            do {
                page.content()
                        .map(Transaction::getId)
                        .forEach(transaction -> executors.execute(() -> {
                            eventPublisher.publishEvent(
                                    new InternalAuthenticationEvent(
                                            this,
                                            authenticationFacade.authenticated()));

                            runtimeResource.startProcess(
                                    "analyzeRule",
                                    Map.of("transactionId", transaction));
                        }));

                filterCommand.page(++currentPage);
                page = transactionProvider.lookup(filterCommand);
            } while (page.hasNext());
        });
    }

    private String convertTransaction(Transaction transaction) {
        var builder = new StringBuilder();

        builder.append(transaction.getDate()).append(",");
        builder.append(valueOrEmpty(transaction.getBookDate())).append(",");
        builder.append(valueOrEmpty(transaction.getInterestDate())).append(",");
        builder.append(transaction.computeFrom().getName()).append(",");
        builder.append(valueOrEmpty(transaction.computeFrom().getIban())).append(",");
        builder.append(transaction.computeTo().getName()).append(",");
        builder.append(valueOrEmpty(transaction.computeTo().getIban())).append(",");
        builder.append(transaction.getDescription()).append(",");
        builder.append(valueOrEmpty(transaction.getCategory())).append(",");
        builder.append(valueOrEmpty(transaction.getBudget())).append(",");
        builder.append(valueOrEmpty(transaction.getContract())).append(",");
        builder.append(transaction.computeAmount(transaction.computeFrom()));
        builder.append("\n");

        return builder.toString();
    }

    private <T> String valueOrEmpty(T value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
