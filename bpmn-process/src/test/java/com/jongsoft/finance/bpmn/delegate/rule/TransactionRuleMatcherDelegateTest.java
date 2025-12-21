package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;

class TransactionRuleMatcherDelegateTest {
    private static final Account TO_ACCOUNT = Account.builder().id(1L).type("checking").name("To Account").build();
    private static final Account FROM_ACCOUNT = Account.builder().id(2L).type("debtor").name("My Account").build();
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2019, 01, 01);

    private TransactionRuleMatcherDelegate subject;
    private DelegateExecution delegateExecution;
    private Transaction transaction;
    private TransactionProvider transactionProvider;
    private TransactionRuleProvider transactionRuleProvider;
    private RuleEngine ruleEngine;

    @BeforeEach
    void setup() {
        transactionProvider = mock(TransactionProvider.class);
        transactionRuleProvider = mock(TransactionRuleProvider.class);
        ruleEngine = mock(RuleEngine.class);
        subject = new TransactionRuleMatcherDelegate(ruleEngine, transactionProvider, List.of(), mock(FilterFactory.class));

        delegateExecution = mock(DelegateExecution.class);
        transaction = Transaction.builder()
                .id(1L)
                .date(TRANSACTION_DATE)
                .currency("EUR")
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .amount(2100)
                                .account(TO_ACCOUNT)
                                .build(),
                        Transaction.Part.builder()
                                .amount(-2100)
                                .account(FROM_ACCOUNT)
                                .build()
                ))
                .description("Income Salary May 2018")
                .build();

        Mockito.when(delegateExecution.getVariableLocalTyped("transactionId"))
                .thenReturn(new PrimitiveTypeValueImpl.LongValueImpl(1L));
        Mockito.when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));
    }

    @Test
    void execute() throws Exception {
        var rule = buildRule(RuleColumn.SOURCE_ACCOUNT, RuleOperation.EQUALS, FROM_ACCOUNT.getName());

        Mockito.when(ruleEngine.run(Mockito.any()))
                .thenReturn(new RuleDataSet());
        Mockito.when(transactionRuleProvider.lookup(2L))
                .thenReturn(Control.Option(rule));

        subject.execute(delegateExecution);

        var captor = ArgumentCaptor.forClass(RuleDataSet.class);
        Mockito.verify(ruleEngine).run(captor.capture());

        Assertions.assertThat(captor.getValue()).hasSize(4);
        Assertions.assertThat(captor.getValue().containsKey(RuleColumn.TO_ACCOUNT)).isTrue();
        Assertions.assertThat(captor.getValue().containsKey(RuleColumn.SOURCE_ACCOUNT)).isTrue();
        Assertions.assertThat(captor.getValue().containsKey(RuleColumn.AMOUNT)).isTrue();
        Assertions.assertThat(captor.getValue().containsKey(RuleColumn.DESCRIPTION)).isTrue();

        Assertions.assertThat(captor.getValue().get(RuleColumn.TO_ACCOUNT)).isEqualTo(TO_ACCOUNT.getName());
        Assertions.assertThat(captor.getValue().get(RuleColumn.SOURCE_ACCOUNT)).isEqualTo(FROM_ACCOUNT.getName());
        Assertions.assertThat(captor.getValue().get(RuleColumn.AMOUNT)).isEqualTo(2100D);
        Assertions.assertThat(captor.getValue().get(RuleColumn.DESCRIPTION)).isEqualTo(transaction.getDescription());
    }

    private TransactionRule buildRule(RuleColumn column, RuleOperation operation, String condition) {
        final TransactionRule sample_rule = TransactionRule.builder().build();
        sample_rule.registerCondition(column, operation, condition);
        return sample_rule;
    }
}
