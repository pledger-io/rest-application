package com.jongsoft.finance.bpmn.delegate.importer;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportAccountExtractorDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing import account extraction '{}' - {}",
                execution.getCurrentActivityName(),
                execution.getVariable("name"),
                execution.getVariable("account"));

        @SuppressWarnings("unchecked")
        var results = (List<Pair<String, Number>>) execution.getVariable("resultSet");

        results.add(API.Tuple(
                execution.<StringValue>getVariableLocalTyped("name").getValue(),
                (Long) execution.getVariableLocal("account")));
    }

}
