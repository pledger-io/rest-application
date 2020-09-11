package com.jongsoft.finance.jpa.transaction;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroupProvider;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.transaction.entity.RuleGroupJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Named("transactionRuleGroupProvider")
public class TransactionRuleGroupProviderJpa extends RepositoryJpa implements TransactionRuleGroupProvider {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;

    public TransactionRuleGroupProviderJpa(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Sequence<TransactionRuleGroup> lookup() {
        log.trace("TransactionRuleGroup listing");

        var hql = """
                select g from RuleGroupJpa g 
                where g.user.username = :username
                    and g.archived = false
                order by g.sort asc""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        return this.<RuleGroupJpa>multiValue(query)
                .map(this::convert);
    }

    @Override
    public Optional<TransactionRuleGroup> lookup(String name) {
        log.trace("TransactionRuleGroup lookup with name: {}", name);

        var hql = """
                select g from RuleGroupJpa g 
                where g.user.username = :username
                    and g.name = :name
                    and g.archived = false""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", name);
        return API.Option(convert(singleValue(query)));
    }

    private TransactionRuleGroup convert(RuleGroupJpa source) {
        if (source == null) {
            return null;
        }

        return TransactionRuleGroup.builder()
                .id(source.getId())
                .name(source.getName())
                .sort(source.getSort())
                .build();
    }

}
