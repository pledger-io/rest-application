package com.jongsoft.finance.messaging.commands.schedule;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.schedule.Schedule;

public record CreateScheduleForContractCommand(String name,
                                               Schedule schedule,
                                               Contract contract,
                                               Account source,
                                               double amount) implements ApplicationEvent {
}
