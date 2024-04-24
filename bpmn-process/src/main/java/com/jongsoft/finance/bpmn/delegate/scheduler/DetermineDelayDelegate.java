package com.jongsoft.finance.bpmn.delegate.scheduler;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.jongsoft.finance.core.JavaBean;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.schedule.Periodicity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetermineDelayDelegate implements JavaDelegate, JavaBean {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Calculating delay based upon start {} and periodicity {} with interval {}",
                execution.getCurrentActivityName(),
                execution.getVariable("start"),
                execution.getVariable("periodicity"),
                execution.getVariable("interval"));

        var interval = (Integer) execution.getVariable("interval");
        var periodicity = (Periodicity) execution.getVariable("periodicity");
        var startCalculation = LocalDate.parse(execution.getVariable("start").toString());
        var datePart = periodicity.toChronoUnit();

        var nextRun = nextRun(startCalculation, interval, datePart);
        execution.setVariable("nextRun", convert(nextRun));
    }

    private LocalDate nextRun(LocalDate currentRun, int interval, ChronoUnit temporalUnit) {
        var now = LocalDate.now().plusDays(1);
        var nextRun = currentRun.plus(interval, temporalUnit);

        while (now.isAfter(nextRun)) {
            nextRun = nextRun.plus(interval, temporalUnit);
        }

        return nextRun;
    }

    private Date convert(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
