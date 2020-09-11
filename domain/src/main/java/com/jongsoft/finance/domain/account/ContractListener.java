package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.domain.account.events.ContractChangedEvent;
import com.jongsoft.finance.domain.account.events.ContractCreatedEvent;
import com.jongsoft.finance.domain.account.events.ContractTerminatedEvent;
import com.jongsoft.finance.domain.account.events.ContractUploadEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;

public interface ContractListener {

    void handleContractCreated(ContractCreatedEvent event);
    void handleContractChanged(ContractChangedEvent event);
    void handleContractWarning(ContractWarningEvent event);
    void handleContractUpload(ContractUploadEvent event);
    void handleContractTerminated(ContractTerminatedEvent event);

}
