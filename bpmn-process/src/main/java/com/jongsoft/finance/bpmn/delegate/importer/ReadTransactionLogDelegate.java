package com.jongsoft.finance.bpmn.delegate.importer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReadTransactionLogDelegate extends CSVReaderDelegate implements JavaDelegate {

    private final StorageService storageService;

    @Inject
    public ReadTransactionLogDelegate(ImportProvider importProvider, StorageService storageService) {
        super(importProvider, storageService);
        this.storageService = storageService;
    }

    @Override
    protected void beforeProcess(DelegateExecution execution, ImportConfigJson configJson) {
        execution.setVariableLocal("generateAccounts", configJson.isGenerateAccounts());
        execution.setVariableLocal("applyRules", configJson.isApplyRules());
        execution.setVariableLocal("targetAccountId", configJson.getAccountId());
        execution.setVariableLocal("storageTokens", new ArrayList<String>());
    }

    @Override
    protected void lineRead(DelegateExecution execution, ParsedTransaction parsedTransaction) {
        var serialized = parsedTransaction.stringify()
                .getBytes(StandardCharsets.UTF_8);

        String storageToken = storageService.store(serialized);

        var storageTokens = (ArrayList<String>) execution.getVariableLocal("storageTokens");
        storageTokens.add(storageToken);
    }

    @Override
    protected void afterProcess(DelegateExecution execution) {
        // no specific implementation required
    }

}
