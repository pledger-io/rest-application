package com.jongsoft.finance.bpmn.listeners;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.value.StringValue;

@Slf4j
@Singleton
public class StartProcessListener implements ExecutionListener, JavaBean {

    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;
    private final AuthenticationFacade authenticationFacade;

    StartProcessListener(
            ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher,
            AuthenticationFacade authenticationFacade) {
        this.eventPublisher = eventPublisher;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public void notify(DelegateExecution execution) {
        if (execution.hasVariable("username") && authenticationFacade.authenticated() == null) {
            var username = execution.<StringValue>getVariableTyped("username").getValue();

            log.info("[{}-{}] Correcting authentication to user {}",
                    execution.getProcessDefinitionId(),
                    execution.getCurrentActivityName(),
                    username);


            log.trace("[{}] Setting up security credentials for {}", execution.getProcessDefinitionId(), username);

            eventPublisher.publishEvent(new InternalAuthenticationEvent(this, username));
        }
    }

}
