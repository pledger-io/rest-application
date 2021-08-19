package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.Removable;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.model.TransactionRuleGroupResponse;
import com.jongsoft.finance.rest.model.TransactionRuleResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.function.Consumer;

@Tag(name = "Transaction Rules")
@Controller("/api/transaction-rules")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TransactionRuleResource {

    private final TransactionRuleGroupProvider ruleGroupProvider;
    private final TransactionRuleProvider ruleProvider;
    private final CurrentUserProvider currentUserProvider;

    @Get("/groups")
    @Operation(
            summary = "List rule groups",
            description = "List all the transaction rule groups available",
            operationId = "getRuleGroups"
    )
    Publisher<TransactionRuleGroupResponse> groups() {
        return ruleGroupProvider.lookup()
                .map(TransactionRuleGroupResponse::new);
    }

    @Put("/groups")
    @Operation(
            summary = "Create rule group",
            description = "Creates a new rule group in the system",
            operationId = "createRuleGroup"
    )
    @ApiResponse(responseCode = "204", description = "Group successfully created")
    void createGroup(@Body GroupRenameRequest request) {
        if (ruleGroupProvider.lookup(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Group name not unique.");
        }

        EventBus.getBus().send(new CreateRuleGroupCommand(request.getName()));
    }

    @Get("/groups/{group}")
    @Operation(
            summary = "List transaction rules",
            description = "Lists all transaction rules present in the requested group",
            operationId = "getRules"
    )
    Publisher<TransactionRuleResponse> rules(@PathVariable String group) {
        return ruleProvider.lookup(group)
                .map(TransactionRuleResponse::new);
    }

    @Status(HttpStatus.NO_CONTENT)
    @Get("/groups/{group}/move-up")
    @Operation(
            summary = "Move group up",
            description = "Move the transaction rule group up one in the ordering",
            operationId = "moveGroupUp"
    )
    @ApiResponse(responseCode = "204", description = "Successfully moved up")
    void groupUp(@PathVariable String group) {
        ruleGroupProvider.lookup(group)
                .ifPresent(g -> g.changeOrder(g.getSort() - 1));
    }

    @Status(HttpStatus.NO_CONTENT)
    @Get("/groups/{group}/move-down")
    @Operation(
            summary = "Move group down",
            description = "Move the transaction rule group down one in the ordering",
            operationId = "moveGroupDown"
    )
    @ApiResponse(responseCode = "204", description = "Successfully moved down")
    void groupDown(@PathVariable String group) {
        ruleGroupProvider.lookup(group)
                .ifPresent(g -> g.changeOrder(g.getSort() + 1));
    }

    @Patch("/groups/{group}")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Rename rule group",
            description = "Renames the transaction rule group",
            operationId = "renameRuleGroup"
    )
    @ApiResponse(responseCode = "204", description = "Successfully updated name")
    void rename(@PathVariable String group, @Body GroupRenameRequest request) {
        ruleGroupProvider.lookup(group)
                .ifPresent(g -> g.rename(request.getName()));
    }

    @Put("/groups/{group}")
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create transaction rule",
            description = "Creates a new transaction rule in the desired group",
            operationId = "createTransactionRule"
    )
    @ApiResponse(responseCode = "201", description = "Rule successfully created", content = @Content(schema = @Schema(implementation = TransactionRuleResponse.class)))
    void create(@PathVariable String group, @Valid @Body CreateRuleRequest request) {
        var rule = currentUserProvider.currentUser().createRule(request.getName(),
                request.isRestrictive());

        rule.assign(group);
        rule.change(
                request.getName(),
                request.getDescription(),
                request.isRestrictive(),
                request.isActive());

        request.getChanges()
                .forEach(change -> rule.registerChange(
                        change.getColumn(),
                        change.getValue()));

        request.getConditions()
                .forEach(condition -> rule.registerCondition(
                        condition.getColumn(),
                        condition.getOperation(),
                        condition.getValue()));

        ruleProvider.save(rule);
    }

    @Get("/groups/{group}/{id}")
    @Operation(
            summary = "Get transaction rule",
            description = "Returns a single transaction rule by the identified group and rule",
            operationId = "getTransactionRule"
    )
    @ApiDefaults
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionRuleResponse.class)))
    Publisher<TransactionRuleResponse> getRule(@PathVariable String group, @PathVariable long id) {
        return Mono.create(emitter -> {
            ruleProvider.lookup(id)
                    .map(TransactionRuleResponse::new)
                    .ifPresent((Consumer<TransactionRuleResponse>) emitter::success)
                    .elseRun(() -> emitter.error(StatusException.notFound("Rule not found with id " + id)));
        });
    }

    @Status(HttpStatus.NO_CONTENT)
    @Get("/groups/{group}/{id}/move-up")
    @Operation(
            summary = "Move transaction rule up",
            description = "Moves the transaction rule up by one in the ordering",
            operationId = "moveTransactionRuleUp"
    )
    @ApiResponse(responseCode = "204", description = "Successfully moved up")
    void ruleUp(@PathVariable String group, @PathVariable long id) {
        ruleProvider.lookup(id)
                .ifPresent(rule -> rule.changeOrder(rule.getSort() - 1));
    }

    @Status(HttpStatus.NO_CONTENT)
    @Get("/groups/{group}/{id}/move-down")
    @Operation(
            summary = "Move transaction rule down",
            description = "Moves the transaction rule down by one in the ordering",
            operationId = "moveTransactionRuleDown"
    )
    @ApiResponse(responseCode = "204", description = "Successfully moved down")
    void ruleDown(@PathVariable String group, @PathVariable long id) {
        ruleProvider.lookup(id)
                .ifPresent(rule -> rule.changeOrder(rule.getSort() + 1));
    }

    @Post("/groups/{group}/{id}")
    @Operation(
            summary = "Update transaction rule",
            description = "Updates the transaction rule with the provided settings",
            operationId = "updateTransactionRule"
    )
    @ApiDefaults
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionRuleResponse.class)))
    Publisher<TransactionRuleResponse> updateRule(
            @PathVariable String group,
            @PathVariable long id,
            @Valid @Body CreateRuleRequest request) {
        return Mono.<TransactionRule>create(emitter -> {
            ruleProvider.lookup(id)
                    .ifPresent(rule -> {
                        rule.change(
                                request.getName(),
                                request.getDescription(),
                                request.isRestrictive(),
                                request.isActive());

                        rule.getChanges()
                                .forEach(Removable::delete);

                        request.getChanges()
                                .forEach(change -> rule.registerChange(change.getColumn(), change.getValue()));

                        rule.getConditions()
                                .forEach(Removable::delete);

                        request.getConditions()
                                .forEach(condition -> rule.registerCondition(
                                        condition.getColumn(),
                                        condition.getOperation(),
                                        condition.getValue()));

                        ruleProvider.save(rule);
                        emitter.success(ruleProvider.lookup(id).get());
                    })
                    .elseRun(() -> emitter.error(StatusException.notFound("Rule not found with id " + id)));
        }).map(TransactionRuleResponse::new);
    }

    @Status(HttpStatus.NO_CONTENT)
    @Delete("/groups/{group}/{id}")
    @Operation(
            summary = "Delete transaction rule",
            description = "Removes the desired rule from the system",
            operationId = "deleteTransactionRule"
    )
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    void deleteRule(@PathVariable String group, @PathVariable long id) {
        var rule = ruleProvider.lookup(id).get();
        rule.remove();
        ruleProvider.save(rule);
    }

}
