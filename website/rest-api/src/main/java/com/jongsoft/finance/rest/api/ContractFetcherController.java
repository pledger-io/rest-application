package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.rest.model.ContractResponse;
import com.jongsoft.finance.rest.model.FindContractByStatusParameter;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class ContractFetcherController implements ContractFetcherApi {

    private final Logger logger;

    private final ContractProvider contractProvider;

    public ContractFetcherController(ContractProvider contractProvider) {
        this.contractProvider = contractProvider;
        this.logger = LoggerFactory.getLogger(ContractFetcherController.class);
    }

    @Override
    public List<ContractResponse> findContractBy(
            String name, FindContractByStatusParameter status) {
        logger.info("Fetching all contracts, with provided filters.");

        if (name != null) {
            return contractProvider
                    .search(name)
                    .map(ContractMapper::toContractResponse)
                    .toJava();
        }

        return contractProvider
                .lookup()
                .filter(contract -> switch (status) {
                    case ACTIVE -> !contract.isTerminated();
                    case INACTIVE -> contract.isTerminated();
                })
                .map(ContractMapper::toContractResponse)
                .toJava();
    }

    @Override
    public ContractResponse getContractById(Long id) {
        logger.info("Fetching contract {}.", id);

        var contract = contractProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Contract is not found"));

        return ContractMapper.toContractResponse(contract);
    }
}
