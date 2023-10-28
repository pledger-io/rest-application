package com.jongsoft.finance.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.transaction.Transaction;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Serdeable.Serializable
public class TransactionResponse {

    private transient final Transaction wrapped;

    public TransactionResponse(Transaction wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The identifier of this transaction", example = "1")
    public long getId() {
        return wrapped.getId();
    }

    @Schema(description = "The description of the transaction", example = "Purchase of flowers")
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Schema(description = "The currency the transaction was in", example = "EUR")
    public String getCurrency() {
        return wrapped.getCurrency();
    }

    @Schema(description = "The amount of money transferred from one account into the other", example = "30.50")
    public double getAmount() {
        return wrapped.computeAmount(wrapped.computeTo());
    }

    @Schema(description = "The meta-information of the transaction")
    public Metadata getMetadata() {
        return new Metadata();
    }

    @Schema(description = "The type of transaction")
    public Type getType() {
        return new Type();
    }

    @Schema(description = "All dates relevant for this transaction")
    public Dates getDates() {
        return new Dates();
    }

    @Schema(description = "The account where the money went to")
    public AccountResponse getDestination() {
        return new AccountResponse(wrapped.computeTo());
    }

    @Schema(description = "The account where the money came from")
    public AccountResponse getSource() {
        return new AccountResponse(wrapped.computeFrom());
    }

    @Schema(description = "The multi-line split of the transaction, eg: purchased items")
    public List<SplitAmount> getSplit() {
        if (!wrapped.isSplit()) {
            return null;
        }

        var splitAccount = switch (wrapped.computeType()) {
            case DEBIT -> wrapped.computeFrom();
            case CREDIT -> wrapped.computeTo();
            case TRANSFER -> throw new IllegalStateException("Split transaction cannot be a transfer");
        };

        return wrapped.getTransactions()
                .filter(t -> t.getAccount().equals(splitAccount))
                .map(SplitAmount::new)
                .toJava();
    }

    @Serdeable.Serializable
    public class Metadata {

        @Schema(description = "The category this transaction was linked to", example = "Food related expenses")
        public String getCategory() {
            return wrapped.getCategory();
        }

        @Schema(description = "The budget expense this transaction contributes to", example = "Dining out")
        public String getBudget() {
            return wrapped.getBudget();
        }

        @Schema(description = "This transaction is part of this contract", example = "Weekly dining")
        public String getContract() {
            return wrapped.getContract();
        }

        @Schema(description = "The import job that created the transaction", example = "0b9b79faddd9ad388f3aa3b59048b7cd")
        public String getImport() {
            return wrapped.getImportSlug();
        }

        public FailureCode getFailureCode() {
            return wrapped.getFailureCode();
        }

        @Schema(description = "The tags that the transaction has", example = "food,dining")
        public List<String> getTags() {
            return wrapped.getTags() != null ? wrapped.getTags().toJava() : null;
        }
    }

    @Serdeable.Serializable
    public class Type {

        @Schema(description = "The type of transaction", allowableValues = {"CREDIT", "DEBIT", "TRANSFER"})
        public String getCode() {
            return wrapped.computeType().name();
        }

        @JsonProperty("class")
        @Schema(description = "The font-awesome class for this transaction type", example = "exchange-alt")
        public String getClazz() {
            return wrapped.computeType().getStyle();
        }
    }

    @Serdeable.Serializable
    public class Dates {
        @Schema(description = "The date this transaction was created")
        public LocalDate getTransaction() {
            return wrapped.getDate();
        }

        @Schema(description = "The date the transaction was recorded into the books")
        public LocalDate getBooked() {
            return wrapped.getBookDate();
        }

        @Schema(description = "The date from which the transaction gets interest applied")
        public LocalDate getInterest() {
            return wrapped.getInterestDate();
        }
    }

    @Serdeable.Serializable
    public static class SplitAmount {
        private final transient Transaction.Part wrapped;

        public SplitAmount(Transaction.Part wrapped) {
            this.wrapped = wrapped;
        }

        public String getDescription() {
            return wrapped.getDescription();
        }

        public double getAmount() {
            return Math.abs(wrapped.getAmount());
        }
    }
}
