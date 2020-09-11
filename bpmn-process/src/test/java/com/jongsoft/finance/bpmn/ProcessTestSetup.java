package com.jongsoft.finance.bpmn;

import java.util.List;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.jupiter.api.BeforeEach;

import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest(application = ApplicationContext.class)
class ProcessTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void initEventing() {
        new EventBus(eventPublisher);
    }

    protected boolean waitUntilNoActiveJobs(ProcessEngine processEngine, long wait) {
        long timeout = System.currentTimeMillis() + wait;

        while (System.currentTimeMillis() < timeout) {
            long jobCount = processEngine.getManagementService().createJobQuery().active().count();
            if (jobCount == 0) {
                return true;
            }
            final List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
            jobs.stream()
                    .filter(Job::isSuspended)
                    .forEach(job -> processEngine.getManagementService().executeJob(job.getId()));
        }

        return false;
    }

}
