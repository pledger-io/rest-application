package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.annotations.BankingModuleEnabled;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.TransactionFetcherApi;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Controller
@BankingModuleEnabled
class TransactionFetcherController implements TransactionFetcherApi {

    private final Logger logger;

    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;
    private final List<LinkableProvider<Classifier>> linkableProviders;

    TransactionFetcherController(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            List<LinkableProvider<Classifier>> linkableProviders) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.linkableProviders = linkableProviders;
        this.logger = LoggerFactory.getLogger(TransactionFetcherController.class);
    }

    @Override
    public TransactionPagedResponse findTransactionBy(
            LocalDate startDate,
            LocalDate endDate,
            Integer numberOfResults,
            Integer offset,
            List<Long> account,
            List<Long> category,
            List<Long> expense,
            List<Long> contract,
            List<String> tag,
            Long importId,
            String description,
            FindTransactionByTypeParameter type,
            String currency) {
        logger.info("Fetching all transactions, with provided filters.");
        var page = offset / numberOfResults;
        var filter = filterFactory
                .create()
                .page(page, numberOfResults)
                .range(Dates.range(startDate, endDate));

        if (!account.isEmpty()) {
            filter.accounts(
                    Collections.List(account.stream().map(EntityRef::new).toList()));
        }
        if (!category.isEmpty()) {
            filter.categories(
                    Collections.List(category.stream().map(EntityRef::new).toList()));
        }
        if (!expense.isEmpty()) {
            filter.expenses(
                    Collections.List(expense.stream().map(EntityRef::new).toList()));
        }
        if (!contract.isEmpty()) {
            filter.contracts(
                    Collections.List(contract.stream().map(EntityRef::new).toList()));
        }
        if (!tag.isEmpty()) {
            // todo not yet supported
        }
        if (importId != null) {
            filter.importSlug(importId);
        }
        if (description != null) {
            filter.description(description, false);
        }

        if (type != null) {
            switch (type) {
                case EXPENSE -> filter.ownAccounts().onlyIncome(false);
                case INCOME -> filter.ownAccounts().onlyIncome(true);
                case TRANSFER -> filter.transfers();
            }
        }

        if (currency != null) {
            filter.currency(currency);
        }

        var results = transactionProvider.lookup(filter);

        return new TransactionPagedResponse(
                new PagedResponseInfo(results.total(), results.pages(), results.pageSize()),
                results.content().map(TransactionMapper::toTransactionResponse).toJava());
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        logger.info("Fetching transaction {}.", id);
        var transaction = transactionProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Transaction is not found"));
        if (transaction.isDeleted()) {
            throw StatusException.gone("Transaction has been removed from the system");
        }

        return TransactionMapper.toTransactionResponse(transaction);
    }
}
