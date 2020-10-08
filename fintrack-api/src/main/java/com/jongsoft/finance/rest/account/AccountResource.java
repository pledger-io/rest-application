package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Nullable;
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
            description = "List all accounts that are creatable in the front-end using one of the selectable account types"
    )
    Single<List<AccountResponse>> ownAccounts() {
        return Single.create(emitter -> {
            var accounts = accountProvider.lookup(
                    accountFilterFactory.account().types(accountTypeProvider.lookup(false)));

            var response = accounts.content()
                    .map(AccountResponse::new);

            emitter.onSuccess(response.toJava());
        });
    }

    @Get("/all")
    @Operation(
            summary = "List all accounts",
            description = "Fetch all accounts registered to the authenticated user"
    )
    Single<List<AccountResponse>> allAccounts() {
        return Single.create(emitter -> {
            var accounts = accountProvider.lookup()
                    .map(AccountResponse::new)
                    .toJava();

            emitter.onSuccess(accounts);
        });
    }

    @Get("/auto-complete{?token,type}")
    @Operation(
            summary = "Autocomplete accounts",
            description = "Performs a search operation based on the partial name (token) of the given account type"
    )
    Single<List<AccountResponse>> autocomplete(@Nullable String token, @Nullable String type) {
        return Single.create(emitter -> {
            var accounts = accountProvider.lookup(
                    accountFilterFactory.account()
                            .name(token, false)
                            .pageSize(settingProvider.getAutocompleteLimit())
                            .types(API.List(type)));

            var response = accounts.content()
                    .map(AccountResponse::new);

            emitter.onSuccess(response.toJava());
        });
    }

    @Post
    @Operation(
            summary = "Search accounts",
            description = "Search through all accounts using the provided filter set"
    )
    Single<ResultPageResponse<AccountResponse>> accounts(@Valid @Body AccountSearchRequest searchRequest) {
        return Single.create(emitter -> {
            var filter = accountFilterFactory.account()
                    .page(Math.max(0, searchRequest.page() - 1))
                    .pageSize(settingProvider.getPageSize())
                    .types(searchRequest.accountTypes());
            if (searchRequest.name() != null) {
                filter.name(searchRequest.name(), false);
            }

            var response = accountProvider.lookup(filter)
                    .map(AccountResponse::new);

            emitter.onSuccess(new ResultPageResponse<>(response));
        });
    }

    @Put
    @Operation(
            summary = "Create account",
            description = "This operation will allow for adding new accounts to the system"
    )
    public Single<AccountResponse> create(@Valid @Body AccountEditRequest accountEditRequest) {
        return accountProvider.lookup(accountEditRequest.getName())
                .switchIfEmpty(
                        Single.just(currentUserProvider.currentUser()
                                .createAccount(
                                        accountEditRequest.getName(),
                                        accountEditRequest.getCurrency(),
                                        accountEditRequest.getType()))
                .flatMapMaybe(a -> accountProvider.lookup(a.getName()))
                .map(account -> {
                    if (accountEditRequest.getInterestPeriodicity() != null) {
                        account.interest(accountEditRequest.getInterest(), accountEditRequest.getInterestPeriodicity());
                    }

                    account.changeAccount(
                            accountEditRequest.getIban(),
                            accountEditRequest.getBic(),
                            accountEditRequest.getNumber());

                    return account;
                }))
                .switchIfEmpty(Single.error(StatusException.badRequest("Failed to create new account")))
                .map(AccountResponse::new);
    }
}
