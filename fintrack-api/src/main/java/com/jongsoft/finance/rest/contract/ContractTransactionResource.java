package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.rest.model.TransactionResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Optional;

@Tag(name = "Contract")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/contracts/{contractId}/transactions")
public class ContractTransactionResource {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionService;
    private final SettingProvider settingProvider;

    public ContractTransactionResource(
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
            description = "Paged listing of all transactions that belong to a contract")
    ResultPageResponse<TransactionResponse> transactions(
            @PathVariable long contractId, @Nullable Integer page) {
        var filter = filterFactory
                .transaction()
                .ownAccounts()
                .onlyIncome(false)
                .contracts(Collections.List(new EntityRef(contractId)))
                .page(Optional.ofNullable(page).orElse(0), settingProvider.getPageSize());

        return new ResultPageResponse<>(
                transactionService.lookup(filter).map(TransactionResponse::new));
    }
}
