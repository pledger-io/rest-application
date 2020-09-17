package com.jongsoft.finance.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;

public class TransactionResponse {

    private transient final Transaction wrapped;

    public TransactionResponse(Transaction wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public String getCurrency() {
        return wrapped.getCurrency();
    }

    public double getAmount() {
        return wrapped.computeAmount(wrapped.computeTo());
    }

    public Metadata getMetadata() {
        return new Metadata();
    }

    public Type getType() {
        return new Type();
    }

    public Dates getDates() {
        return new Dates();
    }

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

    public class Metadata {

        public String getCategory() {
            return wrapped.getCategory();
        }

        public String getBudget() {
            return wrapped.getBudget();
        }

        public String getContract() {
            return wrapped.getContract();
        }

        public String getImport() {
            return wrapped.getImportSlug();
        }

        public FailureCode getFailureCode() {
            return wrapped.getFailureCode();
        }

        public List<String> getTags() {
            return wrapped.getTags().toJava();
        }
    }

    public class Type {
        public String getCode() {
            return wrapped.computeType().name();
        }

        @JsonProperty("class")
        public String getClazz() {
            return wrapped.computeType().getStyle();
        }
    }

    public class Dates {
        public LocalDate getTransaction() {
            return wrapped.getDate();
        }

        public LocalDate getBooked() {
            return wrapped.getBookDate();
        }

        public LocalDate getInterest() {
            return wrapped.getInterestDate();
        }
    }

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
