package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.CategoryJson;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Inject;
import javax.inject.Singleton;

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
public class ProcessCreateCategoryDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;
    private final CategoryProvider categoryProvider;

    @Inject
    public ProcessCreateCategoryDelegate(
            CurrentUserProvider currentUserProvider,
            CategoryProvider categoryProvider) {
        this.currentUserProvider = currentUserProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var categoryJson = ProcessMapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("category").getValue(),
                CategoryJson.class);

        log.debug("{}: Processing category creation from json '{}'",
                execution.getCurrentActivityName(),
                categoryJson.getLabel());

        categoryProvider.lookup(categoryJson.getLabel())
                .switchIfEmpty(Single.create(emitter -> {
                    currentUserProvider.currentUser()
                            .createCategory(categoryJson.getLabel());

                    categoryProvider.lookup(categoryJson.getLabel())
                            .subscribe(category -> {
                                category.rename(
                                        categoryJson.getLabel(),
                                        categoryJson.getDescription());

                                emitter.onSuccess(category);
                            });
                })).blockingGet();
    }

}
