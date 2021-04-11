package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import org.camunda.bpm.engine.ProcessEngine;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@Singleton
public class ContractListener {

    private static final String BUSINESS_KEY = "ContractEndWarning";
    private final AuthenticationFacade authenticationFacade;
    private final ProcessEngine processEngine;

    public ContractListener(AuthenticationFacade authenticationFacade, ProcessEngine processEngine) {
        this.authenticationFacade = authenticationFacade;
        this.processEngine = processEngine;
    }

    @BusinessEventListener
    public void handleContractEnd(ChangeContractCommand event) {
        var runningProcess = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(BUSINESS_KEY)
                .processInstanceBusinessKey("contract_term_" + event.id())
                .singleResult();


        if (runningProcess != null) {
            var timerJob = processEngine.getManagementService()
                    .createJobQuery()
                    .processDefinitionKey(BUSINESS_KEY)
                    .processInstanceId(runningProcess.getProcessInstanceId())
                    .singleResult();

            var newDueDate = event.end().minusMonths(1);
            processEngine.getManagementService()
                    .setJobDuedate(
                            timerJob.getId(),
                            convert(newDueDate));
        }
    }

    @BusinessEventListener
    public void handleShouldWarn(WarnBeforeExpiryCommand event) {
        var newDueDate = event.endDate().minusMonths(1);

        processEngine.getRuntimeService()
                .createProcessInstanceByKey(BUSINESS_KEY)
                .businessKey("contract_term_" + event.id())
                .setVariable("warnAt", convert(newDueDate))
                .setVariable("username", authenticationFacade.authenticated())
                .execute();
    }

    private Date convert(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
