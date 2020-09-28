package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.rest.model.ContractResponse;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
class ContractOverviewResponse {

    private List<ContractResponse> active;
    private List<ContractResponse>terminated;

    public List<ContractResponse> getActive() {
        return active;
    }

    public List<ContractResponse> getTerminated() {
        return terminated;
    }
}
