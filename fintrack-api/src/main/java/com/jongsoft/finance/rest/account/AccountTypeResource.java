package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.providers.AccountTypeProvider;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Controller("/api/account-types")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountTypeResource {

    private final AccountTypeProvider accountTypeProvider;

    @Get
    List<String> list() {
        return accountTypeProvider.lookup(false).toJava();
    }

}
