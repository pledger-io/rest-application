package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.lang.Control;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Serdeable
public class BalanceRequest {

    @Serdeable
    public record DateRange(
        @Schema(description = "Earliest date a transaction may be.", implementation = String.class, format = "yyyy-mm-dd")
        LocalDate start,
        @Schema(description = "Latest date a transaction may be.", implementation = String.class, format = "yyyy-mm-dd")
        LocalDate end
    ) {}

    private List<EntityRef> accounts;
    private List<EntityRef> categories;
    private List<EntityRef> contracts;
    private List<EntityRef> expenses;
    private DateRange dateRange;
    @Schema(description = "Indicator if only income or only expense should be included")
    private boolean onlyIncome;
    @Schema(description = "Indicator that all money (income and expense) should be included")
    private boolean allMoney;
    @Schema(description = "The currency the transaction should be in")
    private String currency;
    private String importSlug;

    @Setter
    @Serdeable
    public static class EntityRef implements AggregateBase {

        @Schema(description = "The unique identifier of the entity", required = true)
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
