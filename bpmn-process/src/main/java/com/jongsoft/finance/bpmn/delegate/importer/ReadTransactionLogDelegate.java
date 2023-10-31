package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
@Singleton
public class ReadTransactionLogDelegate extends CSVReaderDelegate implements JavaDelegate {

    private final StorageService storageService;
    private final ProcessMapper mapper;

    @Inject
    public ReadTransactionLogDelegate(ImportProvider importProvider, StorageService storageService, ProcessMapper mapper) {
        super(importProvider, storageService, mapper);
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    protected void beforeProcess(DelegateExecution execution, ImportConfigJson configJson) {
        log.debug("Setting up reader for import file with configuration {}", configJson);
        execution.setVariableLocal("generateAccounts", configJson.isGenerateAccounts());
        execution.setVariableLocal("applyRules", configJson.isApplyRules());
        execution.setVariableLocal("targetAccountId", configJson.getAccountId());
        execution.setVariableLocal("storageTokens", new ArrayList<String>());
    }

    @Override
    protected void lineRead(DelegateExecution execution, ParsedTransaction parsedTransaction) {
        log.debug("Read line {} of file import", parsedTransaction);

        var serialized = mapper.writeSafe(parsedTransaction)
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
