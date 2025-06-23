package com.jongsoft.finance.rest.account;

import static com.jongsoft.finance.rest.ApiConstants.TAG_ACCOUNTS;

import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.security.AuthenticationRoles;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Singleton;
import java.util.List;

@ApiDefaults
@Singleton
@Tag(name = TAG_ACCOUNTS)
@Controller("/api/account-types")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class AccountTypeResource {

  private final AccountTypeProvider accountTypeProvider;

  public AccountTypeResource(AccountTypeProvider accountTypeProvider) {
    this.accountTypeProvider = accountTypeProvider;
  }

  @Get
  @Operation(
      summary = "List types",
      description = "Get a listing of all available account types in the system.",
      operationId = "listTypes")
  List<String> list() {
    return accountTypeProvider.lookup(false).toJava();
  }
}
