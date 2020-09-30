package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Singleton;

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
                .switchIfEmpty(Single.create(emitter -> emitter.onSuccess(create(name))))
                .blockingGet();

        execution.setVariableLocal("id", tag.name());
    }

    private Tag create(String name) {
        currentUserProvider.currentUser()
                .createTag(name);

        return tagProvider.lookup(name)
                .switchIfEmpty(Single.error(StatusException.internalError("Could not locate tag after creating it")))
                .blockingGet();
    }

}
