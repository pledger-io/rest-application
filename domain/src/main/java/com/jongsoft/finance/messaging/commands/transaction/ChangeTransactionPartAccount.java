package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

public record ChangeTransactionPartAccount(long id, long accountId) implements ApplicationEvent {
}
