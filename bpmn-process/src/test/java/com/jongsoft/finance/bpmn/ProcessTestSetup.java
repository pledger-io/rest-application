package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

@Deprecated
@MicronautTest(startApplication = false)
class ProcessTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void initEventing() {
        new EventBus(eventPublisher);
    }

    protected boolean waitForSuspended(ProcessEngine processEngine, String processId) {
        long timeout = System.currentTimeMillis() + 2500;

        while (System.currentTimeMillis() < timeout) {
            var process = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processId)
                    .singleResult();

            var activities = processEngine.getHistoryService()
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(processId)
                    .unfinished()
                    .list();

            if (!HistoricProcessInstance.STATE_ACTIVE.equals(process.getState())) {
                return true;
            }

            var tasks = activities.stream()
                    .filter(activity ->
                            activity.getActivityType().contains("Task")
                                    || activity.getActivityType().equals("intermediateTimer"))
                    .count();
            if (tasks > 0) {
                return true;
            }

            sleep();
        }

        System.out.println("Wait for process finish timeout!");
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @MockBean
    public Encoder encoder() {
        return Mockito.mock(Encoder.class);
    }

    public static Transaction buildTransaction(double amount, String description, String to, String from) {
        return Transaction.builder()
                .description(description)
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .amount(amount)
                                .account(Account.builder()
                                        .id(1L)
                                        .name(to)
                                        .build())
                                .build(),
                        Transaction.Part.builder()
                                .amount(-amount)
                                .account(Account.builder()
                                        .id(2L)
                                        .name(from)
                                        .build())
                                .build()
                ))
                .build();
    }

    protected byte[] readResource(String name) {
        return Control.Option(getClass().getResourceAsStream(name))
                .map(stream -> Control.Try(stream::readAllBytes).get())
                .getOrThrow(() -> new IllegalStateException("Cannot read resource " + name));
    }
}
