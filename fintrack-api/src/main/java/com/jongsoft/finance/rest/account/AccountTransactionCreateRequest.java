package com.jongsoft.finance.rest.account;

import lombok.Data;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Setter
class AccountTransactionCreateRequest {

    @Data
    static class EntityRef {
        @NotNull
        private Long id;
        private String name;
    }

    @NotNull
    private LocalDate date;
    private LocalDate interestDate;
    private LocalDate bookDate;

    @NotNull
    @NotBlank
    private String currency;

    @NotBlank
    @Size(max = 1024)
    private String description;

    @NotNull
    private double amount;

    @NotNull
    private EntityRef source;
    @NotNull
    private EntityRef destination;

    private EntityRef category;
    private EntityRef budget;
    private EntityRef contract;
    private List<String> tags;

    public LocalDate getDate() {
        return date;
    }

    public LocalDate getInterestDate() {
        return interestDate;
    }

    public LocalDate getBookDate() {
        return bookDate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public EntityRef getSource() {
        return source;
    }

    public EntityRef getDestination() {
        return destination;
    }

    public EntityRef getCategory() {
        return category;
    }

    public EntityRef getBudget() {
        return budget;
    }

    public EntityRef getContract() {
        return contract;
    }

    public List<String> getTags() {
        return tags;
    }
}
