package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Contract;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import org.bouncycastle.cert.dane.DANEEntry;

import java.time.LocalDate;

@Introspected
public class ContractResponse {

    private final Contract wrapped;

    public ContractResponse(Contract wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The identifier of the contract", required = true)
    public long getId() {
        return wrapped.getId();
    }

    @Schema(description = "The name of the contract", required = true, example = "Cable company")
    public String getName() {
        return wrapped.getName();
    }

    @Schema(description = "The description for the contract")
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Schema(description = "Indicator for an digital copy of the contract being present", required = true)
    public boolean isContractAvailable() {
        return wrapped.isUploaded();
    }

    @Schema(description = "The file token to get the digital copy")
    public String getFileToken() {
        return wrapped.getFileToken();
    }

    @Schema(description = "The start date of the contract")
    public LocalDate getStart() {
        return wrapped.getStartDate();
    }

    @Schema(description = "The end date of the contract")
    public LocalDate getEnd() {
        return wrapped.getEndDate();
    }

    @Schema(description = "Indicator that the contract has ended and is closed by the user")
    public boolean isTerminated() {
        return wrapped.isTerminated();
    }

    @Schema(description = "Indicator if a pre-emptive warning is active before the contract end date")
    public boolean isNotification() {
        return wrapped.isNotifyBeforeEnd();
    }

    @Schema(description = "The company / account the contract is with", required = true)
    public AccountResponse getCompany() {
        if (wrapped.getCompany() == null) {
            return null;
        }

        return new AccountResponse(wrapped.getCompany());
    }

}
