package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.importer.ImporterProvider;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ImportJobSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class ReadTransactionLogDelegate implements JavaDelegate {

    private final List<ImporterProvider<? extends ImporterConfiguration>> importerProviders;
    private final ImportProvider importProvider;
    private final StorageService storageService;
    private final ProcessMapper mapper;

    @Inject
    public ReadTransactionLogDelegate(
            List<ImporterProvider<?>> importerProviders,
            ImportProvider importProvider,
            StorageService storageService,
            ProcessMapper mapper) {
        this.importerProviders = importerProviders;
        this.importProvider = importProvider;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var batchImportSlug = (String) execution.getVariableLocal("batchImportSlug");
        var importJobSettings = (ImportJobSettings) execution.getVariable("importConfig");
        log.debug("{}: Processing transaction import {}", execution.getCurrentActivityName(), batchImportSlug);

        var importJob = importProvider.lookup(batchImportSlug).get();
        List<String> storageTokens = new ArrayList<>();

        importerProviders.stream()
                        .filter(provider -> provider.supports(importJobSettings.importConfiguration()))
                        .findFirst()
                        .ifPresentOrElse(
                                provider -> provider.readTransactions(
                                        transactionDTO -> {
                                            var serialized = mapper.writeSafe(transactionDTO)
                                                    .getBytes(StandardCharsets.UTF_8);
                                            storageTokens.add(storageService.store(serialized));
                                        },
                                        importJobSettings.importConfiguration(),
                                        importJob),
                                () -> log.warn("No importer provider found for configuration: {}", importJobSettings.importConfiguration())
                        );

        execution.setVariableLocal("generateAccounts", importJobSettings.generateAccounts());
        execution.setVariableLocal("applyRules", importJobSettings.applyRules());
        execution.setVariableLocal("targetAccountId", importJobSettings.accountId());
        execution.setVariableLocal("storageTokens", storageTokens);
    }
}
