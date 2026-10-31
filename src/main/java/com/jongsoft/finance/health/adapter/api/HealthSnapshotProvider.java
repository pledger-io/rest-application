package com.jongsoft.finance.health.adapter.api;

import com.jongsoft.finance.health.domain.model.HealthSnapshot;

import java.time.YearMonth;

public interface HealthSnapshotProvider {

    HealthSnapshot lookup(YearMonth yearMonth);

}
