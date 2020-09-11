package com.jongsoft.finance.bpmn.delegate.transaction;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.security.CurrentUserProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class TagLookupDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;
    private final TagProvider tagProvider;

    public TagLookupDelegate(CurrentUserProvider currentUserProvider, TagProvider tagProvider) {
        this.currentUserProvider = currentUserProvider;
        this.tagProvider = tagProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Looking up tag {} for current user",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        var name = execution.<StringValue>getVariableLocalTyped("name").getValue();
        var tag = tagProvider.lookup(name)
                .getOrSupply(() -> this.create(name));

        execution.setVariableLocal("id", tag.name());
    }

    private Tag create(String name) {
        currentUserProvider.currentUser()
                .createTag(name);

        return tagProvider.lookup(name)
                .getOrThrow(() -> new IllegalStateException("Could not locate tag after creating it"));
    }

}
