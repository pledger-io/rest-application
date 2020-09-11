package com.jongsoft.finance.rest.statistic;

import java.util.List;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.lang.API;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

@Setter
@Introspected
public class BalanceRequest {

    private List<EntityRef> accounts;
    private List<EntityRef> categories;
    private List<EntityRef> contracts;
    private List<EntityRef> expenses;
    private DateRange dateRange;
    private boolean onlyIncome;
    private boolean allMoney;
    private String currency;
    private String importSlug;

    @Setter
    public static class EntityRef implements AggregateBase {

        private Long id;
        private String name;

        @Override
        public Long getId() {
            return id;
        }

    }

    public List<EntityRef> getAccounts() {
        return API.Option(accounts).getOrSupply(List::of);
    }

    public List<EntityRef> getCategories() {
        return API.Option(categories).getOrSupply(List::of);
    }

    public List<EntityRef> getContracts() {
        return API.Option(contracts).getOrSupply(List::of);
    }

    public List<EntityRef> getExpenses() {
        return API.Option(expenses).getOrSupply(List::of);
    }

    public DateRange dateRange() {
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
