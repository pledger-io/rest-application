package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller("/api/accounts/{accountId}")
@Tag(name = "Account information")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountEditResource {

    private final CurrentUserProvider currentUserProvider;
    private final AccountProvider accountProvider;

    @Get
    @Operation(
            summary = "Get Account",
            description = "Attempts to get the account with matching account id. If no account is found or you are not" +
                    "authorized an exception will be returned.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The resulting account",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The account cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Single<AccountResponse> get(@PathVariable long accountId) {
        return Single.create(emitter -> {
            accountProvider.lookup(accountId)
                    .map(AccountResponse::new)
                    .ifPresent(emitter::onSuccess)
                    .elseRun(() -> emitter.onError(StatusException.notFound("Account not found")));
        });
    }

    @Post
    @Operation(
            summary = "Update Account",
            description = "Update an existing account with the new details provided in the body. The updated account will" +
                    " be returned, or if no account is found an exception.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The updated account",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The account cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Single<HttpResponse<AccountResponse>> update(
            @PathVariable long accountId,
            @Valid @Body AccountEditRequest accountEditRequest) {
        return Single.create(emitter -> {
            var accountOption = getOrFail(emitter, accountId);

            if (accountOption.isPresent()) {
                var account = accountOption.get();

                account.rename(
                        accountEditRequest.getName(),
                        accountEditRequest.getDescription(),
                        accountEditRequest.getCurrency(),
                        accountEditRequest.getType());

                account.changeAccount(
                        accountEditRequest.getIban(),
                        accountEditRequest.getBic(),
                        accountEditRequest.getNumber());

                if (accountEditRequest.getInterestPeriodicity() != null) {
                    account.interest(accountEditRequest.getInterest(), accountEditRequest.getInterestPeriodicity());
                }

                emitter.onSuccess(HttpResponse.ok(new AccountResponse(account)));
            }
        });
    }

    @Post(value = "/image")
    @Operation(
            summary = "Attach icon",
            description = "Attach an icon to the account. If any icon was previously registered it will be removed " +
                    "from the system.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class))
    )
    Single<AccountResponse> persistImage(
            @PathVariable long accountId,
            @Body @Valid AccountImageRequest imageRequest) {
        return Single.create(emitter -> {
            var accountPromise = accountProvider.lookup(accountId);

            if (accountPromise.isPresent()) {
                accountPromise.get()
                        .registerIcon(imageRequest.getFileCode());

                emitter.onSuccess(new AccountResponse(accountPromise.get()));
            } else {
                emitter.onError(StatusException.notFound("Could not find account"));
            }
        });
    }

    @Delete
    @Operation(
            summary = "Delete Account",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Single<HttpResponse<Void>> delete(@PathVariable long accountId) {
        return Single.create(emitter -> {
            var account = accountProvider.lookup(accountId)
                    .filter(a -> a.getUser().getId().equals(currentUserProvider.currentUser().getId()));
            if (account.isPresent()) {
                account.get().terminate();
                emitter.onSuccess(HttpResponse.noContent());
            } else {
                emitter.onSuccess(HttpResponse.notFound());
            }
        });
    }

    private Optional<Account> getOrFail(SingleEmitter<HttpResponse<AccountResponse>> emitter, long accountId) {
        var accountOption = accountProvider.lookup(accountId);

        if (!accountOption.isPresent()) {
            emitter.onSuccess(HttpResponse.notFound());
            return Control.Option();
        } else if (!accountOption.get().getUser().getId().equals(currentUserProvider.currentUser().getId())) {
            emitter.onSuccess(HttpResponse.unauthorized());
            return Control.Option();
        }

        return accountOption;
    }

}
