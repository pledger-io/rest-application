package com.jongsoft.finance.suggestion.domain.jpa.mapper;

import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleGroupJpa;
import com.jongsoft.finance.suggestion.domain.model.TransactionRuleGroup;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

@Singleton
public interface TransactionRuleGroupMapper {

    @Mapper
    TransactionRuleGroup toModel(RuleGroupJpa entity);
}
