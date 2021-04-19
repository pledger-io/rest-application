package com.jongsoft.finance.messaging.commands.tag;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateTagCommand(String tag) implements ApplicationEvent {
}
