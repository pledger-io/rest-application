package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.rest.model.rule.RuleResponse;
import com.jongsoft.finance.rest.model.rule.RuleResponseChangesInner;
import com.jongsoft.finance.rest.model.rule.RuleResponseConditionsInner;

interface RuleMapper {

    static RuleResponse convertToRuleResponse(TransactionRule rule) {
        RuleResponse response = new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.isActive(),
                rule.isRestrictive(),
                rule.getSort());

        response.description(rule.getDescription());
        response.setChanges(
                rule.getChanges().map(RuleMapper::convertToChange).stream().toList());

        response.setConditions(rule.getConditions().map(RuleMapper::convertToCondition).stream()
                .toList());

        return response;
    }

    static RuleResponseChangesInner convertToChange(TransactionRule.Change change) {
        return new RuleResponseChangesInner(change.getId(), change.getField(), change.getChange());
    }

    static RuleResponseConditionsInner convertToCondition(TransactionRule.Condition condition) {
        return new RuleResponseConditionsInner(
                condition.getId(), condition.getField(),
                condition.getOperation(), condition.getCondition());
    }
}
