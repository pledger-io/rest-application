package com.jongsoft.finance.rest.account;

import static com.jongsoft.finance.rest.ApiConstants.TAG_ACCOUNTS;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@ApiDefaults
@Tag(name = TAG_ACCOUNTS)
@Controller("/api/accounts")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class AccountResource {

  private final SettingProvider settingProvider;
  private final CurrentUserProvider currentUserProvider;
  private final AccountProvider accountProvider;
  private final FilterFactory accountFilterFactory;
  private final AccountTypeProvider accountTypeProvider;

  public AccountResource(
      SettingProvider settingProvider,
      CurrentUserProvider currentUserProvider,
      AccountProvider accountProvider,
      FilterFactory accountFilterFactory,
      AccountTypeProvider accountTypeProvider) {
    this.settingProvider = settingProvider;
    this.currentUserProvider = currentUserProvider;
    this.accountProvider = accountProvider;
    this.accountFilterFactory = accountFilterFactory;
    this.accountTypeProvider = accountTypeProvider;
  }

  @Get("/my-own")
  @Operation(
      summary = "List own accounts",
      description =
          "List all accounts that are creatable in the front-end using one of the selectable account types",
      operationId = "ownAccounts")
  List<AccountResponse> ownAccounts() {
    var accounts =
        accountProvider.lookup(
            accountFilterFactory.account().types(accountTypeProvider.lookup(false)));

    return accounts.content().map(AccountResponse::new).toJava();
  }

  @Get("/all")
  @Operation(
      summary = "List all accounts",
      description = "Fetch all accounts registered to the authenticated user",
      operationId = "listAllAccounts")
  List<AccountResponse> allAccounts() {
    return accountProvider.lookup().map(AccountResponse::new).toJava();
  }

  @Get("/auto-complete{?token,type}")
  @Operation(
      summary = "Autocomplete accounts",
      description =
          "Performs a search operation based on the partial name (token) of the given account type",
      operationId = "autocomplete",
      parameters = {
        @Parameter(
            name = "token",
            description = "A partial search text.",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = String.class)),
        @Parameter(
            name = "type",
            description =
                "An account type to limit the search to, see <a href='#get-/api/account-type'>types</a> for available types.",
            example = "credit",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = String.class)),
      })
  List<AccountResponse> autocomplete(@Nullable String token, @Nullable String type) {
    var filter =
        accountFilterFactory
            .account()
            .name(token, false)
            .page(0, settingProvider.getAutocompleteLimit());
    if (type != null) {
      filter.types(Collections.List(type));
    }

    var accounts = accountProvider.lookup(filter);
    return accounts.content().map(AccountResponse::new).toJava();
  }

  @Post
  @Operation(
      summary = "Search accounts",
      description = "Search through all accounts using the provided filter set",
      operationId = "listAccounts")
  ResultPageResponse<AccountResponse> accounts(@Valid @Body AccountSearchRequest searchRequest) {
    var filter =
        accountFilterFactory
            .account()
            .page(Math.max(0, searchRequest.page() - 1), settingProvider.getPageSize())
            .types(searchRequest.accountTypes());
    if (searchRequest.name() != null) {
      filter.name(searchRequest.name(), false);
    }

    var response = accountProvider.lookup(filter).map(AccountResponse::new);

    return new ResultPageResponse<>(response);
  }

  @Put
  @Operation(
      summary = "Create account",
      description = "This operation will allow for adding new accounts to the system",
      operationId = "createAccount")
  AccountResponse create(@Valid @Body AccountEditRequest accountEditRequest) {
    var existing = accountProvider.lookup(accountEditRequest.name());

    if (!existing.isPresent()) {
      currentUserProvider
          .currentUser()
          .createAccount(
              accountEditRequest.name(), accountEditRequest.currency(), accountEditRequest.type());

      return accountProvider
          .lookup(accountEditRequest.name())
          .map(
              inserted -> {
                if (accountEditRequest.interestPeriodicity() != null) {
                  inserted.interest(
                      accountEditRequest.interest(), accountEditRequest.interestPeriodicity());
                }

                inserted.changeAccount(
                    accountEditRequest.iban(),
                    accountEditRequest.bic(),
                    accountEditRequest.number());

                return new AccountResponse(inserted);
              })
          .getOrThrow(() -> StatusException.internalError("Failed to create account"));
    }

    throw StatusException.badRequest("Account already exists");
  }
}
