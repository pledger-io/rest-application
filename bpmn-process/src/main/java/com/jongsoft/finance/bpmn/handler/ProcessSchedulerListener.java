package com.jongsoft.finance.bpmn.handler;

import java.util.Objects;

import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.events.ScheduledLimitEvent;
import com.jongsoft.finance.domain.core.events.ScheduledRescheduleEvent;
import com.jongsoft.finance.domain.core.events.SchedulerEvent;
import com.jongsoft.finance.security.AuthenticationFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProcessSchedulerListener {

    private final AuthenticationFacade authenticationFacade;
    private final ProcessEngine processEngine;

    public ProcessSchedulerListener(AuthenticationFacade authenticationFacade, ProcessEngine processEngine) {
        this.authenticationFacade = authenticationFacade;
        this.processEngine = processEngine;
    }

    @BusinessEventListener
    public void handleScheduleLimit(ScheduledLimitEvent event) {
        log.info("Processing scheduler {} limit change", event.getBusinessKey());
        deleteAnyActiveProcess(event);
        startNewActivity(event);
    }

    @BusinessEventListener
    public void handleReschedule(ScheduledRescheduleEvent event) {
        log.info("Processing scheduler {} schedule change", event.getBusinessKey());
        deleteAnyActiveProcess(event);
        startNewActivity(event);
    }

    private void startNewActivity(SchedulerEvent event) {
        Objects.requireNonNull(event.schedulable(), "Entity to be scheduled cannot be null.");

        var starter = processEngine.getRuntimeService()
                .createProcessInstanceByKey("ProcessScheduler")
                .businessKey(event.getBusinessKey())
                .setVariable("username", authenticationFacade.authenticated())
                .setVariable("subProcess", event.getProcessDefinition())
                .setVariable(event.getProcessDefinition(), event.variables());

        if (Objects.nonNull(event.schedulable().getStart())) {
            starter.setVariable("start", event.schedulable().getStart().toString());
        }

        if (Objects.nonNull(event.schedulable().getEnd())) {
            starter.setVariable("end", event.schedulable().getEnd().toString());
        }

        if (Objects.nonNull(event.schedulable().getSchedule())) {
            starter.setVariable("interval", event.schedulable().getSchedule().interval())
                    .setVariable("periodicity", event.schedulable().getSchedule().periodicity());
        }

        starter.execute();
    }

    private void deleteAnyActiveProcess(SchedulerEvent event) {
        var runningProcess = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(event.getBusinessKey())
                .singleResult();
        if (runningProcess != null) {
            processEngine.getRuntimeService()
                    .deleteProcessInstance(runningProcess.getProcessInstanceId(), "Schedule adjusted");
        }
    }

}
