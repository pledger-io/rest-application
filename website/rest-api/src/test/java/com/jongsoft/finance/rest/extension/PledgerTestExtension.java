package com.jongsoft.finance.rest.extension;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

public class PledgerTestExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

  private final static Logger log = LoggerFactory.getLogger(PledgerTestExtension.class);
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PledgerTestExtension.class);

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter()
        .getType()
        .isAssignableFrom(PledgerContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getStore(extensionContext)
        .get(PledgerContext.class, PledgerContext.class);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    var store = context.getRoot()
        .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
    var applicationContext = store.get(ApplicationContext.class, ApplicationContext.class);

    applicationContext.registerSingleton(AuthenticationFacade.class, mock(AuthenticationFacade.class));
    applicationContext.registerSingleton(CurrentUserProvider.class, mock(CurrentUserProvider.class));
    applicationContext.registerSingleton(FinTrack.class, new FinTrack(new Encoder() {
      @Override
      public String encrypt(String value) {
        return value;
      }

      @Override
      public boolean matches(String encoded, String value) {
        return encoded.equals(value);
      }
    }));

    new EventBus(applicationContext.getBean(ApplicationEventPublisher.class));

    getStore(context)
        .put(PledgerContext.class, new PledgerContext(applicationContext));
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    getStore(extensionContext)
        .get(PledgerContext.class, PledgerContext.class).reset();
  }

  private static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
    return extensionContext.getRoot().getStore(NAMESPACE);
  }
}
