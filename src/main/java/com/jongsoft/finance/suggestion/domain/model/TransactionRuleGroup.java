package com.jongsoft.finance.suggestion.domain.model;

import com.jongsoft.finance.suggestion.domain.commands.CreateRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.RenameRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.ReorderRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.commands.RuleGroupDeleteCommand;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class TransactionRuleGroup {

    private Long id;
    private String name;
    private int sort;

    private TransactionRuleGroup(String name) {
        this.name = name;
        CreateRuleGroupCommand.ruleGroupCreated(name);
    }

    TransactionRuleGroup(Long id, String name, int sort) {
        this.id = id;
        this.name = name;
        this.sort = sort;
    }

    public void changeOrder(int sort) {
        if (sort != this.sort) {
            this.sort = sort;
            ReorderRuleGroupCommand.reorderRuleGroupUpdated(id, sort);
        }
    }

    public void rename(String name) {
        if (!this.name.equalsIgnoreCase(name)) {
            this.name = name;
            RenameRuleGroupCommand.ruleGroupRenamed(id, name);
        }
    }

    public void delete() {
        RuleGroupDeleteCommand.ruleGroupDeleted(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public static TransactionRuleGroup create(String name) {
        return new TransactionRuleGroup(name);
    }
}
