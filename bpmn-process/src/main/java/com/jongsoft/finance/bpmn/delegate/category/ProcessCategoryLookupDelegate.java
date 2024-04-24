package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

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
 *     <li>category, the {@link Category#getId()} that was found</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessCategoryLookupDelegate implements JavaDelegate, JavaBean {

    private final CategoryProvider categoryProvider;

    ProcessCategoryLookupDelegate(CategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing category lookup '{}'",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        final Category category;
        if (execution.hasVariableLocal("name")) {
            var label = (String) execution.getVariableLocal("name");

            category = categoryProvider.lookup(label).get();
        } else {
            category = categoryProvider.lookup((Long) execution.getVariableLocal("id"))
                    .get();
        }

        execution.setVariable("category", category);
    }

}
