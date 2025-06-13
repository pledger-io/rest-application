package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.security.AuthenticationRoles;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "Importer")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/import/{batchSlug}/transactions")
public class ImporterTransactionResource {

  private final Logger log = LoggerFactory.getLogger(ImporterTransactionResource.class);

  private final SettingProvider settingProvider;
  private final FilterFactory filterFactory;
  private final TransactionProvider transactionProvider;

  private final RuleEngine ruleEngine;
  private final RuntimeResource runtimeResource;

  public ImporterTransactionResource(
      SettingProvider settingProvider,
      FilterFactory filterFactory,
      TransactionProvider transactionProvider,
      RuleEngine ruleEngine,
      RuntimeResource runtimeResource) {
    this.settingProvider = settingProvider;
    this.filterFactory = filterFactory;
    this.transactionProvider = transactionProvider;
    this.ruleEngine = ruleEngine;
    this.runtimeResource = runtimeResource;
  }

  @Post
  @Operation(
      summary = "Transaction overview",
      operationId = "getTransactions",
      description = "Search for transactions created by the importer job",
      parameters =
          @Parameter(
              name = "batchSlug",
              in = ParameterIn.PATH,
              schema = @Schema(implementation = String.class)))
  ResultPageResponse<TransactionResponse> search(
      @PathVariable String batchSlug, @Valid @Body TransactionSearchRequest request) {
    var filter =
        filterFactory
            .transaction()
            .importSlug(batchSlug)
            .page(request.getPage(), settingProvider.getPageSize());

    var response = transactionProvider.lookup(filter).map(TransactionResponse::new);

    return new ResultPageResponse<>(response);
  }

  @Post("/run-rule-automation")
  @Status(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Run rule automation",
      operationId = "runRuleAutomation",
      description = "Run rule automation on transactions created by the importer job",
      parameters =
          @Parameter(
              name = "batchSlug",
              in = ParameterIn.PATH,
              schema = @Schema(implementation = String.class)))
  void runRuleAutomation(@PathVariable String batchSlug) {
    var page = 0;
    var searchFilter = filterFactory.transaction()
        .importSlug(batchSlug)
        .page(page, 250);

    var result = transactionProvider.lookup(searchFilter);
    while (true) {
      log.info("Processing page {} of {} transactions for applying rules.", page + 1, result.pages());
      result.content()
          .stream()
          .parallel()
          .forEach(this::processTransaction);
      if (!result.hasNext()) {
        break;
      }
      result = transactionProvider.lookup(searchFilter.page(++page, 250));
    }
  }

  private void processTransaction(Transaction transaction) {
    var inputSet = new RuleDataSet();
    inputSet.put(RuleColumn.TO_ACCOUNT, transaction.computeTo().getName());
    inputSet.put(RuleColumn.SOURCE_ACCOUNT, transaction.computeFrom().getName());
    inputSet.put(RuleColumn.AMOUNT, transaction.computeAmount(transaction.computeTo()));
    inputSet.put(RuleColumn.DESCRIPTION, transaction.getDescription());

    var outputSet = ruleEngine.run(inputSet);

    for (Map.Entry<RuleColumn, ?> entry : outputSet.entrySet()) {
      switch (entry.getKey()) {
        case CATEGORY -> transaction.linkToCategory((String) entry.getValue());
        case TO_ACCOUNT, CHANGE_TRANSFER_TO ->
            transaction.changeAccount(false, (Account) entry.getValue());
        case SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM ->
            transaction.changeAccount(true, (Account) entry.getValue());
        case CONTRACT -> transaction.linkToContract((String) entry.getValue());
        case BUDGET -> transaction.linkToBudget((String) entry.getValue());
        default ->
            throw new IllegalArgumentException(
                "Unsupported rule column provided " + entry.getKey());
      }
    }
  }

  @Delete("/{transactionId}")
  @Status(HttpStatus.NO_CONTENT)
  @Post
  @Operation(
      summary = "Delete transaction",
      operationId = "deleteTransaction",
      description = "Search for transactions created by the importer job",
      parameters = {
        @Parameter(
            name = "batchSlug",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = String.class)),
        @Parameter(
            name = "transactionId",
            in = ParameterIn.PATH,
            schema = @Schema(implementation = Long.class))
      })
  void delete(@PathVariable long transactionId) {
    transactionProvider.lookup(transactionId).ifPresent(Transaction::delete);
  }
}
