package com.jongsoft.finance.rest.api;

import static java.util.Optional.ofNullable;

import com.jongsoft.finance.core.Removable;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rest.LearningRuleCommandApi;
import com.jongsoft.finance.rest.model.rule.*;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
class LearningRuleCommandController implements LearningRuleCommandApi {

    private final Logger logger;
    private final TransactionRuleGroupProvider ruleGroupProvider;
    private final TransactionRuleProvider ruleProvider;
    private final CurrentUserProvider currentUserProvider;

    LearningRuleCommandController(
            TransactionRuleGroupProvider ruleGroupProvider,
            TransactionRuleProvider ruleProvider,
            CurrentUserProvider currentUserProvider) {
        this.ruleGroupProvider = ruleGroupProvider;
        this.ruleProvider = ruleProvider;
        this.currentUserProvider = currentUserProvider;
        this.logger = LoggerFactory.getLogger(LearningRuleCommandController.class);
    }

    @Override
    public HttpResponse<@Valid RuleResponse> createRule(String group, RuleRequest ruleRequest) {
        logger.info("Creating rule {} in group {}.", ruleRequest.getName(), group);
        var rule = currentUserProvider
                .currentUser()
                .createRule(
                        ruleRequest.getName(),
                        ofNullable(ruleRequest.getRestrictive()).orElse(false));

        rule.assign(group);
        rule.change(
                ruleRequest.getName(),
                ruleRequest.getDescription(),
                ofNullable(ruleRequest.getRestrictive()).orElse(false),
                ofNullable(ruleRequest.getActive()).orElse(false));

        ruleRequest
                .getChanges()
                .forEach(change -> rule.registerChange(change.getColumn(), change.getValue()));

        ruleRequest
                .getConditions()
                .forEach(condition -> rule.registerCondition(
                        condition.getColumn(), condition.getOperation(), condition.getValue()));
        ruleProvider.save(rule);

        var insertedRule = ruleProvider
                .lookup(group)
                .first(r -> r.getName().equalsIgnoreCase(ruleRequest.getName()))
                .getOrThrow(() -> StatusException.internalError("Cannot find newly created rule."));

        return HttpResponse.created(RuleMapper.convertToRuleResponse(insertedRule));
    }

    @Override
    public HttpResponse<Void> createRuleGroup(RuleGroup ruleGroup) {
        logger.info("Creating rule group {}.", ruleGroup.getName());

        ruleGroupProvider
                .lookup(ruleGroup.getName())
                .ifPresent(() -> StatusException.badRequest("Rule group name already exists."));

        CreateRuleGroupCommand.ruleGroupCreated(ruleGroup.getName());

        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> deleteRule(String group, Long ruleId) {
        logger.info("Deleting rule {} in group {}.", ruleId, group);

        var rule = ruleProvider
                .lookup(ruleId)
                .getOrThrow(() -> StatusException.notFound("Rule not found."));
        rule.remove();
        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> deleteRuleGroup(String group) {
        logger.info("Deleting rule group {}.", group);

        var ruleGroup = ruleGroupProvider
                .lookup(group)
                .getOrThrow(() -> StatusException.notFound("Rule group not found."));

        ruleProvider.lookup(group).forEach(TransactionRule::remove);
        ruleGroup.delete();

        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> patchRule(
            String group, Long ruleId, PatchRuleRequest patchRuleRequest) {
        logger.info("Patching rule {} in group {}.", ruleId, group);

        var rule = ruleProvider
                .lookup(ruleId)
                .getOrThrow(() -> StatusException.notFound("Rule not found."));

        if (patchRuleRequest.getMove() == PatchRuleRequestMove.UP) {
            rule.changeOrder(rule.getSort() - 1);
        } else {
            rule.changeOrder(rule.getSort() + 1);
        }
        return HttpResponse.noContent();
    }

    @Override
    public RuleResponse updateRule(String group, Long ruleId, RuleRequest ruleRequest) {
        logger.info("Updating rule {} in group {}.", ruleId, group);

        var rule = ruleProvider
                .lookup(ruleId)
                .getOrThrow(() -> StatusException.notFound("Rule not found."));

        rule.change(
                ruleRequest.getName(),
                ruleRequest.getDescription(),
                ruleRequest.getRestrictive(),
                ruleRequest.getActive());
        rule.getChanges().forEach(Removable::delete);
        rule.getConditions().forEach(Removable::delete);

        ruleRequest
                .getChanges()
                .forEach(change -> rule.registerChange(change.getColumn(), change.getValue()));

        ruleRequest
                .getConditions()
                .forEach(condition -> rule.registerCondition(
                        condition.getColumn(), condition.getOperation(), condition.getValue()));

        ruleProvider.save(rule);

        return RuleMapper.convertToRuleResponse(rule);
    }

    @Override
    public HttpResponse<Void> updateRuleGroup(
            String group, PatchRuleGroupRequest patchRuleGroupRequest) {
        logger.info("Updating rule group {}.", group);
        var ruleGroup = ruleGroupProvider
                .lookup(group)
                .getOrThrow(() -> StatusException.notFound("Rule group not found."));

        if (patchRuleGroupRequest.getName() != null) {
            ruleGroup.rename(patchRuleGroupRequest.getName());
        }

        if (patchRuleGroupRequest.getMove() != null) {
            if (patchRuleGroupRequest.getMove() == PatchRuleRequestMove.UP) {
                ruleGroup.changeOrder(ruleGroup.getSort() - 1);
            } else {
                ruleGroup.changeOrder(ruleGroup.getSort() + 1);
            }
        }

        return HttpResponse.noContent();
    }
}
