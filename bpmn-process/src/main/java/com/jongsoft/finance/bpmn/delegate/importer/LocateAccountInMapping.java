package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.providers.AccountProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Collection;

@Slf4j
@Singleton
public class LocateAccountInMapping implements JavaDelegate {

    private final AccountProvider accountProvider;

    public LocateAccountInMapping(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var accountName = (String) delegateExecution.getVariableLocal("name");
        @SuppressWarnings("unchecked")
        var mappings = (Collection<ExtractionMapping>) delegateExecution.getVariable("accountMappings");

        log.debug("{}: Locating account mapping for {}.",
                delegateExecution.getCurrentActivityName(),
                accountName);

        var accountId = mappings.stream()
                .filter(mapping -> mapping.getName().equals(accountName))
                .findFirst()
                .map(ExtractionMapping::getAccountId)
                .orElse(null);

        determineSynonym(accountName, accountId);

        delegateExecution.setVariableLocal("accountId", accountId);
    }

    private void determineSynonym(String accountName, Long accountId) {
        if (accountId == null) {
            return;
        }

        var account = accountProvider.lookup(accountId)
                .getOrThrow(() -> new IllegalStateException("Account not found: " + accountId));
        if (!account.getName().equals(accountName)) {
            log.info("Account name '{}' does not match the account name in the mapping '{}'.",
                    account.getName(),
                    accountName);
            account.registerSynonym(accountName);
        }
    }
}
