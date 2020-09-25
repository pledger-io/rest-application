package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.lang.collection.List;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Introspected
@NoArgsConstructor
@AllArgsConstructor
class ContractOverviewResponse {

    private List<Contract>active;
    private List<Contract>terminated;

    public List<Contract> getActive() {
        return active;
    }

    public List<Contract> getTerminated() {
        return terminated;
    }
}
