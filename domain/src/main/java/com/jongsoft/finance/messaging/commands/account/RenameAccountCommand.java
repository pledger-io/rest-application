package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RenameAccountCommand(
        long id, String type, String name, String description, String currency)
        implements ApplicationEvent {

    /**
     * Renames an account by creating and publishing a RenameAccountCommand with the specified
     * details.
     *
     * @param id the unique identifier of the account
     * @param type the type of the account
     * @param name the new name for the account
     * @param description the description of the account
     * @param currency the currency used by the account
     */
    public static void accountRenamed(
            long id, String type, String name, String description, String currency) {
        new RenameAccountCommand(id, type, name, description, currency).publish();
    }
}
