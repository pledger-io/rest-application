package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.util.List;

public record SplitTransactionCommand(long id, List<Part> split) implements ApplicationEvent {

    public record Part(Long id, long accountId, double amount, String description) {}

    public static void transactionSplit(long id, List<Part> split) {
        new SplitTransactionCommand(id, split).publish();
    }
}
