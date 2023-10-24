package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.rest.model.ContractResponse;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Serdeable.Serializable
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
