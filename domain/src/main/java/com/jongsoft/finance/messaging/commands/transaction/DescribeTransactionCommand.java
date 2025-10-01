package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DescribeTransactionCommand(long id, String description) implements ApplicationEvent {

    public static void transactionDescribed(long id, String description) {
        new DescribeTransactionCommand(id, description).publish();
    }
}
