package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.SplitRecord;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.lang.API;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.function.Consumer;

@Tag(name = "Transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/accounts/{accountId}/transactions")
public class AccountTransactionResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final SettingProvider settingProvider;

    public AccountTransactionResource(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            AccountProvider accountService,
            SettingProvider settingProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountService;
        this.settingProvider = settingProvider;
    }

    @Post
    @Operation(
            summary = "Search transactions",
            description = "Search through all transaction in the account using the provided filter",
            parameters = @Parameter(
                    name = "accountId",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paged result of transactions"),
                    @ApiResponse(responseCode = "401", description = "The account cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Single<HttpResponse<ResultPage<TransactionResponse>>> search(
            @PathVariable long accountId,
            @Valid @Body AccountTransactionSearchRequest request,
            Principal principal) {
        return Single.create(emitter -> {
            var accountOption = accountProvider.lookup(accountId);

            if (!accountOption.isPresent()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else if (!accountOption.get().getUser().getUsername().equals(principal.getName())) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                var command = filterFactory.transaction()
                        .accounts(API.List(new EntityRef(accountId)))
                        .range(request.getDateRange())
                        .pageSize(settingProvider.getPageSize())
                        .page(request.getPage());

                if (request.getText() != null) {
                    command.description(request.getText(), false);
                }

                var results = transactionProvider.lookup(command)
                        .map(TransactionResponse::new);

                emitter.onSuccess(HttpResponse.ok(results));
            }
        });
    }

    @Put
    @Operation(
            summary = "Create transaction",
            description = "Create a new transaction in the provided accounts",
            parameters = @Parameter(
                    name = "accountId",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The transaction",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            }
    )
    Single<HttpResponse<Void>> create(@Valid @Body AccountTransactionCreateRequest request) {
        return Single.create(emitter -> {
            Account fromAccount = accountProvider.lookup(request.getSource().getId()).get();
            Account toAccount = accountProvider.lookup(request.getDestination().getId()).get();

            final Consumer<Transaction.TransactionBuilder> builderConsumer =
                    transactionBuilder -> transactionBuilder.currency(request.getCurrency())
                            .description(request.getDescription())
                            .budget(API.Option(request.getBudget())
                                    .map(AccountTransactionCreateRequest.EntityRef::getName)
                                    .getOrSupply(() -> null))
                            .category(API.Option(request.getCategory())
                                    .map(AccountTransactionCreateRequest.EntityRef::getName)
                                    .getOrSupply(() -> null))
                            .contract(API.Option(request.getContract())
                                    .map(AccountTransactionCreateRequest.EntityRef::getName)
                                    .getOrSupply(() -> null))
                            .date(request.getDate())
                            .bookDate(request.getBookDate())
                            .interestDate(request.getInterestDate())
                            .tags(API.Option(request.getTags()).map(API::List).getOrSupply(API::List));

            final Transaction transaction = fromAccount.createTransaction(
                    toAccount,
                    request.getAmount(),
                    determineType(fromAccount, toAccount),
                    builderConsumer);

            transaction.register();
            emitter.onSuccess(HttpResponse.created(new URI("/accounts/"+ fromAccount.getId() +"/transactions")));
        });
    }

    @Get("/first")
    @Operation(
            summary = "Get the first transaction",
            description = "Returns the first transaction found for the given account",
            parameters = @Parameter(
                    name = "accountId",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The transaction",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No transaction found")
            }
    )
    Single<HttpResponse<TransactionResponse>> first(@PathVariable Long accountId, @QueryValue String description) {
        return Single.create(emitter -> {
            var command = filterFactory.transaction()
                    .accounts(API.List(new EntityRef(accountId)));

            if (description != null) {
                command.description(description, true);
            }

            var presence = transactionProvider.first(command);
            if (presence.isPresent()) {
                emitter.onSuccess(HttpResponse.ok(new TransactionResponse(presence.get())));
            } else {
                emitter.onSuccess(HttpResponse.notFound());
            }
        });
    }

    @Get("/{transactionId}")
    @Operation(
            summary = "Get a transaction",
            description = "Returns one single transaction identified by the provided id",
            parameters = {
                    @Parameter(
                            name = "accountId",
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH),
                    @Parameter(
                            name = "transactionId",
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "The transaction",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The transaction cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Single<HttpResponse<TransactionResponse>> get(@PathVariable long transactionId, Principal principal) {
        return Single.create(emitter -> {
            var transaction = transactionProvider.lookup(transactionId);

            if (!transaction.isPresent()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else if (!transaction.get().getUser().getUsername().equals(principal.getName())) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                emitter.onSuccess(HttpResponse.ok(new TransactionResponse(transaction.get())));
            }
        });
    }

    @Post("/{transactionId}")
    @Operation(
            summary = "Update a transaction",
            description = "Updates a single transaction and returns the updated version",
            parameters = {
                    @Parameter(
                            name = "accountId",
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH),
                    @Parameter(
                            name = "transactionId",
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "The transaction",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The transaction cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found")
            }
    )
    Single<HttpResponse<TransactionResponse>> update(
            @PathVariable long transactionId,
            @Valid @Body AccountTransactionCreateRequest request,
            Principal principal) {
        return Single.create(emitter -> {
            var presence = transactionProvider.lookup(transactionId);
            if (!presence.isPresent()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else if (!presence.get().getUser().getUsername().equals(principal.getName())) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                var fromAccount = accountProvider.lookup(request.getSource().getId()).get();
                var toAccount = accountProvider.lookup(request.getDestination().getId()).get();
                var transaction = presence.get();

                transaction.changeAccount(true, fromAccount);
                transaction.changeAccount(false, toAccount);
                transaction.book(request.getDate(), request.getBookDate(), request.getInterestDate());
                transaction.describe(request.getDescription());

                if (!transaction.isSplit()) {
                    transaction.changeAmount(request.getAmount(), request.getCurrency());
                }

                // update meta-data
                transaction.linkToBudget(API.Option(request.getBudget())
                        .map(AccountTransactionCreateRequest.EntityRef::getName)
                        .getOrSupply(() -> null));
                transaction.linkToCategory(API.Option(request.getCategory())
                        .map(AccountTransactionCreateRequest.EntityRef::getName)
                        .getOrSupply(() -> null));
                transaction.linkToContract(API.Option(request.getContract())
                        .map(AccountTransactionCreateRequest.EntityRef::getName)
                        .getOrSupply(() -> null));

                API.Option(request.getTags())
                        .map(API::List)
                        .ifPresent(transaction::tag);

                emitter.onSuccess(HttpResponse.ok(new TransactionResponse(transaction)));
            }
        });
    }

    @Patch("/{transactionId}")
    Single<HttpResponse<TransactionResponse>> split(
            @PathVariable long transactionId,
            @Valid @Body AccountTransactionSplitRequest request,
            Principal principal) {
        return Single.create(emitter -> {
            var presence = transactionProvider.lookup(transactionId);
            if (!presence.isPresent()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else if (!presence.get().getUser().getUsername().equals(principal.getName())) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                presence.get().split(
                        API.List(request.getSplits())
                                .map(split -> new SplitRecord(split.getDescription(), split.getAmount())));

                emitter.onSuccess(HttpResponse.ok(new TransactionResponse(presence.get())));
            }
        });
    }

    @Delete("/{transactionId}")
    Single<HttpResponse<Void>> delete(@PathVariable long transactionId, Principal principal) {
        return Single.create(emitter -> {
            var presence = transactionProvider.lookup(transactionId);
            if (!presence.isPresent()) {
                emitter.onSuccess(HttpResponse.notFound());
            } else if (!presence.get().getUser().getUsername().equals(principal.getName())) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                presence.get().delete();
                emitter.onSuccess(HttpResponse.noContent());
            }
        });
    }

    private Transaction.Type determineType(Account fromAccount, Account toAccount) {
        if (fromAccount.isManaged() && toAccount.isManaged()) {
            return Transaction.Type.TRANSFER;
        }

        return Transaction.Type.CREDIT;
    }

}