package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.rest.model.ContractResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

@Tag(name = "Contract")
@Controller("/api/contracts")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ContractResource {

    private static final String NO_CONTRACT_FOUND_MESSAGE = "No contract can be found for ";

    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;

    public ContractResource(AccountProvider accountProvider, ContractProvider contractProvider) {
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
    }

    @Get
    @Operation(
            summary = "List contracts",
            description = "List all contracts split in both active and inactive ones",
            operationId = "getAll"
    )
    ContractOverviewResponse list() {
        var contracts = contractProvider.lookup();

        return new ContractOverviewResponse(
                contracts.reject(Contract::isTerminated).map(ContractResponse::new).toJava(),
                contracts.filter(Contract::isTerminated).map(ContractResponse::new).toJava());
    }

    @Get("/auto-complete")
    @Operation(
            summary = "Autocomplete contracts",
            description = "Performs a search operation based on the partial name (token)",
            operationId = "getByToken"
    )
    Flowable<ContractResponse> autocomplete(@QueryValue String token) {
        return contractProvider.search(token)
                .map(ContractResponse::new);
    }

    @Put
    @Operation(
            summary = "Create contract",
            description = "Adds a new contract to FinTrack for the authenticated user",
            operationId = "createContract"
    )
    Single<ContractResponse> create(@Body @Valid ContractCreateRequest createRequest) {
        return Single.create(emitter -> {
            var result = accountProvider.lookup(createRequest.getCompany().getId())
                    .map(account -> account.createContract(
                            createRequest.getName(),
                            createRequest.getDescription(),
                            createRequest.getStart(),
                            createRequest.getEnd()));

            if (result.isPresent()) {
                contractProvider.lookup(createRequest.getName())
                        .switchIfEmpty(Single.error(
                                StatusException.internalError("Error creating contract")))
                        .subscribe(
                                contract -> emitter.onSuccess(new ContractResponse(contract)),
                                emitter::onError);
            } else {
                emitter.onError(
                        StatusException.notFound(
                                "No account can be found for " + createRequest.getCompany().getId()));
            }
        });
    }

    @Post("/{contractId}")
    @Operation(
            summary = "Update contract",
            description = "Updates an existing contract for the authenticated user",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    Single<ContractResponse> update(
            @PathVariable long contractId,
            @Body @Valid ContractCreateRequest updateRequest) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .map(contract -> {
                        contract.change(
                                updateRequest.getName(),
                                updateRequest.getDescription(),
                                updateRequest.getStart(),
                                updateRequest.getEnd());
                        return contract;
                    })
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
            }
        });
    }

    @Get("/{contractId}")
    @Operation(
            summary = "Get contract",
            description = "Get a single contract from FinTrack",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    Single<ContractResponse> get(@PathVariable long contractId) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
            }
        });
    }

    @Get("/{contractId}/expire-warning")
    @Operation(
            summary = "Enable warning",
            description = "This call will enable the warning 1 month before contract expires",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class)),
            operationId = "warnBeforeExpireDate"
    )
    Single<ContractResponse> warnExpiry(@PathVariable long contractId) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .map(contract -> {
                        contract.warnBeforeExpires();
                        return contract;
                    })
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
            }
        });
    }

    @Post("/{contractId}/attachment")
    @Operation(
            summary = "Attach file",
            description = "This call will register an attachment to the contract",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class)),
            operationId = "registerAttachment"
    )
    Single<ContractResponse> attachment(
            @PathVariable long contractId,
            @Body @Valid ContractAttachmentRequest attachmentRequest) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .map(contract -> {
                        contract.registerUpload(attachmentRequest.getFileCode());
                        return contract;
                    })
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
            }
        });
    }

    @Delete("/{contractId}")
    @Operation(
            summary = "Delete contract",
            description = "Archives an existing contract",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    void delete(@PathVariable long contractId) {
        contractProvider.lookup(contractId)
                .ifPresent(Contract::terminate);
    }

}
