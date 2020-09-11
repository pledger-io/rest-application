package rules;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.rule.impl.RuleEngineImpl;
import com.jongsoft.finance.rule.locator.AccountLocator;
import com.jongsoft.finance.rule.locator.NoopLocator;
import com.jongsoft.finance.rule.locator.RelationLocator;
import com.jongsoft.lang.API;

import io.micronaut.context.ApplicationContext;

class RuleEngineImplTest {

    private RuleEngine engine;

    private AccountProvider accountProvider;
    private CategoryProvider categoryProvider;
    private TransactionRuleProvider transactionRuleProvider;

    @BeforeEach
    void setup() {
        categoryProvider = Mockito.mock(CategoryProvider.class);
        Mockito.when(categoryProvider.supports(Category.class)).thenReturn(true);

        ApplicationContext context = ApplicationContext.run()
                .registerSingleton(categoryProvider);

        accountProvider = Mockito.mock(AccountProvider.class);
        transactionRuleProvider = Mockito.mock(TransactionRuleProvider.class);
        engine = new RuleEngineImpl(transactionRuleProvider, List.of(
                new AccountLocator(accountProvider),
                new NoopLocator(),
                new RelationLocator(context)
        ));
    }

    @Test
    void run_all() {
        var changeAccount = Account.builder().name("Sample Account").build();
        var transactionRule1 = TransactionRule.builder()
                .restrictive(true)
                .build();
        var transactionRule2 = TransactionRule.builder()
                .restrictive(false)
                .build();

        transactionRule1.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.STARTS_WITH, "This");
        transactionRule1.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.MORE_THAN, "15.22");
        transactionRule1.new Change(1L, RuleColumn.TO_ACCOUNT, "2");

        transactionRule2.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "trans");
        transactionRule2.new Change(3L, RuleColumn.CATEGORY, "4");

        Mockito.when(transactionRuleProvider.lookup()).thenReturn(API.List(transactionRule1, transactionRule2));
        Mockito.when(categoryProvider.lookup(4L))
                .thenReturn(API.Option(Category.builder().label("Category 1").build()));
        Mockito.when(accountProvider.lookup(2L))
                .thenReturn(API.Option(changeAccount));

        RuleDataSet inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.AMOUNT, 20.22);
        inputSet.put(RuleColumn.DESCRIPTION, "This is a Sample transaction");

        var output = engine.run(inputSet);

        Mockito.verify(transactionRuleProvider).lookup();

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output.get(RuleColumn.TO_ACCOUNT)).isEqualTo(changeAccount);
        Assertions.assertThat(output.get(RuleColumn.CATEGORY)).isEqualTo("Category 1");
    }

    @Test
    void run_restrictive() {
        var changeAccount = Account.builder().name("Sample Account").build();
        var transactionRule = TransactionRule.builder()
                .restrictive(true)
                .build();

        transactionRule.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "Sample");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.MORE_THAN, "15.22");

        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.TAGS, "Hello");
        transactionRule.new Change(3L, RuleColumn.CATEGORY, "4");

        RuleDataSet inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.AMOUNT, 20.22);
        inputSet.put(RuleColumn.DESCRIPTION, "This is a Sample");

        Mockito.when(categoryProvider.lookup(4L))
                .thenReturn(API.Option(Category.builder().label("Category 1").build()));
        Mockito.when(accountProvider.lookup(2L))
                .thenReturn(API.Option(changeAccount));

        var output = engine.run(inputSet, transactionRule);

        Assertions.assertThat(output).hasSize(3);
        Assertions.assertThat(output.get(RuleColumn.TO_ACCOUNT)).isEqualTo(changeAccount);
        Assertions.assertThat(output.get(RuleColumn.TAGS)).isEqualTo("Hello");
        Assertions.assertThat(output.get(RuleColumn.CATEGORY)).isEqualTo("Category 1");
    }

    @Test
    void run_restrictiveNoMatch() {
        var transactionRule = TransactionRule.builder()
                .restrictive(true)
                .build();

        transactionRule.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "Sample");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.MORE_THAN, "60");

        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.TAGS, "Hello");
        transactionRule.new Change(3L, RuleColumn.CATEGORY, "4");

        RuleDataSet inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.AMOUNT, 20.22);
        inputSet.put(RuleColumn.DESCRIPTION, "This is a Sample");

        var output = engine.run(inputSet, transactionRule);

        Assertions.assertThat(output).hasSize(0);
    }

    @Test
    void run_nonRestrictive() {
        var changeAccount = Account.builder().name("Sample Account").build();
        var transactionRule = TransactionRule.builder()
                .restrictive(false)
                .build();

        transactionRule.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "Sample");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "15.22");

        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.TAGS, "Hello");
        transactionRule.new Change(3L, RuleColumn.CATEGORY, "4");

        RuleDataSet inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.AMOUNT, 20.22);
        inputSet.put(RuleColumn.DESCRIPTION, "This is a Sample");

        Mockito.when(categoryProvider.lookup(4L))
                .thenReturn(API.Option(Category.builder().label("Category 1").build()));
        Mockito.when(accountProvider.lookup(2L))
                .thenReturn(API.Option(changeAccount));

        var output = engine.run(inputSet, transactionRule);

        Assertions.assertThat(output).hasSize(3);
        Assertions.assertThat(output.get(RuleColumn.TO_ACCOUNT)).isEqualTo(changeAccount);
        Assertions.assertThat(output.get(RuleColumn.TAGS)).isEqualTo("Hello");
        Assertions.assertThat(output.get(RuleColumn.CATEGORY)).isEqualTo("Category 1");
    }

    @Test
    void run_nonRestrictiveNoMatch() {
        var transactionRule = TransactionRule.builder()
                .restrictive(false)
                .build();

        transactionRule.new Condition(1L, RuleColumn.DESCRIPTION, RuleOperation.CONTAINS, "NoMAtch");
        transactionRule.new Condition(2L, RuleColumn.AMOUNT, RuleOperation.LESS_THAN, "15.22");

        transactionRule.new Change(1L, RuleColumn.TO_ACCOUNT, "2");
        transactionRule.new Change(2L, RuleColumn.TAGS, "Hello");
        transactionRule.new Change(3L, RuleColumn.CATEGORY, "4");

        RuleDataSet inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.AMOUNT, 20.22);
        inputSet.put(RuleColumn.DESCRIPTION, "This is a Sample");

        var output = engine.run(inputSet, transactionRule);

        Assertions.assertThat(output).hasSize(0);
    }

}
