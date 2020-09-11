package com.jongsoft.finance.bpmn.listeners;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.finance.security.AuthenticationFacade;

import io.micronaut.context.event.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class StartProcessListener implements ExecutionListener {

    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationFacade authenticationFacade;

    public StartProcessListener(
            final ApplicationEventPublisher eventPublisher,
            final AuthenticationFacade authenticationFacade) {
        this.eventPublisher = eventPublisher;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("[{}] Starting business process", execution.getProcessDefinitionId());

        if (execution.hasVariable("username") && authenticationFacade.authenticated() == null) {
            var username = execution.<StringValue>getVariableTyped("username").getValue();

            log.debug("[{}] Setting up security credentials for {}", execution.getProcessDefinitionId(), username);

            eventPublisher.publishEvent(new InternalAuthenticationEvent(this, username));
        }
    }

}
