package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.CategoryJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

/**
 * This delegate can be used to create a {@link Category} in the system using a serialize {@link
 * CategoryJson}.
 *
 * <p>This delegate expects the following variables to be present:
 *
 * <ul>
 *   <li>category, the serialize {@link CategoryJson}
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessCreateCategoryDelegate implements JavaDelegate, JavaBean {

  private final CurrentUserProvider currentUserProvider;
  private final CategoryProvider categoryProvider;
  private final ProcessMapper mapper;

  ProcessCreateCategoryDelegate(
      CurrentUserProvider currentUserProvider,
      CategoryProvider categoryProvider,
      ProcessMapper mapper) {
    this.currentUserProvider = currentUserProvider;
    this.categoryProvider = categoryProvider;
    this.mapper = mapper;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var categoryJson =
        mapper.readSafe(
            execution.<StringValue>getVariableLocalTyped("category").getValue(),
            CategoryJson.class);

    log.debug(
        "{}: Processing category creation from json '{}'",
        execution.getCurrentActivityName(),
        categoryJson.getLabel());

    categoryProvider
        .lookup(categoryJson.getLabel())
        .ifNotPresent(
            () -> {
              currentUserProvider.currentUser().createCategory(categoryJson.getLabel());

              categoryProvider
                  .lookup(categoryJson.getLabel())
                  .ifPresent(
                      category ->
                          category.rename(categoryJson.getLabel(), categoryJson.getDescription()));
            });
  }
}
