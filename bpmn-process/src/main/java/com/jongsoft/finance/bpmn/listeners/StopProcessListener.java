package com.jongsoft.finance.bpmn.listeners;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

@Slf4j
@Singleton
public class StopProcessListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("[{}] Finish business process", execution.getProcessDefinitionId());
        execution.removeVariablesLocal();
        execution.removeVariables();
    }

}
