package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

public record DescribeTransactionCommand(long id, String description) implements ApplicationEvent {
}
