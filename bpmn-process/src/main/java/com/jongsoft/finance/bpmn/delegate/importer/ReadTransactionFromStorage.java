package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;

@Singleton
public class ReadTransactionFromStorage implements JavaDelegate {

    private final StorageService storageService;
    private final ProcessMapper processMapper;

    @Inject
    public ReadTransactionFromStorage(StorageService storageService, ProcessMapper processMapper) {
        this.storageService = storageService;
        this.processMapper = processMapper;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var storageToken = (String) delegateExecution.getVariableLocal("storageToken");

        var transaction = storageService.read(storageToken)
                .map(byteArray -> new String(byteArray, StandardCharsets.UTF_8))
                .map(json -> processMapper.readSafe(json, ParsedTransaction.class))
                .getOrThrow(() -> new RuntimeException("Failed to read transaction from storage"));

        delegateExecution.setVariableLocal("transaction", transaction);
    }

}
