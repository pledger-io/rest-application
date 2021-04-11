package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.messaging.commands.schedule.ScheduleCommand;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;
import com.jongsoft.finance.security.AuthenticationFacade;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

class ProcessSchedulerListenerTest {

    private ProcessEngine processEngine;
    private ProcessSchedulerListener subject;

    private Schedulable schedulable;

    private ProcessInstanceQuery queryMock;
    private ProcessInstantiationBuilder processInstantiationBuilder;

    @BeforeEach
    void setup() {
        final AuthenticationFacade authenticationFacade = Mockito.mock(AuthenticationFacade.class);

        processEngine = Mockito.mock(ProcessEngine.class);
        queryMock = Mockito.mock(ProcessInstanceQuery.class);
        processInstantiationBuilder = Mockito.mock(ProcessInstantiationBuilder.class);
        schedulable = new Schedulable() {
            @Override
            public void limit(LocalDate start, LocalDate end) {
            }

            @Override
            public void adjustSchedule(Periodicity periodicity, int interval) {
            }

            @Override
            public LocalDate getStart() {
                return LocalDate.now().minusYears(1);
            }

            @Override
            public LocalDate getEnd() {
                return LocalDate.now().plusYears(2);
            }

            @Override
            public Schedule getSchedule() {
                return new ScheduleValue(Periodicity.MONTHS, 1);
            }
        };

        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);

        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito.when(runtimeService.createProcessInstanceQuery()).thenReturn(queryMock);
        Mockito.when(runtimeService.createProcessInstanceByKey("ProcessScheduler")).thenReturn(processInstantiationBuilder);

        Mockito.when(queryMock.processInstanceBusinessKey(Mockito.anyString())).thenReturn(queryMock);
        Mockito.doReturn(processInstantiationBuilder).when(processInstantiationBuilder).businessKey(Mockito.anyString());
        Mockito.when(processInstantiationBuilder.setVariable(Mockito.anyString(), Mockito.any())).thenReturn(processInstantiationBuilder);
        Mockito.when(authenticationFacade.authenticated()).thenReturn("test-user");

        subject = new ProcessSchedulerListener(authenticationFacade, processEngine);
    }

    @Test
    void handleScheduleLimit() {
        subject.handleScheduleCommand(new ScheduleCommand() {
            @Override
            public String processDefinition() {
                return "EmptyProcess";
            }

            @Override
            public Schedulable schedulable() {
                return schedulable;
            }
        });

        Mockito.verify(processInstantiationBuilder).setVariable("subProcess", "EmptyProcess");
        Mockito.verify(processInstantiationBuilder).setVariable("start", schedulable.getStart().toString());
        Mockito.verify(processInstantiationBuilder).setVariable("end", schedulable.getEnd().toString());
        Mockito.verify(processInstantiationBuilder).setVariable("interval", 1);
        Mockito.verify(processInstantiationBuilder).setVariable("username", "test-user");
        Mockito.verify(processInstantiationBuilder).setVariable("periodicity", Periodicity.MONTHS);
        Mockito.verify(processInstantiationBuilder).execute();
    }

}
