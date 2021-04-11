package com.jongsoft.finance.messaging.handlers;

import com.jongsoft.finance.domain.account.events.ContractUploadEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;

public interface ContractListener {

    void handleContractWarning(ContractWarningEvent event);
    void handleContractUpload(ContractUploadEvent event);

}
