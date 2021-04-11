package com.jongsoft.finance.messaging.commands.storage;

import com.jongsoft.finance.core.ApplicationEvent;

public interface ReplaceFileCommand extends ApplicationEvent {

    String fileCode();
    String oldFileCode();

}
