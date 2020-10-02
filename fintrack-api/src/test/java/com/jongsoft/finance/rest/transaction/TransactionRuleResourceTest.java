package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroupProvider;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

class TransactionRuleResourceTest extends TestSetup {

    private TransactionRuleResource subject;

    @Mock
    private TransactionRuleGroupProvider ruleGroupProvider;
    @Mock
    private TransactionRuleProvider ruleProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subject = new TransactionRuleResource(
                ruleGroupProvider,
                ruleProvider,
                currentUserProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        new EventBus(eventPublisher);
    }

    @Test
    void groups() {
        Mockito.when(ruleGroupProvider.lookup()).thenReturn(Flowable.just(
                TransactionRuleGroup.builder()
                        .id(1L)
                        .name("Grocery stores")
                        .sort(1)
                        .build(),
                TransactionRuleGroup.builder()
                        .id(2L)
                        .name("Savings transactions")
                        .sort(2)
                        .build()));

        subject.groups().test()
                .assertValueCount(2)
                .assertComplete()
                .assertValueAt(0, rule -> "Grocery stores".equals(rule.getName()))
                .assertValueAt(1, rule -> "Savings transactions".equals(rule.getName()));
    }

    @Test
    void createGroup() {
        Mockito.when(ruleGroupProvider.lookup("Group setting")).thenReturn(API.Option());

        subject.createGroup(new GroupRenameRequest("Group setting"));

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(TransactionRuleGroupCreatedEvent.class));
    }

    @Test
    void rules() {
        final TransactionRule transactionRule = TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(API.List())
                .changes(API.List())
                .user(ACTIVE_USER)
                .build();
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.CATEGORY, "3");

        Mockito.when(ruleProvider.lookup("Grocery")).thenReturn(Flowable.just(transactionRule));

        subject.rules("Grocery").test()
                .assertValueCount(1)
                .assertComplete()
                .assertValue(rule -> "Grocery Store 1".equals(rule.getName()));
    }

    @Test
    void groupUp() {
        var ruleGroup = Mockito.spy(TransactionRuleGroup.builder()
                .id(1L)
                .name("Grocery stores")
                .sort(1)
                .build());

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(API.Option(ruleGroup));

        subject.groupUp("Grocery stores");

        Mockito.verify(ruleGroup).changeOrder(0);
    }

    @Test
    void groupDown() {
        var ruleGroup = Mockito.spy(TransactionRuleGroup.builder()
                .id(1L)
                .name("Grocery stores")
                .sort(1)
                .build());

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(API.Option(ruleGroup));

        subject.groupDown("Grocery stores");

        Mockito.verify(ruleGroup).changeOrder(2);
    }

    @Test
    void rename() {
        var ruleGroup = Mockito.spy(TransactionRuleGroup.builder()
                .id(1L)
                .name("Grocery stores")
                .sort(1)
                .build());

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(API.Option(ruleGroup));

        subject.rename("Grocery stores", new GroupRenameRequest("updated"));

        Mockito.verify(ruleGroup).rename("updated");
    }

    @Test
    void create() {
        var request = CreateRuleRequest.builder()
                .name("Grocery Matcher")
                .description("My sample rule")
                .restrictive(true)
                .active(true)
                .conditions(List.of(
                        new CreateRuleRequest.Condition(
                                null,
                                RuleColumn.TO_ACCOUNT,
                                RuleOperation.EQUALS,
                                "sample account")))
                .changes(List.of(
                        new CreateRuleRequest.Change(
                                null,
                                RuleColumn.TO_ACCOUNT,
                                "2")))
                .build();

        subject.create("Group 1", request);

        Mockito.verify(ruleProvider).save(Mockito.any());
    }

    @Test
    void getRule() {
        final TransactionRule transactionRule = TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(API.List())
                .changes(API.List())
                .user(ACTIVE_USER)
                .build();
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.CATEGORY, "3");

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(transactionRule));

        var response = subject.getRule("Grocery", 1L).blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void ruleUp() {
        final TransactionRule transactionRule = Mockito.spy(TransactionRule.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .sort(1)
                .build());

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(transactionRule));

        subject.ruleUp("Group", 1L);
        Mockito.verify(transactionRule).changeOrder(0);
    }

    @Test
    void ruleDown() {
        final TransactionRule transactionRule = Mockito.spy(TransactionRule.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .sort(1)
                .build());

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(transactionRule));

        subject.ruleDown("Group", 1L);
        Mockito.verify(transactionRule).changeOrder(2);
    }

    @Test
    void updateRule() {
        final TransactionRule transactionRule = Mockito.spy(TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(API.List())
                .changes(API.List())
                .user(ACTIVE_USER)
                .build());
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.CATEGORY, "3");

        var request = CreateRuleRequest.builder()
                .name("Grocery Matcher")
                .description("My sample rule")
                .restrictive(true)
                .active(true)
                .conditions(List.of(
                        new CreateRuleRequest.Condition(
                                null,
                                RuleColumn.TO_ACCOUNT,
                                RuleOperation.EQUALS,
                                "sample account")))
                .changes(List.of(
                        new CreateRuleRequest.Change(
                                null,
                                RuleColumn.TO_ACCOUNT,
                                "2")))
                .build();

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(transactionRule));
        Mockito.when(ruleProvider.save(transactionRule)).thenReturn(transactionRule);

        subject.updateRule("Group 1", 1L, request).blockingGet();

        Mockito.verify(transactionRule).change("Grocery Matcher", "My sample rule", true, true);
        Mockito.verify(ruleProvider).save(transactionRule);
    }

    @Test
    void deleteRule() {
        final TransactionRule transactionRule = TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .user(ACTIVE_USER)
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(API.List())
                .changes(API.List())
                .build();

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(API.Option(transactionRule));

        subject.deleteRule("Group", 1L);

        Mockito.verify(ruleProvider).save(transactionRule);
        Assertions.assertThat(transactionRule.isDeleted()).isTrue();
    }

}