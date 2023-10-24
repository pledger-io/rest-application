package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
public class PrepareAccountGenerationDelegate implements JavaDelegate {

    private final StorageService storageService;

    PrepareAccountGenerationDelegate(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

        ParsedTransaction transaction = ParsedTransaction.parse(storageService.read(transactionToken).get());

        execution.setVariableLocal(
                "accountJson",
                ProcessMapper.INSTANCE.writeValueAsString(transaction.getAccount()));
    }

}
