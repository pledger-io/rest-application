package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.providers.AccountTypeProvider;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Singleton
@Tag(name = "Account information")
@Controller("/api/account-types")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountTypeResource {

    private final AccountTypeProvider accountTypeProvider;

    @Get
    @Operation(
            summary = "List types",
            description = "Get a listing of all available account types in the system.",
            operationId = "listTypes"
    )
    List<String> list() {
        return accountTypeProvider.lookup(false).toJava();
    }

}
