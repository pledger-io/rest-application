package com.jongsoft.finance.core.domain.jpa.mapper;

import com.jongsoft.finance.core.domain.jpa.entity.AccountTokenJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.model.SessionToken;
import com.jongsoft.finance.core.domain.model.UserAccount;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface UserAccountMapper {

    @Mapper.Mapping(to = "primaryCurrency", from = "#{entity.currency}")
    @Mapper.Mapping(to = "secret", from = "#{entity.twoFactorSecret}")
    UserAccount toDomain(UserAccountJpa entity);

    @Mapper.Mapping(
            to = "validity",
            from = "#{T(com.jongsoft.lang.Dates).range(entity.created, entity.expires)}")
    @Mapper.Mapping(to = "token", from = "#{entity.refreshToken}")
    SessionToken toDomain(AccountTokenJpa entity);
}
