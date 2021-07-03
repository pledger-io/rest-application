package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.bpmn.KnownProcesses;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
class WarnBeforeExpiryHandler implements CommandHandler<WarnBeforeExpiryCommand> {

    private final ProcessEngine processEngine;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(WarnBeforeExpiryCommand command) {
        log.trace("[{}] - Starting the BPMN process to warn before contract expires.", command.id());

        var newDueDate = command.endDate().minusMonths(1);

        processEngine.getRuntimeService()
                .createProcessInstanceByKey(KnownProcesses.CONTRACT_WARN_EXPIRY)
                .businessKey("contract_term_" + command.id())
                .setVariable("warnAt", DateUtils.toDate(newDueDate))
                .setVariable("username", authenticationFacade.authenticated())
                .execute();
    }

}
