package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.rest.model.*;

public interface TransactionMapper {

    static TransactionResponse toTransactionResponse(Transaction transaction) {
        var response = new TransactionResponse();
        var metadata = new TransactionResponseMetadata();
        var dates = new TransactionResponseDates();
        var destination = transaction.computeTo();
        var source = transaction.computeFrom();

        response.id(transaction.getId());
        response.description(transaction.getDescription());
        response.currency(transaction.getCurrency());
        response.amount(transaction.computeAmount(transaction.computeTo()));
        response.metadata(metadata);
        response.type(
                TransactionResponseType.fromValue(transaction.computeType().name()));
        response.dates(dates);
        response.source(new AccountLink(source.getId(), source.getName(), source.getType()));
        response.destination(
                new AccountLink(destination.getId(), destination.getName(), destination.getType()));

        if (transaction.getMetadata().containsKey("CONTRACT")) {
            metadata.contract(((Contract) transaction.getMetadata().get("CONTRACT")).getName());
        }
        if (transaction.getMetadata().containsKey("EXPENSE")) {
            metadata.budget(((Budget.Expense) transaction.getMetadata().get("EXPENSE")).getName());
        }
        if (transaction.getMetadata().containsKey("CATEGORY")) {
            metadata.category(((Category) transaction.getMetadata().get("CATEGORY")).getLabel());
        }
        metadata._import(transaction.getImportSlug());
        metadata.tags(transaction.getTags().toJava());

        dates.transaction(transaction.getDate());
        dates.booked(transaction.getBookDate());
        dates.interest(transaction.getInterestDate());

        if (transaction.isSplit()) {
            var splitFor =
                    switch (transaction.computeType()) {
                        case CREDIT -> destination;
                        case DEBIT -> source;
                        case TRANSFER ->
                            throw StatusException.internalError(
                                    "Split transaction cannot be a transfer");
                    };

            transaction
                    .getTransactions()
                    .filter(t -> t.getAccount().equals(splitFor))
                    .map(part -> new TransactionResponseSplitInner(
                            part.getDescription(), part.getAmount()))
                    .forEach(response::addSplitItem);
        }

        return response;
    }
}
