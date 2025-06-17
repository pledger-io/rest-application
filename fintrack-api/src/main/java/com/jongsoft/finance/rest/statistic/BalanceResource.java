package com.jongsoft.finance.rest.statistic;

import static com.jongsoft.finance.rest.ApiConstants.TAG_REPORTS;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Tag(name = TAG_REPORTS)
@Controller("/api/statistics/balance")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
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
      summary = "Calculate balance",
      description =
          "This operation will calculate the balance for the current user based upon the given filters",
      operationId = "getBalance")
  BalanceResponse calculate(@Valid @Body BalanceRequest request) {
    TransactionProvider.FilterCommand filter = buildFilterCommand(request);

    var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);

    return new BalanceResponse(balance.doubleValue());
  }

  @Post("/partitioned/{partitionKey}")
  @Operation(
      summary = "Partitioned balance",
      description =
          "Partition all transaction matching the balance request using the partitionKey provided.",
      operationId = "partitionedBalance",
      parameters = {
        @Parameter(
            name = "partitionKey",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = String.class),
            description = "The partition key can be one of: account, budget or category")
      })
  public List<BalancePartitionResponse> calculatePartitioned(
      @PathVariable String partitionKey, @Valid @Body BalanceRequest request) {
    Sequence<? extends AggregateBase> entityProvider =
        switch (partitionKey) {
          case "account" -> applicationContext.getBean(AccountProvider.class).lookup();
          case "budget" ->
              applicationContext
                  .getBean(ExpenseProvider.class)
                  .lookup(filterFactory.expense())
                  .content();
          case "category" -> applicationContext.getBean(CategoryProvider.class).lookup();
          default ->
              throw new IllegalArgumentException("Unsupported partition used " + partitionKey);
        };

    Function<Sequence<EntityRef>, TransactionProvider.FilterCommand> filterBuilder =
        switch (partitionKey) {
          case "account" -> (e) -> buildFilterCommand(request).accounts(e);
          case "budget" -> (e) -> buildFilterCommand(request).expenses(e);
          case "category" -> (e) -> buildFilterCommand(request).categories(e);
          default ->
              throw new IllegalArgumentException("Unsupported partition used " + partitionKey);
        };

    var result = new ArrayList<BalancePartitionResponse>();
    var total =
        transactionProvider.balance(buildFilterCommand(request)).getOrSupply(() -> BigDecimal.ZERO);

    for (AggregateBase entity : entityProvider) {
      var filter = filterBuilder.apply(Collections.List(new EntityRef(entity.getId())));
      var balance = transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);

      result.add(new BalancePartitionResponse(entity.toString(), balance.doubleValue()));
      total = total.subtract(BigDecimal.valueOf(balance.doubleValue()));
    }

    result.add(new BalancePartitionResponse("", total.doubleValue()));
    return result;
  }

  @Post("/daily")
  @Operation(
      summary = "Daily balance",
      description = "Compute the daily balance based upon the provided request",
      operationId = "dailyBalance")
  List<DailyResponse> daily(@Valid @Body BalanceRequest request) {
    return transactionProvider.daily(buildFilterCommand(request)).map(DailyResponse::new).toJava();
  }

  @Post("/monthly")
  @Operation(
      summary = "Monthly balance",
      description = "Compute the monthly balance based upon the provided request",
      operationId = "monthlyBalance")
  List<DailyResponse> monthly(@Valid @Body BalanceRequest request) {
    return transactionProvider
        .monthly(buildFilterCommand(request))
        .map(DailyResponse::new)
        .toJava();
  }

  private TransactionProvider.FilterCommand buildFilterCommand(BalanceRequest request) {
    var filter = filterFactory.transaction();

    if (!request.getAccounts().isEmpty()) {
      filter.accounts(Collections.List(request.getAccounts()).map(a -> new EntityRef(a.getId())));
    } else {
      filter.ownAccounts();
    }

    if (!request.getCategories().isEmpty()) {
      filter.categories(
          Collections.List(request.getCategories()).map(a -> new EntityRef(a.getId())));
    }

    if (!request.getExpenses().isEmpty()) {
      filter.expenses(Collections.List(request.getExpenses()).map(a -> new EntityRef(a.getId())));
    }

    if (request.getDateRange() != null) {
      filter.range(Dates.range(request.getDateRange().start(), request.getDateRange().end()));
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
