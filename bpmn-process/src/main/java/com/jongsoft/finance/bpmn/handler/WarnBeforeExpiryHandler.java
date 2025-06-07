package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.bpmn.KnownProcesses;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;

@Slf4j
@Singleton
class WarnBeforeExpiryHandler implements CommandHandler<WarnBeforeExpiryCommand> {

  private final ProcessEngine processEngine;
  private final AuthenticationFacade authenticationFacade;

  WarnBeforeExpiryHandler(ProcessEngine processEngine, AuthenticationFacade authenticationFacade) {
    this.processEngine = processEngine;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(WarnBeforeExpiryCommand command) {
    log.trace("[{}] - Starting the BPMN process to warn before contract expires.", command.id());

    var newDueDate = command.endDate().minusMonths(1);

    processEngine
        .getRuntimeService()
        .createProcessInstanceByKey(KnownProcesses.CONTRACT_WARN_EXPIRY)
        .businessKey("contract_term_" + command.id())
        .setVariable("warnAt", DateUtils.toDate(newDueDate))
        .setVariable("username", authenticationFacade.authenticated())
        .setVariable("contractId", command.id())
        .execute();
  }
}
