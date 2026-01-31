package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.lang.collection.Sequence;

public record TagTransactionCommand(long id, Sequence<String> tags) implements ApplicationEvent {

    public static void tagCreated(long id, Sequence<String> tags) {
        new TagTransactionCommand(id, tags).publish();
    }
}
