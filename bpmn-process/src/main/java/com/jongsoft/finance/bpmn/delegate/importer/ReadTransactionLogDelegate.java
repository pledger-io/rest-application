package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.importer.ImporterProvider;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ExtractedAccountLookup;
import com.jongsoft.finance.serialized.ImportJobSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Delegate to trigger the actual {@link ImporterProvider} to start the job of fetching and converting the transactions.
 * <p>
 *     The delegate will look up the {@link ImporterProvider} that supports the {@link ImporterConfiguration} of the
 *     current import job and start the process of reading the transactions.
 *     The transactions are then serialized and stored in the {@link StorageService} and the tokens are stored in the
 *     process variables called {@code storageTokens}.
 * </p>
 * <p>
 *     The delegate will also set the process variables {@code generateAccounts}, {@code applyRules} and {@code targetAccountId}
 *     based on the {@link ImportJobSettings} of the current import job.
 *     The delegate will log a warning if no {@link ImporterProvider} is found for the {@link ImporterConfiguration} of the
 *     current import job.
 * </p>
 */
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
        Set<ExtractedAccountLookup> locatable = new HashSet<>();

        importerProviders.stream()
                        .filter(provider -> provider.supports(importJobSettings.importConfiguration()))
                        .findFirst()
                        .ifPresentOrElse(
                                provider -> provider.readTransactions(
                                        transactionDTO -> {
                                            // write the serialized transaction to storage and store the token
                                            var serialized = mapper.writeSafe(transactionDTO)
                                                    .getBytes(StandardCharsets.UTF_8);
                                            storageTokens.add(storageService.store(serialized));

                                            // write the extracted account lookup to the locatable set
                                            locatable.add(new ExtractedAccountLookup(
                                                    transactionDTO.opposingName(),
                                                    transactionDTO.opposingIBAN(),
                                                    transactionDTO.description()));
                                        },
                                        importJobSettings.importConfiguration(),
                                        importJob),
                                () -> log.warn("No importer provider found for configuration: {}", importJobSettings.importConfiguration())
                        );

        if (locatable.isEmpty()) {
            log.warn("No accounts found for import job {}", batchImportSlug);
            throw StatusException.internalError("No parsable accounts found for import job", "bpmn.transaction.import.no-accounts-found");
        }

        execution.setVariableLocal("locatable", locatable);
        execution.setVariableLocal("generateAccounts", importJobSettings.generateAccounts());
        execution.setVariableLocal("applyRules", importJobSettings.applyRules());
        execution.setVariableLocal("targetAccountId", importJobSettings.accountId());
        execution.setVariableLocal("storageTokens", storageTokens);
    }
}
