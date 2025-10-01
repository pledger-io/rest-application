package com.jongsoft.finance.rest.contract;

import static com.jongsoft.finance.rest.ApiConstants.TAG_CONTRACTS;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.model.ContractResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;

@Tag(name = TAG_CONTRACTS)
@Controller("/api/contracts")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class ContractResource {

    private static final String NO_CONTRACT_FOUND_MESSAGE = "No contract can be found for ";

    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final FilterFactory filterFactory;

    public ContractResource(
            AccountProvider accountProvider,
            ContractProvider contractProvider,
            TransactionScheduleProvider scheduleProvider,
            FilterFactory filterFactory) {
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
        this.scheduleProvider = scheduleProvider;
        this.filterFactory = filterFactory;
    }

    @Get
    @Operation(
            summary = "List contracts",
            description = "List all contracts split in both active and inactive ones",
            operationId = "getAll")
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
            operationId = "getByToken")
    List<ContractResponse> autocomplete(@QueryValue String token) {
        return contractProvider.search(token).map(ContractResponse::new).toJava();
    }

    @Put
    @Validated
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Create contract",
            description = "Adds a new contract to FinTrack for the authenticated user",
            operationId = "createContract")
    ContractResponse create(@Body ContractCreateRequest createRequest) {
        return accountProvider
                .lookup(createRequest.company().id())
                .map(
                        account ->
                                account.createContract(
                                        createRequest.name(),
                                        createRequest.description(),
                                        createRequest.start(),
                                        createRequest.end()))
                .map(
                        account ->
                                contractProvider
                                        .lookup(createRequest.name())
                                        .getOrThrow(
                                                () ->
                                                        StatusException.internalError(
                                                                "Error creating contract")))
                .map(ContractResponse::new)
                .getOrThrow(
                        () ->
                                StatusException.notFound(
                                        "No account can be found for "
                                                + createRequest.company().id()));
    }

    @Post("/{contractId}")
    @Operation(
            summary = "Update contract",
            description = "Updates an existing contract for the authenticated user",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)))
    ContractResponse update(
            @PathVariable long contractId, @Body @Valid ContractCreateRequest updateRequest) {
        return contractProvider
                .lookup(contractId)
                .map(
                        contract -> {
                            contract.change(
                                    updateRequest.name(),
                                    updateRequest.description(),
                                    updateRequest.start(),
                                    updateRequest.end());
                            return contract;
                        })
                .map(ContractResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
    }

    @Get("/{contractId}")
    @Operation(
            summary = "Get contract",
            description = "Get a single contract from FinTrack",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)))
    ContractResponse get(@PathVariable long contractId) {
        return contractProvider
                .lookup(contractId)
                .map(ContractResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
    }

    @Put("/{contractId}/schedule")
    @Operation(
            summary = "Schedule transaction",
            description = "Create a new schedule for creating transaction under this contract.",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)))
    void schedule(@PathVariable long contractId, @Body @Valid CreateScheduleRequest request) {
        var account =
                accountProvider
                        .lookup(request.source().id())
                        .getOrThrow(
                                () ->
                                        StatusException.badRequest(
                                                "No source account found with provided id."));

        var contract =
                contractProvider
                        .lookup(contractId)
                        .getOrThrow(
                                () ->
                                        StatusException.badRequest(
                                                "No contract found with provided id."));

        contract.createSchedule(request.getSchedule(), account, request.amount());

        // update the schedule start / end date
        scheduleProvider
                .lookup(
                        filterFactory
                                .schedule()
                                .activeOnly()
                                .contract(Collections.List(new EntityRef(contractId))))
                .content()
                .forEach(ScheduledTransaction::limitForContract);
    }

    @Get("/{contractId}/expire-warning")
    @Operation(
            summary = "Enable warning",
            description = "This call will enable the warning 1 month before contract expires",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)),
            operationId = "warnBeforeExpireDate")
    ContractResponse warnExpiry(@PathVariable long contractId) {
        return contractProvider
                .lookup(contractId)
                .map(
                        contract -> {
                            contract.warnBeforeExpires();
                            return contract;
                        })
                .map(ContractResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
    }

    @Post("/{contractId}/attachment")
    @Operation(
            summary = "Attach file",
            description = "This call will register an attachment to the contract",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)),
            operationId = "registerAttachment")
    ContractResponse attachment(
            @PathVariable long contractId,
            @Body @Valid ContractAttachmentRequest attachmentRequest) {
        return contractProvider
                .lookup(contractId)
                .map(
                        contract -> {
                            contract.registerUpload(attachmentRequest.fileCode());
                            return contract;
                        })
                .map(ContractResponse::new)
                .getOrThrow(() -> StatusException.notFound(NO_CONTRACT_FOUND_MESSAGE + contractId));
    }

    @Delete("/{contractId}")
    @Operation(
            summary = "Delete contract",
            description = "Archives an existing contract",
            parameters =
                    @Parameter(
                            name = "contractId",
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)))
    void delete(@PathVariable long contractId) {
        contractProvider.lookup(contractId).ifPresent(Contract::terminate);
    }
}
