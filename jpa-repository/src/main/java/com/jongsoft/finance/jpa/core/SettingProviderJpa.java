package com.jongsoft.finance.jpa.core;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.core.entity.SettingJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SettingProviderJpa extends RepositoryJpa implements SettingProvider {

    private final EntityManager entityManager;

    public SettingProviderJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<Setting> lookup() {
        log.trace("Setting listing");

        var query = entityManager.createQuery("select s from SettingJpa s");
        return this.<SettingJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<Setting> lookup(String name) {
        log.trace("Setting lookup by name {}", name);

        var hql = """
                select s from SettingJpa s
                where s.name = :name""";

        var query = entityManager.createQuery(hql);
        query.setParameter("name", name);
        return API.Option(convert(singleValue(query)));
    }

    @Transactional
    @BusinessEventListener
    public void handleSettingUpdated(SettingUpdatedEvent event) {
        var hql = """
                update SettingJpa
                set value = :value
                where name = :name""";

        var query = entityManager.createQuery(hql);
        query.setParameter("name", event.getSetting());
        query.setParameter("value", event.getValue());
        query.executeUpdate();
    }

    private Setting convert(SettingJpa source) {
        return Setting.builder()
                .name(source.getName())
                .type(source.getType())
                .value(source.getValue())
                .build();
    }

}
