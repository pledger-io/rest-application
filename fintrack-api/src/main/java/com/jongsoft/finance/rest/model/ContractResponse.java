package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Contract;
import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;

@Introspected
public class ContractResponse {

    private final Contract wrapped;

    public ContractResponse(Contract wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public boolean isContractAvailable() {
        return wrapped.isUploaded();
    }

    public String getFileToken() {
        return wrapped.getFileToken();
    }

    public LocalDate getStart() {
        return wrapped.getStartDate();
    }

    public LocalDate getEnd() {
        return wrapped.getEndDate();
    }

    public boolean isTerminated() {
        return wrapped.isTerminated();
    }

    public boolean isNotification() {
        return wrapped.isNotifyBeforeEnd();
    }

    public AccountResponse getCompany() {
        return new AccountResponse(wrapped.getCompany());
    }

}
