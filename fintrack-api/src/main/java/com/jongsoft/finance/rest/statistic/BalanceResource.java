package com.jongsoft.finance.rest.statistic;

import javax.validation.Valid;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.lang.API;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reports")
@Controller("/api/statistics/balance")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class BalanceResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    public BalanceResource(FilterFactory filterFactory, TransactionProvider transactionProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
    }

    @Post
    @Operation(
            summary = "Calculate a balance based upon request",
            description = "This operation will calculate the balance for the current user based upon the given filters"
    )
    public Single<BalanceResponse> calculate(@Valid @Body BalanceRequest request) {
        var filter = filterFactory.transaction();

        if (!request.getAccounts().isEmpty()) {
            filter.accounts(API.List(request.getAccounts())
                    .map(a -> new EntityRef(a.getId())));
        } else {
            filter.ownAccounts();
        }

        if (!request.getCategories().isEmpty()) {
            filter.categories(API.List(request.getCategories())
                    .map(a -> new EntityRef(a.getId())));
        }

        if (!request.getExpenses().isEmpty()) {
            filter.expenses(API.List(request.getExpenses())
                    .map(a -> new EntityRef(a.getId())));
        }

        if (request.dateRange() != null) {
            filter.range(request.dateRange());
        }

        if (!request.allMoney()) {
            filter.onlyIncome(request.onlyIncome());
        }

        if (request.currency() != null) {
            filter.currency(request.currency());
        }

        if (request.importSlug() != null) {
            filter.importSlug(request.importSlug());
        }

        return Single.create(emitter -> {
            var balance = transactionProvider.balance(filter)
                    .getOrSupply(() -> 0D);

            emitter.onSuccess(new BalanceResponse(balance));
        });
    }

}
