package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Tag(name = "Transactions")
@Controller("/api/transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class TransactionResource {

    private final SettingProvider settingProvider;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final FilterFactory filterFactory;
    private final AccountTypeProvider accountTypeProvider;
    private final RuntimeResource runtimeResource;

    private final ApplicationEventPublisher eventPublisher;

    public TransactionResource(
            SettingProvider settingProvider,
            TransactionProvider transactionProvider,
            AccountProvider accountProvider,
            FilterFactory filterFactory,
            AccountTypeProvider accountTypeProvider,
            RuntimeResource runtimeResource,
            ApplicationEventPublisher eventPublisher) {
        this.settingProvider = settingProvider;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountProvider;
        this.filterFactory = filterFactory;
        this.accountTypeProvider = accountTypeProvider;
        this.runtimeResource = runtimeResource;
        this.eventPublisher = eventPublisher;
    }

    @Post
    ResultPageResponse<TransactionResponse> search(@Valid @Body TransactionSearchRequest request) {
        var command = filterFactory.transaction()
                .ownAccounts()
                .range(request.getDateRange())
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        API.Option(request.getCategory())
                .map(e -> new EntityRef(e.getId()))
                .ifPresent(category -> command.categories(API.List(category)));
        API.Option(request.getBudget())
                .map(e -> new EntityRef(e.getId()))
                .ifPresent(category -> command.expenses(API.List(category)));

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
        } else if (request.isOnlyExpenses()) {
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
    void patch(@Body TransactionBulkEditRequest request, Principal principal) {
        for (var id : request.getTransactions()) {
            var isPresent = transactionProvider.lookup(id)
                    .filter(t -> t.getUser().getUsername().equals(principal.getName()));
            if (!isPresent.isPresent()) {
                continue;
            }

            var transaction = isPresent.get();
            API.Option(request.getBudget())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToBudget);

            API.Option(request.getCategory())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToCategory);

            API.Option(request.getContract())
                    .map(TransactionBulkEditRequest.EntityRef::getName)
                    .ifPresent(transaction::linkToContract);

            API.Option(request.getTags())
                    .ifPresent(adding -> {
                        var tags = API.Option(transaction.getTags())
                                .getOrSupply(API::List)
                                .union(adding);

                        transaction.tag(tags);
                    });
        }
    }

    @Post("/locate-first")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = JsonError.class)))
    Single<LocalDate> firstTransaction(@Body TransactionSearchRequest request) {
        var command = filterFactory.transaction();

        if (request.getAccount() != null) {
            command.name(request.getAccount(), true);
        } else {
            command.ownAccounts();
        }

        if (request.getDateRange() != null) {
            command.range(request.getDateRange());
        }

        if (request.isTransfers()) {
            command.transfers();
        }

        return transactionProvider.first(command)
                .map(Transaction::getDate)
                .switchIfEmpty(Single.error(StatusException.notFound("No transaction found")));
    }

    @Get(value = "/export", produces = MediaType.TEXT_PLAIN)
    Flowable<String> export() {
        return Flowable.create(emitter -> {
           emitter.onNext("Date,Booking Date,Interest Date,From name,From IBAN," +
                   "To name,To IBAN,Description,Category,Budget,Contract,Amount");

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
                    emitter.onNext(convertTransaction(transaction));
                }

                filterCommand.page(++currentPage);
                page = transactionProvider.lookup(filterCommand);
            } while (page.hasNext());

            emitter.onComplete();
        }, BackpressureStrategy.LATEST);
    }

    @Get("/apply-all-rules")
    void applyRules(Principal principal) {
        var executors = Executors.newFixedThreadPool(25);

        Consumer<Long> ruleRunner = (id) -> runtimeResource.startProcess(
                "analyzeRule",
                Map.of("transactionId", id));

        executors.execute(() -> {
            eventPublisher.publishEvent(new InternalAuthenticationEvent(this, principal.getName()));

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
                                            principal.getName()));

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

        return builder.toString();
    }

    private <T> String valueOrEmpty(T value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
