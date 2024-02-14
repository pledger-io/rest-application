package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.bpmn.delegate.importer.ParsedTransaction;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * This is a delegate that prepares the account generation.
 * It reads the transaction token from the execution and reads the transaction from the storage.
 * It then writes the account JSON to the execution in the property {@code accountJson}.
 */
@Slf4j
@Singleton
public class PrepareAccountGenerationDelegate implements JavaDelegate {
    private final ProcessMapper mapper;

    PrepareAccountGenerationDelegate(ProcessMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var transaction = (ParsedTransaction) execution.getVariableLocal("transaction");

        log.debug("{}: Extracting the account to be created from the transaction {}.",
                execution.getCurrentActivityName(),
                transaction.getAccount().getName());

        execution.setVariableLocal(
                "accountJson",
                mapper.writeSafe(transaction.getAccount()));
    }

}
