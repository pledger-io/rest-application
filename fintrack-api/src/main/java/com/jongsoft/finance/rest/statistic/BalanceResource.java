package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.function.Function;

@Tag(name = "Reports")
@Controller("/api/statistics/balance")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BalanceResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final ApplicationContext applicationContext;

    @Post
    @Operation(
            summary = "Calculate a balance based upon request",
            description = "This operation will calculate the balance for the current user based upon the given filters",
            operationId = "getBalance"
    )
    public Publisher<BalanceResponse> calculate(@Valid @Body BalanceRequest request) {
        TransactionProvider.FilterCommand filter = buildFilterCommand(request);

        return Mono.create(emitter -> {
            var balance = transactionProvider.balance(filter)
                    .getOrSupply(() -> BigDecimal.ZERO);

            emitter.success(new BalanceResponse(balance.doubleValue()));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Post("/partitioned/{partitionKey}")
    public Publisher<BalancePartitionResponse> calculatePartitioned(
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

        return Flux.create(flowableEmitter -> {
            var total = transactionProvider.balance(buildFilterCommand(request))
                    .getOrSupply(() -> BigDecimal.ZERO);

            for (AggregateBase entity : entityProvider) {
                var filter = filterBuilder.apply(
                        Collections.List(
                                new EntityRef(entity.getId())));
                var balance = transactionProvider.balance(filter)
                        .getOrSupply(() -> BigDecimal.ZERO);

                flowableEmitter.next(new BalancePartitionResponse(entity.toString(), balance.doubleValue()));
                total = total.subtract(BigDecimal.valueOf(balance.doubleValue()));
            }

            flowableEmitter.next(new BalancePartitionResponse("", total.doubleValue()));
            flowableEmitter.complete();
        });
    }

    @Post("/daily")
    public Publisher<?> daily(@Valid @Body BalanceRequest request) {
        return Flux.create(emitter -> {
            transactionProvider.daily(buildFilterCommand(request))
                    .map(DailyResponse::new)
                    .forEach(emitter::next);

            emitter.complete();
        });

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
