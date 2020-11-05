package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MicronautTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;

@MicronautTest(application = ApplicationContext.class, rollback = false)
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

}
