package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public interface ReplaceFileCommand extends ApplicationEvent {

    String fileCode();

    String oldFileCode();
}
