package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.importer.ImporterProvider;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.serialized.ExtractedAccountLookup;
import com.jongsoft.finance.serialized.ImportJobSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class ExtractAccountDetailsDelegate implements JavaDelegate {
    private final static Logger logger = LoggerFactory.getLogger(ExtractAccountDetailsDelegate.class);

    private final List<ImporterProvider<? extends ImporterConfiguration>> importerProviders;
    private final ImportProvider importProvider;

    @Inject
    public ExtractAccountDetailsDelegate(List<ImporterProvider<? extends ImporterConfiguration>> importerProviders, ImportProvider importProvider) {
        this.importerProviders = importerProviders;
        this.importProvider = importProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var batchImportSlug = (String) execution.getVariableLocal("batchImportSlug");
        var importJobSettings = (ImportJobSettings) execution.getVariable("importConfig");
        logger.debug("{}: Processing transaction import {}", execution.getCurrentActivityName(), batchImportSlug);

        var importJob = importProvider.lookup(batchImportSlug).get();
        Set<ExtractedAccountLookup> locatable = new HashSet<>();
        importerProviders.stream()
                .filter(provider -> provider.supports(importJobSettings.importConfiguration()))
                .findFirst()
                .ifPresentOrElse(
                        provider -> provider.readTransactions(
                                transactionDTO -> {
                                    locatable.add(new ExtractedAccountLookup(
                                            transactionDTO.opposingName(),
                                            transactionDTO.opposingIBAN(),
                                            transactionDTO.description()));
                                },
                                importJobSettings.importConfiguration(),
                                importJob),
                        () -> logger.warn("No importer provider found for configuration: {}", importJobSettings.importConfiguration())
                );

        if (locatable.isEmpty()) {
            logger.warn("No accounts found for import job {}", batchImportSlug);
            throw StatusException.internalError("No parsable accounts found for import job", "bpmn.transaction.import.no-accounts-found");
        }

        execution.setVariableLocal("locatable", locatable);
    }
}
