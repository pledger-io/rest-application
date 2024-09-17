package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.core.JavaBean;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Collection;
import java.util.HashSet;

@Slf4j
@Singleton
public class AddToAccountMapping implements JavaDelegate, JavaBean {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var accountName = (String) execution.getVariableLocal("name");
        var accountId = (Number) execution.getVariableLocal("accountId");

        log.debug("{}: Adding account mapping for '{}' with id {}.",
                execution.getCurrentActivityName(),
                accountName,
                accountId);

        @SuppressWarnings("unchecked")
        var mappings = new HashSet<>((Collection<ExtractionMapping>)execution.getVariable("accountMappings"));
        mappings.removeIf(mapping -> mapping.getName().equals(accountName));
        mappings.add(new ExtractionMapping(accountName, accountId.longValue()));

        execution.setVariable("accountMappings", mappings);
    }
}
