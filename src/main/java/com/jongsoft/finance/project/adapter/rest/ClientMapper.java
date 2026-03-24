package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.finance.rest.model.ClientResponse;

public interface ClientMapper {

    static ClientResponse toClientResponse(Client client) {
        var response = new ClientResponse(client.getId(), client.getName());
        response.setEmail(client.getEmail());
        response.setPhone(client.getPhone());
        response.setAddress(client.getAddress());
        response.setArchived(client.isArchived());
        return response;
    }
}
