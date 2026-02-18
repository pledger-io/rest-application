package com.jongsoft.finance.exporter.domain.service.transaction;

import com.jongsoft.finance.exporter.domain.model.ProcessConfiguration;
import com.jongsoft.finance.exporter.types.ProcessingStage;

import io.micronaut.serde.annotation.Serdeable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Serdeable
class ImportContext {
    private ProcessConfiguration configuration;
    private Map<String, Long> accountMapping;
    private ProcessingStage currentStage;
    private boolean pendingUserAction;

    public ImportContext() {
        accountMapping = new HashMap<>();
        currentStage = ProcessingStage.CONFIGURATION;
    }

    public ProcessConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ProcessConfiguration configuration) {
        this.configuration = configuration;
    }

    public Map<String, Long> getAccountMapping() {
        return accountMapping;
    }

    public void setAccountMapping(Map<String, Long> accountMapping) {
        this.accountMapping = accountMapping;
    }

    public ProcessingStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(ProcessingStage currentStage) {
        this.currentStage = currentStage;
    }

    public void waitForUser() {
        pendingUserAction = true;
    }

    public boolean isPendingUserAction() {
        return pendingUserAction;
    }

    public void addMapping(String accountName, Long accountId) {
        accountMapping.put(accountName, accountId);
    }

    public boolean hasMissingAccounts() {
        return accountMapping.values().stream().anyMatch(Objects::isNull);
    }

    public long locateAccount(String accountName) {
        return accountMapping.get(accountName);
    }

    public void userReplied() {
        pendingUserAction = false;
    }
}
