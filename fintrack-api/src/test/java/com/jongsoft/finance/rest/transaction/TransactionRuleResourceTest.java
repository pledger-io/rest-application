package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Mockito.when(ruleGroupProvider.lookup()).thenReturn(Collections.List(
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

        Assertions.assertThat(subject.groups())
                .isNotNull()
                .hasSize(2)
                .extracting("name")
                .containsExactly("Grocery stores", "Savings transactions");
    }

    @Test
    void createGroup() {
        Mockito.when(ruleGroupProvider.lookup("Group setting")).thenReturn(Control.Option());

        subject.createGroup(new GroupRenameRequest("Group setting"));

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateRuleGroupCommand.class));
    }

    @Test
    void rules() {
        final TransactionRule transactionRule = TransactionRule.builder()
                .id(1L)
                .name("Grocery Store 1")
                .active(true)
                .restrictive(false)
                .group("Grocery")
                .conditions(Collections.List())
                .changes(Collections.List())
                .user(ACTIVE_USER)
                .build();
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.CATEGORY, "3");

        Mockito.when(ruleProvider.lookup("Grocery")).thenReturn(Collections.List(transactionRule));

        Assertions.assertThat(subject.rules("Grocery"))
                .isNotNull()
                .hasSize(1)
                .extracting("name")
                .containsExactly("Grocery Store 1");
    }

    @Test
    void groupUp() {
        var ruleGroup = Mockito.spy(TransactionRuleGroup.builder()
                .id(1L)
                .name("Grocery stores")
                .sort(1)
                .build());

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(Control.Option(ruleGroup));

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

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(Control.Option(ruleGroup));

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

        Mockito.when(ruleGroupProvider.lookup("Grocery stores")).thenReturn(Control.Option(ruleGroup));

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
                .conditions(Collections.List())
                .changes(Collections.List())
                .user(ACTIVE_USER)
                .build();
        transactionRule.new Condition(1L, RuleColumn.TO_ACCOUNT, RuleOperation.CONTAINS, "Store");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "100.00");
        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.CATEGORY, "3");

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(Control.Option(transactionRule));

        Assertions.assertThat(subject.getRule("Grocery", 1L))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Grocery Store 1")
                .hasFieldOrPropertyWithValue("active", true)
                .hasFieldOrPropertyWithValue("restrictive", false)
                .satisfies(rule -> assertThat(rule.getConditions())
                        .isNotNull()
                        .hasSize(2)
                        .extracting("id")
                        .containsExactly(1L, 2L));
    }

    @Test
    void ruleUp() {
        final TransactionRule transactionRule = Mockito.spy(TransactionRule.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .sort(1)
                .build());

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(Control.Option(transactionRule));

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

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(Control.Option(transactionRule));

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
                .conditions(Collections.List())
                .changes(Collections.List())
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

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(Control.Option(transactionRule));

        Assertions.assertThat(subject.updateRule("Group 1", 1L, request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Grocery Matcher")
                .hasFieldOrPropertyWithValue("active", true)
                .hasFieldOrPropertyWithValue("restrictive", true);

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
                .conditions(Collections.List())
                .changes(Collections.List())
                .build();

        Mockito.when(ruleProvider.lookup(1L)).thenReturn(Control.Option(transactionRule));

        subject.deleteRule("Group", 1L);

        Mockito.verify(ruleProvider).save(transactionRule);
        assertThat(transactionRule.isDeleted()).isTrue();
    }

}
