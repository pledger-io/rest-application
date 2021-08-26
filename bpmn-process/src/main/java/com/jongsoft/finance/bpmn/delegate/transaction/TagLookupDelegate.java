package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TagLookupDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;
    private final TagProvider tagProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Looking up tag {} for current user",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        var name = execution.<StringValue>getVariableLocalTyped("name").getValue();
        var tag = tagProvider.lookup(name)
                .switchIfEmpty(Mono.create(emitter -> emitter.success(create(name))))
                .block();

        execution.setVariableLocal("id", tag.name());
    }

    private Tag create(String name) {
        currentUserProvider.currentUser()
                .createTag(name);

        return tagProvider.lookup(name)
                .switchIfEmpty(Mono.error(StatusException.internalError("Could not locate tag after creating it")))
                .block();
    }

}
