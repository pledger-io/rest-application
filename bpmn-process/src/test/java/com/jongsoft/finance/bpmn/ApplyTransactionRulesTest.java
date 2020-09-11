package com.jongsoft.finance.bpmn;

import static org.assertj.core.api.Assertions.*;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;

class ApplyTransactionRulesTest extends ProcessTestSetup {

    @Inject
    private ProcessEngine runtimeService;
    @Inject
    private CurrentUserProvider currentUserFacade;

    @Inject
    private TransactionRuleProvider ruleProvider;

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private RuleEngine ruleEngine;

    @BeforeEach
    void setup() {
        Mockito.reset(ruleProvider, transactionProvider);

        Mockito.when(currentUserFacade.currentUser()).thenReturn(
                UserAccount.builder()
                        .id(1L)
                        .username("test-user")
                        .roles(API.List(new Role("admin")))
                        .build());

        final TransactionRule sample_rule = TransactionRule.builder()
                .id(1L)
                .name("Sample rule")
                .restrictive(true)
                .active(true)
                .build();
        sample_rule.registerCondition(RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "salary");
        sample_rule.registerCondition(RuleColumn.AMOUNT, RuleOperation.MORE_THAN, "2000");
        sample_rule.registerChange(RuleColumn.CATEGORY, "1");
        Mockito.when(ruleProvider.lookup()).thenReturn(API.List(sample_rule));
        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(sample_rule));
    }

    @Test
    void run_matching() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .currency("EUR")
                .transactions(API.List(
                        Transaction.Part.builder()
                                .amount(2100)
                                .account(Account.builder().id(1L).type("checking").build())
                                .build(),
                        Transaction.Part.builder()
                                .amount(-2100)
                                .account(Account.builder().id(2L).type("debtor").build())
                                .build()
                ))
                .description("Income Salary May 2018")
                .build();

        final RuleDataSet mockSet = new RuleDataSet();
        mockSet.put(RuleColumn.CATEGORY, "Sample category");

        Mockito.when(transactionProvider.lookup(1L)).thenReturn(API.Option(transaction));
        Mockito.when(ruleEngine.run(Mockito.any())).thenReturn(mockSet);

        runtimeService.getRuntimeService().startProcessInstanceByKey("analyzeRule", Variables.createVariables()
                .putValue("update", false)
                .putValue("transactionId", transaction.getId()));

        assertThat(transaction.getCategory()).isEqualTo("Sample category");
    }

}
