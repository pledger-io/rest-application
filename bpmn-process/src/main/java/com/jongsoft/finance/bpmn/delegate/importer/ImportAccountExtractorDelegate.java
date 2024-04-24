package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.core.JavaBean;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.HashSet;
import java.util.Set;

/**
 * Extracts account mappings from the import transaction.
 * <p>
 *     This delegate is used to extract account mappings from the imported transaction.
 *     The account mappings are stored in a set of {@link ExtractionMapping} objects.
 *     The account mappings are used to map the account names in the import transaction to the account IDs in the finance system.
 * <p>
 */
@Slf4j
public class ImportAccountExtractorDelegate implements JavaDelegate, JavaBean {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Creating import transaction account mapping '{}' - {}",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"),
                execution.getVariableLocal("accountId"));

        var mapping = new ExtractionMapping(
                execution.<StringValue>getVariableLocalTyped("name").getValue(),
                (Long) execution.getVariableLocal("accountId"));

        getAccountMappings(execution).add(mapping);
    }

    @SuppressWarnings("unchecked")
    private Set<ExtractionMapping> getAccountMappings(DelegateExecution execution) {
        if (!execution.hasVariable("accountMappings")) {
            execution.setVariable("accountMappings", new HashSet<>());
        }
        return (Set<ExtractionMapping>) execution.getVariable("accountMappings");
    }

}
