package com.jongsoft.finance.bpmn.feature;

import com.jongsoft.finance.bpmn.feature.junit.ProcessExtension;
import com.jongsoft.finance.bpmn.feature.junit.RuntimeContext;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.commons.lang3.mutable.MutableObject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@MicronautTest
@ProcessExtension
@DisplayName("Process Scheduler feature")
class ProcessSchedulerIT  {

    @Test
    @DisplayName("Run a schedule, not yet due")
    void runSchedule(RuntimeContext context)  {
        var execution = context.execute("ProcessScheduler", Map.of(
                "subProcess", "EmptyProcess",
                "start", "2019-01-02",
                "end", LocalDate.now().plusMonths(5).toString(),
                "interval", 3,
                "periodicity", Periodicity.MONTHS
        ));

        execution.verifyPendingActivity("scheduled_wait");
    }


    @Test
    @DisplayName("Run a schedule, force run to trigger next iteration")
    void runSchedule_forceRun(RuntimeContext context) throws InterruptedException {
        var execution = context.execute("ProcessScheduler", Map.of(
                "subProcess", "EmptyProcess",
                "start", "2019-01-02",
                "end", LocalDate.now().plusYears(1).toString(),
                "interval", 3,
                "periodicity", Periodicity.MONTHS,
                "EmptyProcess", Map.of("subProcess-1", 1, "variable-2", "test")
        ));

        MutableObject<Date> nextRun = new MutableObject<>();
        MutableObject<String> scheduled = new MutableObject<>();
        MutableObject<Integer> subValue = new MutableObject<>();

        execution
                .verifyPendingActivity("scheduled_wait")
                .yankVariable("nextRun", nextRun::setValue)
                .forceJob("scheduled_wait")
                .verifyPendingActivity("scheduled_wait")
                .obtainChildProcess("EmptyProcess")
                .yankVariable("scheduled", scheduled::setValue)
                .yankVariable("subProcess-1", subValue::setValue)
                .verifyCompleted();

        Assertions.assertThat(subValue.getValue()).isEqualTo(1);
        Assertions.assertThat(scheduled.getValue())
                .isEqualTo(
                        LocalDate.ofInstant(
                            nextRun.getValue().toInstant(),
                            ZoneId.systemDefault()).toString());
    }

}
