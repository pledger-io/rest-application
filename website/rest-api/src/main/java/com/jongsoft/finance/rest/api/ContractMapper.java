package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.rest.model.AccountLink;
import com.jongsoft.finance.rest.model.ContractResponse;

interface ContractMapper {

    static ContractResponse toContractResponse(Contract contract) {
        var response = new ContractResponse(
                contract.getId(),
                contract.getName(),
                contract.getStartDate(),
                contract.getEndDate(),
                new AccountLink(
                        contract.getCompany().getId(),
                        contract.getCompany().getName(),
                        contract.getCompany().getType()));
        response.setDescription(contract.getDescription());
        response.setFileToken(contract.getFileToken());
        response.setNotification(contract.isNotifyBeforeEnd());
        response.setTerminated(contract.isTerminated());

        return response;
    }
}
