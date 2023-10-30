package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Importer")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Controller("/api/import/{batchSlug}/transactions")
public class ImporterTransactionResource {

    private final SettingProvider settingProvider;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    @Post
    @Operation(
            summary = "Transaction overview",
            operationId = "getTransactions",
            description = "Search for transactions created by the importer job",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, schema = @Schema(implementation = String.class))
    )
    ResultPageResponse<TransactionResponse> search(
            @PathVariable String batchSlug,
            @Valid @Body TransactionSearchRequest request) {
        var filter = filterFactory.transaction()
                .importSlug(batchSlug)
                .pageSize(settingProvider.getPageSize())
                .page(request.getPage());

        var response = transactionProvider.lookup(filter)
                .map(TransactionResponse::new);

        return new ResultPageResponse<>(response);
    }

    @Delete("/{transactionId}")
    @Status(HttpStatus.NO_CONTENT)
    @Post
    @Operation(
            summary = "Delete transaction",
            operationId = "deleteTransaction",
            description = "Search for transactions created by the importer job",
            parameters = {
                    @Parameter(name = "batchSlug", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = "transactionId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
            }
    )
    void delete(@PathVariable long transactionId) {
        transactionProvider.lookup(transactionId)
                .ifPresent(Transaction::delete);
    }
}
