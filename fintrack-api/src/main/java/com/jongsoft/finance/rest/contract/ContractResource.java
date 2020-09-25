package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.rest.model.ContractResponse;
import com.jongsoft.lang.API;
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
import java.security.Principal;

@Tag(name = "Contract")
@Controller("/api/contracts")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ContractResource {

    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;

    public ContractResource(AccountProvider accountProvider, ContractProvider contractProvider) {
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
    }

    @Get
    @Operation(
            summary = "List contracts",
            description = "List all contracts split in both active and inactive ones"
    )
    ContractOverviewResponse list() {
        var contracts = contractProvider.lookup();

        return new ContractOverviewResponse(
                contracts.reject(Contract::isTerminated),
                API.List(contracts.filter(Contract::isTerminated)));
    }

    @Get("/auto-complete")
    @Operation(
            summary = "Autocomplete contracts",
            description = "Performs a search operation based on the partial name (token)"
    )
    Flowable<ContractResponse> autocomplete(@QueryValue String token) {
        return contractProvider.search(token)
                .map(ContractResponse::new);
    }

    @Put
    @Operation(
            summary = "Create contract",
            description = "Adds a new contract to FinTrack for the authenticated user"
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
                emitter.onSuccess(new ContractResponse(result.get()));
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
            @Body @Valid ContractCreateRequest updateRequest,
            Principal principal) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .filter(c -> c.getCompany().getUser().getUsername().equals(principal.getName()))
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
                        StatusException.notFound("No contract can be found for " + contractId));
            }
        });
    }

    @Get("/{contractId}")
    @Operation(
            summary = "Get contract",
            description = "Get a single contract from FinTrack",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    Single<ContractResponse> get(@PathVariable long contractId, Principal principal) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .filter(contract -> contract.getCompany().getUser().getUsername().equals(principal.getName()))
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound("No contract can be found for " + contractId));
            }
        });
    }

    @Get("/{contractId}/expire-warning")
    @Operation(
            summary = "Enable warning",
            description = "This call will enable the warning 1 month before contract expires",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    Single<ContractResponse> warnExpiry(@PathVariable long contractId, Principal principal) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .filter(c -> c.getCompany().getUser().getUsername().equals(principal.getName()))
                    .map(contract -> {
                        contract.warnBeforeExpires();
                        return contract;
                    })
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound("No contract can be found for " + contractId));
            }
        });
    }

    @Post("/{contractId}/attachment")
    @Operation(
            summary = "Attach file",
            description = "This call will register an attachment to the contract",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    Single<ContractResponse> attachment(
            @PathVariable long contractId,
            @Body @Valid ContractAttachmentRequest attachmentRequest,
            Principal principal) {
        return Single.create(emitter -> {
            var response = contractProvider.lookup(contractId)
                    .filter(c -> c.getCompany().getUser().getUsername().equals(principal.getName()))
                    .map(contract -> {
                        contract.registerUpload(attachmentRequest.getFileCode());
                        return contract;
                    })
                    .map(ContractResponse::new);

            if (response.isPresent()) {
                emitter.onSuccess(response.get());
            } else {
                emitter.onError(
                        StatusException.notFound("No contract can be found for " + contractId));
            }
        });
    }

    @Delete("/{contractId}")
    @Operation(
            summary = "Delete contract",
            description = "Archives an existing contract",
            parameters = @Parameter(name = "contractId", in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    )
    void delete(@PathVariable long contractId, Principal principal) {
        contractProvider.lookup(contractId)
                .filter(c -> c.getCompany().getUser().getUsername().equals(principal.getName()))
                .ifPresent(Contract::terminate);
    }

}
