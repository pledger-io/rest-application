package com.jongsoft.finance.health.adapter.api;

import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.control.Optional;

import java.time.YearMonth;

public interface HealthSnapshotProvider {

    Optional<HealthSnapshot> lookup(YearMonth yearMonth);
}
