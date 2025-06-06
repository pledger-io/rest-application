package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedule;

public record ScheduleValue(Periodicity periodicity, int interval) implements Schedule {}
