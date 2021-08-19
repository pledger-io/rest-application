package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PrepareAccountGenerationDelegate implements JavaDelegate {

    private final StorageService storageService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

        ParsedTransaction transaction = ParsedTransaction.parse(storageService.read(transactionToken).block());

        execution.setVariableLocal(
                "accountJson",
                ProcessMapper.INSTANCE.writeValueAsString(transaction.getAccount()));
    }

}
