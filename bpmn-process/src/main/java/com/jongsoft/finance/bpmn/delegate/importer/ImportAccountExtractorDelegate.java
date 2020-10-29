package com.jongsoft.finance.bpmn.delegate.importer;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.Set;

@Slf4j
public class ImportAccountExtractorDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing import account extraction '{}' - {}",
                execution.getCurrentActivityName(),
                execution.getVariable("name"),
                execution.getVariable("account"));

        @SuppressWarnings("unchecked")
        var results = (Set<ExtractionMapping>) execution.getVariable("extractionResult");

        var mapping = new ExtractionMapping(
                execution.<StringValue>getVariableLocalTyped("name").getValue(),
                (Long) execution.getVariableLocal("account"));

        results.add(mapping);
    }

}
