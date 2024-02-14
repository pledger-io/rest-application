package com.jongsoft.finance.bpmn.delegate.importer;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Set;

@Slf4j
public class LocateAccountInMapping implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        var accountName = (String) delegateExecution.getVariableLocal("name");
        @SuppressWarnings("unchecked")
        var mappings = (Set<ExtractionMapping>) delegateExecution.getVariable("accountMappings");

        log.debug("{}: Locating account mapping for {}.",
                delegateExecution.getCurrentActivityName(),
                accountName);

        var accountId = mappings.stream()
                .filter(mapping -> mapping.getName().equals(accountName))
                .findFirst()
                .map(ExtractionMapping::getAccountId)
                .orElse(null);

        delegateExecution.setVariableLocal("accountId", accountId);
    }
}
