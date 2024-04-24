package com.jongsoft.finance.bpmn.listeners;

import com.jongsoft.finance.core.JavaBean;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

@Slf4j
@Singleton
public class StopProcessListener implements ExecutionListener, JavaBean {

    @Override
    public void notify(DelegateExecution execution) {
        log.info("[{}] Finish business process", execution.getProcessDefinitionId());
        execution.removeVariablesLocal();
        execution.removeVariables();
    }

}
