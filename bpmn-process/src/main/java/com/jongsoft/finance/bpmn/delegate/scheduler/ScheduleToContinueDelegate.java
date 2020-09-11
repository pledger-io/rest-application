package com.jongsoft.finance.bpmn.delegate.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleToContinueDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Determine if the schedule should continue {} to run or not with end date {}",
                execution.getCurrentActivityName(),
                execution.getVariable("nextRun"),
                execution.getVariable("end"));

        var nextRun = (Date) execution.getVariable("nextRun");
        var end = LocalDate.parse(execution.getVariable("end").toString());

        execution.setVariable("continue", end.isAfter(convert(nextRun)));
    }

    private LocalDate convert(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }
}
