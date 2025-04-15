package com.jongsoft.finance.llm.junit;

import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

public class AiTestRunExtension extends MicronautJunit5Extension implements BeforeAllCallback {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(AiTestRunExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        var store = context.getRoot()
                .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
        var applicationContext = store.get(ApplicationContext.class, ApplicationContext.class);
        applicationContext.registerSingleton(Mockito.spy(CategoryProvider.class));
        applicationContext.registerSingleton(Mockito.spy(TransactionProvider.class));
        applicationContext.registerSingleton(Mockito.spy(BudgetProvider.class));
        applicationContext.registerSingleton(Mockito.spy(TagProvider.class));
    }
}
