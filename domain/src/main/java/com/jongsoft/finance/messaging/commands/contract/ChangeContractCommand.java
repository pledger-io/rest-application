package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.core.ApplicationEvent;

import java.time.LocalDate;

public record ChangeContractCommand(long id, String name, String description, LocalDate start, LocalDate end)
        implements ApplicationEvent {
}
