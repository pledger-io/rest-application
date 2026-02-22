package com.jongsoft.finance.core.domain.jpa.mapper;

import com.jongsoft.finance.core.domain.jpa.entity.AccountTokenJpa;
import com.jongsoft.finance.core.domain.jpa.entity.RoleJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.model.Role;
import com.jongsoft.finance.core.domain.model.SessionToken;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.lang.collection.Collectors;
import com.jongsoft.lang.collection.List;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.util.Set;

@Singleton
public abstract class UserAccountMapper {

    @Mapper.Mapping(to = "primaryCurrency", from = "#{entity.currency}")
    @Mapper.Mapping(to = "secret", from = "#{entity.twoFactorSecret}")
    @Mapper.Mapping(to = "roles", from = "#{this.convertRoles(entity.roles)}")
    public abstract UserAccount toDomain(UserAccountJpa entity);

    @Mapper.Mapping(
            to = "validity",
            from = "#{T(com.jongsoft.lang.Dates).range(entity.created, entity.expires)}")
    @Mapper.Mapping(to = "token", from = "#{entity.refreshToken}")
    public abstract SessionToken toDomain(AccountTokenJpa entity);

    public List<Role> convertRoles(Set<RoleJpa> entities) {
        return entities.stream().map(role -> new Role(role.getName())).collect(Collectors.toList());
    }
}
