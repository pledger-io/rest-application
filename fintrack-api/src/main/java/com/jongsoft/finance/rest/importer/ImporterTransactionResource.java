package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.security.Principal;

@Tag(name = "Transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/import/{batchSlug}/transactions")
public class ImporterTransactionResource {

    private final SettingProvider settingProvider;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    public ImporterTransactionResource(
            SettingProvider settingProvider,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider) {
        this.settingProvider = settingProvider;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
    }

    @Post
    @Operation(
            summary = "Importer transactions",
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
    @Post
    @Operation(
            summary = "Delete import transactions",
            description = "Search for transactions created by the importer job",
            parameters = {
                    @Parameter(name = "batchSlug", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = "transactionId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
            }
    )
    void delete(@PathVariable long transactionId, Principal principal) {
        transactionProvider.lookup(transactionId)
                .filter(transaction -> transaction.getUser().getUsername().equals(principal.getName()))
                .ifPresent(Transaction::delete);
    }
}
