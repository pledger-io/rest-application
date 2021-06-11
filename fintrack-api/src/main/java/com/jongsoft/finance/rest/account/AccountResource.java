package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@Controller("/api/accounts")
@Tag(name = "Account information")
@Secured(SecurityRule.IS_AUTHENTICATED)
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
            description = "List all accounts that are creatable in the front-end using one of the selectable account types",
            operationId = "ownAccounts"
    )
    Mono<List<AccountResponse>> ownAccounts() {
        return Mono.create(emitter -> {
            var accounts = accountProvider.lookup(
                    accountFilterFactory.account().types(accountTypeProvider.lookup(false)));

            var response = accounts.content()
                    .map(AccountResponse::new);

            emitter.success(response.toJava());
        });
    }

    @Get("/all")
    @Operation(
            summary = "List all accounts",
            description = "Fetch all accounts registered to the authenticated user",
            operationId = "listAllAccounts"
    )
    Mono<List<AccountResponse>> allAccounts() {
        return Mono.create(emitter -> {
            var accounts = accountProvider.lookup()
                    .map(AccountResponse::new)
                    .toJava();

            emitter.success(accounts);
        });
    }

    @Get("/auto-complete{?token,type}")
    @Operation(
            summary = "Autocomplete accounts",
            description = "Performs a search operation based on the partial name (token) of the given account type",
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
                            description = "An account type to limit the search to, see <a href='#get-/api/account-type'>types</a> for available types.",
                            example = "credit",
                            in = ParameterIn.QUERY,
                            required = true,
                            schema = @Schema(implementation = String.class)),
            }
    )
    Mono<List<AccountResponse>> autocomplete(@Nullable String token, @Nullable String type) {
        return Mono.create(emitter -> {
            var accounts = accountProvider.lookup(
                    accountFilterFactory.account()
                            .name(token, false)
                            .pageSize(settingProvider.getAutocompleteLimit())
                            .types(Collections.List(type)));

            var response = accounts.content()
                    .map(AccountResponse::new);

            emitter.success(response.toJava());
        });
    }

    @Post
    @Operation(
            summary = "Search accounts",
            description = "Search through all accounts using the provided filter set",
            operationId = "listAccounts"
    )
    Mono<ResultPageResponse<AccountResponse>> accounts(@Valid @Body AccountSearchRequest searchRequest) {
        return Mono.create(emitter -> {
            var filter = accountFilterFactory.account()
                    .page(Math.max(0, searchRequest.page() - 1))
                    .pageSize(settingProvider.getPageSize())
                    .types(searchRequest.accountTypes());
            if (searchRequest.name() != null) {
                filter.name(searchRequest.name(), false);
            }

            var response = accountProvider.lookup(filter)
                    .map(AccountResponse::new);

            emitter.success(new ResultPageResponse<>(response));
        });
    }

    @Put
    @Operation(
            summary = "Create account",
            description = "This operation will allow for adding new accounts to the system",
            operationId = "createAccount"
    )
    public Mono<AccountResponse> create(@Valid @Body AccountEditRequest accountEditRequest) {
        return accountProvider.lookup(accountEditRequest.getName())
                .switchIfEmpty(Mono.create(emitter -> {
                    currentUserProvider.currentUser()
                            .createAccount(
                                    accountEditRequest.getName(),
                                    accountEditRequest.getCurrency(),
                                    accountEditRequest.getType());

                    accountProvider.lookup(accountEditRequest.getName())
                            .map(account -> {
                                if (accountEditRequest.getInterestPeriodicity() != null) {
                                    account.interest(accountEditRequest.getInterest(), accountEditRequest.getInterestPeriodicity());
                                }

                                account.changeAccount(
                                        accountEditRequest.getIban(),
                                        accountEditRequest.getBic(),
                                        accountEditRequest.getNumber());

                                return account;
                            })
                            .switchIfEmpty(Mono.error(StatusException.internalError("Failed to create account")))
                            .doOnError(emitter::error)
                            .subscribe(emitter::success);
                }))
                .map(AccountResponse::new);
    }

}
