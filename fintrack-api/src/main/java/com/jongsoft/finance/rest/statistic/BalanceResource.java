package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Tag(name = "Reports")
@Controller("/api/statistics/balance")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class BalanceResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final ApplicationContext applicationContext;

    public BalanceResource(
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            ApplicationContext applicationContext) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.applicationContext = applicationContext;
    }

    @Post
    @Operation(
            summary = "Calculate a balance based upon request",
            description = "This operation will calculate the balance for the current user based upon the given filters",
            operationId = "getBalance"
    )
    public Single<BalanceResponse> calculate(@Valid @Body BalanceRequest request) {
        TransactionProvider.FilterCommand filter = buildFilterCommand(request);

        return Single.create(emitter -> {
            var balance = transactionProvider.balance(filter)
                    .getOrSupply(() -> 0D);

            emitter.onSuccess(new BalanceResponse(balance));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Post("/partitioned/{partitionKey}")
    public Flowable<BalancePartitionResponse> calculatePartitioned(
            @PathVariable String partitionKey,
            @Valid @Body BalanceRequest request) {
        Sequence<? extends AggregateBase> entityProvider = switch (partitionKey) {
            case "account" -> applicationContext.getBean(AccountProvider.class).lookup();
            case "budget" -> applicationContext.getBean(ExpenseProvider.class)
                    .lookup(filterFactory.expense())
                    .content();
            case "category" -> applicationContext.getBean(CategoryProvider.class).lookup();
            default -> throw new IllegalArgumentException("Unsupported partition used " + partitionKey);
        };

        Function<Sequence<EntityRef>, TransactionProvider.FilterCommand> filterBuilder = switch (partitionKey) {
            case "account" -> (e) -> buildFilterCommand(request).accounts(e);
            case "budget" -> (e) -> buildFilterCommand(request).expenses(e);
            case "category" -> (e) -> buildFilterCommand(request).categories(e);
            default -> throw new IllegalArgumentException("Unsupported partition used " + partitionKey);
        };

        return Flowable.create(flowableEmitter -> {
            var total = transactionProvider.balance(buildFilterCommand(request))
                    .map(BigDecimal::valueOf)
                    .getOrSupply(() -> BigDecimal.ZERO);

            for (AggregateBase entity : entityProvider) {
                var filter = filterBuilder.apply(
                        Collections.List(
                                new EntityRef(entity.getId())));
                var balance = transactionProvider.balance(filter)
                        .getOrSupply(() -> 0D);

                flowableEmitter.onNext(new BalancePartitionResponse(entity.toString(), balance));
                total = total.subtract(BigDecimal.valueOf(balance));
            }

            flowableEmitter.onNext(new BalancePartitionResponse("", total.doubleValue()));
            flowableEmitter.onComplete();
        }, BackpressureStrategy.LATEST);
    }

    @Post("/daily")
    public Flowable<?> daily(@Valid @Body BalanceRequest request) {
        return Flowable.create(emitter -> {
            transactionProvider.daily(buildFilterCommand(request))
                    .map(DailyResponse::new)
                    .forEach(emitter::onNext);

            emitter.onComplete();
        }, BackpressureStrategy.DROP);

    }

    private TransactionProvider.FilterCommand buildFilterCommand(BalanceRequest request) {
        var filter = filterFactory.transaction();

        if (!request.getAccounts().isEmpty()) {
            filter.accounts(Collections.List(request.getAccounts())
                    .map(a -> new EntityRef(a.getId())));
        } else {
            filter.ownAccounts();
        }

        if (!request.getCategories().isEmpty()) {
            filter.categories(Collections.List(request.getCategories())
                    .map(a -> new EntityRef(a.getId())));
        }

        if (!request.getExpenses().isEmpty()) {
            filter.expenses(Collections.List(request.getExpenses())
                    .map(a -> new EntityRef(a.getId())));
        }

        if (request.getDateRange() != null) {
            filter.range(
                    Dates.range(
                            request.getDateRange().getStart(),
                            request.getDateRange().getEnd()
                    )
            );
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
        return filter;
    }

}
