package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;

import jakarta.inject.Singleton;

@Singleton
public class HealthSnapshotJpaProvider {

    public Optional<HealthSnapshot> get(int year, int month) {
        return Control.Option();
    }

    public void storeCached(HealthSnapshot healthSnapshot) {}
}
