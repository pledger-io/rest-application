package com.jongsoft.finance.rule.impl;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.rule.locator.ChangeLocator;
import com.jongsoft.finance.rule.matcher.ConditionMatcher;
import com.jongsoft.finance.rule.matcher.NumberMatcher;
import com.jongsoft.finance.rule.matcher.StringMatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
public class RuleEngineImpl implements RuleEngine {

    private final TransactionRuleProvider transactionRuleProvider;
    private final List<ChangeLocator> locators;

    @Inject
    public RuleEngineImpl(TransactionRuleProvider transactionRuleProvider, List<ChangeLocator> locators) {
        this.transactionRuleProvider = transactionRuleProvider;
        this.locators = locators;
    }

    @Override
    public RuleDataSet run(RuleDataSet input) {
        var outputSet = new RuleDataSet();

        for (TransactionRule rule : transactionRuleProvider.lookup()) {
            var workingSet = new RuleDataSet();
            workingSet.putAll(input);
            workingSet.putAll(outputSet);

            outputSet.putAll(run(workingSet, rule));
        }

        return outputSet;
    }

    public RuleDataSet run(RuleDataSet input, TransactionRule rule) {
        var matchers = rule.getConditions()
                .map(condition -> locateMatcher(condition.getField())
                    .prepare(
                            condition.getOperation(),
                            condition.getCondition(),
                            input.get(condition.getField())));

        boolean matches;
        if (rule.isRestrictive()) {
            matches = matchers.all(ConditionMatcher::matches);
        } else {
            matches = matchers.exists(ConditionMatcher::matches);
        }

        RuleDataSet output = new RuleDataSet();
        if (matches) {
            for (TransactionRule.Change change : rule.getChanges()) {
                var located = findLocator(change.getField()).locate(change.getField(), change.getChange());
                output.put(change.getField(), located);
            }
        }

        return output;
    }

    ConditionMatcher locateMatcher(RuleColumn column) {
        return switch (column) {
            case AMOUNT -> new NumberMatcher();
            default -> new StringMatcher();
        };
    }

    ChangeLocator findLocator(RuleColumn column) {
        return locators.stream()
                .filter(locator -> locator.supports(column))
                .findFirst()
                .get();
    }

}
