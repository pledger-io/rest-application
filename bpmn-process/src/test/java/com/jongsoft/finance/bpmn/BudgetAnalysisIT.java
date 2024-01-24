package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import jakarta.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetAnalysisIT extends ProcessTestSetup {

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private ProcessEngine processEngine;

    @Inject
    private FilterFactory filterFactory;

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private SettingProvider applicationSettings;

    private TransactionProvider.FilterCommand filterCommand;

    @BeforeEach
    void setup() {
        Mockito.reset(budgetProvider, filterFactory, transactionProvider, applicationSettings);

        Mockito.when(applicationSettings.getBudgetAnalysisMonths()).thenReturn(3);

        Mockito.when(filterFactory.transaction())
                .thenReturn(filterCommand = Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));

        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(Control.Option(
                Budget.builder()
                        .expenses(Collections.List(
                                Budget.Expense.builder()
                                        .id(1L)
                                        .lowerBound(75)
                                        .upperBound(100)
                                        .name("Groceries")
                                        .build()))
                        .id(1L)
                        .build()
        ));
    }

    @Test
    @DisplayName("Budget analysis without a recorded deviation")
    void budgetWithoutDeviation() {
        Mockito.when(transactionProvider.lookup(filterCommand))
                .thenReturn(ResultPage.of(
                        buildTransaction(50.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(20.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(12.20, "Groceries", "My Account", "To Account"),
                        buildTransaction(40.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(50, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account"),
                        buildTransaction(10, "Groceries", "My Account", "To Account")
                ));
        Mockito.when(applicationSettings.getMaximumBudgetDeviation()).thenReturn(0.50);

        var response = processEngine.getRuntimeService()
                .createProcessInstanceByKey("budget_analysis")
                .setVariable("id", 1L)
                .setVariable("scheduled", "2019-01-01")
                .execute();

        waitForSuspended(processEngine, response.getProcessInstanceId());

        // Validate the process completed successfully
        var variables = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("deviates")
                .list();

        assertThat(variables)
                .hasSize(2)
                .anySatisfy(variable -> assertThat(variable.getValue()).isEqualTo(false));

        // Validate no tasks have been created
        var tasks = processEngine.getTaskService()
                .createTaskQuery()
                .processInstanceId(response.getProcessInstanceId())
                .active()
                .list();

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Budget analysis with a recorded deviation")
    void budgetWithDeviation() {
        Mockito.when(transactionProvider.lookup(filterCommand))
                .thenReturn(ResultPage.of(
                        buildTransaction(50.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(20.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(30.2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(12.20, "Groceries", "My Account", "To Account"),
                        buildTransaction(40.2, "Groceries", "My Account", "To Account"),
                        buildTransaction(123.2, "Groceries", "My Account", "To Account")
                ))
                .thenReturn(ResultPage.of(
                        buildTransaction(4, "Groceries", "My Account", "To Account"),
                        buildTransaction(12, "Groceries", "My Account", "To Account"),
                        buildTransaction(13, "Groceries", "My Account", "To Account")
                ));
        Mockito.when(applicationSettings.getMaximumBudgetDeviation()).thenReturn(0.05);

        var response = processEngine.getRuntimeService()
                .createProcessInstanceByKey("budget_analysis")
                .setVariable("id", 1L)
                .setVariable("scheduled", "2019-01-01")
                .execute();

        waitForSuspended(processEngine, response.getProcessInstanceId());

        // Validate the process completed successfully
        var variables = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("deviation")
                .list();

        assertThat(variables)
                .hasSize(2)
                .anySatisfy(variable -> assertThat(variable.getValue()).isEqualTo(-14.23));

        // Validate the tasks have been created according to the number of months

        var tasks = processEngine.getTaskService()
                .createTaskQuery()
                .processInstanceId(response.getProcessInstanceId())
                .active()
                .taskDefinitionKey("handle_deviation")
                .list();

        assertThat(tasks)
                .hasSize(1)
                .anySatisfy(task -> {
                    assertThat(task.getName()).isEqualTo("Handle deviation");
                    assertThat(processEngine.getRuntimeService()
                            .createVariableInstanceQuery()
                            .processInstanceIdIn(response.getProcessInstanceId())
                            .variableName("needed_correction")
                            .list())
                            .hasSize(1)
                            .anySatisfy(variable -> assertThat(variable.getValue()).isEqualTo(-14.23));
                });
    }
}
