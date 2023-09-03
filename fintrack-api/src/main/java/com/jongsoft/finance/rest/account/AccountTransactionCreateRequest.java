package com.jongsoft.finance.rest.account;

import io.micronaut.core.annotation.Introspected;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Setter
@Introspected
class AccountTransactionCreateRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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

    @Builder
    @Generated
    public AccountTransactionCreateRequest(@NotNull LocalDate date, LocalDate interestDate, LocalDate bookDate, @NotNull @NotBlank String currency, @NotBlank @Size(max = 1024) String description, @NotNull double amount, @NotNull EntityRef source, @NotNull EntityRef destination, EntityRef category, EntityRef budget, EntityRef contract, List<String> tags) {
        this.date = date;
        this.interestDate = interestDate;
        this.bookDate = bookDate;
        this.currency = currency;
        this.description = description;
        this.amount = amount;
        this.source = source;
        this.destination = destination;
        this.category = category;
        this.budget = budget;
        this.contract = contract;
        this.tags = tags;
    }

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
