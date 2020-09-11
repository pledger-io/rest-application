package com.jongsoft.finance.bpmn.listeners;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import lombok.extern.slf4j.Slf4j;

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
