package com.jongsoft.finance.banking.adapter.rest;

import static com.jongsoft.finance.banking.types.TransactionLinkType.*;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.SplitRecord;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.rest.TransactionCommandApi;
import com.jongsoft.finance.rest.model.SplitTransactionRequestInner;
import com.jongsoft.finance.rest.model.TransactionRequest;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.lang.Collections;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Controller
class TransactionCommandController implements TransactionCommandApi {

    private final Logger logger;

    private final TransactionProvider transactionProvider;
    private final TransactionCreationHandler transactionCreationHandler;

    private final AccountProvider accountProvider;

    TransactionCommandController(
            TransactionProvider transactionProvider,
            TransactionCreationHandler transactionCreationHandler,
            AccountProvider accountProvider) {
        this.transactionProvider = transactionProvider;
        this.transactionCreationHandler = transactionCreationHandler;
        this.accountProvider = accountProvider;
        this.logger = LoggerFactory.getLogger(TransactionCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TransactionResponse> createTransaction(TransactionRequest request) {
        logger.info("Creating new transaction.");
        var source = accountProvider
                .lookup(request.getSource())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.badRequest("Source account is not found."));
        var destination = accountProvider
                .lookup(request.getTarget())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.badRequest("Destination account is not found."));

        var id = transactionCreationHandler.handleCreatedEvent(new CreateTransactionCommand(
                request.getDate(),
                request.getDescription(),
                determineType(source, destination),
                null,
                request.getCurrency(),
                source.getId(),
                destination.getId(),
                BigDecimal.valueOf(request.getAmount())));

        var transaction = lookupTransactionByIdOrThrow(id);
        if (request.getBookDate() != null || request.getInterestDate() != null) {
            transaction.book(request.getDate(), request.getBookDate(), request.getInterestDate());
        }

        updateTransactionRelations(transaction, request);

        return HttpResponse.created(
                TransactionMapper.toTransactionResponse(lookupTransactionByIdOrThrow(id)));
    }

    @Override
    public HttpResponse<Void> deleteTransaction(Long id) {
        logger.info("Deleting transaction {}.", id);

        lookupTransactionByIdOrThrow(id).delete();

        return HttpResponse.noContent();
    }

    @Override
    public TransactionResponse splitTransaction(
            Long id, List<@Valid SplitTransactionRequestInner> splitTransactionRequestInners) {
        logger.info("Splitting transaction {}.", id);

        var transaction = lookupTransactionByIdOrThrow(id);
        var splits = Collections.List(splitTransactionRequestInners)
                .map(split -> new SplitRecord(
                        split.getDescription(), split.getAmount().doubleValue()));

        transaction.split(splits);

        return TransactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest transactionRequest) {
        logger.info("Updating transaction {}.", id);

        var transaction = lookupTransactionByIdOrThrow(id);
        updateAccounts(transaction, transactionRequest);
        updateTransactionRelations(transaction, transactionRequest);
        transaction.describe(transactionRequest.getDescription());
        transaction.book(
                transactionRequest.getDate(),
                transactionRequest.getBookDate(),
                transactionRequest.getInterestDate());
        if (!transaction.isSplit()) {
            transaction.changeAmount(
                    transactionRequest.getAmount(), transactionRequest.getCurrency());
        }

        return TransactionMapper.toTransactionResponse(lookupTransactionByIdOrThrow(id));
    }

    private void updateAccounts(Transaction transaction, TransactionRequest request) {
        var source = accountProvider
                .lookup(request.getSource())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.notFound("Source account is not found."));
        var destination = accountProvider
                .lookup(request.getTarget())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.notFound("Destination account is not found."));

        transaction.changeAccount(true, source);
        transaction.changeAccount(false, destination);
    }

    private void updateTransactionRelations(Transaction transaction, TransactionRequest request) {
        transaction.link(EXPENSE, request.getExpense());
        transaction.link(CATEGORY, request.getCategory());
        transaction.link(CONTRACT, request.getContract());
        Optional.ofNullable(request.getTags())
                .map(Collections::List)
                .ifPresentOrElse(transaction::tag, () -> transaction.tag(Collections.List()));
    }

    private Transaction lookupTransactionByIdOrThrow(long id) {
        var transaction = transactionProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Transaction is not found."));
        if (transaction.isDeleted()) {
            throw StatusException.gone("Transaction has been removed from the system");
        }

        return transaction;
    }

    private TransactionType determineType(Account fromAccount, Account toAccount) {
        if (fromAccount.isManaged() && toAccount.isManaged()) {
            return TransactionType.TRANSFER;
        }

        return TransactionType.CREDIT;
    }
}
