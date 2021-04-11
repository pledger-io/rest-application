package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.schedule.Periodicity;

public record ChangeInterestCommand(long id, double interest, Periodicity periodicity)
        implements ApplicationEvent {
}
