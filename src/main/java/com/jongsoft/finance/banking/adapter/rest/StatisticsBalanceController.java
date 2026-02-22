package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.StatisticsBalanceApi;
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
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;
    private final TransactionProvider transactionProvider;

    private final AccountProvider accountProvider;
    private final List<LinkableProvider<? extends Classifier>> linkableProviders;

    StatisticsBalanceController(
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            TransactionProvider transactionProvider,
            AccountProvider accountProvider,
            List<LinkableProvider<? extends Classifier>> linkableProviders) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.accountProvider = accountProvider;
        this.linkableProviders = linkableProviders;
        this.logger = LoggerFactory.getLogger(StatisticsBalanceController.class);
    }

    @Override
    public List<@Valid BalanceDatedResponse> computeBalanceGroupedByDate(
            ComputeBalanceGroupedByDateTypeParameter type, BalanceRequest balanceRequest) {
        logger.info("Computing balance grouped by date with provided filters.");
        var filter = toFilterCommand(balanceRequest);

        var balances =
                switch (type) {
                    case DAILY -> transactionProvider.daily(filter);
                    case MONTHLY -> transactionProvider.monthly(filter);
                };

        return balances.map(b -> new BalanceDatedResponse(b.summary(), b.day())).toJava();
    }

    @Override
    public BalanceResponse computeBalanceWithFilter(BalanceRequest balanceRequest) {
        logger.info("Computing balance with provided filters.");
        var filter = toFilterCommand(balanceRequest);

        var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);

        return new BalanceResponse(balance.doubleValue());
    }

    @Override
    public List<@Valid BalancePartitionedResponse> computePartitionedBalanceWithFilter(
            ComputePartitionedBalanceWithFilterPartitionParameter partition,
            BalanceRequest balanceRequest) {
        logger.info("Computing partitioned balance with provided filters.");

        var total = transactionProvider
                .balance(toFilterCommand(balanceRequest))
                .getOrSupply(() -> BigDecimal.ZERO);

        Sequence<? extends Classifier> entities =
                switch (partition) {
                    case ACCOUNT -> accountProvider.lookup();
                    case CATEGORY, BUDGET ->
                        linkableProviders.stream()
                                .filter(provider -> provider.typeOf().equals(partition.name()))
                                .findFirst()
                                .map(LinkableProvider::lookup)
                                .orElse(Collections.List());
                };

        var results = new ArrayList<BalancePartitionedResponse>();
        for (var entity : entities) {
            var entityList = Collections.List(new EntityRef(entity.getId()));
            var filter =
                    switch (partition) {
                        case ACCOUNT -> toFilterCommand(balanceRequest).accounts(entityList);
                        case CATEGORY -> toFilterCommand(balanceRequest).categories(entityList);
                        case BUDGET -> toFilterCommand(balanceRequest).expenses(entityList);
                    };

            var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);
            total = total.subtract(balance);
            results.add(new BalancePartitionedResponse(balance.doubleValue(), entity.toString()));
        }
        results.add(new BalancePartitionedResponse(total.doubleValue(), ""));

        return results;
    }

    private TransactionProvider.FilterCommand toFilterCommand(BalanceRequest balanceRequest) {
        var filter = filterFactory.create();

        if (balanceRequest.getRange() != null) {
            filter.range(Dates.range(
                    balanceRequest.getRange().getStartDate(),
                    balanceRequest.getRange().getEndDate()));
        }

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
            // todo fix
            //            filter.importSlug(balanceRequest.getImportSlug());
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
