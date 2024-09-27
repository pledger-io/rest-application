package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.SplitRecord;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.function.Consumer;

@Tag(name = "Transactions")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/accounts/{accountId}/transactions")
public class AccountTransactionResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final SettingProvider settingProvider;

    public AccountTransactionResource(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            AccountProvider accountProvider,
            SettingProvider settingProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountProvider;
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
    ResultPageResponse<TransactionResponse> search(
            @PathVariable long accountId,
            @Valid @Body AccountTransactionSearchRequest request) {
        var accountOption = accountProvider.lookup(accountId);

        if (!accountOption.isPresent()) {
            throw StatusException.notFound("Account not found with id " + accountId);
        }
        var command = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(accountId)))
                .range(Dates.range(
                        request.dateRange().start(),
                        request.dateRange().end()))
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        if (request.text() != null) {
            command.description(request.text(), false);
        }

        var results = transactionProvider.lookup(command)
                .map(TransactionResponse::new);

        return new ResultPageResponse<>(results);
    }

    @Put
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Create transaction",
            description = "Create a new transaction in the provided accounts",
            parameters = @Parameter(
                    name = "accountId",
                    schema = @Schema(implementation = Long.class),
                    in = ParameterIn.PATH),
            responses = {
                    @ApiResponse(responseCode = "204", description = "The transaction",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            }
    )
    void create(@Valid @Body AccountTransactionCreateRequest request) {
        Account fromAccount = accountProvider.lookup(request.source().id()).get();
        Account toAccount = accountProvider.lookup(request.destination().id()).get();

        final Consumer<Transaction.TransactionBuilder> builderConsumer =
                transactionBuilder -> transactionBuilder.currency(request.currency())
                        .description(request.description())
                        .budget(Control.Option(request.budget())
                                .map(AccountTransactionCreateRequest.EntityRef::name)
                                .getOrSupply(() -> null))
                        .category(Control.Option(request.category())
                                .map(AccountTransactionCreateRequest.EntityRef::name)
                                .getOrSupply(() -> null))
                        .contract(Control.Option(request.contract())
                                .map(AccountTransactionCreateRequest.EntityRef::name)
                                .getOrSupply(() -> null))
                        .date(request.date())
                        .bookDate(request.bookDate())
                        .interestDate(request.interestDate())
                        .tags(Control.Option(request.tags()).map(Collections::List).getOrSupply(Collections::List));

        final Transaction transaction = fromAccount.createTransaction(
                toAccount,
                request.amount(),
                determineType(fromAccount, toAccount),
                builderConsumer);

        transaction.register();
    }

    @Get("/first{?description}")
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
    TransactionResponse first(@PathVariable Long accountId, @Nullable String description) {
        var command = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(accountId)));

        if (description != null) {
            command.description(description, true);
        }

        return transactionProvider.first(command)
                .map(TransactionResponse::new)
                .getOrThrow(() -> StatusException.notFound("No transactions found"));
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
    TransactionResponse get(@PathVariable long transactionId) {
        return transactionProvider.lookup(transactionId)
                .map(TransactionResponse::new)
                .getOrThrow(() -> StatusException.notFound("No transaction found for id " + transactionId));
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
    TransactionResponse update(
            @PathVariable long transactionId,
            @Valid @Body AccountTransactionCreateRequest request) {
        var presence = transactionProvider.lookup(transactionId);
        if (!presence.isPresent()) {
            throw StatusException.notFound("No transaction found for id " + transactionId);
        }
        var fromAccount = accountProvider.lookup(request.source().id()).get();
        var toAccount = accountProvider.lookup(request.destination().id()).get();
        var transaction = presence.get();

        transaction.changeAccount(true, fromAccount);
        transaction.changeAccount(false, toAccount);
        transaction.book(request.date(), request.bookDate(), request.interestDate());
        transaction.describe(request.description());

        if (!transaction.isSplit()) {
            transaction.changeAmount(request.amount(), request.currency());
        }

        // update meta-data
        transaction.linkToBudget(Control.Option(request.budget())
                .map(AccountTransactionCreateRequest.EntityRef::name)
                .getOrSupply(() -> null));
        transaction.linkToCategory(Control.Option(request.category())
                .map(AccountTransactionCreateRequest.EntityRef::name)
                .getOrSupply(() -> null));
        transaction.linkToContract(Control.Option(request.contract())
                .map(AccountTransactionCreateRequest.EntityRef::name)
                .getOrSupply(() -> null));

        Control.Option(request.tags())
                .map(Collections::List)
                .ifPresent(transaction::tag);

        return new TransactionResponse(transaction);
    }

    @Patch("/{transactionId}")
    @Operation(
            summary = "Split transactions",
            description = "Split the transaction into smaller pieces, all belonging to the same actual transaction.",
            parameters = @Parameter(name = "transactionId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    TransactionResponse split(
            @PathVariable long transactionId,
            @Valid @Body AccountTransactionSplitRequest request) {
        return transactionProvider.lookup(transactionId)
                .map(transaction -> {
                    var splits = Collections.List(request.getSplits())
                            .map(split -> new SplitRecord(split.description(), split.amount()));
                    transaction.split(splits);

                    return new TransactionResponse(transaction);
                })
                .getOrThrow(() -> StatusException.notFound("No transaction found for id " + transactionId));
    }

    @Delete("/{transactionId}")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete transaction",
            description = "Delete a transaction from the account",
            parameters = @Parameter(name = "transactionId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    void delete(@PathVariable long transactionId) {
        transactionProvider.lookup(transactionId)
                .ifPresent(Transaction::delete)
                .elseThrow(() -> StatusException.notFound("No transaction found with id " + transactionId));
    }

    private Transaction.Type determineType(Account fromAccount, Account toAccount) {
        if (fromAccount.isManaged() && toAccount.isManaged()) {
            return Transaction.Type.TRANSFER;
        }

        return Transaction.Type.CREDIT;
    }

}
