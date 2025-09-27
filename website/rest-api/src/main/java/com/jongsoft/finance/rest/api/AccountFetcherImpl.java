package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.providers.AccountProvider;
import io.micronaut.http.annotation.Controller;

@Controller
class AccountFetcherImpl {

  private final AccountProvider accountProvider;

  AccountFetcherImpl(AccountProvider accountProvider) {
    this.accountProvider = accountProvider;
  }
}
