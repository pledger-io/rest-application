package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DescribeTransactionCommand(long id, String description) implements ApplicationEvent {

    public static void transactionDescribed(long id, String description) {
        new DescribeTransactionCommand(id, description).publish();
    }
}
