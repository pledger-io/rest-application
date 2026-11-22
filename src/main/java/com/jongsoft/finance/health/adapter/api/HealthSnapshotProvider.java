package com.jongsoft.finance.health.adapter.api;

import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.control.Optional;
import com.jongsoft.lang.time.Range;

import java.time.YearMonth;
import java.util.List;

public interface HealthSnapshotProvider {

    Optional<HealthSnapshot> lookup(YearMonth yearMonth);

    List<HealthSnapshot> lookup(Range<YearMonth> range);
}
