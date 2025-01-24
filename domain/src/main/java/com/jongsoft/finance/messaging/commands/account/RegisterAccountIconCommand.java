package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.finance.messaging.commands.storage.ReplaceFileCommand;

public record RegisterAccountIconCommand(long id, String fileCode, String oldFileCode)
        implements ReplaceFileCommand, ApplicationEvent {

    /**
     * Notify that an icon has been changed for a specific item identified by its ID.
     *
     * @param id the ID of the item for which the icon has changed
     * @param fileCode the new file code representing the updated icon
     * @param oldFileCode the previous file code representing the old icon
     */
    public static void iconChanged(long id, String fileCode, String oldFileCode) {
        new RegisterAccountIconCommand(id, fileCode, oldFileCode)
                .publish();
    }
}
