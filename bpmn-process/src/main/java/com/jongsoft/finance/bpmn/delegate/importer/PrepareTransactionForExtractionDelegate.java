package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.StorageService;
import com.jongsoft.lang.collection.tuple.Triplet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PrepareTransactionForExtractionDelegate implements JavaDelegate {

    private final StorageService storageService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariableLocal("accountLookup")) {
            @SuppressWarnings("unchecked")
            var nameIbanPair = (Triplet<String, String, String>) execution.getVariableLocal("accountLookup");

            execution.setVariableLocal("iban", nameIbanPair.getSecond());
            execution.setVariableLocal("name", nameIbanPair.getFirst());
        } else {
            String transactionToken = execution.<StringValue>getVariableLocalTyped("transactionToken").getValue();

            ParsedTransaction transaction = ParsedTransaction.parse(storageService.read(transactionToken).block());

            execution.setVariableLocal("iban", transaction.getOpposingIBAN());
            execution.setVariableLocal("name", transaction.getOpposingName());
        }
    }

}
