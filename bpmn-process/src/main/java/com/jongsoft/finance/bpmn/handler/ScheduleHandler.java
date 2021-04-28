package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.bpmn.KnownProcesses;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.ScheduleCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;

import javax.inject.Singleton;
import java.util.Objects;

@Slf4j
@Singleton
public class ScheduleHandler implements CommandHandler<ScheduleCommand> {

    private final AuthenticationFacade authenticationFacade;
    private final ProcessEngine processEngine;

    public ScheduleHandler(AuthenticationFacade authenticationFacade, ProcessEngine processEngine) {
        this.authenticationFacade = authenticationFacade;
        this.processEngine = processEngine;
    }

    @BusinessEventListener
    public void handle(ScheduleCommand command) {
        log.info("Processing scheduler {} command", command.businessKey());
        deleteAnyActiveProcess(command);
        startNewActivity(command);
    }

    private void startNewActivity(ScheduleCommand command) {
        Objects.requireNonNull(command.schedulable(), "Entity to be scheduled cannot be null.");

        var starter = processEngine.getRuntimeService()
                .createProcessInstanceByKey(KnownProcesses.PROCESS_SCHEDULE)
                .businessKey(command.businessKey())
                .setVariable("username", authenticationFacade.authenticated())
                .setVariable("subProcess", command.processDefinition())
                .setVariable(command.processDefinition(), command.variables());

        if (Objects.nonNull(command.schedulable().getStart())) {
            starter.setVariable("start", command.schedulable().getStart().toString());
        }

        if (Objects.nonNull(command.schedulable().getEnd())) {
            starter.setVariable("end", command.schedulable().getEnd().toString());
        }

        if (Objects.nonNull(command.schedulable().getSchedule())) {
            starter.setVariable("interval", command.schedulable().getSchedule().interval())
                    .setVariable("periodicity", command.schedulable().getSchedule().periodicity());
        }

        starter.execute();
    }

    private void deleteAnyActiveProcess(ScheduleCommand command) {
        var runningProcess = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(command.businessKey())
                .singleResult();
        if (runningProcess != null) {
            processEngine.getRuntimeService()
                    .deleteProcessInstance(runningProcess.getProcessInstanceId(), "Schedule adjusted");
        }
    }

}
