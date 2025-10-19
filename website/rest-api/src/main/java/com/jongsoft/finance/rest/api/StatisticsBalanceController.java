package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
class StatisticsBalanceController implements StatisticsBalanceApi {

    private final Logger logger;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    private final AccountProvider accountProvider;
    private final ExpenseProvider expenseProvider;
    private final CategoryProvider categoryProvider;

    StatisticsBalanceController(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            AccountProvider accountProvider,
            ExpenseProvider expenseProvider,
            CategoryProvider categoryProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountProvider;
        this.expenseProvider = expenseProvider;
        this.categoryProvider = categoryProvider;
        this.logger = LoggerFactory.getLogger(StatisticsBalanceController.class);
    }

    @Override
    public List<@Valid DatedBalanceResponse> computeBalanceGroupedByDate(
            ComputeBalanceGroupedByDateTypeParameter type, BalanceRequest balanceRequest) {
        logger.info("Computing balance grouped by date with provided filters.");
        var filter = toFilterCommand(balanceRequest);

        var balances =
                switch (type) {
                    case DAILY -> transactionProvider.daily(filter);
                    case MONTHLY -> transactionProvider.monthly(filter);
                };

        return balances.map(b -> new DatedBalanceResponse(b.summary(), b.day())).toJava();
    }

    @Override
    public BalanceResponse computeBalanceWithFilter(BalanceRequest balanceRequest) {
        logger.info("Computing balance with provided filters.");
        var filter = toFilterCommand(balanceRequest);

        var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);

        return new BalanceResponse(balance.doubleValue());
    }

    @Override
    public List<@Valid PartitionedBalanceResponse> computePartitionedBalanceWithFilter(
            ComputePartitionedBalanceWithFilterPartitionParameter partition,
            BalanceRequest balanceRequest) {
        logger.info("Computing partitioned balance with provided filters.");

        var total = transactionProvider
                .balance(toFilterCommand(balanceRequest))
                .getOrSupply(() -> BigDecimal.ZERO);

        var entities =
                switch (partition) {
                    case ACCOUNT -> accountProvider.lookup();
                    case CATEGORY -> categoryProvider.lookup();
                    case BUDGET ->
                        expenseProvider.lookup(filterFactory.expense()).content();
                };

        var results = new ArrayList<PartitionedBalanceResponse>();
        for (var entity : entities) {
            var filter =
                    switch (partition) {
                        case ACCOUNT ->
                            toFilterCommand(balanceRequest)
                                    .accounts(toEntityRefList(List.of(entity.getId())));
                        case CATEGORY ->
                            toFilterCommand(balanceRequest)
                                    .categories(toEntityRefList(List.of(entity.getId())));
                        case BUDGET ->
                            toFilterCommand(balanceRequest)
                                    .expenses(toEntityRefList(List.of(entity.getId())));
                    };

            var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);
            total = total.subtract(balance);
            results.add(new PartitionedBalanceResponse(balance.doubleValue(), entity.toString()));
        }
        results.add(new PartitionedBalanceResponse(total.doubleValue(), ""));

        return results;
    }

    private TransactionProvider.FilterCommand toFilterCommand(BalanceRequest balanceRequest) {
        var filter = filterFactory
                .transaction()
                .range(Dates.range(
                        balanceRequest.getRange().getStartDate(),
                        balanceRequest.getRange().getEndDate()));

        if (balanceRequest.getAccounts() != null
                && !balanceRequest.getAccounts().isEmpty()) {
            filter.accounts(toEntityRefList(balanceRequest.getAccounts()));
        } else {
            filter.ownAccounts();
        }
        if (balanceRequest.getCategories() != null
                && !balanceRequest.getCategories().isEmpty()) {
            filter.categories(toEntityRefList(balanceRequest.getCategories()));
        }
        if (balanceRequest.getContracts() != null
                && !balanceRequest.getContracts().isEmpty()) {
            filter.contracts(toEntityRefList(balanceRequest.getContracts()));
        }
        if (balanceRequest.getExpenses() != null
                && !balanceRequest.getExpenses().isEmpty()) {
            filter.expenses(toEntityRefList(balanceRequest.getExpenses()));
        }

        if (balanceRequest.getCurrency() != null) {
            filter.currency(balanceRequest.getCurrency());
        }
        if (balanceRequest.getImportSlug() != null) {
            filter.importSlug(balanceRequest.getImportSlug());
        }
        if (balanceRequest.getType() != null) {
            switch (balanceRequest.getType()) {
                case INCOME -> filter.onlyIncome(true);
                case EXPENSE -> filter.onlyIncome(false);
                case ALL -> filter.hashCode();
            }
        }

        return filter;
    }

    private Sequence<EntityRef> toEntityRefList(List<Long> ids) {
        return Collections.List(ids.stream().map(EntityRef::new).toList());
    }
}
