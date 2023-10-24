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
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.Dates;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@Tag(name = "Transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Controller("/api/accounts/{accountId}/transactions")
public class AccountTransactionResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final AccountProvider accountProvider;
    private final SettingProvider settingProvider;

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
            throw StatusException.notAuthorized("Account not found with id " + accountId);
        }
        var command = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(accountId)))
                .range(Dates.range(
                        request.getDateRange().getStart(),
                        request.getDateRange().getEnd()))
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        if (request.getText() != null) {
            command.description(request.getText(), false);
        }

        var results = transactionProvider.lookup(command)
                .map(TransactionResponse::new);

        return new ResultPageResponse<>(results);
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
    void create(@Valid @Body AccountTransactionCreateRequest request) {
        Account fromAccount = accountProvider.lookup(request.getSource().getId()).get();
        Account toAccount = accountProvider.lookup(request.getDestination().getId()).get();

        final Consumer<Transaction.TransactionBuilder> builderConsumer =
                transactionBuilder -> transactionBuilder.currency(request.getCurrency())
                        .description(request.getDescription())
                        .budget(Control.Option(request.getBudget())
                                .map(AccountTransactionCreateRequest.EntityRef::getName)
                                .getOrSupply(() -> null))
                        .category(Control.Option(request.getCategory())
                                .map(AccountTransactionCreateRequest.EntityRef::getName)
                                .getOrSupply(() -> null))
                        .contract(Control.Option(request.getContract())
                                .map(AccountTransactionCreateRequest.EntityRef::getName)
                                .getOrSupply(() -> null))
                        .date(request.getDate())
                        .bookDate(request.getBookDate())
                        .interestDate(request.getInterestDate())
                        .tags(Control.Option(request.getTags()).map(Collections::List).getOrSupply(Collections::List));

        final Transaction transaction = fromAccount.createTransaction(
                toAccount,
                request.getAmount(),
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
        transaction.linkToBudget(Control.Option(request.getBudget())
                .map(AccountTransactionCreateRequest.EntityRef::getName)
                .getOrSupply(() -> null));
        transaction.linkToCategory(Control.Option(request.getCategory())
                .map(AccountTransactionCreateRequest.EntityRef::getName)
                .getOrSupply(() -> null));
        transaction.linkToContract(Control.Option(request.getContract())
                .map(AccountTransactionCreateRequest.EntityRef::getName)
                .getOrSupply(() -> null));

        Control.Option(request.getTags())
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
                            .map(split -> new SplitRecord(split.getDescription(), split.getAmount()));
                    transaction.split(splits);

                    return new TransactionResponse(transaction);
                })
                .getOrThrow(() -> StatusException.notFound("No transaction found for id " + transactionId));
    }

    @Delete("/{transactionId}")
    @Operation(
            summary = "Delete transaction",
            description = "Delete a transaction from the account",
            parameters = @Parameter(name = "transactionId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    void delete(@PathVariable long transactionId) {
        transactionProvider.lookup(transactionId)
                .ifPresent(transaction -> {
                    transaction.delete();
                })
                .elseThrow(() -> StatusException.notFound("No transaction found with id " + transactionId));
    }

    private Transaction.Type determineType(Account fromAccount, Account toAccount) {
        if (fromAccount.isManaged() && toAccount.isManaged()) {
            return Transaction.Type.TRANSFER;
        }

        return Transaction.Type.CREDIT;
    }

}
