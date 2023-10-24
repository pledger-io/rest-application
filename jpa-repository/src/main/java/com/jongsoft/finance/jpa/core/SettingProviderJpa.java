package com.jongsoft.finance.jpa.core;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.core.entity.SettingJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@Singleton
public class SettingProviderJpa implements SettingProvider {

    private final ReactiveEntityManager entityManager;

    public SettingProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Setting> lookup() {
        log.trace("Setting listing");

        return entityManager.<SettingJpa>blocking()
                .hql("select s from SettingJpa s")
                .sequence()
                .map(this::convert);
    }

    @Override
    public Optional<Setting> lookup(String name) {
        log.trace("Setting lookup by name {}", name);

        var hql = """
                select s from SettingJpa s
                where s.name = :name""";

        return entityManager.<SettingJpa>blocking()
                .hql(hql)
                .set("name", name)
                .maybe()
                .map(this::convert);
    }

    @Transactional
    @BusinessEventListener
    public void handleSettingUpdated(SettingUpdatedEvent event) {
        var hql = """
                update SettingJpa
                set value = :value
                where name = :name""";

        entityManager.update()
                .hql(hql)
                .set("name", event.getSetting())
                .set("value", event.getValue())
                .execute();
    }

    private Setting convert(SettingJpa source) {
        return Setting.builder()
                .name(source.getName())
                .type(source.getType())
                .value(source.getValue())
                .build();
    }

}
