package com.jongsoft.finance.bpmn.delegate.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;

@Slf4j
public class SchedulerVariableMappingDelegate implements DelegateVariableMapping {

  private static final String SUB_PROCESS = "subProcess";

  @Override
  public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
    log.debug(
        "{}: Mapping input variables for sub process {}",
        superExecution.getCurrentActivityName(),
        superExecution.getVariable(SUB_PROCESS));

    var subProcess = superExecution.getVariable(SUB_PROCESS).toString();
    if (superExecution.hasVariable(subProcess)) {
      var variableMap = (Map<String, Object>) superExecution.getVariable(subProcess);
      variableMap.forEach(subVariables::putValue);
    }

    var scheduledDate = LocalDate.ofInstant(
        ((Date) superExecution.getVariable("nextRun")).toInstant(), ZoneId.systemDefault());
    subVariables.putValue("scheduled", scheduledDate.toString());
    subVariables.putValue("username", superExecution.getVariable("username"));
  }

  @Override
  public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
    log.debug(
        "{}: Mapping output variables for sub process {}",
        superExecution.getCurrentActivityName(),
        superExecution.getVariable(SUB_PROCESS));
  }
}
