package com.jongsoft.finance.bpmn.feature.junit;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class ProcessTestExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, ParameterResolver {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProcessTestExtension.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ProcessTestExtension.class);

    public interface ProcessExecution<T extends ProcessExecution> {
        ProcessExecution<?> obtainChildProcess(String processKey);

        HistoricProcessExecution verifyCompleted();

        <Y> T yankVariable(String variableName, Consumer<Y> consumer);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        log.debug("Initializing process test extension {}.", extensionContext.getDisplayName());
        var applicationBean = Mockito.spy(new FinTrack(Mockito.mock(Encoder.class)));

        var store = extensionContext.getRoot()
                .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
        var applicationContext = store.get(ApplicationContext.class, ApplicationContext.class);
        applicationContext.registerSingleton(FinTrack.class, applicationBean);
        applicationContext.registerSingleton(StorageService.class, Mockito.spy(StorageService.class), Qualifiers.byName("storageService"));
        applicationContext.registerSingleton(MailDaemon.class, Mockito.spy(MailDaemon.class), Qualifiers.byName("mailDaemon"));
        applicationContext.registerSingleton(new Consumer<CreateAccountCommand>() {
            @EventListener
            public void accept(CreateAccountCommand accountCreatedEvent) {
                Mockito.when(applicationContext.getBean(AccountProvider.class).lookup(accountCreatedEvent.name()))
                        .thenReturn(Control.Option(Account.builder()
                                .name(accountCreatedEvent.name())
                                .currency(accountCreatedEvent.currency())
                                .type(accountCreatedEvent.type())
                                .build()));
            }
        }, true);

        getStore(extensionContext)
                .put(RuntimeContext.class, new RuntimeContext(applicationContext));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        getStore(extensionContext)
                .get(RuntimeContext.class, RuntimeContext.class).clean();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        getStore(extensionContext)
                .get(RuntimeContext.class, RuntimeContext.class).resetMocks();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isProcessContext(parameterContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getStore(extensionContext)
                .get(RuntimeContext.class, RuntimeContext.class);
    }

    private static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE);
    }

    private static boolean isProcessContext(ParameterContext parameterContext) {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(RuntimeContext.class);
    }
}
