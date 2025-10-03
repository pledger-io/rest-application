package com.jongsoft.finance.rest.extension;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.ApplicationContext;

import static org.mockito.Mockito.when;

public class PledgerContext {

  private final ApplicationContext applicationContext;

  public PledgerContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public PledgerContext withUser(String user) {
    applicationContext.getBean(UserProvider.class)
            .lookup(new UserIdentifier(user))
            .ifNotPresent(() -> new UserAccount(user, "test123"));
    when(applicationContext.getBean(AuthenticationFacade.class).authenticated()).thenReturn(user);
    when(applicationContext.getBean(CurrentUserProvider.class).currentUser())
        .thenAnswer(_ ->
            applicationContext.getBean(UserProvider.class)
                .lookup(new UserIdentifier(user))
                .getOrThrow(() -> new RuntimeException("Cannot find user " + user)));
    return this;
  }

  public PledgerContext withBankAccount(String name, String currency, String type) {
      var accountProvider = applicationContext.getBean(AccountProvider.class);
      if (accountProvider.lookup(name).isPresent()) {
          return this;
      }

      applicationContext.getBean(CurrentUserProvider.class)
            .currentUser()
            .createAccount(name, currency, type);
      return this;
  }

  public PledgerContext withCreditor(String name, String currency) {
      return withBankAccount(name, currency, "creditor");
  }

  public PledgerContext withDebtor(String name, String currency) {
      return withBankAccount(name, currency, "debtor");
  }

  void reset() {

  }
}
