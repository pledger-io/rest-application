package com.jongsoft.finance.project.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.domain.commands.ArchiveClientCommand;
import com.jongsoft.finance.project.domain.commands.CreateClientCommand;
import com.jongsoft.finance.project.domain.commands.UpdateClientCommand;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;

@Introspected
public class Client implements Serializable {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private boolean archived;

    // Used by the Mapper strategy
    Client(Long id, String name, String email, String phone, String address, boolean archived) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.archived = archived;
    }

    private Client(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.archived = false;
        CreateClientCommand.clientCreated(name, email, phone, address);
    }

    public void update(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        UpdateClientCommand.clientUpdated(id, name, email, phone, address);
    }

    public void archive() {
        if (archived) {
            throw StatusException.badRequest(
                    "Client is already archived.", "client.already.archived");
        }
        this.archived = true;
        ArchiveClientCommand.clientArchived(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isArchived() {
        return archived;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Client create(String name, String email, String phone, String address) {
        return new Client(name, email, phone, address);
    }
}
