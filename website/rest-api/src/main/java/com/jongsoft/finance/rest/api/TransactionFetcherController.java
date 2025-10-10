package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.FindTransactionByTypeParameter;
import com.jongsoft.finance.rest.model.PagedResponseInfo;
import com.jongsoft.finance.rest.model.PagedTransactionResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Controller
class TransactionFetcherController implements TransactionFetcherApi {

    private final Logger logger;

    private final TransactionProvider transactionProvider;
    private final FilterFactory filterFactory;

    TransactionFetcherController(
            TransactionProvider transactionProvider, FilterFactory filterFactory) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.logger = LoggerFactory.getLogger(TransactionFetcherController.class);
    }

    @Override
    public PagedTransactionResponse findTransactionBy(
            LocalDate startDate,
            LocalDate endDate,
            Integer numberOfResults,
            Integer offset,
            List<Long> account,
            List<Long> category,
            List<Long> expense,
            List<String> tag,
            String importSlug,
            String description,
            FindTransactionByTypeParameter type,
            String currency) {
        logger.info("Fetching all transactions, with provided filters.");
        var page = offset / numberOfResults;
        var filter = filterFactory
                .transaction()
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
        if (!tag.isEmpty()) {
            // todo not yet supported
        }
        if (importSlug != null) {
            filter.importSlug(importSlug);
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

        return new PagedTransactionResponse(
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
