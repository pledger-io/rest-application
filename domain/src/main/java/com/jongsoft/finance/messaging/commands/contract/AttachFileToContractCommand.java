package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.core.ApplicationEvent;

public record AttachFileToContractCommand(long id, String fileCode)
        implements ApplicationEvent {
}
