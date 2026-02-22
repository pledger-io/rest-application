package com.jongsoft.finance.suggestion.domain.jpa;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleGroupProvider;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleGroupJpa;
import com.jongsoft.finance.suggestion.domain.jpa.mapper.TransactionRuleGroupMapper;
import com.jongsoft.finance.suggestion.domain.model.TransactionRuleGroup;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import io.micronaut.transaction.annotation.ReadOnly;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ReadOnly
@Singleton
public class TransactionRuleGroupProviderJpa implements TransactionRuleGroupProvider {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;
    private final TransactionRuleGroupMapper mapper;

    @Inject
    public TransactionRuleGroupProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager,
            TransactionRuleGroupMapper mapper) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public Sequence<TransactionRuleGroup> lookup() {
        log.trace("TransactionRuleGroup listing");

        return entityManager
                .from(RuleGroupJpa.class)
                .fieldEq("archived", false)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .stream()
                .map(mapper::toModel)
                .collect(ReactiveEntityManager.sequenceCollector());
    }

    @Override
    public Optional<TransactionRuleGroup> lookup(String name) {
        log.trace("TransactionRuleGroup lookup with name: {}", name);

        return entityManager
                .from(RuleGroupJpa.class)
                .fieldEq("archived", false)
                .fieldEq("name", name)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .singleResult()
                .map(mapper::toModel);
    }
}
