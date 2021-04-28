package com.jongsoft.finance.messaging;

import com.jongsoft.finance.core.ApplicationEvent;

/**
 * A CommandHandler can be used to persist changes or trigger additional logic on aggregates. A command is fired by an
 * aggregate to notify the application of changes in state.
 *
 * @param <T> the type of command
 */
public interface CommandHandler<T extends ApplicationEvent> {

    /**
     * Process the command to apply the changes indicated in the command.
     *
     * @param command the actual command
     */
    void handle(T command);

}
