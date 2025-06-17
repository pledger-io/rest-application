package com.jongsoft.finance.rest.budget;

import static com.jongsoft.finance.rest.ApiConstants.TAG_BUDGETS;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TAG_BUDGETS)
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/budgets/expenses/{expenseId}/{year}/{month}/transactions")
public class ExpenseTransactionResource {

  private final FilterFactory filterFactory;
  private final TransactionProvider transactionService;
  private final SettingProvider settingProvider;

  public ExpenseTransactionResource(
      FilterFactory filterFactory,
      TransactionProvider transactionService,
      SettingProvider settingProvider) {
    this.filterFactory = filterFactory;
    this.transactionService = transactionService;
    this.settingProvider = settingProvider;
  }

  @Get("{?page}")
  @Operation(
      summary = "Transaction overview",
      description = "Paged listing of all transactions for the provided expense and month.",
      parameters = {
        @Parameter(
            name = "expenseId",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = Long.class),
            required = true),
        @Parameter(
            name = "year",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = Integer.class),
            required = true),
        @Parameter(
            name = "month",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = Integer.class),
            required = true),
        @Parameter(
            name = "page",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class))
      })
  ResultPageResponse<TransactionResponse> transactions(
      @PathVariable long expenseId,
      @PathVariable int year,
      @PathVariable int month,
      @Nullable Integer page) {
    var filter =
        filterFactory
            .transaction()
            .range(DateUtils.forMonth(year, month))
            .onlyIncome(false)
            .ownAccounts()
            .expenses(Collections.List(new EntityRef(expenseId)))
            .page(Control.Option(page).getOrSupply(() -> 1), settingProvider.getPageSize());

    return new ResultPageResponse<>(
        transactionService.lookup(filter).map(TransactionResponse::new));
  }
}
