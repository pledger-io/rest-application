package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.bpmn.KnownProcesses;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.ProcessEngine;

@Slf4j
@Singleton
class ChangeContractHandler implements CommandHandler<ChangeContractCommand> {

    private final ProcessEngine processEngine;

    ChangeContractHandler(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeContractCommand command) {
        log.trace(
                "[{}] - Updating existing BPMN flow for contract expired notification.",
                command.id());

        var runningProcess = processEngine
                .getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(KnownProcesses.CONTRACT_WARN_EXPIRY)
                .processInstanceBusinessKey("contract_term_" + command.id())
                .singleResult();

        if (runningProcess != null) {
            var timerJob = processEngine
                    .getManagementService()
                    .createJobQuery()
                    .processDefinitionKey(KnownProcesses.CONTRACT_WARN_EXPIRY)
                    .processInstanceId(runningProcess.getProcessInstanceId())
                    .singleResult();

            var newDueDate = command.end().minusMonths(1);
            processEngine
                    .getManagementService()
                    .setJobDuedate(timerJob.getId(), DateUtils.toDate(newDueDate));
        }
    }
}
