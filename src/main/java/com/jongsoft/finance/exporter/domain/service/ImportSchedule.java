package com.jongsoft.finance.exporter.domain.service;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.exporter.adapter.api.ImportProvider;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.service.transaction.ImportProcess;
import com.jongsoft.lang.Control;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Singleton
class ImportSchedule {
    private final Logger log = LoggerFactory.getLogger(ImportSchedule.class);

    private final UserProvider userProvider;
    private final ImportProvider importProvider;
    private final List<ImporterProvider<?>> importerProviders;

    private final AccountProvider accountProvider;
    private final StorageService storageService;
    private final TransactionCreationHandler transactionCreationHandler;

    ImportSchedule(
            UserProvider userProvider,
            ImportProvider importProvider,
            List<ImporterProvider<?>> importerProviders,
            AccountProvider accountProvider,
            StorageService storageService,
            TransactionCreationHandler transactionCreationHandler) {
        this.userProvider = userProvider;
        this.importProvider = importProvider;
        this.importerProviders = importerProviders;
        this.accountProvider = accountProvider;
        this.storageService = storageService;
        this.transactionCreationHandler = transactionCreationHandler;
    }

    @Scheduled(fixedRate = "${application.schedules.exporter.import.rate}")
    void execute() {
        for (UserAccount userAccount : userProvider.lookup()) {
            MDC.put("correlationId", UUID.randomUUID().toString());
            InternalAuthenticationEvent.authenticate(userAccount.getUsername().email());

            importProvider
                    .lookup()
                    .filter(b -> b.getFinished() == null)
                    .forEach(this::processImport);
        }
    }

    private Long lookupAccount(String name) {
        return accountProvider.lookup(name).map(WithId::getId).getOrSupply(() -> accountProvider
                .synonymOf(name)
                .map(WithId::getId)
                .getOrSupply(() -> null));
    }

    private void processImport(BatchImport batchImport) {
        log.info("Processing import {}", batchImport.getSlug());
        ImportProcess process = new ImportProcess(this::lookupAccount, batchImport, storageService);

        try {
            process.loadContext(importerProviders);
            if (!process.isWaiting()) {
                log.debug("Continue import process for {}", batchImport.getSlug());
                process.process(
                        importerProviders,
                        accountId -> accountProvider.lookup(accountId).get().getCurrency(),
                        transactionCreationHandler);
            }
        } catch (IOException e) {
            log.warn("Failed to load context for import {}", batchImport.getId());
        } finally {
            if (Control.Try(process::saveContext).isFailure()) {
                log.warn("Failed to save context for import {}", batchImport.getId());
            }
        }
    }
}
