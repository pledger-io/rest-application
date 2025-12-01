package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rest.LearningRuleFetcherApi;
import com.jongsoft.finance.rest.model.rule.RuleGroup;
import com.jongsoft.finance.rest.model.rule.RuleResponse;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;

import java.util.List;

@Controller
class LearningRuleFetcherController implements LearningRuleFetcherApi {

    private final Logger logger;
    private final TransactionRuleProvider ruleProvider;
    private final TransactionRuleGroupProvider ruleGroupProvider;

    LearningRuleFetcherController(
            TransactionRuleProvider ruleProvider, TransactionRuleGroupProvider ruleGroupProvider) {
        this.ruleProvider = ruleProvider;
        this.ruleGroupProvider = ruleGroupProvider;
        this.logger = org.slf4j.LoggerFactory.getLogger(LearningRuleFetcherController.class);
    }

    @Override
    public RuleResponse fetchRule(String group, Long ruleId) {
        logger.info("Fetching rule {} in group {}.", ruleId, group);

        var rule = ruleProvider
                .lookup(ruleId)
                .getOrThrow(() -> StatusException.notFound("Rule not found in system"));

        return RuleMapper.convertToRuleResponse(rule);
    }

    @Override
    public List<RuleGroup> fetchRuleGroups() {
        logger.info("Fetching all rule groups.");

        return ruleGroupProvider
                .lookup()
                .map(ruleGroup -> new RuleGroup(ruleGroup.getSort(), ruleGroup.getName()))
                .toJava();
    }

    @Override
    public List<@Valid RuleResponse> fetchRulesByGroup(String group) {
        logger.info("Fetching all rules in group {}.", group);
        return ruleProvider.lookup(group).map(RuleMapper::convertToRuleResponse).toJava();
    }
}
