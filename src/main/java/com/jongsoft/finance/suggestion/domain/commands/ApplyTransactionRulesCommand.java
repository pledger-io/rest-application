package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ApplyTransactionRulesCommand(long transactionId) implements ApplicationEvent {
    public static void applyTransactionRules(long transactionId) {
        new ApplyTransactionRulesCommand(transactionId).publish();
    }
}
