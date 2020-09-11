package com.jongsoft.finance.bpmn.delegate.transaction;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;

@Singleton
public class PrepareAccountGenerationDelegate implements JavaDelegate {

    private final StorageService storageService;

    public PrepareAccountGenerationDelegate(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

        ParsedTransaction transaction = ParsedTransaction.parse(storageService.read(transactionToken));

        execution.setVariableLocal(
                "accountJson",
                ProcessMapper.INSTANCE.writeValueAsString(transaction.getAccount()));
    }

}
