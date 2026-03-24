package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record UpdateClientCommand(long id, String name, String email, String phone, String address)
        implements ApplicationEvent {

    public static void clientUpdated(
            long id, String name, String email, String phone, String address) {
        new UpdateClientCommand(id, name, email, phone, address).publish();
    }
}
