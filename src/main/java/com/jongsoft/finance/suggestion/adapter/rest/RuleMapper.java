package com.jongsoft.finance.suggestion.adapter.rest;

import com.jongsoft.finance.rest.model.RuleResponse;
import com.jongsoft.finance.rest.model.RuleResponseChangesInner;
import com.jongsoft.finance.rest.model.RuleResponseConditionsInner;
import com.jongsoft.finance.suggestion.domain.model.TransactionRule;

public interface RuleMapper {

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
