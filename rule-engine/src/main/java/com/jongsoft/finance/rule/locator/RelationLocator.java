package com.jongsoft.finance.rule.locator;

import java.util.List;

import javax.inject.Singleton;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;

import io.micronaut.context.ApplicationContext;

@Singleton
public class RelationLocator implements ChangeLocator {

    private final static List<RuleColumn> SUPPORTED_COLUMNS = List.of(
            RuleColumn.CATEGORY,
            RuleColumn.BUDGET,
            RuleColumn.CONTRACT);

    private final ApplicationContext applicationContext;

    public RelationLocator(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object locate(RuleColumn column, String change) {
        Class<?> genericType = switch (column) {
            case CATEGORY -> Category.class;
            case BUDGET -> Budget.Expense.class;
            case CONTRACT -> Contract.class;
            default -> throw new IllegalArgumentException("Unsupported type");
        };

        var dataProvider = applicationContext.getBeansOfType(DataProvider.class)
                .stream()
                .filter(bean -> bean.supports(genericType))
                .findFirst();

        if (dataProvider.isPresent()) {
            var entity = dataProvider.get().lookup(Long.parseLong(change));

            return entity.get().toString();
        }

        throw new IllegalArgumentException("Unsupported type " + genericType.getSimpleName());
    }

    @Override
    public boolean supports(RuleColumn column) {
        return SUPPORTED_COLUMNS.contains(column);
    }

}
