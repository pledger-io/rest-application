package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.account.AccountTypeProvider;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@Controller("/api/account-types")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AccountTypeResource {

    private final AccountTypeProvider accountTypeProvider;

    public AccountTypeResource(AccountTypeProvider accountTypeProvider) {
        this.accountTypeProvider = accountTypeProvider;
    }

    @Get
    List<String> list() {
        return accountTypeProvider.lookup(false).toJava();
    }

}
