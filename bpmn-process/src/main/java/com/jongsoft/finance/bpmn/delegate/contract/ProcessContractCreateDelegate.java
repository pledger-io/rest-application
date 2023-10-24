package com.jongsoft.finance.bpmn.delegate.contract;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.serialized.ContractJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Slf4j
@Singleton
public class ProcessContractCreateDelegate implements JavaDelegate {

    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;
    private final StorageService storageService;

    ProcessContractCreateDelegate(
            AccountProvider accountProvider,
            ContractProvider contractProvider,
            StorageService storageService) {
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var contractJson = ProcessMapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("contract").getValue(),
                ContractJson.class);

        log.debug("{}: Processing contract creation from json '{}'",
                execution.getCurrentActivityName(),
                contractJson.getName());

        contractProvider.lookup(contractJson.getName())
                .ifNotPresent(() -> {
                    accountProvider.lookup(contractJson.getCompany())
                            .map(account -> account.createContract(
                                    contractJson.getName(),
                                    contractJson.getDescription(),
                                    contractJson.getStart(),
                                    contractJson.getEnd()))
                            .ifPresent(unsaved -> {
                                contractProvider.lookup(contractJson.getName())
                                        .ifPresent(contract -> {
                                            if (contractJson.getContract() != null) {
                                                contract.registerUpload(storageService.store(Hex.decode(contractJson.getContract())));
                                            }

                                            if (contractJson.isTerminated()) {
                                                contract.terminate();
                                            }
                                        });
                            });
                });
    }
}
