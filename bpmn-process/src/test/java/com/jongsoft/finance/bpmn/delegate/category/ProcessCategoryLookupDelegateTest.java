package com.jongsoft.finance.bpmn.delegate.category;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.lang.Control;
import io.reactivex.Maybe;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProcessCategoryLookupDelegateTest {

    private ProcessCategoryLookupDelegate subject;

    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private DelegateExecution execution;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new ProcessCategoryLookupDelegate(categoryProvider);
    }

    @Test
    void execute_byName() throws Exception {
        Category category = Category.builder().id(1L).label("Grocery").build();

        BDDMockito.given(execution.hasVariableLocal("name")).willReturn(true);
        BDDMockito.given(execution.getVariableLocal("name")).willReturn("Grocery");
        BDDMockito.given(categoryProvider.lookup("Grocery")).willReturn(Maybe.just(category));

        subject.execute(execution);

        BDDMockito.verify(execution).setVariable("category", category);
    }

    @Test
    void execute_byId() throws Exception {
        Category category = Category.builder().id(1L).label("Grocery").build();

        BDDMockito.given(execution.hasVariableLocal("id")).willReturn(true);
        BDDMockito.given(execution.getVariableLocal("id")).willReturn(1L);
        BDDMockito.given(categoryProvider.lookup(1L)).willReturn(Control.Option(category));

        subject.execute(execution);

        BDDMockito.verify(execution).setVariable("category", category);
    }
}
