package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class TransactionBulkEditRequest {

    @NoArgsConstructor
    @AllArgsConstructor
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @NotNull
    @Size(min = 1)
    private List<Long> transactions;

    private EntityRef contract;
    private EntityRef budget;
    private EntityRef category;
    private List<String> tags;

    public List<Long> getTransactions() {
        return transactions;
    }

    public EntityRef getContract() {
        return contract;
    }

    public EntityRef getBudget() {
        return budget;
    }

    public EntityRef getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }
}
