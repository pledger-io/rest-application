package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.SplitRecord;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.rest.model.SplitTransactionRequestInner;
import com.jongsoft.finance.rest.model.TransactionRequest;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Value;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Controller
class TransactionCommandController implements TransactionCommandApi {

    private final Logger logger;

    private final TransactionProvider transactionProvider;
    private final TransactionCreationHandler transactionCreationHandler;

    private final CategoryProvider categoryProvider;
    private final ExpenseProvider expenseProvider;
    private final ContractProvider contractProvider;
    private final AccountProvider accountProvider;

    TransactionCommandController(
            TransactionProvider transactionProvider,
            TransactionCreationHandler transactionCreationHandler,
            CategoryProvider categoryProvider,
            ExpenseProvider expenseProvider,
            ContractProvider contractProvider,
            AccountProvider accountProvider) {
        this.transactionProvider = transactionProvider;
        this.transactionCreationHandler = transactionCreationHandler;
        this.categoryProvider = categoryProvider;
        this.expenseProvider = expenseProvider;
        this.contractProvider = contractProvider;
        this.accountProvider = accountProvider;
        this.logger = LoggerFactory.getLogger(TransactionCommandController.class);
    }

    @Override
    public HttpResponse<@Valid TransactionResponse> createTransaction(TransactionRequest request) {
        logger.info("Creating new transaction.");
        final Consumer<Transaction.TransactionBuilder> builderConsumer =
                transactionBuilder -> transactionBuilder
                        .currency(request.getCurrency())
                        .description(request.getDescription())
                        .budget(Optional.ofNullable(request.getExpense())
                                .map(expenseProvider::lookup)
                                .map(Value::get)
                                .map(EntityRef.NamedEntity::name)
                                .orElse(null))
                        .category(Optional.ofNullable(request.getCategory())
                                .map(categoryProvider::lookup)
                                .map(Value::get)
                                .map(Category::getLabel)
                                .orElse(null))
                        .contract(Optional.ofNullable(request.getContract())
                                .map(contractProvider::lookup)
                                .map(Value::get)
                                .map(Contract::getName)
                                .orElse(null))
                        .date(request.getDate())
                        .bookDate(request.getBookDate())
                        .interestDate(request.getInterestDate())
                        .tags(Optional.ofNullable(request.getTags())
                                .map(Collections::List)
                                .orElse(Collections.List()));

        var source = accountProvider
                .lookup(request.getSource())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.badRequest("Source account is not found."));
        var destination = accountProvider
                .lookup(request.getTarget())
                .filter(Predicate.not(Account::isRemove))
                .getOrThrow(() -> StatusException.badRequest("Destination account is not found."));

        final Transaction transaction = source.createTransaction(
                destination,
                request.getAmount(),
                determineType(source, destination),
                builderConsumer);

        var id = transactionCreationHandler.handleCreatedEvent(
                new CreateTransactionCommand(transaction));
        var transactionWithId = transactionProvider
                .lookup(id)
                .getOrThrow(() ->
                        StatusException.internalError("Transaction not found after creation."));
        return HttpResponse.created(TransactionMapper.toTransactionResponse(transactionWithId));
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
            transactionRequest.getDate(), transactionRequest.getBookDate(), transactionRequest.getInterestDate());
        if (!transaction.isSplit()) {
            transaction.changeAmount(
                    transactionRequest.getAmount(), transactionRequest.getCurrency());
        }

        return TransactionMapper.toTransactionResponse(transaction);
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
        Optional.ofNullable(request.getExpense())
                .map(expenseProvider::lookup)
                .map(Value::get)
                .map(EntityRef.NamedEntity::name)
                .ifPresentOrElse(transaction::linkToBudget, () -> transaction.linkToBudget(null));
        Optional.ofNullable(request.getCategory())
                .map(categoryProvider::lookup)
                .map(Value::get)
                .map(Category::getLabel)
                .ifPresentOrElse(
                        transaction::linkToCategory, () -> transaction.linkToCategory(null));
        Optional.ofNullable(request.getContract())
                .map(contractProvider::lookup)
                .map(Value::get)
                .map(Contract::getName)
                .ifPresentOrElse(
                        transaction::linkToContract, () -> transaction.linkToContract(null));
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

    private Transaction.Type determineType(Account fromAccount, Account toAccount) {
        if (fromAccount.isManaged() && toAccount.isManaged()) {
            return Transaction.Type.TRANSFER;
        }

        return Transaction.Type.CREDIT;
    }
}
