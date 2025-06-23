package com.jongsoft.finance.rest.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Optional;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

@Serdeable.Serializable
public class ProcessResponse {

  private final ProcessInstance wrapped;

  public ProcessResponse(ProcessInstance wrapped) {
    this.wrapped = wrapped;
  }

  public String getId() {
    return Optional.ofNullable(wrapped).map(Execution::getId).orElse(null);
  }

  public String getProcess() {
    return Optional.ofNullable(wrapped)
        .map(ProcessInstance::getProcessDefinitionId)
        .orElse(null);
  }

  public String getBusinessKey() {
    return Optional.ofNullable(wrapped).map(ProcessInstance::getBusinessKey).orElse(null);
  }

  public String getState() {
    return Optional.ofNullable(wrapped).map(Execution::isEnded).orElse(true)
        ? "COMPLETED"
        : "ACTIVE";
  }
}
