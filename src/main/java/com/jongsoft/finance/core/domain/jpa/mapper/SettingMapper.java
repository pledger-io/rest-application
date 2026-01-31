package com.jongsoft.finance.core.domain.jpa.mapper;

import com.jongsoft.finance.core.domain.jpa.entity.SettingJpa;
import com.jongsoft.finance.core.domain.model.Setting;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface SettingMapper {

    @Mapper
    Setting toDomain(SettingJpa settingJpa);
}
