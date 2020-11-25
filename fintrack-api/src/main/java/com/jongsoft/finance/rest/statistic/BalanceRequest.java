package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.lang.Control;
import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

import java.util.List;

@Setter
@Introspected
public class BalanceRequest {

    private List<EntityRef> accounts;
    private List<EntityRef> categories;
    private List<EntityRef> contracts;
    private List<EntityRef> expenses;
    private DateRangeOld dateRange;
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

    public DateRangeOld dateRange() {
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
