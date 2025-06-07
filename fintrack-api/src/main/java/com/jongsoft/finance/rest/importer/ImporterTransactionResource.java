package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.rest.process.RuntimeResource;
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
import java.util.Map;

@Tag(name = "Importer")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/import/{batchSlug}/transactions")
public class ImporterTransactionResource {

  private final SettingProvider settingProvider;
  private final FilterFactory filterFactory;
  private final TransactionProvider transactionProvider;

  private final RuntimeResource runtimeResource;

  public ImporterTransactionResource(
      SettingProvider settingProvider,
      FilterFactory filterFactory,
      TransactionProvider transactionProvider,
      RuntimeResource runtimeResource) {
    this.settingProvider = settingProvider;
    this.filterFactory = filterFactory;
    this.transactionProvider = transactionProvider;
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
    var searchFilter = filterFactory.transaction().importSlug(batchSlug).page(0, Integer.MAX_VALUE);

    transactionProvider
        .lookup(searchFilter)
        .content()
        .map(Transaction::getId)
        .forEach(
            transactionId ->
                runtimeResource.startProcess(
                    "analyzeRule", Map.of("transactionId", transactionId)));
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
