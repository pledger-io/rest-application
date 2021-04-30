package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.messaging.commands.storage.ReplaceFileCommand;

public record RegisterAccountIconCommand(long id, String fileCode, String oldFileCode)
        implements ReplaceFileCommand, ApplicationEvent {
}
