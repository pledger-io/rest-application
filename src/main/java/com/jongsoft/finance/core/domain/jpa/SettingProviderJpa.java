package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.core.adapter.api.SettingProvider;
import com.jongsoft.finance.core.domain.commands.SettingUpdatedEvent;
import com.jongsoft.finance.core.domain.jpa.entity.SettingJpa;
import com.jongsoft.finance.core.domain.jpa.mapper.SettingMapper;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.model.Setting;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;

@ReadOnly
@Singleton
class SettingProviderJpa implements SettingProvider {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(SettingProviderJpa.class);

    private final ReactiveEntityManager entityManager;
    private final SettingMapper settingMapper;

    public SettingProviderJpa(ReactiveEntityManager entityManager, SettingMapper settingMapper) {
        this.entityManager = entityManager;
        this.settingMapper = settingMapper;
    }

    @Override
    public Sequence<Setting> lookup() {
        log.trace("Setting listing");

        return entityManager.from(SettingJpa.class).stream()
                .map(settingMapper::toDomain)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<Setting> lookup(String name) {
        log.trace("Setting lookup by name {}", name);

        return entityManager
                .from(SettingJpa.class)
                .fieldEq("name", name)
                .singleResult()
                .map(settingMapper::toDomain);
    }

    @Transactional
    @EventListener
    public void handleSettingUpdated(SettingUpdatedEvent event) {
        entityManager
                .update(SettingJpa.class)
                .set("value", event.value())
                .fieldEq("name", event.setting())
                .execute();
    }
}
