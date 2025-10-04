package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.rest.model.ContractRequest;
import com.jongsoft.finance.rest.model.ContractResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ContractCommandController implements ContractCommandApi {

    private final Logger logger;

    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;

    public ContractCommandController(
            AccountProvider accountProvider, ContractProvider contractProvider) {
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
        this.logger = LoggerFactory.getLogger(ContractCommandController.class);
    }

    @Override
    public HttpResponse<@Valid ContractResponse> createContract(ContractRequest contractRequest) {
        logger.info("Creating contract {}.", contractRequest.getName());

        var account = accountProvider
                .lookup(contractRequest.getCompany().getId())
                .getOrThrow(() -> StatusException.badRequest("No account can be found for "
                        + contractRequest.getCompany().getId()));

        account.createContract(
                contractRequest.getName(),
                contractRequest.getDescription(),
                contractRequest.getStart(),
                contractRequest.getEnd());

        var contract = contractProvider
                .lookup(contractRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create contract"));

        return HttpResponse.created(ContractMapper.toContractResponse(contract));
    }

    @Override
    public HttpResponse<Void> deleteContractById(Long id) {
        logger.info("Deleting contract {}.", id);

        locateByIdOrThrow(id).terminate();
        return HttpResponse.noContent();
    }

    @Override
    public ContractResponse updateContract(Long id, ContractRequest contractRequest) {
        logger.info("Updating contract {}.", id);

        var contract = locateByIdOrThrow(id);
        contract.change(
                contractRequest.getName(),
                contractRequest.getDescription(),
                contractRequest.getStart(),
                contractRequest.getEnd());

        if (contractRequest.getAttachmentCode() != null) {
            contract.registerUpload(contractRequest.getAttachmentCode());
        }

        return ContractMapper.toContractResponse(contract);
    }

    @Override
    public HttpResponse<Void> warnBeforeContractExpiry(Long id) {
        logger.info("Warn before contract expiry {}.", id);

        var contract = locateByIdOrThrow(id);
        if (contract.isNotifyBeforeEnd()) {
            throw StatusException.badRequest("Warning already scheduled for contract");
        }

        contract.warnBeforeExpires();
        return HttpResponse.noContent();
    }

    private Contract locateByIdOrThrow(Long id) {
        return contractProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Contract is not found"));
    }
}
