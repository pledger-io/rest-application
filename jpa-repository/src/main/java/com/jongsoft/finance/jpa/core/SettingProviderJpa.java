package com.jongsoft.finance.jpa.core;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.core.entity.SettingJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReadOnly
@RequiresJpa
@Singleton
public class SettingProviderJpa implements SettingProvider {

  private final ReactiveEntityManager entityManager;

  public SettingProviderJpa(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Sequence<Setting> lookup() {
    log.trace("Setting listing");

    return entityManager.from(SettingJpa.class).stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public Optional<Setting> lookup(String name) {
    log.trace("Setting lookup by name {}", name);

    return entityManager
        .from(SettingJpa.class)
        .fieldEq("name", name)
        .singleResult()
        .map(this::convert);
  }

  @Transactional
  @BusinessEventListener
  public void handleSettingUpdated(SettingUpdatedEvent event) {
    entityManager
        .update(SettingJpa.class)
        .set("value", event.value())
        .fieldEq("name", event.setting())
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
