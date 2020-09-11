package com.jongsoft.finance.bpmn.handler;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.events.ContractChangedEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;
import com.jongsoft.finance.security.AuthenticationFacade;

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
    public void handleContractEnd(ContractChangedEvent event) {
        var runningProcess = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(BUSINESS_KEY)
                .processInstanceBusinessKey("contract_term_" + event.getId())
                .singleResult();


        if (runningProcess != null) {
            var timerJob = processEngine.getManagementService()
                    .createJobQuery()
                    .processDefinitionKey(BUSINESS_KEY)
                    .processInstanceId(runningProcess.getProcessInstanceId())
                    .singleResult();

            var newDueDate = event.getEnd().minusMonths(1);
            processEngine.getManagementService()
                    .setJobDuedate(
                            timerJob.getId(),
                            convert(newDueDate));
        }
    }

    @BusinessEventListener
    public void handleShouldWarn(ContractWarningEvent event) {
        var newDueDate = event.getEndDate().minusMonths(1);

        processEngine.getRuntimeService()
                .createProcessInstanceByKey(BUSINESS_KEY)
                .businessKey("contract_term_" + event.getContractId())
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
