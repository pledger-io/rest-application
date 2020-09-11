package com.jongsoft.finance.domain.transaction;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.events.RuleChangeUpdatedEvent;
import com.jongsoft.finance.domain.transaction.events.RuleConditionUpdatedEvent;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleSortedEvent;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

class TransactionRuleTest {

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void change() {
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.change("Updated rule", "Updated description" , false, false);

        assertThat(rule.getName()).isEqualTo("Updated rule");
        assertThat(rule.isActive()).isFalse();
        assertThat(rule.isRestrictive()).isFalse();
    }

    @Test
    void changeOrder() {
        ArgumentCaptor<TransactionRuleSortedEvent> captor = ArgumentCaptor.forClass(TransactionRuleSortedEvent.class);
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .sort(1)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.changeOrder(4);

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(rule.getSort()).isEqualTo(4);
        assertThat(captor.getValue().getSort()).isEqualTo(4);
        assertThat(captor.getValue().getRuleId()).isEqualTo(1L);
    }

    @Test
    void assign() {
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.assign("Sample Group");

        assertThat(rule.getGroup()).isEqualTo("Sample Group");
    }

    @Test
    void registerCondition() {
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.registerCondition(RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "22.5");
        rule.registerCondition(RuleColumn.AMOUNT, RuleOperation.MORE_THAN, "12.5");

        assertThat(rule.getName()).isEqualTo("Sample Rule");
        assertThat(rule.isRestrictive()).isTrue();
        assertThat(rule.getConditions()).hasSize(2);

        assertThat(rule.getConditions().get(0).getCondition()).isEqualTo("22.5");
        assertThat(rule.getConditions().get(0).getField()).isEqualTo(RuleColumn.AMOUNT);
        assertThat(rule.getConditions().get(0).getOperation()).isEqualTo(RuleOperation.LESS_THAN);
    }

    @Test
    void registerChange() {
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.registerChange(RuleColumn.CATEGORY, "sample-cat");

        assertThat(rule.getChanges()).hasSize(1);
        assertThat(rule.getChanges().get(0).getField()).isEqualTo(RuleColumn.CATEGORY);
        assertThat(rule.getChanges().get(0).getChange()).isEqualTo("sample-cat");
    }

    @Test
    void registerChange_duplicateColumn() {
        final TransactionRule rule = TransactionRule.builder()
                .id(1L)
                .name("Sample Rule")
                .restrictive(true)
                .build();

        rule.registerChange(RuleColumn.CATEGORY, "sample-cat");
        rule.registerChange(RuleColumn.CATEGORY, "updated-cat");

        assertThat(rule.getChanges()).hasSize(1);
        assertThat(rule.getChanges().get(0).getField()).isEqualTo(RuleColumn.CATEGORY);
        assertThat(rule.getChanges().get(0).getChange()).isEqualTo("updated-cat");
    }

    @Test
    void ruleConditionUpdate() {
        ArgumentCaptor<RuleConditionUpdatedEvent> captor = ArgumentCaptor.forClass(RuleConditionUpdatedEvent.class);

        final TransactionRule rule = TransactionRule.builder().build();

        rule.new Condition(1L, RuleColumn.CATEGORY, RuleOperation.CONTAINS, "sal")
                .update(RuleColumn.CATEGORY, RuleOperation.EQUALS, "salary");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().getRuleConditionId()).isEqualTo(1L);
        assertThat(captor.getValue().getCondition()).isEqualTo("salary");
        assertThat(captor.getValue().getField()).isEqualTo(RuleColumn.CATEGORY);
        assertThat(captor.getValue().getOperation()).isEqualTo(RuleOperation.EQUALS);
    }

    @Test
    void ruleChangeUpdate() {
        ArgumentCaptor<RuleChangeUpdatedEvent> captor = ArgumentCaptor.forClass(RuleChangeUpdatedEvent.class);

        final TransactionRule rule = TransactionRule.builder().build();
        rule.new Change(1L, RuleColumn.AMOUNT, "20.3")
                .update(RuleColumn.TO_ACCOUNT, "3");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().getRuleChangeId()).isEqualTo(1L);
        assertThat(captor.getValue().getColumn()).isEqualTo(RuleColumn.TO_ACCOUNT);
        assertThat(captor.getValue().getChange()).isEqualTo("3");
    }

}
