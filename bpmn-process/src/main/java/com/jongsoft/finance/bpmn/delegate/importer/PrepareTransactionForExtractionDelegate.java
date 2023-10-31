package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.lang.collection.tuple.Triplet;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
public class PrepareTransactionForExtractionDelegate implements JavaDelegate {

    private final StorageService storageService;
    private final ProcessMapper processMapper;

    PrepareTransactionForExtractionDelegate(StorageService storageService, ProcessMapper processMapper) {
        this.storageService = storageService;
        this.processMapper = processMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariableLocal("accountLookup")) {
            @SuppressWarnings("unchecked")
            var nameIbanPair = (Triplet<String, String, String>) execution.getVariableLocal("accountLookup");

            execution.setVariableLocal("iban", nameIbanPair.getSecond());
            execution.setVariableLocal("name", nameIbanPair.getFirst());
        } else {
            String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

            var transaction = processMapper.readSafe(
                    new String(storageService.read(transactionToken).get()),
                    ParsedTransaction.class);

            execution.setVariableLocal("iban", transaction.getOpposingIBAN());
            execution.setVariableLocal("name", transaction.getOpposingName());
        }
    }

}
