package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleProvider;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;
import com.jongsoft.finance.suggestion.domain.model.TransactionRule;
import com.jongsoft.finance.suggestion.domain.service.rule.matcher.NumberMatcher;
import com.jongsoft.finance.suggestion.domain.service.rule.matcher.StringMatcher;
import com.jongsoft.finance.suggestion.types.RuleColumn;

import io.micrometer.core.annotation.Timed;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class RuleSuggestionEngine implements SuggestionEngine {

    private final TransactionRuleProvider transactionRuleProvider;
    private final List<ChangeLocator> locators;

    RuleSuggestionEngine(
            TransactionRuleProvider transactionRuleProvider, List<ChangeLocator> locators) {
        this.transactionRuleProvider = transactionRuleProvider;
        this.locators = locators;
    }

    @Override
    @Timed(
            value = "learning.rule-based",
            extraTags = {"action", "classify-transaction"})
    public SuggestionResult makeSuggestions(SuggestionInput transactionInput) {
        var ruleDataset = new RuleDataSet();
        if (transactionInput.description() != null) {
            ruleDataset.put(RuleColumn.DESCRIPTION, transactionInput.description());
        }
        if (transactionInput.fromAccount() != null) {
            ruleDataset.put(RuleColumn.SOURCE_ACCOUNT, transactionInput.fromAccount());
        }
        if (transactionInput.toAccount() != null) {
            ruleDataset.put(RuleColumn.TO_ACCOUNT, transactionInput.toAccount());
        }
        ruleDataset.put(RuleColumn.AMOUNT, transactionInput.amount());

        RuleDataSet ruleOutput = run(ruleDataset);

        return new SuggestionResult(
                ruleOutput.getCasted(RuleColumn.BUDGET),
                ruleOutput.getCasted(RuleColumn.CATEGORY),
                List.of());
    }

    RuleDataSet run(RuleDataSet input) {
        var outputSet = new RuleDataSet();

        for (TransactionRule rule : transactionRuleProvider.lookup()) {
            var workingSet = new RuleDataSet();
            workingSet.putAll(input);
            workingSet.putAll(outputSet);

            outputSet.putAll(run(workingSet, rule));
        }

        return outputSet;
    }

    RuleDataSet run(RuleDataSet input, TransactionRule rule) {
        var matchers = rule.getConditions().map(condition -> locateMatcher(condition.getField())
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
                var located = findLocator(change.getField())
                        .locate(change.getField(), change.getChange());
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
                .orElseThrow();
    }
}
