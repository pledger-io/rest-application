package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Executors;

@Tag(name = "Transactions")
@Controller("/api/transactions")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class TransactionResource {

    private final SettingProvider settingProvider;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;
    private final AccountTypeProvider accountTypeProvider;
    private final RuntimeResource runtimeResource;
    private final AuthenticationFacade authenticationFacade;

    public TransactionResource(SettingProvider settingProvider, TransactionProvider transactionProvider, AccountProvider accountProvider, FilterFactory filterFactory, AccountTypeProvider accountTypeProvider, RuntimeResource runtimeResource, AuthenticationFacade authenticationFacade) {
        this.settingProvider = settingProvider;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.accountTypeProvider = accountTypeProvider;
        this.runtimeResource = runtimeResource;
        this.authenticationFacade = authenticationFacade;
    }

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
                        request.getDateRange().start(),
                        request.getDateRange().end()))
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        Control.Option(request.getCategory())
                .map(e -> new EntityRef(e.id()))
                .ifPresent(category -> command.categories(Collections.List(category)));
        Control.Option(request.getBudget())
                .map(e -> new EntityRef(e.id()))
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
                    .map(TransactionBulkEditRequest.EntityRef::name)
                    .ifPresent(transaction::linkToBudget);

            Control.Option(request.getCategory())
                    .map(TransactionBulkEditRequest.EntityRef::name)
                    .ifPresent(transaction::linkToCategory);

            Control.Option(request.getContract())
                    .map(TransactionBulkEditRequest.EntityRef::name)
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
    LocalDate firstTransaction(@Body TransactionSearchRequest request) {
        var command = filterFactory.transaction();

        if (request.getAccount() != null) {
            command.name(request.getAccount(), true);
        } else {
            command.ownAccounts();
        }

        if (request.getDateRange() != null) {
            command.range(Dates.range(
                    request.getDateRange().start(),
                    request.getDateRange().end()));
        }

        if (request.isTransfers()) {
            command.transfers();
        }

        return transactionProvider.first(command)
                .map(Transaction::getDate)
                .getOrThrow(() -> StatusException.notFound("No transaction found"));
    }

    @Get(value = "/export", produces = MediaType.TEXT_PLAIN)
    @Operation(
            operationId = "exportTransactions",
            summary = "Export transactions",
            description = "Creates a CSV export of all transactions in the system."
    )
    OutputStream export() throws IOException {
        var outputStream = new ByteArrayOutputStream();
        outputStream.write(("Date,Booking Date,Interest Date,From name,From IBAN," +
                "To name,To IBAN,Description,Category,Budget,Contract,Amount\n").getBytes(StandardCharsets.UTF_8));

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
                outputStream.write(convertTransaction(transaction));
            }

            filterCommand.page(++currentPage);
            page = transactionProvider.lookup(filterCommand);
        } while (page.hasNext());

        return outputStream;
    }

    @Get("/apply-all-rules")
    @Operation(hidden = true)
    void applyRules() {
        try (var executors = Executors.newFixedThreadPool(25)) {
            executors.execute(() -> {
                EventBus.getBus().sendSystemEvent(new InternalAuthenticationEvent(
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
                                EventBus.getBus().sendSystemEvent(
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
    }

    private byte[] convertTransaction(Transaction transaction) {
        return (transaction.getDate() + "," +
                valueOrEmpty(transaction.getBookDate()) + "," +
                valueOrEmpty(transaction.getInterestDate()) + "," +
                transaction.computeFrom().getName() + "," +
                valueOrEmpty(transaction.computeFrom().getIban()) + "," +
                transaction.computeTo().getName() + "," +
                valueOrEmpty(transaction.computeTo().getIban()) + "," +
                transaction.getDescription() + "," +
                valueOrEmpty(transaction.getCategory()) + "," +
                valueOrEmpty(transaction.getBudget()) + "," +
                valueOrEmpty(transaction.getContract()) + "," +
                transaction.computeAmount(transaction.computeFrom()) +
                "\n").getBytes(StandardCharsets.UTF_8);
    }

    private <T> String valueOrEmpty(T value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
