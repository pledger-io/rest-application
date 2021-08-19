package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.schedule.Periodicity;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

class ProcessSchedulerIT extends ProcessTestSetup {

    @Inject
    private ProcessEngine processEngine;

    @Test
    void runSchedule() {
        var process = processEngine.getRuntimeService().createProcessInstanceByKey("ProcessScheduler")
                .setVariable("subProcess", "EmptyProcess")
                .setVariable("start", "2019-01-02")
                .setVariable("end", LocalDate.now().plusMonths(5).toString())
                .setVariable("interval", 3)
                .setVariable("periodicity", Periodicity.MONTHS)
                .execute();

        waitForSuspended(processEngine, process.getProcessInstanceId());

        var subProcess = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("EmptyProcess")
                .list();

        Assertions.assertThat(subProcess).isEmpty();
    }


    @Test
    void runSchedule_forceRun() throws InterruptedException {
        var process = processEngine.getRuntimeService().createProcessInstanceByKey("ProcessScheduler")
                .setVariable("subProcess", "EmptyProcess")
                .setVariable("start", "2019-01-02")
                .setVariable("end", LocalDate.now().plusYears(1).toString())
                .setVariable("interval", 3)
                .setVariable("periodicity", Periodicity.MONTHS)
                .setVariable("EmptyProcess", Map.of("subProcess-1", 1, "variable-2", "test"))
                .execute();

        waitForSuspended(processEngine, process.getProcessInstanceId());

        var nextRun = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processDefinitionKey("ProcessScheduler")
                .processInstanceId(process.getProcessInstanceId())
                .variableName("nextRun")
                .singleResult();

        var jobs = processEngine.getManagementService()
                .createJobQuery()
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();

        processEngine.getManagementService()
                .executeJob(jobs.getId());

        waitForSuspended(processEngine, process.getProcessInstanceId());

        var subProcess = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("EmptyProcess")
                .singleResult();

        Assertions.assertThat(subProcess).isNotNull();

        var variableCount = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processDefinitionKey("EmptyProcess")
                .count();

        var scheduled = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processDefinitionKey("EmptyProcess")
                .variableName("scheduled")
                .singleResult();

        var var1 = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processDefinitionKey("EmptyProcess")
                .variableName("subProcess-1")
                .singleResult();

        Assertions.assertThat(variableCount).isEqualTo(4);
        Assertions.assertThat(var1.getValue()).isEqualTo(1);
        Assertions.assertThat(scheduled.getValue()).isEqualTo(LocalDate.ofInstant(((Date)nextRun.getValue()).toInstant(),
                ZoneId.systemDefault()).toString());
    }


}
