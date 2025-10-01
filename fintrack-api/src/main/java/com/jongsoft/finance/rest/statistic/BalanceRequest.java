package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.lang.Control;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Serdeable
public class BalanceRequest {

    @Serdeable
    public record DateRange(
            @Schema(
                            description = "Earliest date a transaction may be.",
                            implementation = String.class,
                            format = "yyyy-mm-dd")
                    LocalDate start,
            @Schema(
                            description = "Latest date a transaction may be.",
                            implementation = String.class,
                            format = "yyyy-mm-dd")
                    LocalDate end) {}

    @Serdeable
    public static class EntityRef implements AggregateBase {

        @Schema(description = "The unique identifier of the entity", required = true)
        private final Long id;

        private final String name;

        public EntityRef(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    private final List<EntityRef> accounts;
    private final List<EntityRef> categories;
    private final List<EntityRef> contracts;
    private final List<EntityRef> expenses;
    private final DateRange dateRange;

    @Schema(description = "Indicator if only income or only expense should be included")
    private final boolean onlyIncome;

    @Schema(description = "Indicator that all money (income and expense) should be included")
    private final boolean allMoney;

    @Schema(description = "The currency the transaction should be in")
    private final String currency;

    private final String importSlug;

    public BalanceRequest(
            List<EntityRef> accounts,
            List<EntityRef> categories,
            List<EntityRef> contracts,
            List<EntityRef> expenses,
            DateRange dateRange,
            boolean onlyIncome,
            boolean allMoney,
            String currency,
            String importSlug) {
        this.accounts = accounts;
        this.categories = categories;
        this.contracts = contracts;
        this.expenses = expenses;
        this.dateRange = dateRange;
        this.onlyIncome = onlyIncome;
        this.allMoney = allMoney;
        this.currency = currency;
        this.importSlug = importSlug;
    }

    public List<EntityRef> getAccounts() {
        return Control.Option(accounts).getOrSupply(List::of);
    }

    public List<EntityRef> getCategories() {
        return Control.Option(categories).getOrSupply(List::of);
    }

    public List<EntityRef> getContracts() {
        return Control.Option(contracts).getOrSupply(List::of);
    }

    public List<EntityRef> getExpenses() {
        return Control.Option(expenses).getOrSupply(List::of);
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public boolean onlyIncome() {
        return onlyIncome;
    }

    public boolean allMoney() {
        return allMoney;
    }

    public String currency() {
        return currency;
    }

    public String importSlug() {
        return importSlug;
    }
}
