package com.jongsoft.finance.messaging;

import com.jongsoft.finance.core.ApplicationEvent;

public interface CommandHandler<T extends ApplicationEvent> {

    void handle(T command);

}
