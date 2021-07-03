package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This delegate will lookup a {@link Category} in the system.
 *
 * <p>
 * This delegate expects one of the following variables to be present:
 * </p>
 * <ol>
 *     <li>name, the label of the category</li>
 *     <li>id, the unique system generated id of the category</li>
 * </ol>
 *
 * <p>
 *     This delegate will result in the following variables
 * </p>
 * <ul>
 *     <li>category, the {@link Category} that was found</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProcessCategoryLookupDelegate implements JavaDelegate {

    private final CategoryProvider categoryProvider;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing category lookup '{}'",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        final Category category;
        if (execution.hasVariableLocal("name")) {
            var label = (String) execution.getVariableLocal("name");

            category = categoryProvider.lookup(label).blockingGet();
        } else {
            category = categoryProvider.lookup((Long) execution.getVariableLocal("id"))
                    .get();
        }

        execution.setVariable("category", category);
    }

}
