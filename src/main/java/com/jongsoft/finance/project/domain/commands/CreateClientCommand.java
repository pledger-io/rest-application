package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateClientCommand(String name, String email, String phone, String address)
        implements ApplicationEvent {

    public static void clientCreated(String name, String email, String phone, String address) {
        new CreateClientCommand(name, email, phone, address).publish();
    }
}
