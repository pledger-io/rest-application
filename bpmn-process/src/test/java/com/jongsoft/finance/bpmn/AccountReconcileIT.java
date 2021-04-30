package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Control;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

class AccountReconcileIT extends ProcessTestSetup {

    @Inject
    private ProcessEngine processEngine;

    @Inject
    private TransactionProvider transactionProvider;
    @Inject
    private AccountProvider accountProvider;

    @Inject
    private FilterFactory filterFactory;

    private TransactionProvider.FilterCommand filterCommand;

    @BeforeEach
    void setup() {
        filterCommand = Mockito.mock(TransactionProvider.FilterCommand.class);
        Mockito.reset(transactionProvider, accountProvider, filterFactory);

        Mockito.when(filterFactory.transaction()).thenReturn(filterCommand);
    }

    @Test
    void run_differentStartBalance() throws InterruptedException {
        Mockito.when(transactionProvider.balance(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(BigDecimal.valueOf( -20.0)));

        var response = processEngine.getRuntimeService().startProcessInstanceByKey(
                "AccountReconcile",
                Variables.createVariables()
                        .putValue("startDate", "2019-01-01")
                        .putValue("endDate", "2019-12-31")
                        .putValue("openBalance", 10.0)
                        .putValue("endBalance", 100.2)
                        .putValue("accountId", 1L)
        );

        waitForSuspended(processEngine, response.getProcessInstanceId());

        var computedStartBalance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("computedStartBalance")
                .singleResult();
        var computedEndBalance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("computedEndBalance")
                .singleResult();
        var hasDifference = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("balanceDifference")
                .singleResult();
        var task = processEngine.getTaskService()
                .createTaskQuery()
                .processInstanceId(response.getProcessInstanceId())
                .taskId("task_reconcile_before");

        Assertions.assertThat(computedStartBalance.getValue()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(computedEndBalance).isNull();
        Assertions.assertThat(hasDifference).isNull();
        Assertions.assertThat(task).isNotNull();
    }

    @Test
    void run_endBalanceOff() {
        Account reconcileAccount = Mockito.spy(Account.builder().id(1L).type("checking").name("Example Account").build());
        Account reconcile = Account.builder().id(3L).type("reconcile").build();

        Mockito.when(accountProvider.lookup(1L))
                .thenReturn(Control.Option(reconcileAccount));
        Mockito.when(accountProvider.lookup(SystemAccountTypes.RECONCILE))
                .thenReturn(Maybe.just(reconcile));
        Mockito.when(transactionProvider.balance(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(BigDecimal.valueOf(-20.0)));

        var response = processEngine.getRuntimeService().startProcessInstanceByKey(
                "AccountReconcile",
                Variables.createVariables()
                        .putValue("startDate", "2019-01-01")
                        .putValue("endDate", "2019-12-31")
                        .putValue("openBalance", 0)
                        .putValue("endBalance", 100.2)
                        .putValue("accountId", 1L));

        waitForSuspended(processEngine, response.getProcessInstanceId());

        var computedStartBalance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("computedStartBalance")
                .singleResult();
        var computedEndBalance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("computedEndBalance")
                .singleResult();
        var balanceDifference = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("balanceDifference")
                .singleResult();

        Assertions.assertThat(computedStartBalance.getValue()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(computedEndBalance.getValue()).isEqualTo(BigDecimal.valueOf(-20.0));
        Assertions.assertThat(((BigDecimal) balanceDifference.getValue()).setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(BigDecimal.valueOf(-120.2));

        Mockito.verify(reconcileAccount).createTransaction(Mockito.eq(reconcile), Mockito.eq(120.2D),
                Mockito.eq(Transaction.Type.DEBIT), Mockito.any());
    }
}
