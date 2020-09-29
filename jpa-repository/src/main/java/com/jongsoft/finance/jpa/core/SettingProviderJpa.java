package com.jongsoft.finance.jpa.core;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.core.entity.SettingJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class SettingProviderJpa extends RepositoryJpa implements SettingProvider {

    private final ReactiveEntityManager entityManager;

    public SettingProviderJpa(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Flowable<Setting> lookup() {
        log.trace("Setting listing");

        return entityManager.<SettingJpa>reactive()
                .hql("select s from SettingJpa s")
                .flow()
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
                .update();
    }

    private Setting convert(SettingJpa source) {
        return Setting.builder()
                .name(source.getName())
                .type(source.getType())
                .value(source.getValue())
                .build();
    }

}
