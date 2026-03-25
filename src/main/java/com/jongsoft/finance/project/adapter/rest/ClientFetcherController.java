package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.rest.ClientFetcherApi;
import com.jongsoft.finance.rest.model.ClientResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class ClientFetcherController implements ClientFetcherApi {

    private final Logger logger;
    private final ClientProvider clientProvider;

    public ClientFetcherController(ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
        this.logger = LoggerFactory.getLogger(ClientFetcherController.class);
    }

    @Override
    public List<ClientResponse> findClients(String name) {
        logger.info("Fetching all clients with provided filters.");

        if (name != null) {
            return clientProvider
                    .lookup(name)
                    .map(ClientMapper::toClientResponse)
                    .map(List::of)
                    .getOrSupply(List::of);
        }

        return clientProvider.lookup().map(ClientMapper::toClientResponse).toJava();
    }

    @Override
    public ClientResponse getClientById(Long id) {
        logger.info("Fetching client {}.", id);

        var client = clientProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Client is not found"));

        return ClientMapper.toClientResponse(client);
    }
}
