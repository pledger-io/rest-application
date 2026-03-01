package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.TransactionLinkType;
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

        if (transaction.getMetadata().containsKey(TransactionLinkType.CONTRACT.name())) {
            metadata.contract(transaction
                    .getMetadata()
                    .get(TransactionLinkType.CONTRACT.name())
                    .toString());
        }
        if (transaction.getMetadata().containsKey(TransactionLinkType.EXPENSE.name())) {
            metadata.budget(transaction
                    .getMetadata()
                    .get(TransactionLinkType.EXPENSE.name())
                    .toString());
        }
        if (transaction.getMetadata().containsKey(TransactionLinkType.CATEGORY.name())) {
            metadata.category(transaction
                    .getMetadata()
                    .get(TransactionLinkType.CATEGORY.name())
                    .toString());
        }
        if (transaction.getMetadata().containsKey(TransactionLinkType.IMPORT.name())) {
            metadata._import(transaction
                    .getMetadata()
                    .get(TransactionLinkType.IMPORT.name())
                    .toString());
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
