package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.CategoryJson;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import reactor.core.publisher.Mono;

/**
 * This delegate can be used to create a {@link Category} in the system using
 * a serialize {@link CategoryJson}.
 *
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>category, the serialize {@link CategoryJson}</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProcessCreateCategoryDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;
    private final CategoryProvider categoryProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var categoryJson = ProcessMapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("category").getValue(),
                CategoryJson.class);

        log.debug("{}: Processing category creation from json '{}'",
                execution.getCurrentActivityName(),
                categoryJson.getLabel());

        categoryProvider.lookup(categoryJson.getLabel())
                .switchIfEmpty(Mono.create(emitter -> {
                    currentUserProvider.currentUser()
                            .createCategory(categoryJson.getLabel());

                    categoryProvider.lookup(categoryJson.getLabel())
                            .subscribe(category -> {
                                category.rename(
                                        categoryJson.getLabel(),
                                        categoryJson.getDescription());

                                emitter.success(category);
                            });
                })).block();
    }

}
