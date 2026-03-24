package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.rest.ClientCommandApi;
import com.jongsoft.finance.rest.model.ClientRequest;
import com.jongsoft.finance.rest.model.ClientResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ClientCommandController implements ClientCommandApi {

    private final Logger logger;
    private final ClientProvider clientProvider;

    public ClientCommandController(ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
        this.logger = LoggerFactory.getLogger(ClientCommandController.class);
    }

    @Override
    public HttpResponse<@Valid ClientResponse> createClient(ClientRequest clientRequest) {
        logger.info("Creating client {}.", clientRequest.getName());

        Client.create(
                clientRequest.getName(),
                clientRequest.getEmail(),
                clientRequest.getPhone(),
                clientRequest.getAddress());

        var client = clientProvider
                .lookup(clientRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create client"));

        return HttpResponse.created(ClientMapper.toClientResponse(client));
    }

    @Override
    public ClientResponse updateClient(Long id, ClientRequest clientRequest) {
        logger.info("Updating client {}.", id);

        var client = locateByIdOrThrow(id);
        client.update(
                clientRequest.getName(),
                clientRequest.getEmail(),
                clientRequest.getPhone(),
                clientRequest.getAddress());

        return ClientMapper.toClientResponse(client);
    }

    @Override
    public HttpResponse<Void> archiveClientById(Long id) {
        logger.info("Archiving client {}.", id);

        locateByIdOrThrow(id).archive();
        return HttpResponse.noContent();
    }

    private Client locateByIdOrThrow(Long id) {
        return clientProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Client is not found"));
    }
}
