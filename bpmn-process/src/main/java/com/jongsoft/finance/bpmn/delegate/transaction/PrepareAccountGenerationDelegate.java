package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

/**
 * This is a delegate that prepares the account generation.
 * It reads the transaction token from the execution and reads the transaction from the storage.
 * It then writes the account JSON to the execution in the property {@code accountJson}.
 */
@Singleton
public class PrepareAccountGenerationDelegate implements JavaDelegate {

    private final StorageService storageService;
    private final ProcessMapper mapper;

    PrepareAccountGenerationDelegate(StorageService storageService, ProcessMapper mapper) {
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

        var account = storageService.read(transactionToken)
                .map(String::new)
                .map(s -> mapper.readSafe(s, ParsedTransaction.class))
                .map(ParsedTransaction::getAccount)
                .getOrThrow(() -> new RuntimeException("Transaction not found"));

        execution.setVariableLocal(
                "accountJson",
                mapper.writeSafe(account));
    }

}
